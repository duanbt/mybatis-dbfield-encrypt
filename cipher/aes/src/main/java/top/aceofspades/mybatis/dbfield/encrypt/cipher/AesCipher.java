package top.aceofspades.mybatis.dbfield.encrypt.cipher;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import top.aceofspades.mybatis.dbfield.encrypt.core.cipher.AbstractCipher;
import top.aceofspades.mybatis.dbfield.encrypt.core.exception.MybatisDbfieldEncryptException;

/**
 * @author duanbt
 * @create 2023-06-25 15:13
 **/
public class AesCipher extends AbstractCipher {

    private static final String PROP_KEY = "key";

    private AES aes;

    @Override
    public String getType() {
        return "AES";
    }

    @Override
    public void afterPropsSet() {
        String key = this.props.get(PROP_KEY);
        if (StrUtil.isBlank(key)) {
            throw new MybatisDbfieldEncryptException("key不能为空");
        }
        this.aes = new AES(SecureUtil.decode(key));
    }

    @Override
    protected String doEncrypt(String rawData) {
        return aes.encryptHex(rawData);
    }

    @Override
    protected String doDecrypt(String encryptedData) {
        return aes.decryptStr(encryptedData);
    }
}
