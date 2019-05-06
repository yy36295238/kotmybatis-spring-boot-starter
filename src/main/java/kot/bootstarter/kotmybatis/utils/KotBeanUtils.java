package kot.bootstarter.kotmybatis.utils;

import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author YangYu
 */
public class KotBeanUtils {

    private static final Map<Class<?>, Field[]> FIELDS_CACHE = new HashMap<>();

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
        if (field == null) {
            return null;
        }
        field.setAccessible(true);
        return ReflectionUtils.getField(field, bean);
    }

    /**
     * 获取对象属性
     */
    public static Field[] fields(Object obj) {
        Class<?> clazz = obj.getClass();
        if (FIELDS_CACHE.containsKey(clazz)) {
            return FIELDS_CACHE.get(clazz);
        }
        final Field[] declaredFields = clazz.getDeclaredFields();
        FIELDS_CACHE.put(clazz, declaredFields);
        return declaredFields;
    }
}
