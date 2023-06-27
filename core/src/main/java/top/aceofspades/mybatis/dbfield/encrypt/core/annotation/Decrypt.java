package top.aceofspades.mybatis.dbfield.encrypt.core.annotation;

import java.lang.annotation.*;

/**
 * 解密注解
 *
 * @author duanbt
 * @create 2023-04-27 14:41
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface Decrypt {
    /**
     * 密码算法实例
     */
    String cipherInstance() default "default";

    /**
     * 标注map中要解密的字段
     */
    MapCrypt[] map() default {};
}
