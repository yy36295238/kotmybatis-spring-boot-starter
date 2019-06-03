package kot.bootstarter.kotmybatis.config;

import kot.bootstarter.kotmybatis.annotation.Column;
import kot.bootstarter.kotmybatis.annotation.Delete;
import kot.bootstarter.kotmybatis.annotation.ID;
import kot.bootstarter.kotmybatis.annotation.TableName;
import kot.bootstarter.kotmybatis.common.CT;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author YangYu
 */
public class KotTableInfo {

    private static final Map<Class<?>, TableInfo> TABLE_INFO_CACHE = new ConcurrentHashMap<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableInfo {
        private String tableName;
        private String primaryKey;
        private String columns;
        private List<FieldWrapper> fields;
        private Map<String, String> fieldColumnMap;
        private Map<String, String> columnFieldMap;
    }

    /**
     * 表字段-实体属性-注解
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FieldWrapper {
        private Field field;
        private String fieldName;
        private String column;
        private Map<Class<?>, Annotation> annotationMap;
    }

    /**
     * 获取表信息
     */
    public static TableInfo get(Object entity) {
        final Class<?> entityClass = entity.getClass();
        TableInfo tableInfo = TABLE_INFO_CACHE.get(entityClass);
        if (tableInfo != null) {
            return tableInfo;
        }
        tableInfo = makeTableInfo(entityClass);
        TABLE_INFO_CACHE.put(entityClass, tableInfo);
        return tableInfo;
    }

    /**
     * 封装表信息
     */
    private static TableInfo makeTableInfo(Class<?> entityClass) {
        final TableInfo.TableInfoBuilder builder = TableInfo.builder();
        // 获取表名
        TableName tableNameAnnotation = entityClass.getAnnotation(TableName.class);
        Assert.notNull(tableNameAnnotation, String.format("实体:[%s],注解:[@TableName]不存在", entityClass.getSimpleName()));

        List<FieldWrapper> fields = new ArrayList<>();
        Map<String, String> fieldColumnMap = new HashMap<>();
        Map<String, String> columnFieldMap = new HashMap<>();
        StringBuilder columnBuilder = new StringBuilder();
        final Field[] declaredFields = entityClass.getDeclaredFields();
        for (Field field : declaredFields) {

            // 获取主键
            final ID id = field.getAnnotation(ID.class);
            if (id != null) {
                builder.primaryKey(id.value());
            }

            // 封装属性
            String column = null;
            final Column annotation = field.getAnnotation(Column.class);
            if (annotation != null) {
                column = annotation.value();
                // 封装列
                columnBuilder.append(column).append(CT.SPILT);
                // 属性和列映射
                fieldColumnMap.put(field.getName(), column);
                columnFieldMap.put(column, field.getName());
            }
            Map<Class<?>, Annotation> annotationMap = new HashMap<>();
            Arrays.stream(field.getDeclaredAnnotations()).forEach(a -> annotationMap.put(a.getClass(), a));
            final FieldWrapper fieldWrapper = new FieldWrapper(field, field.getName(), column, annotationMap);
            fields.add(fieldWrapper);
        }
        Assert.hasLength(columnBuilder.toString(), "属性注解:[@Column]一个都不存在");
        columnBuilder.deleteCharAt(columnBuilder.lastIndexOf(CT.SPILT));

        return builder.tableName(tableNameAnnotation.value()).fields(fields).columns(columnBuilder.toString())
                .fieldColumnMap(fieldColumnMap).columnFieldMap(columnFieldMap).build();


    }


    public static String primaryKey(Object entity) {
        return get(entity.getClass()).getPrimaryKey();
    }

    public static Field getField(Object entity, String column) {
        final List<FieldWrapper> fields = TABLE_INFO_CACHE.get(entity).getFields();
        for (FieldWrapper fieldWrapper : fields) {
            if (fieldWrapper.column.equals(column)) {
                return fieldWrapper.getField();
            }
        }
        return null;
    }

    public static String getDeleteAnno(Object entity) {
        final List<FieldWrapper> fields = TABLE_INFO_CACHE.get(entity).getFields();
        for (FieldWrapper fieldWrapper : fields) {
            if (fieldWrapper.getAnnotationMap() == null) {
                return null;
            }
            final Delete annotation = (Delete) fieldWrapper.getAnnotationMap().get(Delete.class);
            if (annotation != null) {
                return annotation.value();
            }

        }
        return null;
    }


}
