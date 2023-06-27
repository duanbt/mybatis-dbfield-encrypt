package top.aceofspades.mybatis.dbfield.encrypt.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static top.aceofspades.mybatis.dbfield.encrypt.core.cipher.CipherInstance.DEFAULT_CIPHER;

/**
 * 加密注解
 *
 * @author duanbt
 * @create 2023-04-27 11:55
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Encrypt {
    /**
     * 密码算法实例
     */
    String cipherInstance() default DEFAULT_CIPHER;

    /**
     * 标注map中要加密的字段
     */
    MapCrypt[] map() default {};
}
