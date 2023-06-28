package top.aceofspades.mybatis.dbfield.encrypt.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static top.aceofspades.mybatis.dbfield.encrypt.core.cipher.CipherInstance.DEFAULT_CIPHER;

/**
 * 标注map中的值
 *
 * @author duanbt
 * @create 2023-06-27 14:20
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface MapCrypt {

    /**
     * map 的key
     */
    String[] key();

    /**
     * 密码算法实例
     */
    String cipherInstance() default DEFAULT_CIPHER;
}
