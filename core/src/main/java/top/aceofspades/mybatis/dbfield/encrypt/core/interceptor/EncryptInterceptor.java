package top.aceofspades.mybatis.dbfield.encrypt.core.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import top.aceofspades.mybatis.dbfield.encrypt.core.annotation.Encrypt;
import top.aceofspades.mybatis.dbfield.encrypt.core.annotation.MapCrypt;
import top.aceofspades.mybatis.dbfield.encrypt.core.cipher.Cipher;
import top.aceofspades.mybatis.dbfield.encrypt.core.cipher.CipherManager;
import top.aceofspades.mybatis.dbfield.encrypt.core.manager.CryptManager;
import top.aceofspades.mybatis.dbfield.encrypt.core.sqlparse.EncryptableSqlParam;
import top.aceofspades.mybatis.dbfield.encrypt.core.util.ProxyUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * 查询的 注解识别加密和sql参数加密 拦截 Executor#query
 * 拦截时机选择原因：mybatis-plus的分页插件拦截了Executor#query，因此必须在分页插件执行之前加密参数
 * <p>
 * 更新的 注解识别加密 拦截 Executor#update
 * 拦截时机选择原因：与查询的注解识别加密保持一致
 * <p>
 * 更新的 sql参数加密 拦截 StatementHandler#parameterize
 * 拦截时机选择原因：拦截 Executor#update实现不了where中foreach标签条件的加密，foreach标签参数在boundSql对象中，而在调用Executor#update时boundSql对象还没生成，
 * 因此得在boundSql对象生成之后再执行加密，选择StatementHandler#parameterize之前拦截
 *
 * @author duanbt
 * @create 2023-06-01 18:28
 **/
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "parameterize", args = {Statement.class})
})
public class EncryptInterceptor implements Interceptor {

    private final CipherManager cipherManager;

    private final CryptManager cryptManager;

    public EncryptInterceptor(CipherManager cipherManager,
                              CryptManager cryptManager) {
        this.cipherManager = cipherManager;
        this.cryptManager = cryptManager;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        Object[] args = invocation.getArgs();

        if (target instanceof Executor) {
            Executor executor = (Executor) target;
            MappedStatement ms = (MappedStatement) args[0];
            Object parameterObject = args[1];

            // 跳过 com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor.buildAutoCountMappedStatement 中创建的count MappedStatement
            if (ms.getId().endsWith("_mpCount")) {
                return invocation.proceed();
            }

            //判断是否要忽略加密
            if (cryptManager.isMethodIgnoreEncrypt(ms.getId()) || parameterObject == null) {
                return invocation.proceed();
            }

            //查询|更新 注解识别加密
            annotationEncrypt(args, ms, parameterObject);

            //查询的 sql参数加密
            boolean isQuery = args.length == 4;
            if (isQuery) {
                BoundSql boundSql = ms.getBoundSql(parameterObject);
                sqlParamEncrypt(parameterObject, boundSql);
                RowBounds rowBounds = (RowBounds) args[2];
                ResultHandler<?> resultHandler = (ResultHandler<?>) args[3];
                CacheKey cacheKey = executor.createCacheKey(ms, parameterObject, rowBounds, boundSql);
                return executor.query(ms, parameterObject, rowBounds, resultHandler, cacheKey, boundSql);
            }
        }

        //更新的 sql参数加密
        if (target instanceof StatementHandler) {
            StatementHandler statementHandler = ProxyUtils.realTarget(invocation.getTarget());
            MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
            MappedStatement ms = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
            BoundSql boundSql = statementHandler.getBoundSql();
            Object parameterObject = boundSql.getParameterObject();

            // 跳过 com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor.buildAutoCountMappedStatement 中创建的count MappedStatement
            if (ms.getId().endsWith("_mpCount")) {
                return invocation.proceed();
            }

            //判断是否要忽略加密
            if (cryptManager.isMethodIgnoreEncrypt(ms.getId()) || parameterObject == null) {
                return invocation.proceed();
            }

            SqlCommandType sqlCommandType = ms.getSqlCommandType();
            if (sqlCommandType != SqlCommandType.SELECT) {
                sqlParamEncrypt(parameterObject, boundSql);
            }

        }

        return invocation.proceed();
    }

    private boolean annotationEncrypt(Object[] args, MappedStatement ms, Object parameterObject) {
        AtomicBoolean canEncrypt = new AtomicBoolean(false);
        Method mapperMethod = cryptManager.getMethodByMappedStatementId(ms.getId());
        ParamNameResolver paramNameResolver = new ParamNameResolver(ms.getConfiguration(), mapperMethod);
        String[] paramNames = paramNameResolver.getNames();
        Parameter[] parameters = mapperMethod.getParameters();
        if (parameterObject instanceof MapperMethod.ParamMap) {
            MapperMethod.ParamMap parameterMap = (MapperMethod.ParamMap) parameterObject;
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                String paramName = paramNames[i];
                Object paramValue = parameterMap.get(paramName);
                if (paramValue == null) {
                    continue;
                }
                //处理对象字段加密
                encryptObjField(paramValue, canEncrypt);

                //处理参数上有加密注解的加密
                Encrypt encryptAnnotation = parameter.getAnnotation(Encrypt.class);
                if (encryptAnnotation == null) {
                    continue;
                }
                canEncrypt.compareAndSet(false, true);
                Cipher cipher = cipherManager.getInstance(encryptAnnotation.cipherInstance()).getInstance();
                if (paramValue instanceof String) { //参数为string
                    if (parameterMap.containsKey(paramName)) {
                        String encryptData = cipher.encrypt((String) paramValue);
                        parameterMap.put(paramName, encryptData);
                        parameterMap.computeIfPresent(ParamNameResolver.GENERIC_NAME_PREFIX + (i + 1), (k, v) -> encryptData);
                    }
                } else {
                    encryptMultiString(encryptAnnotation, cipher, paramValue);
                }
            }
        } else {
            //处理对象字段加密
            encryptObjField(parameterObject, canEncrypt);
            //处理参数上有加密注解的加密
            Parameter parameter = parameters[0];
            Encrypt encryptAnnotation = parameter.getAnnotation(Encrypt.class);
            if (encryptAnnotation != null) {
                canEncrypt.compareAndSet(false, true);
                Cipher cipher = cipherManager.getInstance(encryptAnnotation.cipherInstance()).getInstance();
                if (parameterObject instanceof String) { //参数为string
                    String encryptData = cipher.encrypt((String) parameterObject);
                    args[1] = encryptData;
                } else {
                    encryptMultiString(encryptAnnotation, cipher, parameterObject);
                }
            }
        }
        return canEncrypt.get();
    }

    private boolean sqlParamEncrypt(Object parameterObject, BoundSql boundSql) {
        boolean canEncrypt = false;
        List<EncryptableSqlParam> encryptableSqlParams = cryptManager.getEncryptableSqlParams(boundSql.getSql());
        if (!encryptableSqlParams.isEmpty()) {
            canEncrypt = true;
            MetaObject paramMetaObject = SystemMetaObject.forObject(parameterObject);
            List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
            encryptableSqlParams.forEach(sqlParam -> {
                int jdbcParamIndex = sqlParam.getJdbcParamIndex();
                ParameterMapping parameterMapping = parameterMappings.get(jdbcParamIndex - 1);
                String property = parameterMapping.getProperty();
                Cipher cipher = cipherManager.getInstance(sqlParam.getCipherInstance()).getInstance();
                if (property.startsWith(ForEachSqlNode.ITEM_PREFIX)) {
                    Object originalValue = boundSql.getAdditionalParameter(property);
                    if (originalValue instanceof String) {
                        boundSql.setAdditionalParameter(property, cipher.encrypt((String) originalValue));
                    }
                } else {
                    Object value = paramMetaObject.getValue(property);
                    if (value instanceof String) {
                        paramMetaObject.setValue(property, cipher.encrypt((String) value));
                    }
                }
            });
        }
        return canEncrypt;
    }

    /**
     * 加密对象字段
     *
     * @param object     待加密对象
     * @param canEncrypt 可能已加密标志
     */
    private void encryptObjField(Object object, AtomicBoolean canEncrypt) {
        if (object instanceof Map) {
            for (Object o : ((Map<?, ?>) object).values()) {
                encryptObjField(o, canEncrypt);
            }
        } else if (object instanceof Collection) {
            for (Object o : ((Collection<?>) object)) {
                encryptObjField(o, canEncrypt);
            }
        } else if (object.getClass().isArray()) {
            for (Object o : ((Object[]) object)) {
                encryptObjField(o, canEncrypt);
            }
        } else {
            List<Field> encryptFields = cryptManager.getEncryptFields(object);
            if (!encryptFields.isEmpty()) {
                canEncrypt.compareAndSet(false, true);
            }
            for (Field field : encryptFields) {
                Encrypt encryptAnnotation = field.getAnnotation(Encrypt.class);
                Cipher cipher = cipherManager.getInstance(encryptAnnotation.cipherInstance()).getInstance();
                Object fieldValue = null;
                try {
                    fieldValue = field.get(object);
                } catch (IllegalAccessException e) {
                    log.error("加密字段出错", e);
                }
                if (fieldValue == null) {
                    continue;
                }
                if (fieldValue instanceof String) {
                    try {
                        field.set(object, cipher.encrypt((String) fieldValue));
                    } catch (IllegalAccessException e) {
                        log.error("加密字段出错", e);
                    }
                } else {
                    boolean isMultiString = encryptMultiString(encryptAnnotation, cipher, fieldValue);
                    if (!isMultiString) {
                        encryptObjField(fieldValue, canEncrypt);
                    }
                }
            }
        }
    }

    private boolean encryptMultiString(Encrypt encryptAnnotation, Cipher cipher, Object value) {
        AtomicBoolean isMultiString = new AtomicBoolean(false);
        if (value instanceof Map) {
            MapCrypt[] mapCrypts = encryptAnnotation.map();
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
                        map.put(key, mapCipher.encrypt((String) v));
                    }
                }
            }
        } else if (value instanceof Collection) {
            Collection<Object> colValue = (Collection<Object>) value;
            List<?> encryptedList = colValue.stream()
                    .map(o -> {
                        if (o instanceof String) {
                            isMultiString.compareAndSet(false, true);
                            return cipher.encrypt((String) o);
                        } else {
                            if (o instanceof Map) {
                                encryptMultiString(encryptAnnotation, cipher, o);
                            }
                            return o;
                        }
                    })
                    .collect(Collectors.toList());
            colValue.clear();
            colValue.addAll(encryptedList);
        } else if (value instanceof String[]) {
            isMultiString.compareAndSet(false, true);
            String[] strArrValue = (String[]) value;
            for (int j = 0; j < strArrValue.length; j++) {
                strArrValue[j] = cipher.encrypt(strArrValue[j]);
            }
        }
        return isMultiString.get();
    }

}
