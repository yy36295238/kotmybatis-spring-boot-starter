package kot.bootstarter.kotmybatis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 表名注解
 *
 * @author yangyu
 */
@Target(ElementType.TYPE)
@Retention(RUNTIME)
@Documented
public @interface TableName {
    /**
     * 表实际名称(t_user)
     */
    String value();

    /**
     * 业务类(UserService.class)
     */
    Class<?> service() default Void.class;
}
