package kot.bootstarter.kotmybatis.utils;

import kot.bootstarter.kotmybatis.exception.KotException;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

/**
 * @author YangYu
 */
public class MapUtils {


    public static boolean isMap(Object obj) {
        return obj != null && obj instanceof Map;
    }

    public static boolean isMap(List<?> list) {
        return list.size() > 0 && list.get(0) instanceof Map;
    }


    /**
     * Map 中的key 转成驼峰
     */
    public static Object toCamel(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof Map) {
            return toCamel((Map) obj);
        }
        if (obj instanceof List) {
            List list = (List) obj;
            if (list.size() > 0 && list.get(0) instanceof Map) {
                return toCamel(list);
            }
        }
        return obj;

    }

    /**
     * Map 中的key 转成驼峰
     */
    public static Map<String, Object> toCamel(Map<String, Object> map) {
        if (CollectionUtils.isEmpty(map)) {
            return map;
        }
        Map<String, Object> retMap = new HashMap<>();
        map.forEach((k, v) -> retMap.put(KotStringUtils.underline2Camel(k), v));
        return retMap;
    }

    /**
     * List<Map> 中的key 转成驼峰
     */
    public static List<Map<String, Object>> toCamel(List<Map<String, Object>> list) {
        if (CollectionUtils.isEmpty(list)) {
            return list;
        }
        List<Map<String, Object>> maps = new ArrayList<>();
        list.forEach(m -> maps.add(toCamel(m)));
        return maps;

    }

    /**
     * Map 转成 Bean, 下划线转驼峰
     */
    public static Object mapToBean(Object bean, Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        try {
            BeanUtils.populate(bean, toCamel(map));
        } catch (Exception e) {
            throw new KotException("Map转成Bean错误", e);
        }
        return bean;
    }

    public static List mapsToBeans(Object bean, List<Map<String, Object>> maps) {
        if (CollectionUtils.isEmpty(maps)) {
            return maps;
        }
        return maps.stream().map(m -> mapToBean(bean, m)).collect(toList());
    }
}
