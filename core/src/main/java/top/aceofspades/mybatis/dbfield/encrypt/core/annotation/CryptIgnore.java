package top.aceofspades.mybatis.dbfield.encrypt.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 加解密忽略
 * <p>
 * 有些复杂sql情况下，sql解析可能会出错，可以给mapper方法贴该注解跳过加解密拦截
 *
 * @author duanbt
 * @create 2023-04-27 14:41
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface CryptIgnore {
}
