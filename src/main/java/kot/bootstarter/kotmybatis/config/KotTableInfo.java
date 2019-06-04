package kot.bootstarter.kotmybatis.config;

import kot.bootstarter.kotmybatis.annotation.Column;
import kot.bootstarter.kotmybatis.annotation.Delete;
import kot.bootstarter.kotmybatis.annotation.ID;
import kot.bootstarter.kotmybatis.annotation.TableName;
import kot.bootstarter.kotmybatis.common.CT;
import kot.bootstarter.kotmybatis.utils.KotStringUtils;
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

    /**
     * 表信息缓存
     */
    private static final Map<Class<?>, TableInfo> TABLE_INFO_CACHE = new ConcurrentHashMap<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TableInfo {
        private String tableName;
        /**
         * 主键字段
         */
        private String primaryKey;
        /**
         * 表列集合:(id,name,create_time...)
         */
        private String columns;
        /**
         * 逻辑删除字段
         */
        private FieldWrapper logicDelFieldWrapper;
        /**
         * 属性集合
         */
        private List<FieldWrapper> fields;
        /**
         * 表列属性
         */
        private List<FieldWrapper> columnFields;
        /**
         * 字段和属性关系
         */
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
        private List<Annotation> annotations;
        private String deleteAnnoVal;

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
        List<FieldWrapper> columnFields = new ArrayList<>();
        Map<String, String> fieldColumnMap = new HashMap<>();
        Map<String, String> columnFieldMap = new HashMap<>();
        StringBuilder columnBuilder = new StringBuilder();
        final Field[] declaredFields = entityClass.getDeclaredFields();
        for (Field field : declaredFields) {

            Map<Class<?>, Annotation> annotationMap = new HashMap<>();
            List<Annotation> annotations = new ArrayList<>();

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
                columnBuilder.append("`").append(column).append("`").append(CT.SPILT);
                // 属性和列映射
                fieldColumnMap.put(field.getName(), column);
                columnFieldMap.put(column, field.getName());
                columnFields.add(new FieldWrapper(field, field.getName(), column, annotations, null));
            }

            String finalColumn = column;
            Arrays.stream(field.getDeclaredAnnotations()).forEach(anno -> {
                // 封装逻辑删除字段
                if (anno instanceof Delete) {
                    builder.logicDelFieldWrapper(new FieldWrapper(field, field.getName(), finalColumn, annotations, ((Delete) anno).value()));
                }
                annotations.add(anno);
            });
            fields.add(new FieldWrapper(field, field.getName(), column, annotations, null));
        }
        Assert.hasLength(columnBuilder.toString(), "[实体: " + entityClass.getSimpleName() + "]中属性注解:[@Column]一个都不存在");
        KotStringUtils.delLastChat(columnBuilder);

        return builder.tableName(tableNameAnnotation.value()).fields(fields).columns(columnBuilder.toString())
                .fieldColumnMap(fieldColumnMap).columnFieldMap(columnFieldMap).columnFields(columnFields).build();


    }


    public static String primaryKey(Object entity) {
        return get(entity).getPrimaryKey();
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

    /**
     * 逻辑删除注解属性
     */
    public static Delete getDeleteAnno(List<Annotation> list) {
        for (Annotation anno : list) {
            if (anno instanceof Delete) {
                return (Delete) anno;
            }
        }
        return null;
    }


}
