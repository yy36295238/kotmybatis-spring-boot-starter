package kot.bootstarter.kotmybatis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 数据库字段
 */
@Target(ElementType.FIELD)
@Retention(RUNTIME)
@Documented
public @interface Column {
    String value();

    /**
     * 模糊查询
     */
    boolean isLike() default false;

    /**
     * 唯一字段
     */
    boolean unique() default false;

    /**
     * 乐观锁
     */
    boolean version() default false;

    String keyWords() default "";
}
