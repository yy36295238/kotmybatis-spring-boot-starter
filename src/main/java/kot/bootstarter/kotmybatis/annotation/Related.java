package kot.bootstarter.kotmybatis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 关联注解
 */
@Target(ElementType.FIELD)
@Retention(RUNTIME)
@Documented
public @interface Related {

    /**
     * 关联表名
     * User.class
     */
    Class<?> clazz();

    /**
     * 关联关系字段
     * createUserName
     */
    String[] columns();

    /**
     * 外键关联表字段
     * user_name
     */
    String fkColumn() default "";


}
