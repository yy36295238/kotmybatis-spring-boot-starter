package kot.bootstarter.kotmybatis.service.impl;

import kot.bootstarter.kotmybatis.common.CT;
import kot.bootstarter.kotmybatis.common.Page;
import kot.bootstarter.kotmybatis.config.KotTableInfo;
import kot.bootstarter.kotmybatis.enums.ConditionEnum;
import kot.bootstarter.kotmybatis.lambda.Property;
import kot.bootstarter.kotmybatis.mapper.BaseMapper;
import kot.bootstarter.kotmybatis.properties.KotMybatisProperties;
import kot.bootstarter.kotmybatis.service.MapperService;
import kot.bootstarter.kotmybatis.utils.KotBeanUtils;
import kot.bootstarter.kotmybatis.utils.KotStringUtils;
import kot.bootstarter.kotmybatis.utils.LambdaUtils;
import kot.bootstarter.kotmybatis.utils.MapUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author YangYu
 * 通用实现
 */
@Slf4j
public class MapperServiceImpl<T> implements MapperService<T> {

    private Page<T> page;
    private T entity;
    private T setEntity;
    private MethodEnum methodEnum;
    private boolean setNull;
    private List<T> batchList;
    private boolean skipLogicDelMethod = false;
    private Map<String, String> fieldColumnMap;

    private BaseMapper<T> baseMapper;

    private KotMybatisProperties properties;

    MapperServiceImpl(BaseMapper<T> baseMapper, KotMybatisProperties properties) {
        this.baseMapper = baseMapper;
        this.properties = properties;
    }

    /**
     * ======================
     * 公共方法
     * ======================
     */
    @Override
    public int insert(T entity) {
        this.skipLogicDelMethod = true;
        this.methodEnum = MethodEnum.INSERT;
        this.entity = entity;
        return (int) execute();
    }

    @Override
    public int batchInsert(List<T> batchList) {
        this.skipLogicDelMethod = true;
        this.methodEnum = MethodEnum.BATCH_INSERT;
        this.batchList = batchList;
        return (int) execute();
    }

    @Override
    public int save(T entity) {
        this.skipLogicDelMethod = true;
        final String primaryKey = KotTableInfo.primaryKey(entity);
        final Object id = KotBeanUtils.fieldVal(primaryKey, entity);
        if (id == null) {
            return insert(entity);
        }
        return updateById(entity, true);
    }

    @Override
    public T findOne(T entity) {
        this.methodEnum = MethodEnum.FIND_ONE;
        this.entity = entity;
        return (T) execute();
    }

    @Override
    public List<T> list(T entity) {
        this.methodEnum = MethodEnum.LIST;
        this.entity = entity;
        return (List<T>) execute();
    }

    @Override
    public Integer count(T entity) {
        this.methodEnum = MethodEnum.COUNT;
        this.entity = entity;
        return (Integer) execute();
    }

    @Override
    public Page<T> selectPage(Page<T> page, T entity) {
        this.entity = entity;
        this.page = page;
        boolean containsOrderBy = false;
        // count 不拼接 order by
        Object orderBy = conditionMap.get(CT.ORDER_BY);
        if (conditionMap.containsKey(CT.ORDER_BY)) {
            containsOrderBy = true;
            conditionMap.remove(CT.ORDER_BY);
        }
        final int count = count(entity);
        if (count <= 0) {
            return page;
        }
        if (containsOrderBy) {
            conditionMap.put(CT.ORDER_BY, orderBy);
        }
        this.methodEnum = MethodEnum.SELECT_PAGE;
        final List<T> list = (List<T>) execute();
        page.setData(list);
        page.setTotal(count);
        return page;
    }

    @Override
    public int delete(T entity) {
        this.skipLogicDelMethod = true;
        this.methodEnum = MethodEnum.DELETE;
        this.entity = entity;
        return (int) execute();
    }

    @Override
    public int logicDelete(T entity) {
        this.entity = entity;
        if (!properties.isLogicDelete()) {
            throw new RuntimeException("未启用逻辑删除功能,如果想启用,添加配置:[kot.mybatis.logicDelete=true]");
        }
        final KotTableInfo.FieldWrapper fieldWrapper = logicDel(true);
        Assert.notNull(fieldWrapper, "未找到逻辑删除注解@Delete");
        try {
            T setEntity = (T) entity.getClass().newInstance();
            KotBeanUtils.setField(fieldWrapper.getField(), setEntity, KotBeanUtils.cast(fieldWrapper.getField().getGenericType(), fieldWrapper.getDeleteAnnoVal()));
            return update(setEntity, entity);
        } catch (Exception e) {
            throw new RuntimeException("", e);
        }

    }

    @Override
    public int updateById(T entity) {
        return updateById(entity, false);
    }

    @Override
    public int updateById(T entity, boolean setNull) {
        this.methodEnum = MethodEnum.UPDATE_BY_ID;
        this.entity = entity;
        this.setNull = setNull;
        return (int) execute();
    }

    @Override
    public int update(T setEntity, T whereEntity) {
        return update(setEntity, whereEntity, false);
    }

    @Override
    public int update(T setEntity, T whereEntity, boolean setNull) {
        this.methodEnum = MethodEnum.UPDATE;
        this.entity = whereEntity;
        this.setEntity = setEntity;
        this.setNull = setNull;
        return (int) execute();
    }

    private Object execute() {
        // 开启逻辑删除
        if (!this.skipLogicDelMethod) {
            logicDel(false);
        }

        conditionSql = KotStringUtils.isBlank(conditionSql) ? conditionSql() : conditionSql;
        switch (this.methodEnum) {
            case INSERT:
                return baseMapper.insert(this.entity);
            case BATCH_INSERT:
                return baseMapper.batchInsert(this.batchList);
            case FIND_ONE:
                return baseMapper.findOne(columns, conditionSql, conditionMap, this.entity);
            case LIST:
                return baseMapper.list(columns, conditionSql, conditionMap, this.entity);
            case SELECT_PAGE:
                return baseMapper.selectPage(columns, conditionSql, this.page, conditionMap, this.entity);
            case COUNT:
                return baseMapper.count(conditionSql, conditionMap, this.entity);
            case UPDATE:
                return baseMapper.update(columns, conditionSql, conditionMap, this.entity, this.setEntity, this.setNull);
            case UPDATE_BY_ID:
                return baseMapper.updateById(this.entity, this.setNull);
            case DELETE:
                return baseMapper.delete(conditionSql, conditionMap, entity);
            default:
                throw new RuntimeException("not find method: " + this.methodEnum);
        }

    }

    private KotTableInfo.FieldWrapper logicDel(boolean isLogicDelete) {
        if (!properties.isLogicDelete()) {
            return null;
        }
        final KotTableInfo.FieldWrapper logicDelFieldWrapper = KotTableInfo.get(entity).getLogicDelFieldWrapper();
        if (logicDelFieldWrapper == null) {
            return null;
        }
        if (logicDelFieldWrapper.getDeleteAnnoVal() == null) {
            return null;
        }
        if (!isLogicDelete) {
            this.neq(logicDelFieldWrapper.getColumn(), KotBeanUtils.cast(logicDelFieldWrapper.getField().getGenericType(), logicDelFieldWrapper.getDeleteAnnoVal()));
        }
        return logicDelFieldWrapper;
    }

    private Map<String, Object> map(Map<String, Object> conditionMap) {
        return (conditionMap == null ? new HashMap<>() : conditionMap);
    }

    /**
     * ======================
     * 各种条件集合
     * ======================
     */
    private Set<String> columns = new HashSet<>();
    private Map<String, Object> eqMap = null;
    private Map<String, Object> neqMap = null;
    private Map<String, Object> inMap = null;
    private Map<String, Object> ninMap = null;
    private Map<String, Object> ltMap = null;
    private Map<String, Object> gtMap = null;
    private Map<String, Object> lteMap = null;
    private Map<String, Object> gteMap = null;
    private Map<String, Object> orMap = null;
    private Map<String, Object> likeMap = null;
    private Map<String, Object> nullMap = null;
    private Map<String, Object> conditionMap = new HashMap<>();
    private String conditionSql = "";
    private StringBuilder sqlBuilder = new StringBuilder();

    @Override
    public MapperService<T> fields(String... field) {
        fields(Arrays.asList(field));
        return this;
    }

    @Override
    public MapperService<T> fields(Property... properties) {
        fields(LambdaUtils.fieldNames(properties));
        return this;
    }

    @Override
    public MapperService<T> fields(List<String> fields) {
        columns.addAll(fields);
        return this;
    }

    @Override
    public MapperService<T> fieldsByLambda(List<Property> fields) {
        return fields(LambdaUtils.fieldNames(fields));
    }

    @Override
    public MapperService<T> orderBy(String orderBy) {
        conditionMap.put("orderBy", orderBy);
        return this;
    }

    @Override
    public MapperService<T> eq(String key, Object value) {
        (eqMap = map(eqMap)).put(key, value);
        return this;
    }

    @Override
    public MapperService<T> eq(Property property, Object value) {
        return eq(LambdaUtils.fieldName(property), value);
    }

    @Override
    public MapperService<T> neq(String key, Object value) {
        (neqMap = map(neqMap)).put(key, value);
        return this;
    }

    @Override
    public MapperService<T> neq(Property property, Object value) {
        return neq(LambdaUtils.fieldName(property), value);
    }

    @Override
    public MapperService<T> in(String key, String values) {
        return in(key, values.split(CT.SPILT));
    }

    @Override
    public MapperService<T> in(Property property, String values) {
        return in(LambdaUtils.fieldName(property), values.split(CT.SPILT));
    }

    @Override
    public MapperService<T> in(String key, Object[] values) {
        (inMap = map(inMap)).put(key, Arrays.asList(values));
        return this;
    }

    @Override
    public MapperService<T> in(Property property, Object[] values) {
        return in(LambdaUtils.fieldName(property), values);
    }

    @Override
    public MapperService<T> in(String key, Collection<?> values) {
        (inMap = map(inMap)).put(key, values);
        return this;
    }

    @Override
    public MapperService<T> in(Property property, Collection<?> values) {
        return in(LambdaUtils.fieldName(property), values);
    }

    @Override
    public MapperService<T> nin(String key, Object[] values) {
        (ninMap = map(ninMap)).put(key, Arrays.asList(values));
        return this;
    }

    @Override
    public MapperService<T> nin(Property property, Object[] values) {
        return nin(LambdaUtils.fieldName(property), values);
    }

    @Override
    public MapperService<T> nin(String key, Collection<?> values) {
        (ninMap = map(ninMap)).put(key, values);
        return this;
    }

    @Override
    public MapperService<T> nin(Property property, Collection<?> values) {
        return nin(LambdaUtils.fieldName(property), values);
    }

    @Override
    public MapperService<T> lt(String key, Object value) {
        (ltMap = map(ltMap)).put(key, value);
        return this;
    }

    @Override
    public MapperService<T> lt(Property property, Object value) {
        return lt(LambdaUtils.fieldName(property), value);
    }

    @Override
    public MapperService<T> gt(String key, Object value) {
        (gtMap = map(gtMap)).put(key, value);
        return this;
    }

    @Override
    public MapperService<T> gt(Property property, Object value) {
        return gt(LambdaUtils.fieldName(property), value);
    }


    @Override
    public MapperService<T> lte(String key, Object value) {
        (lteMap = map(lteMap)).put(key, value);
        return this;
    }

    @Override
    public MapperService<T> lte(Property property, Object value) {
        return lte(LambdaUtils.fieldName(property), value);
    }

    @Override
    public MapperService<T> gte(String key, Object value) {
        (gteMap = map(gteMap)).put(key, value);
        return this;
    }

    @Override
    public MapperService<T> gte(Property property, Object value) {
        return gte(LambdaUtils.fieldName(property), value);
    }

    @Override
    public MapperService<T> or(String key, Object value) {
        (orMap = map(orMap)).put(key, value);
        return this;
    }

    @Override
    public MapperService<T> or(Property property, Object value) {
        return or(LambdaUtils.fieldName(property), value);
    }

    @Override
    public MapperService<T> like(String key, Object value) {
        (likeMap = map(likeMap)).put(key, value);
        return this;
    }

    @Override
    public MapperService<T> like(Property property, Object value) {
        return like(LambdaUtils.fieldName(property), value);
    }

    @Override
    public MapperService<T> between(String key, Object left, Object right) {
        (gteMap = map(gteMap)).put(key, left);
        (lteMap = map(lteMap)).put(key, right);
        return this;
    }

    @Override
    public MapperService<T> between(Property property, Object left, Object right) {
        return between(LambdaUtils.fieldName(property), left, right);
    }

    @Override
    public MapperService<T> isNull(String key) {
        (nullMap = map(nullMap)).put(key, null);
        return this;
    }

    @Override
    public MapperService<T> isNull(Property property) {
        return isNull(LambdaUtils.fieldName(property));
    }

    /**
     * 实际查询条件
     */
    private String conditionSql() {

        // 实体条件
        this.entityCondition();

        this.fieldColumnMap = KotTableInfo.get(this.entity).getFieldColumnMap();

        // 链式条件
        if (eqMap != null) {
            conditionMapBuilder(ConditionEnum.EQ, eqMap);
        }
        if (neqMap != null) {
            conditionMapBuilder(ConditionEnum.NEQ, neqMap);
        }
        if (inMap != null) {
            conditionMapBuilder(ConditionEnum.IN, inMap);
        }
        if (ninMap != null) {
            conditionMapBuilder(ConditionEnum.NIN, ninMap);
        }
        if (ltMap != null) {
            conditionMapBuilder(ConditionEnum.LT, ltMap);
        }
        if (gtMap != null) {
            conditionMapBuilder(ConditionEnum.GT, gtMap);
        }
        if (lteMap != null) {
            conditionMapBuilder(ConditionEnum.LTE, lteMap);
        }
        if (gteMap != null) {
            conditionMapBuilder(ConditionEnum.GTE, gteMap);
        }
        if (likeMap != null) {
            conditionMapBuilder(ConditionEnum.LIKE, likeMap);
        }
        if (nullMap != null) {
            conditionMapBuilder(ConditionEnum.NULL, nullMap);
        }
        // 放在最后，否则拼接sql会有问题
        if (orMap != null) {
            conditionMapBuilder(ConditionEnum.OR, orMap);
        }
        conditionSql = KotStringUtils.removeFirstAndOr(sqlBuilder.toString());
        return conditionSql;
    }

    /**
     * 构建条件Map
     */
    private void conditionMapBuilder(ConditionEnum conditionEnum, Map<String, Object> logicMap) {
        logicMap.forEach((k, v) -> {
            // lambda属性映射表字段
            if (fieldColumnMap.containsKey(k)) {
                k = fieldColumnMap.get(k);
            }
            sqlBuilder(sqlBuilder, conditionEnum, k, v);
            conditionMap.put(conditionEnum.name() + "_" + k, v);
        });
//        MapUtils.aliasKey(logicMap, newKey(conditionEnum));
//        conditionMap.putAll(logicMap);
    }

    /**
     * 实体条件
     */
    private void entityCondition() {
        if (this.entity != null) {
            final KotTableInfo.TableInfo tableInfo = KotTableInfo.get(entity);
            tableInfo.getFields().forEach(fieldWrapper -> {
                final Field field = fieldWrapper.getField();
                field.setAccessible(true);
                try {
                    final Object val = field.get(entity);
                    if (val != null) {
                        (eqMap = map(eqMap)).put(tableInfo.getFieldColumnMap().get(field.getName()), val);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * 构建SQL语句
     */
    private void sqlBuilder(StringBuilder sqlBuilder, ConditionEnum conditionEnum, String k, Object v) {
        if (conditionEnum == ConditionEnum.OR) {
            sqlBuilder.append(CT.OR);
        } else {
            sqlBuilder.append(CT.AND);
        }
        sqlBuilder.append(k).append(conditionEnum.oper);
        k = conditionEnum.name() + "_" + k;
        // in 查询拼接SQL语法
        if (conditionEnum == ConditionEnum.IN || conditionEnum == ConditionEnum.NIN) {
            Collection collection = (Collection) v;
            StringBuilder inBuilder = new StringBuilder(CT.OPEN);
            for (int i = 0; i < collection.size(); i++) {
                inBuilder.append("#{").append(CT.ALIAS_CONDITION).append(CT.DOT).append(k).append("[").append(i).append("]").append("}").append(CT.SPILT);
            }
            inBuilder.deleteCharAt(inBuilder.lastIndexOf(CT.SPILT));
            inBuilder.append(CT.CLOSE);
            sqlBuilder.append(inBuilder.toString());
        } else if (conditionEnum == ConditionEnum.LIKE) {
            // like 查询拼接SQL语法
            sqlBuilder.append("CONCAT").append("('%',").append("#{").append(CT.ALIAS_CONDITION).append(CT.DOT).append(k).append("},").append("'%')");
        } else if (conditionEnum == ConditionEnum.NULL) {
            // nothing
        } else {
            // 默认查询拼接SQL语法
            sqlBuilder.append("#{").append(CT.ALIAS_CONDITION).append(CT.DOT).append(k).append("}");
        }
    }

    /**
     * key 别名
     */
    private String newKey(ConditionEnum conditionEnum) {
        return conditionEnum.name() + "_%s";
    }

    enum MethodEnum {
        /**
         * 调用函数
         */
        INSERT, BATCH_INSERT, FIND_ONE, LIST, COUNT, SELECT_PAGE, UPDATE, UPDATE_BY_ID, DELETE
    }

}
