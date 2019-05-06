package kot.bootstarter.kotmybatis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 表名注解
 */
@Target(ElementType.FIELD)
@Retention(RUNTIME)
@Documented
public @interface Exist {
    boolean value() default true;
}
