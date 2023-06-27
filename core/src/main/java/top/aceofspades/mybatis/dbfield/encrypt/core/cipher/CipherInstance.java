package top.aceofspades.mybatis.dbfield.encrypt.core.cipher;

import lombok.Getter;
import lombok.Setter;

/**
 * @author duanbt
 * @create 2023-05-05 10:15
 **/
@Getter
@Setter
public class CipherInstance {

    public static final String DEFAULT_CIPHER = "default";

    /**
     * 密码算法实例id
     */
    private String id;

    /**
     * 密码算法实例基础信息
     */
    private CipherInstanceInfo info;

    /**
     * 密码算法实例
     */
    private Cipher instance;
}
