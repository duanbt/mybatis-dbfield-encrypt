package top.aceofspades.mybatis.dbfield.encrypt.core.manager;

import top.aceofspades.mybatis.dbfield.encrypt.core.annotation.CryptIgnore;
import top.aceofspades.mybatis.dbfield.encrypt.core.annotation.Decrypt;
import top.aceofspades.mybatis.dbfield.encrypt.core.annotation.Encrypt;
import top.aceofspades.mybatis.dbfield.encrypt.core.exception.MybatisDbfieldEncryptException;
import top.aceofspades.mybatis.dbfield.encrypt.core.sqlparse.EncryptableSqlParam;
import top.aceofspades.mybatis.dbfield.encrypt.core.sqlparse.SqlExpressionParser;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author duanbt
 * @create 2023-06-02 17:20
 **/
public class CryptManager {

    private final Map<String, Boolean> mappedStatementCryptIgnoreExistsCache = new ConcurrentHashMap<>();

    private final Map<String, Boolean> mappedStatementDecryptExistsCache = new ConcurrentHashMap<>();

    private final Map<String, Decrypt> mappedStatementDecryptCache = new ConcurrentHashMap<>();

    private final Map<Class<?>, List<Field>> encryptFieldCache = new ConcurrentHashMap<>();

    private final Map<Class<?>, List<Field>> decryptFieldCache = new ConcurrentHashMap<>();

    private final Map<String, Method> mappedStatementMethodCache = new ConcurrentHashMap<>();

    private final Map<String, List<EncryptableSqlParam>> encryptableSqlParamCache = new ConcurrentHashMap<>();

    /**
     * 要加密的 表字段  表名 -> List(列信息)
     */
    private final Map<String, List<EncryptColumn>> cryptColumns = new ConcurrentHashMap<>();

    /**
     * 要加密的 表字段  表名 -> List(列信息)
     */
    public void addCryptColumns(Map<String, List<EncryptColumn>> cryptColumns) {
        this.cryptColumns.putAll(cryptColumns);
    }

    public List<EncryptableSqlParam> getEncryptableSqlParams(String sql) {
        if (cryptColumns.size() == 0) {
            return Collections.emptyList();
        }

        return encryptableSqlParamCache.computeIfAbsent(sql, sqlStr -> {
            List<EncryptableSqlParam> encryptableSqlParams = SqlExpressionParser.parseEncryptableParam(sqlStr);
            Iterator<EncryptableSqlParam> iterator = encryptableSqlParams.iterator();
            while (iterator.hasNext()) {
                EncryptableSqlParam encryptableSqlParam = iterator.next();
                boolean encryptable = false;
                if (cryptColumns.containsKey(encryptableSqlParam.getTableName())) {
                    List<EncryptColumn> encryptColumns = this.cryptColumns.get(encryptableSqlParam.getTableName());
                    for (EncryptColumn encryptColumn : encryptColumns) {
                        if (encryptColumn.getColumn().equals(encryptableSqlParam.getColumnName())) {
                            encryptableSqlParam.setCipherInstance(encryptColumn.getCipherInstance());
                            encryptable = true;
                            break;
                        }
                    }
                }

                if (!encryptable) {
                    iterator.remove();
                }
            }
            return encryptableSqlParams;
        });
    }

    public boolean isMethodIgnoreEncrypt(String mappedStatementId) {
        return mappedStatementCryptIgnoreExistsCache.computeIfAbsent(mappedStatementId, id -> {
            Method method = getMethodByMappedStatementId(id);
            if (method == null) {
                return false;
            }
            return method.getAnnotation(CryptIgnore.class) != null;
        });
    }

    public Decrypt getMethodDecryptAnnotation(String mappedStatementId) {
        Boolean decryptExists = mappedStatementDecryptExistsCache.computeIfAbsent(mappedStatementId, id -> {
            Method method = getMethodByMappedStatementId(id);
            Decrypt decryptAnnotation = method.getAnnotation(Decrypt.class);
            return decryptAnnotation != null;
        });

        if (decryptExists) {
            return mappedStatementDecryptCache.computeIfAbsent(mappedStatementId, id -> {
                Method method = getMethodByMappedStatementId(id);
                return method.getAnnotation(Decrypt.class);
            });
        } else {
            return null;
        }
    }

    public Method getMethodByMappedStatementId(String mappedStatementId) {
        return mappedStatementMethodCache.computeIfAbsent(mappedStatementId, id -> {
            int typeMethodSpiltIndex = id.lastIndexOf(".");
            String mapperClassName = id.substring(0, typeMethodSpiltIndex);
            String mapperMethodName = id.substring(typeMethodSpiltIndex + 1);
            try {
                Class<?> mapperClass = Class.forName(mapperClassName);
                for (Method method : mapperClass.getMethods()) {
                    if (method.getName().equals(mapperMethodName)) {
                        return method;
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new MybatisDbfieldEncryptException("获取mapper class失败", e);
            }
            throw new MybatisDbfieldEncryptException("获取mapper method失败");
        });
    }

    public List<Field> getEncryptFields(Object object) {
        return encryptFieldCache.computeIfAbsent(object.getClass(), clazz -> {
            Field[] fields = clazz.getDeclaredFields();
            return Arrays.stream(fields)
                    .filter(field -> field.isAnnotationPresent(Encrypt.class))
                    .peek(field -> field.setAccessible(true))
                    .collect(Collectors.toList());
        });
    }

    public List<Field> getDecryptFields(Object object) {
        return decryptFieldCache.computeIfAbsent(object.getClass(), clazz -> {
            Field[] fields = clazz.getDeclaredFields();
            return Arrays.stream(fields)
                    .filter(field -> field.isAnnotationPresent(Decrypt.class))
                    .peek(field -> field.setAccessible(true))
                    .collect(Collectors.toList());
        });
    }

}
