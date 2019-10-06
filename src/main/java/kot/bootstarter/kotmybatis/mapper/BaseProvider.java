package kot.bootstarter.kotmybatis.mapper;

import kot.bootstarter.kotmybatis.annotation.ID;
import kot.bootstarter.kotmybatis.common.CT;
import kot.bootstarter.kotmybatis.config.KotTableInfo;
import kot.bootstarter.kotmybatis.properties.KotMybatisProperties;
import kot.bootstarter.kotmybatis.utils.KotBeanUtils;
import kot.bootstarter.kotmybatis.utils.KotStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.CollectionUtils;

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
     * 批量表值缓存
     */
    private static final Map<Class, String> BATCH_INSERT_VALUE_CACHE = new ConcurrentHashMap<>();

    public String batchInsert(Map<String, List<T>> map) {
        final List<T> list = map.get(CT.KOT_LIST);
        final KotMybatisProperties properties = (KotMybatisProperties) map.get(CT.PROPERTIES);
        final T entity = list.get(0);
        String batchValues = insertValues(entity, properties);
        final SQL sql = new SQL().INSERT_INTO(tableName(entity)).INTO_COLUMNS(insertColumns(entity, properties));
        final StringBuilder valuesBuilder = new StringBuilder();

        for (int i = 0; i < list.size(); i++) {
            //拼接单个values,(#{list[0].a})
            valuesBuilder.append("(").append(batchValues.replaceAll("%d", i + "")).append(")").append(CT.SPILT);
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
        SQL sql = new SQL().UPDATE(tableName(setEntity)).SET(updateSqlBuilder(setEntity, CT.SET_ENTITY, setNull));
        if (KotStringUtils.isNotBlank(conditionSql)) {
            sql.WHERE(conditionSql);
        }
        return sql.toString();
    }

    /*
     * =====================条件组装分割线==================
     */

    /**
     * 组装插入SQL
     */
    private String insertValues(Object entity, KotMybatisProperties properties) {
        final Class<?> entityClass = entity.getClass();
        if (BATCH_INSERT_VALUE_CACHE.containsKey(entityClass)) {
            return BATCH_INSERT_VALUE_CACHE.get(entityClass);
        }
        String values;
        StringBuilder valuesBuilder = new StringBuilder();
        final KotTableInfo.TableInfo tableInfo = KotTableInfo.get(entity);
        final List<KotTableInfo.FieldWrapper> fieldsList = tableInfo.getColumnFields();
        for (KotTableInfo.FieldWrapper fieldWrapper : fieldsList) {
            if (fieldWrapper.isPk() && isAuto(fieldWrapper, properties)) {
                continue;
            }
            valuesBuilder.append("#{");
            valuesBuilder.append(CT.KOT_LIST).append("[%d].");
            valuesBuilder.append(fieldWrapper.getFieldName()).append("}").append(CT.SPILT);
        }
        values = KotStringUtils.delLastChat(valuesBuilder).toString();
        BATCH_INSERT_VALUE_CACHE.put(entityClass, values);
        return values;
    }

    private boolean isAuto(KotTableInfo.FieldWrapper fieldWrapper, KotMybatisProperties properties) {
        ID.IdType idType = fieldWrapper.getIdType() == ID.IdType.NONE ? properties.getIdType() : fieldWrapper.getIdType();
        return idType == ID.IdType.NONE || idType == ID.IdType.AUTO;
    }


    /**
     * 更新SQL
     */
    private String updateSqlBuilder(Object entity, String alias, boolean setNull) {
        StringBuilder columnsBuilder = new StringBuilder();
        final KotTableInfo.TableInfo tableInfo = KotTableInfo.get(entity);
        final List<KotTableInfo.FieldWrapper> fieldsList = tableInfo.getColumnFields();
        for (KotTableInfo.FieldWrapper fieldWrapper : fieldsList) {
            final Object val = KotBeanUtils.getFieldVal(fieldWrapper, entity);
            if ((setNull || val != null) && !fieldWrapper.getFieldName().equals(tableInfo.getPrimaryKey().getColumn())) {
                final String keyWords = fieldWrapper.getColumnAnno().keyWords();
                columnsBuilder.append(keyWords).append(fieldWrapper.getColumn()).append(keyWords).append("=");
                String aliasField = StringUtils.isBlank(alias) ? fieldWrapper.getFieldName() : alias + CT.DOT + fieldWrapper.getFieldName();
                columnsBuilder.append("#{").append(aliasField).append("}").append(CT.SPILT);
            }
        }
        KotStringUtils.delLastChat(columnsBuilder);
        return columnsBuilder.toString();
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
    private String tableName(Object entity) {
        return KotTableInfo.get(entity).getTableName();
    }

    private String insertColumns(T entity, KotMybatisProperties properties) {
        final KotTableInfo.TableInfo tableInfo = KotTableInfo.get(entity);
        return isAuto(tableInfo.getPrimaryKey(), properties) ? tableInfo.getNoPkColumns() : tableInfo.getColumns();
    }


}
