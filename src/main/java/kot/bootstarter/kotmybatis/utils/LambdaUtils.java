package kot.bootstarter.kotmybatis.utils;


import kot.bootstarter.kotmybatis.config.KotTableInfo;
import kot.bootstarter.kotmybatis.lambda.Property;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author YangYu
 */
public class LambdaUtils {
    private static final Map<Class, String> FUNC_CACHE = new ConcurrentHashMap<>();


    public static List<String> fieldNames(List<Property> properties) {
        List<String> list = new ArrayList<>();
        for (Property property : properties) {
            list.add(fieldName(property));
        }
        return list;
    }

    public static List<String> fieldNames(Property... properties) {
        return fieldNames(Arrays.asList(properties));
    }

    public static String fieldName(Property property) {
        Class<? extends Property> clazz = property.getClass();
        return Optional.ofNullable(FUNC_CACHE.get(clazz)).orElseGet(() -> {
            try {
                Method method = property.getClass().getDeclaredMethod("writeReplace");
                method.setAccessible(Boolean.TRUE);
                SerializedLambda serializedLambda = (SerializedLambda) method.invoke(property);
                String getter = serializedLambda.getImplMethodName();
                String fileName = Introspector.decapitalize(getter.replace("get", ""));
                FUNC_CACHE.put(clazz, fileName);
                return fileName;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });


    }

}
