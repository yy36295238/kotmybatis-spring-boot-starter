package kot.bootstarter.kotmybatis.utils;

import kot.bootstarter.kotmybatis.common.CT;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

@Slf4j
public class KotStringUtils {

    private static final char UNDERLINE_CHAR = '_';

    /**
     * 集合拼接逗号并添加前后缀  ('a','b','c')
     */
    public static String joinSplit(Collection<?> collection, String prefix, String suffix) {
        StringBuilder sb = new StringBuilder(prefix);
        collection.forEach(c -> {
            sb.append(c.toString()).append(",");
        });
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append(suffix);
        return sb.toString();
    }

    /**
     * 集合拼接逗号并添加前后缀  'a'
     */
    public static String joinPreSuffix(String str, String prefix, String suffix) {
        return prefix + str + suffix;
    }


    /**
     * 截取后缀 AND 1=1 -> 1=1
     */
    public static String removeFirstAndOr(String oriStr) {
        if (oriStr.startsWith(CT.AND)) {
            return oriStr.replaceFirst(CT.AND, "");
        }
        if (oriStr.startsWith(CT.OR)) {
            return oriStr.replaceFirst(CT.OR, "");
        }
        return oriStr;

    }

    /**
     * 截取后缀
     */
    public static String subSuffix(String oriStr, String suffix) {
        return oriStr.substring(0, oriStr.lastIndexOf(suffix));
    }

    /**
     * 驼峰转下划线
     */
    public static String camel2Underline(String name) {

        StringBuilder result = new StringBuilder();
        if (name != null && name.length() > 0) {
            // 将第一个字符处理成大写
            result.append(name.substring(0, 1).toUpperCase());
            // 循环处理其余字符
            for (int i = 1; i < name.length(); i++) {
                String s = name.substring(i, i + 1);
                // 在大写字母前添加下划线
                if (s.equals(s.toUpperCase()) && !Character.isDigit(s.charAt(0))) {
                    result.append(UNDERLINE_CHAR);
                }
                // 其他字符直接转成大写
                result.append(s.toUpperCase());
            }
        }
        return result.toString().toLowerCase();
    }

    /**
     * 下划线转驼峰
     */
    public static String underline2Camel(String underscoreName) {
        StringBuilder result = new StringBuilder();
        if (underscoreName != null && underscoreName.length() > 0) {
            boolean flag = false;
            for (int i = 0; i < underscoreName.length(); i++) {
                char ch = underscoreName.charAt(i);
                if ("_".charAt(0) == ch) {
                    flag = true;
                } else {
                    if (flag) {
                        result.append(Character.toUpperCase(ch));
                        flag = false;
                    } else {
                        result.append(ch);
                    }
                }
            }
        }
        return result.toString();
    }


    /**
     * 删除最后一个逗号','
     */
    public static StringBuilder delLastChat(StringBuilder sb) {
        return sb.deleteCharAt(sb.lastIndexOf(CT.SPILT));
    }


}
