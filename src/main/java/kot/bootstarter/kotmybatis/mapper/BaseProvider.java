package kot.bootstarter.kotmybatis.mapper;

import kot.bootstarter.kotmybatis.common.CT;
import kot.bootstarter.kotmybatis.common.Page;
import kot.bootstarter.kotmybatis.config.KotTableInfo;
import kot.bootstarter.kotmybatis.utils.KotStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

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
     * 表值缓存
     */
    private static final Map<Class, String> INSERT_VALUE_CACHE = new ConcurrentHashMap<>();

    /**
     * 批量表值缓存
     */
    private static final Map<Class, String> BATCH_INSERT_VALUE_CACHE = new ConcurrentHashMap<>();

    public String insert(T entity) {
        String columns = KotTableInfo.get(entity).getNoPkColumns();
        String values = insertValues(entity, false);
        return new SQL().INSERT_INTO(tableName(entity)).INTO_COLUMNS(columns).INTO_VALUES(values).toString();
    }

    public String batchInsert(Map<String, List<T>> map) {
        final List<T> list = map.get("list");
        Assert.notEmpty(list, "[批量插入数据,List不能为空]");
        final T entity = list.get(0);

        String columns = KotTableInfo.get(entity).getNoPkColumns();
        String batchValues = insertValues(entity, true);

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

    public String list(Map<String, Object> map) {
        final T entity = (T) map.get(CT.ALIAS_ENTITY);
        return selectGeneralSql(map, new SQL(), KotTableInfo.get(entity).getColumns()).toString();
    }

    public String count(Map<String, Object> map) {
        return selectGeneralSql(map, new SQL(), "COUNT(*)").toString();
    }

    public String selectPage(Map<String, Object> map) {
        final T entity = (T) map.get(CT.ALIAS_ENTITY);
        final Page page = (Page) map.get("page");
        int pageIndex = (page.getPageIndex() - 1) * page.getPageSize();
        final SQL sql = selectGeneralSql(map, new SQL(), KotTableInfo.get(entity).getColumns());
        return sql.toString() + CT.LIMIT + pageIndex + CT.SPILT + page.getPageSize();
    }

    public String delete(Map<String, Object> map) {
        final T entity = (T) map.get(CT.ALIAS_ENTITY);
        final String conditionSql = (String) map.get(CT.SQL_CONDITION);
        final SQL sql = new SQL().DELETE_FROM(tableName(entity));
        if (KotStringUtils.isNotBlank(conditionSql)) {
            sql.WHERE(conditionSql);
        }
        return sql.toString();
    }

    public String update(Map<String, Object> map) {
        final T setEntity = (T) map.get(CT.SET_ENTITY);
        final boolean setNull = (boolean) map.get("setNull");
        final String conditionSql = (String) map.get(CT.SQL_CONDITION);
        return new SQL().UPDATE(tableName(setEntity)).SET(updateSqlBuilder(setEntity, CT.SET_ENTITY, setNull)).WHERE(conditionSql).toString();
    }

    /*
     * =====================条件组装分割线==================
     */

    /**
     * 组装插入SQL
     */
    private static String insertValues(Object entity, boolean batch) {
        final Class<?> entityClass = entity.getClass();
        if (batch && BATCH_INSERT_VALUE_CACHE.containsKey(entityClass)) {
            return BATCH_INSERT_VALUE_CACHE.get(entityClass);
        }
        if (!batch && INSERT_VALUE_CACHE.containsKey(entityClass)) {
            return INSERT_VALUE_CACHE.get(entityClass);
        }

        String values;
        StringBuilder valuesBuilder = new StringBuilder();
        try {

            final KotTableInfo.TableInfo tableInfo = KotTableInfo.get(entity);
            final List<KotTableInfo.FieldWrapper> fieldsList = tableInfo.getColumnFields();
            for (KotTableInfo.FieldWrapper fieldWrapper : fieldsList) {
                if (fieldWrapper.isPk()) {
                    continue;
                }
                Field field = fieldWrapper.getField();
                field.setAccessible(true);
                valuesBuilder.append("#{");
                if (batch) {
                    valuesBuilder.append("list[%d].");
                }
                valuesBuilder.append(field.getName()).append("}").append(CT.SPILT);
            }
            values = KotStringUtils.delLastChat(valuesBuilder).toString();
            if (batch) {
                BATCH_INSERT_VALUE_CACHE.put(entityClass, values);
            } else {
                INSERT_VALUE_CACHE.put(entityClass, values);
            }
        } catch (Exception e) {
            throw new RuntimeException("", e);
        }
        return values;
    }


    /**
     * 更新SQL
     */
    private static String updateSqlBuilder(Object entity, String alias, boolean setNull) {
        StringBuilder columnsBuilder = new StringBuilder();
        try {
            final KotTableInfo.TableInfo tableInfo = KotTableInfo.get(entity);
            final List<KotTableInfo.FieldWrapper> fieldsList = tableInfo.getColumnFields();
            for (KotTableInfo.FieldWrapper fieldWrapper : fieldsList) {
                Field field = fieldWrapper.getField();
                field.setAccessible(true);
                Object val = field.get(entity);
                if ((setNull || val != null) && !field.getName().equals(tableInfo.getPrimaryKey().getColumn())) {
                    columnsBuilder.append("`").append(fieldWrapper.getColumn()).append("`").append("=");
                    String aliasField = StringUtils.isBlank(alias) ? field.getName() : alias + CT.DOT + field.getName();
                    columnsBuilder.append("#{").append(aliasField).append("}").append(CT.SPILT);
                }
            }
            KotStringUtils.delLastChat(columnsBuilder);
            return columnsBuilder.toString();
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
        if (StringUtils.isNotBlank(conditionSql)) {
            sql.WHERE(conditionSql);
        }
        if (conditionMap.containsKey(CT.ORDER_BY)) {
            sql.ORDER_BY(conditionMap.get(CT.ORDER_BY).toString());
        }
        return sql;
    }

    /**
     * 实体获取表名
     */
    private static String tableName(Object entity) {
        return KotTableInfo.get(entity).getTableName();
    }


}
