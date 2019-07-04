package kot.bootstarter.kotmybatis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 主键
 */
@Target(ElementType.FIELD)
@Retention(RUNTIME)
@Documented
public @interface ID {
    String value();

    IdType idType() default IdType.AUTO;

    public enum IdType {
        /**
         * 主键生成类型
         */
        AUTO, UUID, SNOW_FLAKE, CUSTOMIZE;
    }


}

