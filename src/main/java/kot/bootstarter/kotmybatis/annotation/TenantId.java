package kot.bootstarter.kotmybatis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 租户ID
 *
 * @author yangyu
 */
@Target(ElementType.FIELD)
@Retention(RUNTIME)
@Documented
public @interface TenantId {
}
