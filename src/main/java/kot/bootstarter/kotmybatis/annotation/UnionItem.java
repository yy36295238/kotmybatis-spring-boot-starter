package kot.bootstarter.kotmybatis.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 关联子表查询
 */
@Target(ElementType.FIELD)
@Retention(RUNTIME)
@Documented
public @interface UnionItem {

    /**
     * 关联表名
     * User.class
     */
    Class<?> clazz();

    /**
     * 外键关联表字段
     * user_name
     */
    String fkColumn();


}
