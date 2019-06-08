package kot.bootstarter.kotmybatis.utils;

import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

/**
 * @author YangYu
 */
public class KotBeanUtils {

    protected KotBeanUtils() {
    }

    /**
     * 类包含注解
     */
    public static boolean classContainsAnno(Class<?> clazz, Annotation annotation) {
        return clazz.getAnnotation(annotation.getClass()) != null;
    }

    /**
     * 属性包含注解
     */
    public static boolean fieldContainsAnno(Field field, Class annotationClass) {
        return field.getAnnotation(annotationClass) != null;
    }

    /**
     * 类中包含注解的属性
     */
    public static Field getContainsAnnoField(Class<?> clazz, Class annotationClass) {
        final Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            if (fieldContainsAnno(field, annotationClass)) {
                return field;
            }
        }
        return null;
    }

    /**
     * 类中包含注解的属性
     */
    public static Object getContainsAnnoFieldVal(Object bean, Class annotationClass) throws IllegalAccessException {
        final Field field = getContainsAnnoField(bean.getClass(), annotationClass);
        if (field == null) {
            return null;
        }
        return field.get(bean);
    }

    /**
     * 根据属性名称获取属性值
     */
    public static <T> Object fieldVal(String fieldName, T bean) {
        final Field field = ReflectionUtils.findField(bean.getClass(), fieldName);
        field.setAccessible(true);
        return ReflectionUtils.getField(field, bean);
    }

    /**
     * 根据属性获取值
     */
    public static Object getFieldVal(Field field, Object bean) {
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
