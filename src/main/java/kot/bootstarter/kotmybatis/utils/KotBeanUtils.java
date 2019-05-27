package kot.bootstarter.kotmybatis.utils;

import kot.bootstarter.kotmybatis.annotation.Delete;
import kot.bootstarter.kotmybatis.annotation.Exist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YangYu
 */
public class KotBeanUtils {

    private static final Map<Class<?>, List<FieldWarpper>> FIELDS_CACHE = new HashMap<>();

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
    public static List<FieldWarpper> fields(Object obj) {
        Class<?> clazz = obj.getClass();
        if (FIELDS_CACHE.containsKey(clazz)) {
            return FIELDS_CACHE.get(clazz);
        }
        List<FieldWarpper> list = new ArrayList<>();
        final Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            list.add(new FieldWarpper(field, field.getDeclaredAnnotations()));
        }
        FIELDS_CACHE.put(clazz, list);
        return list;
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

    /**
     * 数据库表中包含此列
     */
    public static boolean fieldIsExist(KotBeanUtils.FieldWarpper fields) {
        final Annotation[] annotations = fields.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof Exist && !((Exist) annotation).value()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 逻辑删除注解
     */
    public static KV logicFiled(Object entity, boolean set) {
        final List<KotBeanUtils.FieldWarpper> fieldsList = KotBeanUtils.fields(entity);
        for (KotBeanUtils.FieldWarpper fieldWarpper : fieldsList) {
            final Annotation[] annotations = fieldWarpper.getAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation instanceof Delete) {
                    final String logicVal = ((Delete) annotation).value();
                    Assert.notNull(logicVal, "@Delete value is empty");
                    final Field field = fieldWarpper.getField();
                    field.setAccessible(true);
                    final Object val = cast(field.getGenericType(), logicVal);
                    if (set) {
                        try {
                            field.set(entity, val);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException("", e);
                        }
                    }
                    return new KV(field.getName(), val);
                }
            }
        }
        return null;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FieldWarpper {
        private Field field;
        private Annotation[] annotations;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class KV {
        private String filed;
        private Object val;
    }
}
