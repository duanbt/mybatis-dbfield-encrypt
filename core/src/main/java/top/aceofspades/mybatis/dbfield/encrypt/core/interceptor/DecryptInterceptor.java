package top.aceofspades.mybatis.dbfield.encrypt.core.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import top.aceofspades.mybatis.dbfield.encrypt.core.annotation.Decrypt;
import top.aceofspades.mybatis.dbfield.encrypt.core.annotation.MapCrypt;
import top.aceofspades.mybatis.dbfield.encrypt.core.cipher.Cipher;
import top.aceofspades.mybatis.dbfield.encrypt.core.cipher.CipherManager;
import top.aceofspades.mybatis.dbfield.encrypt.core.manager.CryptManager;
import top.aceofspades.mybatis.dbfield.encrypt.core.util.ProxyUtils;

import java.lang.reflect.Field;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author duanbt
 * @create 2023-06-19 17:40
 **/
@Slf4j
@Intercepts({
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
})
public class DecryptInterceptor implements Interceptor {

    private final CipherManager cipherManager;

    private final CryptManager cryptManager;

    public DecryptInterceptor(CipherManager cipherManager,
                              CryptManager cryptManager) {
        this.cipherManager = cipherManager;
        this.cryptManager = cryptManager;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        List<Object> result = (List<Object>) invocation.proceed();
        if (result == null || result.isEmpty()) {
            return result;
        }

        ResultSetHandler resultSetHandler = ProxyUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(resultSetHandler);
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("mappedStatement");

        // 跳过 com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor.buildAutoCountMappedStatement 中创建的count MappedStatement
        if (mappedStatement.getId().endsWith("_mpCount")) {
            return result;
        }

        //判断是否要忽略解密
        if (cryptManager.isMethodIgnoreEncrypt(mappedStatement.getId())) {
            return result;
        }

        Decrypt decryptAnnotation = cryptManager.getMethodDecryptAnnotation(mappedStatement.getId());
        for (int i = 0; i < result.size(); i++) {
            Object obj = result.get(i);
            //处理对象字段解密
            decryptObjField(obj);

            if (decryptAnnotation != null) {
                Cipher cipher = cipherManager.getInstance(decryptAnnotation.cipherInstance()).getInstance();
                if (obj instanceof String) {
                    result.set(i, cipher.decrypt((String) obj));
                } else {
                    decryptMultiString(decryptAnnotation, cipher, obj);
                }
            }
        }

        return result;
    }

    /**
     * 解密对象字段
     *
     * @param object 待解密对象
     */
    private void decryptObjField(Object object) {
        if (object instanceof Map) {
            for (Object o : ((Map<?, ?>) object).values()) {
                decryptObjField(o);
            }
        } else if (object instanceof Collection) {
            for (Object o : (Collection<?>) object) {
                decryptObjField(o);
            }
        } else if (object.getClass().isArray()) {
            for (Object o : (Object[]) object) {
                decryptObjField(o);
            }
        } else {
            List<Field> decryptFields = cryptManager.getDecryptFields(object);
            for (Field field : decryptFields) {
                Decrypt decryptAnnotation = field.getAnnotation(Decrypt.class);
                Cipher cipher = cipherManager.getInstance(decryptAnnotation.cipherInstance()).getInstance();
                Object fieldValue = null;
                try {
                    fieldValue = field.get(object);
                } catch (IllegalAccessException e) {
                    log.error("解密字段出错", e);
                }
                if (fieldValue == null) {
                    continue;
                }
                if (fieldValue instanceof String) {
                    try {
                        field.set(object, cipher.decrypt((String) fieldValue));
                    } catch (IllegalAccessException e) {
                        log.error("解密字段出错", e);
                    }
                } else {
                    boolean isMultiString = decryptMultiString(decryptAnnotation, cipher, fieldValue);
                    if (!isMultiString) {
                        decryptObjField(fieldValue);
                    }
                }
            }
        }
    }

    private boolean decryptMultiString(Decrypt decryptAnnotation, Cipher cipher, Object value) {
        AtomicBoolean isMultiString = new AtomicBoolean(false);
        if (value instanceof Map) {
            MapCrypt[] mapCrypts = decryptAnnotation.map();
            if (mapCrypts.length != 0) {
                isMultiString.compareAndSet(false, true);
            }
            Map map = (Map) value;
            for (MapCrypt mapCrypt : mapCrypts) {
                Cipher mapCipher = cipherManager.getInstance(mapCrypt.cipherInstance()).getInstance();
                String[] keys = mapCrypt.key();
                for (String key : keys) {
                    Object v = map.get(key);
                    if (v instanceof String) {
                        map.put(key, mapCipher.decrypt((String) v));
                    }
                }
            }
        } else if (value instanceof Collection) {
            Collection<Object> colValue = (Collection<Object>) value;
            List<?> decryptedList = colValue.stream()
                    .map(o -> {
                        if (o instanceof String) {
                            isMultiString.compareAndSet(false, true);
                            return cipher.decrypt((String) o);
                        } else {
                            return o;
                        }
                    })
                    .collect(Collectors.toList());
            colValue.clear();
            colValue.addAll(decryptedList);
        } else if (value instanceof String[]) {
            isMultiString.compareAndSet(false, true);
            String[] strArrValue = (String[]) value;
            for (int j = 0; j < strArrValue.length; j++) {
                strArrValue[j] = cipher.decrypt(strArrValue[j]);
            }
        }

        return isMultiString.get();
    }
}
