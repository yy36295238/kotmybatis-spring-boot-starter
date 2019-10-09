package kot.bootstarter.kotmybatis.utils;

import kot.bootstarter.kotmybatis.config.KotTableInfo;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * @author YangYu
 */
public class KotBeanUtils {

    protected KotBeanUtils() {
    }

    /**
     * 根据属性获取值
     */
    public static Object getFieldVal(Field field, Object bean) {
        field.setAccessible(true);
        return ReflectionUtils.getField(field, bean);
    }

    /**
     * 根据属性获取值
     */
    public static Object getFieldVal(KotTableInfo.FieldWrapper fieldWrapper, Object bean) {
        Field field  = fieldWrapper.getField();
        field.setAccessible(true);
        return ReflectionUtils.getField(field, bean);
    }

    /**
     * 根据属性获赋值
     */
    public static void setField(Field field, Object bean, Object val) {
        field.setAccessible(true);
        ReflectionUtils.setField(field, bean, val);
    }

    public static Object cast(Type type, String val) {
        final String typeName = type.getTypeName();
        if ("java.lang.Integer".equals(typeName) || "int".equals(typeName)) {
            return Integer.valueOf(val);
        } else if ("java.lang.Long".equals(typeName) || "long".equals(typeName)) {
            return Long.valueOf(val);
        } else if ("java.lang.Boolean".equals(typeName) || "boolean".equals(typeName)) {
            return Boolean.valueOf(val);
        }
        return val;
    }

}
