package top.aceofspades.mybatis.dbfield.encrypt.core.cipher;

import java.util.Map;

/**
 * 加密算法抽象基类
 * <p>
 * 添加了密文前缀功能，例：'AES:密文'
 *
 * @author duanbt
 * @create 2023-04-27 14:51
 **/
public abstract class AbstractCipher implements Cipher {

    private static final String TYPE_SEPARATOR = ":";

    protected Map<String, String> props;

    protected void setProps(Map<String, String> props) {
        this.props = props;
    }

    /**
     * 加密算法类型标识
     * <p>
     * 如 'AES'，加密后会置于密文前面作为前缀
     *
     * @return 加密算法类型标识
     */
    public abstract String getType();

    /**
     * 在props set后执行一些初始化操作
     */
    public abstract void afterPropsSet();

    /**
     * 加密实现
     *
     * @param rawData 明文
     * @return 密文
     */
    protected abstract String doEncrypt(String rawData);

    /**
     * 解密实现
     *
     * @param encryptedData 密文
     * @return 明文
     */
    protected abstract String doDecrypt(String encryptedData);

    @Override
    public String encrypt(String rawData) {
        if (rawData == null) {
            return null;
        }
        if (rawData.trim().length() == 0) {
            return rawData;
        } else if (rawData.startsWith(getType())) {
            return rawData;
        } else {
            return getType() + TYPE_SEPARATOR + doEncrypt(rawData.trim());
        }
    }

    @Override
    public String decrypt(String encryptedData) {
        if (encryptedData == null) {
            return null;
        }
        if (!encryptedData.startsWith(getType())) {
            return encryptedData;
        } else {
            encryptedData = encryptedData.replace(getType() + TYPE_SEPARATOR, "");
            return doDecrypt(encryptedData);
        }
    }

}
