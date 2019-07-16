package kot.bootstarter.kotmybatis.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import kot.bootstarter.kotmybatis.annotation.ID;
import kot.bootstarter.kotmybatis.common.CT;
import kot.bootstarter.kotmybatis.common.KotHelper;
import kot.bootstarter.kotmybatis.common.Page;
import kot.bootstarter.kotmybatis.common.id.IdGenerator;
import kot.bootstarter.kotmybatis.common.id.IdGeneratorFactory;
import kot.bootstarter.kotmybatis.config.KotTableInfo;
import kot.bootstarter.kotmybatis.exception.KotException;
import kot.bootstarter.kotmybatis.lambda.Property;
import kot.bootstarter.kotmybatis.mapper.BaseMapper;
import kot.bootstarter.kotmybatis.properties.KotMybatisProperties;
import kot.bootstarter.kotmybatis.service.MapperService;
import kot.bootstarter.kotmybatis.utils.KotBeanUtils;
import kot.bootstarter.kotmybatis.utils.KotStringUtils;
import kot.bootstarter.kotmybatis.utils.LambdaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.*;

import static kot.bootstarter.kotmybatis.service.impl.BaseMapperService.MethodEnum.*;

/**
 * @author YangYu
 * 通用实现
 */
@Slf4j
public class MapperServiceImpl<T> extends BaseMapperService<T> implements MapperService<T> {


    MapperServiceImpl(BaseMapper<T> baseMapper, KotMybatisProperties properties, IdGeneratorFactory idGeneratorFactory) {
        super.baseMapper = baseMapper;
        super.properties = properties;
        super.idGeneratorFactory = idGeneratorFactory;
    }

    /**
     * ======================
     * 公共方法
     * ======================
     */
    @Override
    public int insert(T entity) {
        Assert.notNull(entity, "插入数据实体对象不能为空");
        List<T> batchList = new ArrayList<>();
        batchList.add(entity);
        return this.batchInsert(batchList);
    }

    @Override
    public int batchInsert(List<T> batchList) {
        Assert.notEmpty(batchList, "批量插入数据,List不能为空");
        this.methodEnum = BATCH_INSERT;
        this.batchList = batchList;
        final T entity = batchList.get(0);
        final KotTableInfo.FieldWrapper fieldWrapper = KotTableInfo.get(entity).getPrimaryKey();
        ID.IdType idType = fieldWrapper.getIdType() == ID.IdType.NONE ? properties.getIdType() : fieldWrapper.getIdType();
        final IdGenerator idGenerator = idGeneratorFactory.get(idType);
        if (idGenerator != null) {
            this.batchList.forEach(o -> KotBeanUtils.setField(fieldWrapper.getField(), o, idGenerator.gen()));
        }
        return (int) execute();
    }

    @Override
    public int save(T entity) {
        final KotTableInfo.FieldWrapper fieldWrapper = KotTableInfo.get(entity).getPrimaryKey();
        final Object fieldVal = KotBeanUtils.getFieldVal(fieldWrapper.getField(), entity);
        if (fieldVal == null) {
            return insert(entity);
        }
        return updateById(entity);
    }

    @Override
    public T findOne(T entity) {
        this.entity = entity;
        final List<T> list = this.list(entity);
        return list.size() <= 0 ? null : list.get(0);
    }

    @Override
    public List<T> list(T entity) {
        this.methodEnum = LIST;
        this.entity = entity;
        List<T> list = (List<T>) execute();
        if (list.size() <= 0) {
            return list;
        }
        // 关联字段查询
        if (this.activeRelated) {
            KotHelper.relatedHelp(entity, list, baseMapper);
        }
        return list;
    }

    @Override
    public int count(T entity) {
        this.methodEnum = COUNT;
        this.entity = entity;
        return (int) execute();
    }

    @Override
    public PageInfo<T> selectPage(Page<T> page, T entity) {
        PageHelper.startPage(page.getPageIndex(), page.getPageSize());
        return new PageInfo<>(this.list(entity));
    }

    @Override
    public int delete(T entity) {
        this.methodEnum = MethodEnum.DELETE;
        this.entity = entity;
        return (int) execute();
    }

    @Override
    public int logicDelete(T entity) {
        this.entity = entity;
        this.tableInfo = KotTableInfo.get(entity);
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
            throw new KotException(e);
        }

    }

    @Override
    public int updateById(T entity) {
        return updateById(entity, false);
    }

    @Override
    public int updateById(T entity, boolean setNull) {
        T whereEntity;
        try {
            whereEntity = (T) entity.getClass().newInstance();
            final KotTableInfo.TableInfo tableInfo = KotTableInfo.get(entity);
            final Field primaryField = tableInfo.getPrimaryKey().getField();
            final KotTableInfo.FieldWrapper versionFieldWrapper = tableInfo.getVersionFieldWrapper();
            final Object primaryVal = KotBeanUtils.getFieldVal(primaryField, entity);
            Assert.notNull(primaryVal, "method [updateById] id is null");
            KotBeanUtils.setField(primaryField, whereEntity, primaryVal);
            if (versionFieldWrapper != null) {
                KotBeanUtils.setField(versionFieldWrapper.getField(), whereEntity, KotBeanUtils.getFieldVal(versionFieldWrapper.getField(), entity));
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new KotException(e);
        }
        return update(entity, whereEntity, setNull);
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

    @Override
    public Map<String, Object> columnExist(T entity) {
        Map<String, Object> existMap = new HashMap<>();
        this.activeEntityCondition = false;
        // 判断该字段值存在
        final List<KotTableInfo.FieldWrapper> columnFields = KotTableInfo.get(entity).getColumnFields();
        for (KotTableInfo.FieldWrapper fieldWrapper : columnFields) {
            this.resetCondition();
            if (!fieldWrapper.getColumnAnno().unique()) {
                continue;
            }
            final Object fieldVal = KotBeanUtils.getFieldVal(fieldWrapper.getField(), entity);
            if (KotStringUtils.isEmpty(fieldVal)) {
                continue;
            }
            this.eq(fieldWrapper.getColumn(), fieldVal);
            final int count = this.count(entity);
            if (count > 0) {
                existMap.put(fieldWrapper.getFieldName(), fieldVal);
            }
        }
        return existMap;
    }

    /**
     * ======================
     * 各种条件集合
     * ======================
     */

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
    public MapperService<T> orderByIdAsc() {
        this.orderByIdAsc = true;
        return this;
    }

    @Override
    public MapperService<T> orderByIdDesc() {
        this.orderByIdDesc = true;
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

    @Override
    public MapperService<T> activeLike() {
        this.activeLike = true;
        return this;
    }

    @Override
    public MapperService<T> activeRelated() {
        this.activeRelated = true;
        return this;
    }


    /**
     * 执行调用
     */
    private Object execute() {

        if (this.entity != null) {
            tableInfo = KotTableInfo.get(this.entity);
        }

        // 全局注解处理器
        super.handleGlobalAnnotation();

        conditionSql = KotStringUtils.isBlank(conditionSql) ? super.conditionSql() : conditionSql;
        switch (this.methodEnum) {
            case BATCH_INSERT:
                return baseMapper.batchInsert(this.batchList, this.properties);
            case LIST:
                return baseMapper.list(super.columnsBuilder(), conditionSql, conditionMap, this.entity);
            case COUNT:
                return baseMapper.count(conditionSql, conditionMap, this.entity);
            case UPDATE:
                return baseMapper.update(super.columnsBuilder(), conditionSql, conditionMap, this.entity, this.setEntity, this.setNull);
            case DELETE:
                return baseMapper.delete(conditionSql, conditionMap, entity);
            default:
                throw new KotException("not find method: " + this.methodEnum);
        }

    }


}
