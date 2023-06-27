package top.aceofspades.mybatis.dbfield.encrypt.core.cipher;

import top.aceofspades.mybatis.dbfield.encrypt.core.exception.MybatisDbfieldEncryptException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author duanbt
 * @create 2023-06-02 10:28
 **/
public class CipherManager {

    private final Map<String, CipherInstance> cipherInstances = new HashMap<>();

    public void registerInstance(CipherInstanceInfo instanceInfo) {
        CipherInstance instance = new CipherInstance();
        instance.setId(instanceInfo.getId());
        instance.setInfo(instanceInfo);
        instance.setInstance(createCipher(instanceInfo.getProps(), instanceInfo.getClassName()));
        cipherInstances.put(instance.getId(), instance);

    }

    public CipherInstance getInstance(String id) {
        CipherInstance instance = cipherInstances.get(id);
        if (instance == null) {
            throw new MybatisDbfieldEncryptException(String.format("未获取到密码算法实例[%s]", id));
        } else {
            return instance;
        }
    }

    private Cipher createCipher(Map<String, String> props, String cipherClassName) {
        Object cipher;
        try {
            Class<?> cipherClass = Class.forName(cipherClassName);
            cipher = cipherClass.newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new MybatisDbfieldEncryptException("cipher实例创建出错", e);
        }
        if (!(cipher instanceof AbstractCipher)) {
            throw new MybatisDbfieldEncryptException("Class: " + cipherClassName + "must extend AbstractCipher");
        }

        AbstractCipher abstractCipher = (AbstractCipher) cipher;
        abstractCipher.setProps(props);
        abstractCipher.afterPropsSet();
        return abstractCipher;
    }

}
