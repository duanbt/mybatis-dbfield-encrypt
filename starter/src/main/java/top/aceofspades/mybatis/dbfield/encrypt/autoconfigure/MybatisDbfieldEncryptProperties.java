package top.aceofspades.mybatis.dbfield.encrypt.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import top.aceofspades.mybatis.dbfield.encrypt.core.cipher.CipherInstance;
import top.aceofspades.mybatis.dbfield.encrypt.core.cipher.CipherInstanceInfo;
import top.aceofspades.mybatis.dbfield.encrypt.core.manager.EncryptColumn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author duanbt
 * @create 2023-06-25 11:03
 **/
@ConfigurationProperties(prefix = "mybatis-dbfield-encrypt")
@Getter
@Setter
public class MybatisDbfieldEncryptProperties {

    /**
     * 是否启用 mybatis数据库字段加解密
     */
    private Boolean enable = false;

    /**
     * 默认加解密实例id，如果在cipherInstances中未找到该实例，则使用第一个
     */
    private String defaultCipherInstance = CipherInstance.DEFAULT_CIPHER;

    /**
     * 加解密实例列表
     */
    private List<CipherInstanceInfo> cipherInstances = new ArrayList<>();

    /**
     * sql解析要加密的表字段
     * table -> columns
     */
    private Map<String, List<EncryptColumn>> sqlParseEncryptTableColumns = new HashMap<>();
}
