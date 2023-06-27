package top.aceofspades.mybatis.dbfield.encrypt.core.cipher;

/**
 * 加密算法
 *
 * @author duanbt
 * @create 2023-04-27 14:25
 **/
public interface Cipher {

    /**
     * 加密
     *
     * @param rawData 明文
     * @return 密文
     */
    String encrypt(String rawData);

    /**
     * 解密
     *
     * @param encryptedData 密文
     * @return 明文
     */
    String decrypt(String encryptedData);
}
