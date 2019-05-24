package kot.bootstarter.kotmybatis.mapper;

import kot.bootstarter.kotmybatis.annotation.Exist;
import kot.bootstarter.kotmybatis.annotation.TableName;
import kot.bootstarter.kotmybatis.common.CT;
import kot.bootstarter.kotmybatis.common.Page;
import kot.bootstarter.kotmybatis.utils.KotBeanUtils;
import kot.bootstarter.kotmybatis.utils.KotStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author YangYu
 */
@Slf4j
public class BaseProvider<T> implements ProviderMethodResolver {

    /**
     * 表名缓存
     */
    private static final Map<Class, String> TABLE_CACHE = new ConcurrentHashMap<>();
    /**
     * 表字段缓存
     */
    private static final Map<Class, String> COLUMN_CACHE = new ConcurrentHashMap<>();

    /**
     * 表值缓存
     */
    private static final Map<Class, String> VALUE_CACHE = new ConcurrentHashMap<>();

    /**
     * 批量表值缓存
     */
    private static final Map<Class, String> BATCH_VALUE_CACHE = new ConcurrentHashMap<>();

    public String insert(T entity) {
        final Class<?> entityClass = entity.getClass();
        String columns = COLUMN_CACHE.get(entityClass);
        String values = VALUE_CACHE.get(entityClass);
        if (StringUtils.isBlank(columns) || StringUtils.isBlank(values)) {
            insertSqlBuilder(entity, false);
        }
        return new SQL().INSERT_INTO(tableName(entity))
                .INTO_COLUMNS(COLUMN_CACHE.get(entityClass))
                .INTO_VALUES(VALUE_CACHE.get(entityClass)).toString();
    }

    public String batchInsert(Map<String, List<T>> map) {
        final List<T> list = map.get("list");
        Assert.notEmpty(list, "[batch insert list size must be > 0]");
        final T entity = list.get(0);

        String columns = COLUMN_CACHE.get(entity.getClass());
        String batchValues = BATCH_VALUE_CACHE.get(entity.getClass());

        if (StringUtils.isBlank(columns) || StringUtils.isBlank(batchValues)) {
            insertSqlBuilder(entity, true);
        }
        columns = COLUMN_CACHE.get(entity.getClass());
        batchValues = BATCH_VALUE_CACHE.get(entity.getClass());

        final SQL sql = new SQL().INSERT_INTO(tableName(entity)).INTO_COLUMNS(columns);

        final StringBuilder valuesBuilder = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            //拼接单个values,(#{list[0].a})
            valuesBuilder.append("(")
                    .append(batchValues.replaceAll("%d", i + ""))
                    .append(")")
                    .append(CT.SPILT);
        }
        KotStringUtils.delLastChat(valuesBuilder);

        return sql.toString() + CT.VALUES + valuesBuilder.toString();
    }


    public String findOne(Map<String, Object> map) {
        return list(map) + CT.LIMIT_ONE;
    }

    public String list(Map<String, Object> map) {
        return selectGeneralSql(map, new SQL(), "*").toString();
    }

    public String count(Map<String, Object> map) {
        map.remove(CT.ORDER_BY);
        return selectGeneralSql(map, new SQL(), "COUNT(*)").toString();
    }

    public String selectPage(Map<String, Object> map) {
        final Page page = (Page) map.get("page");
        int pageIndex = (page.getPageIndex() - 1) * page.getPageSize();
        final SQL sql = selectGeneralSql(map, new SQL(), "*");
        return sql.toString() + CT.LIMIT + pageIndex + CT.SPILT + page.getPageSize();
    }

    public String delete(Map<String, Object> map) {
        final T entity = (T) map.get(CT.ALIAS_ENTITY);
        final String conditionSql = (String) map.get(CT.SQL_CONDITION);
        final String whereBuilder = whereBuilder(entity, conditionSql);
        Assert.hasLength(whereBuilder, "[delete must be contain where condition!!!]");
        return new SQL().DELETE_FROM(tableName(entity)).WHERE(whereBuilder).toString();
    }

    public String updateById(T entity) {
        final Object id = KotBeanUtils.fieldVal("id", entity);
        Assert.notNull(id, "id is null");
        return new SQL().UPDATE(tableName(entity)).SET(updateSqlBuilder(entity)).WHERE("id=#{id}").toString();
    }

    public String update(Map<String, Object> map) {
        final T whereEntity = (T) map.get(CT.ALIAS_ENTITY);
        final T setEntity = (T) map.get(CT.SET_ENTITY);
        final String conditionSql = (String) map.get(CT.SQL_CONDITION);
        final String whereBuilder = whereBuilder(whereEntity, conditionSql);
        Assert.hasLength(whereBuilder, "[update must be contain where condition!!!]");
        return new SQL().UPDATE(tableName(whereEntity)).SET(updateSqlBuilder(setEntity, CT.SET_ENTITY)).WHERE(whereBuilder).toString();
    }

    /*
     * =====================条件组装分割线==================
     */

    /**
     * where条件组装
     */
    private String whereBuilder(T entity, String conditionSql) {
        StringBuilder whereBuilder = new StringBuilder();
        // 实体条件
        entitySqlBuilder(whereBuilder, entity);
        // 链式条件
        whereBuilder.append(conditionSql);

        String condition = "";
        final int len = whereBuilder.length();
        if (len > 0) {
            // 删除第一个一个`AND`、`OR`
            condition = KotStringUtils.removeFirstAndOr(whereBuilder.toString());
        }
        return condition;
    }

    /**
     * 实体条件组装
     */
    private static void entitySqlBuilder(StringBuilder whereBuilder, Object entity) {
        final List<KotBeanUtils.FieldWarpper> fieldsList = KotBeanUtils.fields(entity);
        for (KotBeanUtils.FieldWarpper fields : fieldsList) {
            if (!fieldIsExist(fields)) {
                continue;
            }
            Field field = fields.getField();
            field.setAccessible(true);
            Object val;
            try {
                val = field.get(entity);
            } catch (Exception e) {
                throw new RuntimeException("", e);
            }
            if (val != null) {
                String col = field.getName();
                whereBuilder.append(String.format("%s%s=#{%s%s}", CT.AND, KotStringUtils.camel2Underline(col), CT.ALIAS_ENTITY + CT.DOT, col));
            }
        }
    }

    /**
     * 组装插入SQL
     */
    private static void insertSqlBuilder(Object entity, boolean batch) {
        final Class<?> entityClass = entity.getClass();

        StringBuilder columnsBuilder = new StringBuilder();
        StringBuilder valuesBuilder = new StringBuilder();
        try {
            final List<KotBeanUtils.FieldWarpper> fieldsList = KotBeanUtils.fields(entity);
            for (KotBeanUtils.FieldWarpper fields : fieldsList) {
                if (!fieldIsExist(fields)) {
                    continue;
                }
                Field field = fields.getField();
                field.setAccessible(true);
                String column = KotStringUtils.camel2Underline(field.getName());
                columnsBuilder.append("`").append(column).append("`").append(CT.SPILT);
                valuesBuilder.append("#{");
                if (batch) {
                    valuesBuilder.append("list[%d].");
                }
                valuesBuilder.append(field.getName()).append("}").append(CT.SPILT);
            }
            String columns = columnsBuilder.deleteCharAt(columnsBuilder.lastIndexOf(CT.SPILT)).toString();
            String values = valuesBuilder.deleteCharAt(valuesBuilder.lastIndexOf(CT.SPILT)).toString();
            COLUMN_CACHE.put(entityClass, columns);
            if (batch) {
                BATCH_VALUE_CACHE.put(entityClass, values);
            } else {
                VALUE_CACHE.put(entityClass, values);
            }
        } catch (Exception e) {
            throw new RuntimeException("", e);
        }
    }

    /**
     * 组装字段属性, column1,column2,column3...
     */
    private static String columnsBuilder(Object entity) {
        String columns = COLUMN_CACHE.get(entity.getClass());
        if (StringUtils.isNotBlank(columns)) {
            return columns;
        }
        StringBuilder columnsBuilder = new StringBuilder();
        // 获取实体属性
        final List<KotBeanUtils.FieldWarpper> fieldsList = KotBeanUtils.fields(entity);
        fieldsList.forEach(field -> {
            if (fieldIsExist(field)) {
                String column = KotStringUtils.camel2Underline(field.getField().getName());
                columnsBuilder.append("`").append(column).append("`").append(CT.SPILT);
            }
        });
        columns = KotStringUtils.delLastChat(columnsBuilder).toString();
        COLUMN_CACHE.put(entity.getClass(), columns);
        return columns;
    }

    /**
     * 更新SQL
     */
    private static String updateSqlBuilder(Object entity) {
        return updateSqlBuilder(entity, "");
    }

    private static String updateSqlBuilder(Object entity, String alias) {
        StringBuilder columnsBuilder = new StringBuilder();
        try {
            final List<KotBeanUtils.FieldWarpper> fieldsList = KotBeanUtils.fields(entity);
            for (KotBeanUtils.FieldWarpper fields : fieldsList) {
                if (!fieldIsExist(fields)) {
                    continue;
                }
                Field field = fields.getField();
                field.setAccessible(true);
                Object val = field.get(entity);
                if (val != null && !"id".equals(field.getName())) {
                    String column = KotStringUtils.camel2Underline(field.getName());
                    columnsBuilder.append("`").append(column).append("`").append("=");
                    String aliasField = StringUtils.isBlank(alias) ? field.getName() : alias + CT.DOT + field.getName();
                    columnsBuilder.append("#{").append(aliasField).append("}").append(CT.SPILT);
                }
            }
            return columnsBuilder.deleteCharAt(columnsBuilder.lastIndexOf(CT.SPILT)).toString();
        } catch (Exception e) {
            throw new RuntimeException("", e);
        }
    }

    /**
     * 通用查询构建
     */
    private SQL selectGeneralSql(Map<String, Object> map, SQL sql, String column) {
        final T entity = (T) map.get(CT.ALIAS_ENTITY);
        final String conditionSql = (String) map.get(CT.SQL_CONDITION);
        final Map<String, Object> conditionMap = (Map<String, Object>) map.get(CT.ALIAS_CONDITION);
        final Set<String> columns = map.containsKey(CT.COLUMNS) ? (Set<String>) map.get(CT.COLUMNS) : null;
        sql.SELECT(CollectionUtils.isEmpty(columns) ? column : String.join(CT.SPILT, columns)).FROM(tableName(entity));
        final String whereSql = whereBuilder(entity, conditionSql);
        if (StringUtils.isNotBlank(whereSql)) {
            sql.WHERE(whereSql);
        }
        if (conditionMap.containsKey(CT.ORDER_BY)) {
            sql.ORDER_BY(conditionMap.get(CT.ORDER_BY).toString());
        }
        return sql;
    }

    /**
     * 实体获取表名
     */
    private static String tableName(Object obj) {
        Class<?> entityClass = obj.getClass();
        if (TABLE_CACHE.containsKey(entityClass)) {
            return TABLE_CACHE.get(entityClass);
        }
        String tableName;
        // 实体带注解，直接使用做表名
        TableName tableNameAnnotation = entityClass.getAnnotation(TableName.class);
        if (tableNameAnnotation != null && StringUtils.isNoneBlank(tableNameAnnotation.value())) {
            tableName = tableNameAnnotation.value();
        } else {
            // 实体名直接当作表名
            tableName = KotStringUtils.camel2Underline(entityClass.getSimpleName());
        }
        TABLE_CACHE.put(entityClass, tableName);
        return tableName;
    }

    /**
     * 数据库表中包含此列
     */
    private static boolean fieldIsExist(KotBeanUtils.FieldWarpper fields) {
        final Annotation[] annotations = fields.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof Exist && !((Exist) annotation).value()) {
                return false;
            }
        }
        return true;
    }

}
