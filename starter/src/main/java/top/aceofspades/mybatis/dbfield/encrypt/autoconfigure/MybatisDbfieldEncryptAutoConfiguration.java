package top.aceofspades.mybatis.dbfield.encrypt.autoconfigure;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import top.aceofspades.mybatis.dbfield.encrypt.core.cipher.CipherInstance;
import top.aceofspades.mybatis.dbfield.encrypt.core.cipher.CipherInstanceInfo;
import top.aceofspades.mybatis.dbfield.encrypt.core.cipher.CipherManager;
import top.aceofspades.mybatis.dbfield.encrypt.core.exception.MybatisDbfieldEncryptException;
import top.aceofspades.mybatis.dbfield.encrypt.core.interceptor.DecryptInterceptor;
import top.aceofspades.mybatis.dbfield.encrypt.core.interceptor.EncryptInterceptor;
import top.aceofspades.mybatis.dbfield.encrypt.core.manager.CryptManager;
import top.aceofspades.mybatis.dbfield.encrypt.core.manager.EncryptColumn;

import java.util.List;
import java.util.Map;

/**
 * @author duanbt
 * @create 2023-06-25 11:16
 **/
@Configuration
@ConditionalOnClass({SqlSessionFactory.class})
@EnableConfigurationProperties(MybatisDbfieldEncryptProperties.class)
@ConditionalOnProperty(name = "mybatis-dbfield-encrypt.enable", havingValue = "true")
public class MybatisDbfieldEncryptAutoConfiguration implements InitializingBean {

    private final List<SqlSessionFactory> sqlSessionFactories;
    private final MybatisDbfieldEncryptProperties properties;

    public MybatisDbfieldEncryptAutoConfiguration(List<SqlSessionFactory> sqlSessionFactories, MybatisDbfieldEncryptProperties properties) {
        this.sqlSessionFactories = sqlSessionFactories;
        this.properties = properties;
    }


    @Override
    public void afterPropertiesSet() {
        checkProperties(properties);

        List<CipherInstanceInfo> cipherInstances = properties.getCipherInstances();
        CipherManager cipherManager = new CipherManager();
        cipherInstances.forEach(cipherManager::registerInstance);
        String defaultCipherInstance = properties.getDefaultCipherInstance();
        CipherInstanceInfo defaultCipherInstanceInfo = cipherInstances.stream()
                .filter(cipherInstanceInfo -> cipherInstanceInfo.getId().equals(defaultCipherInstance))
                .findFirst()
                .orElse(cipherInstances.get(0));
        CipherInstanceInfo defaultCipherInstanceInfoCopy = new CipherInstanceInfo();
        BeanUtils.copyProperties(defaultCipherInstanceInfo, defaultCipherInstanceInfoCopy);
        defaultCipherInstanceInfoCopy.setId(CipherInstance.DEFAULT_CIPHER);
        cipherManager.registerInstance(defaultCipherInstanceInfoCopy);

        Map<String, List<EncryptColumn>> sqlParseEncryptTableColumns = properties.getSqlParseEncryptTableColumns();
        CryptManager cryptManager = new CryptManager();
        cryptManager.addCryptColumns(sqlParseEncryptTableColumns);

        EncryptInterceptor encryptInterceptor = new EncryptInterceptor(cipherManager, cryptManager);
        DecryptInterceptor decryptInterceptor = new DecryptInterceptor(cipherManager, cryptManager);
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactories) {
            sqlSessionFactory.getConfiguration().addInterceptor(encryptInterceptor);
            sqlSessionFactory.getConfiguration().addInterceptor(decryptInterceptor);
        }
    }

    private void checkProperties(MybatisDbfieldEncryptProperties properties) {
        List<CipherInstanceInfo> cipherInstances = properties.getCipherInstances();
        if (CollectionUtils.isEmpty(cipherInstances)) {
            throw new MybatisDbfieldEncryptException("未配置cipher实例，mybatis-dbfield-encrypt.cipher-instances为空");
        }
        for (int i = 0; i < cipherInstances.size(); i++) {
            CipherInstanceInfo cipherInstance = cipherInstances.get(i);
            if (!StringUtils.hasText(cipherInstance.getId())) {
                throw new MybatisDbfieldEncryptException(String.format("未配置cipher实例id，mybatis-dbfield-encrypt.cipher-instances[%s].id为空", i));
            }
            if (!StringUtils.hasText(cipherInstance.getClassName())) {
                throw new MybatisDbfieldEncryptException(String.format("未配置cipher实例class，mybatis-dbfield-encrypt.cipher-instances[%s].class-name为空", i));
            }
        }

        Map<String, List<EncryptColumn>> sqlParseEncryptTableColumns = properties.getSqlParseEncryptTableColumns();
        for (Map.Entry<String, List<EncryptColumn>> entry : sqlParseEncryptTableColumns.entrySet()) {
            List<EncryptColumn> columns = entry.getValue();
            for (int i = 0; i < columns.size(); i++) {
                EncryptColumn column = columns.get(i);
                if (!StringUtils.hasText(column.getColumn())) {
                    throw new MybatisDbfieldEncryptException(String.format("未配置sql解析加密列名，mybatis-dbfield-encrypt.sql-parse-encrypt-table-columns.%s[%s].column为空", entry.getKey(), i));
                }
            }
        }
    }
}
