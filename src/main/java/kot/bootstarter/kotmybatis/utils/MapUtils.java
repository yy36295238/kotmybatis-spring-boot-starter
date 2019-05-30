package kot.bootstarter.kotmybatis.utils;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YangYu
 */
public class MapUtils {

    public static void aliasKey(Map<String, Object> map, String format) {
        Map<String, Object> newMap = new HashMap<>();
        map.keySet().forEach(k -> newMap.put(String.format(format, k), map.get(k)));
        map.putAll(newMap);

    }

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
}
