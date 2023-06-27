package top.aceofspades.mybatis.dbfield.encrypt.core.cipher;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author duanbt
 * @create 2023-06-02 10:33
 **/
@Getter
@Setter
public class CipherInstanceInfo {

    /**
     * cipher实例id
     */
    private String id;

    /**
     * cipher配置项
     */
    private Map<String, String> props;

    /**
     * cipher class name
     */
    private String className;

}
