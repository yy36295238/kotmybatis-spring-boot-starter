package kot.bootstarter.kotmybatis.service.impl;

import kot.bootstarter.kotmybatis.common.CT;
import kot.bootstarter.kotmybatis.common.id.IdGeneratorFactory;
import kot.bootstarter.kotmybatis.config.KotTableInfo;
import kot.bootstarter.kotmybatis.enums.ConditionEnum;
import kot.bootstarter.kotmybatis.mapper.BaseMapper;
import kot.bootstarter.kotmybatis.properties.KotMybatisProperties;
import kot.bootstarter.kotmybatis.utils.KotBeanUtils;
import kot.bootstarter.kotmybatis.utils.KotStringUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;

import static java.util.stream.Collectors.toSet;
import static kot.bootstarter.kotmybatis.service.impl.BaseMapperService.MethodEnum.*;
import static kot.bootstarter.kotmybatis.utils.KotStringUtils.classKeyWords;

/**
 * @author YangYu
 */
public class BaseMapperService<T> {

    BaseMapper<T> baseMapper;

    protected KotMybatisProperties properties;

    IdGeneratorFactory idGeneratorFactory;

    T entity;
    T setEntity;
    MapperServiceImpl.MethodEnum methodEnum;
    /**
     * 开启属性设置为null
     */
    boolean setNull;
    List<T> batchList;
    private Map<String, String> fieldColumnMap;
    KotTableInfo.TableInfo tableInfo;
    /**
     * 开启字段模糊查询
     */
    boolean activeLike;
    /**
     * 开启关联字段查询
     */
    boolean activeRelated;
    /**
     * 按主键排序
     */
    boolean orderByIdAsc;
    boolean orderByIdDesc;

    /**
     * 开启实体条件
     */
    boolean activeEntityCondition = true;


    /**
     * ======================
     * 各种条件集合
     * ======================
     */
    Set<String> columns = new HashSet<>();
    Map<String, Object> eqMap = null;
    Map<String, Object> neqMap = null;
    Map<String, Object> inMap = null;
    Map<String, Object> ninMap = null;
    Map<String, Object> ltMap = null;
    Map<String, Object> gtMap = null;
    Map<String, Object> lteMap = null;
    Map<String, Object> gteMap = null;
    Map<String, Object> orMap = null;
    Map<String, Object> likeMap = null;
    Map<String, Object> nullMap = null;
    Map<String, Object> conditionMap = new HashMap<>();
    String conditionSql = "";
    private StringBuilder sqlBuilder = new StringBuilder();

    /**
     * 实体条件，注解处理
     */
    private void entityCondition() {
        if (!this.activeEntityCondition) {
            return;
        }
        final List<KotTableInfo.FieldWrapper> columnFields = tableInfo.getColumnFields();
        for (KotTableInfo.FieldWrapper fieldWrapper : columnFields) {
            final Object fieldVal = KotBeanUtils.getFieldVal(fieldWrapper.getField(), this.entity);
            if (KotStringUtils.isEmpty(fieldVal)) {
                continue;
            }
            (eqMap = map(eqMap)).put(fieldWrapper.getColumn(), fieldVal);
            this.handleEntityAnnotation(fieldWrapper, fieldVal);
        }
    }


    /**
     * 全局注解
     */
    void handleGlobalAnnotation() {
        // 开启逻辑删除
        if (!isSkipLogicDelMethod()) {
            logicDel(false);
        }
    }

    /**
     * 实体属性注解
     */
    private void handleEntityAnnotation(KotTableInfo.FieldWrapper fieldWrapper, Object fieldVal) {
        // 模糊查询
        if (fieldWrapper.getColumnAnno().isLike() && activeLike) {
            (likeMap = map(likeMap)).put(fieldWrapper.getColumn(), fieldVal);
            this.eqMap.remove(fieldWrapper.getColumn());
        }
        // 乐观锁设置
        if (isVersionLock(fieldWrapper)) {
            KotBeanUtils.setField(fieldWrapper.getField(), setEntity, KotBeanUtils.cast(fieldWrapper.getField().getGenericType(), String.valueOf(Long.valueOf(fieldVal.toString()) + 1)));
            KotBeanUtils.setField(fieldWrapper.getField(), entity, fieldVal);
        }
    }

    KotTableInfo.FieldWrapper logicDel(boolean isLogicDelete) {
        if (!properties.isLogicDelete()) {
            return null;
        }
        final KotTableInfo.FieldWrapper logicDelFieldWrapper = this.tableInfo.getLogicDelFieldWrapper();
        if (logicDelFieldWrapper == null) {
            return null;
        }
        if (logicDelFieldWrapper.getDeleteAnnoVal() == null) {
            return null;
        }
        if (!isLogicDelete) {
            (neqMap = map(neqMap)).put(logicDelFieldWrapper.getColumn(), KotBeanUtils.cast(logicDelFieldWrapper.getField().getGenericType(), logicDelFieldWrapper.getDeleteAnnoVal()));
        }
        return logicDelFieldWrapper;
    }

    /**
     * 实际查询条件
     */
    String conditionSql() {

        if (this.entity != null) {
            // 表信息
            this.fieldColumnMap = tableInfo().getFieldColumnMap();
            // 实体条件
            this.entityCondition();
        }

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

        // orderBy条件
        orderByBuilder();

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
            sqlBuilder(conditionEnum, k, v);
            conditionMap.put(newKey(conditionEnum, k), v);
        });
    }


    /**
     * 构建SQL语句
     */
    private void sqlBuilder(ConditionEnum conditionEnum, String k, Object v) {
        // AND OR 处理
        doAndOr(conditionEnum);
        // 关键词处理
        doKeyWords(k);
        sqlBuilder.append(conditionEnum.oper);
        k = newKey(conditionEnum, k);
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
     * AND OR 处理
     */
    private void doAndOr(ConditionEnum conditionEnum) {
        if (conditionEnum == ConditionEnum.OR) {
            sqlBuilder.append(CT.OR);
        } else {
            sqlBuilder.append(CT.AND);
        }
    }

    /**
     * 关键词处理
     */
    private void doKeyWords(String k) {
        final Map<String, String> kewWordsMap = tableInfo().getKewWordsMap();
        final String words = classKeyWords(entity.getClass(), k);
        if (kewWordsMap.containsKey(words)) {
            final String keyWords = kewWordsMap.get(words);
            sqlBuilder.append(keyWords).append(k).append(keyWords);
        } else {
            sqlBuilder.append(k);
        }
    }

    /**
     * 处理排序
     */
    private void orderByBuilder() {
        if (this.orderByIdAsc) {
            conditionMap.put("orderBy", tableInfo.getPrimaryKey().getColumn() + " ASC ");
        }
        if (this.orderByIdDesc) {
            conditionMap.put("orderBy", tableInfo.getPrimaryKey().getColumn() + " DESC ");
        }
    }

    /**
     * key 别名
     */
    private String newKey(ConditionEnum conditionEnum, String key) {
        return conditionEnum.name() + "_" + key;
    }

    Set<String> columnsBuilder() {
        if (CollectionUtils.isEmpty(columns)) {
            return null;
        }
        final Map<String, String> fieldColumnMap = tableInfo().getFieldColumnMap();
        return columns.stream().map(c -> fieldColumnMap.getOrDefault(c, c)).collect(toSet());
    }

    enum MethodEnum {
        /**
         * 调用函数
         */
        BATCH_INSERT, LIST, COUNT, UPDATE, DELETE
    }

    /**
     * 跳过逻辑删除方法
     */
    private boolean isSkipLogicDelMethod() {
        return Arrays.asList(BATCH_INSERT, DELETE).contains(this.methodEnum);
    }

    /**
     * 重置查询条件
     */
    void resetCondition() {
        this.eqMap = null;
        this.conditionSql = "";
        this.sqlBuilder = new StringBuilder();
    }

    private boolean isVersionLock(KotTableInfo.FieldWrapper fieldWrapper) {
        return this.methodEnum == UPDATE && fieldWrapper.getColumnAnno().version();
    }


    Map<String, Object> map(Map<String, Object> conditionMap) {
        return (conditionMap == null ? new HashMap<>() : conditionMap);
    }

    private KotTableInfo.TableInfo tableInfo() {
        return this.tableInfo == null ? KotTableInfo.get(this.entity) : this.tableInfo;
    }
}
