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
        private FieldWrapper primaryKey;
        /**
         * 表列集合:(id,name,create_time...)
         */
        private String columns;
        /**
         * 无主键表列集合:(name,create_time...)
         */
        private String noPkColumns;
        /**
         * 逻辑删除字段
         */
        private FieldWrapper logicDelFieldWrapper;
        /**
         * 乐观锁字段
         */
        private FieldWrapper versionFieldWrapper;
        /**
         * 表列属性
         */
        private List<FieldWrapper> columnFields;
        /**
         * 字段和属性关系
         */
        private Map<String, String> fieldColumnMap;
        private Map<String, String> columnFieldMap;
        private Map<String, FieldWrapper> fieldWrapperMap;
    }

    /**
     * 表字段-实体属性-注解
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class FieldWrapper {
        private Field field;
        private String fieldName;
        private String column;
        private List<Annotation> annotations;
        private String deleteAnnoVal;
        private Column columnAnno;
        private boolean isPk;

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

        List<FieldWrapper> columnFields = new ArrayList<>();
        Map<String, String> fieldColumnMap = new HashMap<>();
        Map<String, String> columnFieldMap = new HashMap<>();
        Map<String, FieldWrapper> fieldWrapperMap = new HashMap<>();
        StringBuilder columnBuilder = new StringBuilder();
        StringBuilder noPkColumnBuilder = new StringBuilder();
        final Field[] declaredFields = entityClass.getDeclaredFields();
        for (Field field : declaredFields) {

            // 所有注解
            final List<Annotation> annotations = Arrays.asList(field.getDeclaredAnnotations());

            // 封装属性
            final FieldWrapper.FieldWrapperBuilder fieldWrapperBuilder = FieldWrapper.builder().field(field).fieldName(field.getName()).annotations(annotations);

            // 获取主键
            final ID id = field.getAnnotation(ID.class);

            // 逻辑删除字段
            final Delete deleteAnno = field.getAnnotation(Delete.class);

            final Column columnAnno = field.getAnnotation(Column.class);
            if (columnAnno != null) {
                String column = columnAnno.value();
                if (id == null) {
                    // 无主键列
                    noPkColumnBuilder.append(column).append(CT.SPILT);
                } else {
                    builder.primaryKey(fieldWrapperBuilder.column(column).isPk(true).build());
                }
                // 全字段列
                columnBuilder.append(column).append(CT.SPILT);
                // 属性和列映射
                fieldColumnMap.put(field.getName(), column);
                columnFieldMap.put(column, field.getName());
                final FieldWrapper fieldWrapper = fieldWrapperBuilder.column(column).columnAnno(columnAnno).build();
                columnFields.add(fieldWrapper);

                // 乐观锁字段
                if (columnAnno.version()) {
                    builder.versionFieldWrapper(fieldWrapperBuilder.build());
                }

                // 逻辑删除字段
                if (deleteAnno != null) {
                    builder.logicDelFieldWrapper(fieldWrapperBuilder.deleteAnnoVal(deleteAnno.value()).build());
                }
            }
            fieldWrapperMap.put(field.getName(), fieldWrapperBuilder.build());


        }
        Assert.hasLength(columnBuilder.toString(), "[实体: " + entityClass.getSimpleName() + "]中属性注解:[@Column]一个都不存在");
        KotStringUtils.delLastChat(columnBuilder);
        KotStringUtils.delLastChat(noPkColumnBuilder);

        return builder.tableName(tableNameAnnotation.value()).columns(columnBuilder.toString()).noPkColumns(noPkColumnBuilder.toString())
                .fieldColumnMap(fieldColumnMap).columnFieldMap(columnFieldMap).columnFields(columnFields)
                .fieldWrapperMap(fieldWrapperMap)
                .build();

    }


}
