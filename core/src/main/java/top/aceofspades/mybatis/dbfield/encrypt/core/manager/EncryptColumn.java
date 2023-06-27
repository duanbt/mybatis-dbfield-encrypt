package top.aceofspades.mybatis.dbfield.encrypt.core.manager;

import lombok.Getter;
import lombok.Setter;
import top.aceofspades.mybatis.dbfield.encrypt.core.cipher.CipherInstance;

/**
 * @author duanbt
 * @create 2023-06-02 19:08
 **/
@Getter
@Setter
public class EncryptColumn {

    /**
     * 列名
     */
    private String column;

    /**
     * cipher实例id
     */
    private String cipherInstance = CipherInstance.DEFAULT_CIPHER;
}
