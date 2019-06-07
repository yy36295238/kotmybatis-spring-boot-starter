package kot.bootstarter.kotmybatis.utils;


import kot.bootstarter.kotmybatis.exception.KotException;
import kot.bootstarter.kotmybatis.lambda.Property;

import java.beans.Introspector;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author YangYu
 */
public class LambdaUtils {

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
        try {
            Method method = property.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            SerializedLambda serializedLambda = (SerializedLambda) method.invoke(property);
            String getter = serializedLambda.getImplMethodName();
            return Introspector.decapitalize(getter.replace("get", ""));
        } catch (Exception e) {
            throw new KotException(e);
        }


    }

}
