package kot.bootstarter.kotmybatis.common;

import kot.bootstarter.kotmybatis.annotation.Related;
import kot.bootstarter.kotmybatis.annotation.UnionItem;
import kot.bootstarter.kotmybatis.config.KotTableInfo;
import kot.bootstarter.kotmybatis.exception.KotException;
import kot.bootstarter.kotmybatis.mapper.BaseMapper;
import kot.bootstarter.kotmybatis.utils.KotBeanUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class KotHelper {

    /**
     * 关联字段帮助类
     */
    public static <T> void relatedHelp(Object entity, List<T> list, BaseMapper baseMapper) {
        final KotTableInfo.TableInfo tableInfo = KotTableInfo.get(entity);
        final List<KotTableInfo.FieldWrapper> columnFields = tableInfo.getColumnFields();
        final Map<String, KotTableInfo.FieldWrapper> fieldWrapperMap = tableInfo.getFieldWrapperMap();
        for (KotTableInfo.FieldWrapper fieldWrapper : columnFields) {
            final Related related = fieldWrapper.getField().getAnnotation(Related.class);
            if (related == null) {
                continue;
            }
            final Class<?> relatedClazz = related.clazz();
            final String pkColumn = isBlank(related.fkColumn()) ? tableInfo.getPrimaryKey().getColumn() : related.fkColumn();

            // 获取查询数据中关联pk集合
            List relatedVals = list.stream().map(o -> KotBeanUtils.getFieldVal(fieldWrapper.getField(), o)).distinct().collect(toList());
            if (CollectionUtils.isEmpty(relatedVals)) {
                continue;
            }
            String columns = Stream.of(related.columns()).map(c -> (c.contains(".") ? c.split("\\.")[0] : c) + "," + pkColumn).collect(Collectors.joining(","));

            try {
                final KotTableInfo.TableInfo relatedTableInfo = KotTableInfo.get(relatedClazz.newInstance());
                final String relatedTableName = relatedTableInfo.getTableName();
                final List<Map<String, Object>> relatedMaps = baseMapper.kotRelatedFindAll(relatedTableName, columns, pkColumn, relatedVals);
                if (relatedMaps.size() <= 0) {
                    continue;
                }
                Map<Object, Map<String, Object>> relatedMap = new HashMap<>();
                relatedMaps.forEach(m -> {
                    final Object pkColumnVal = m.containsKey(pkColumn) ? m.get(pkColumn) : m.get(related.fkColumn());
                    relatedMap.put(pkColumnVal, m);
                });

                for (Object oriEntity : list) {
                    final Object oriRelatedVal = KotBeanUtils.getFieldVal(fieldWrapper.getField(), oriEntity);
                    if (oriRelatedVal == null) {
                        continue;
                    }
                    for (String column : related.columns()) {
                        final String[] columnSplit = column.split("\\.");
                        String relatedColumn = columnSplit.length == 1 ? column : columnSplit[0];
                        String mappingColumn = columnSplit.length == 1 ? column : columnSplit[1];
                        final KotTableInfo.FieldWrapper newFieldWrapper = fieldWrapperMap.get(mappingColumn);
                        final Map<String, Object> relatedValMap = relatedMap.get(oriRelatedVal);
                        if (relatedValMap == null) {
                            continue;
                        }
                        Object val = relatedValMap.get(relatedColumn);
                        KotBeanUtils.setField(newFieldWrapper.getField(), oriEntity, val);
                    }
                }

            } catch (InstantiationException | IllegalAccessException e) {
                throw new KotException(e);
            }
        }
    }

    /**
     * 关联子表查询帮助类
     */
    public static <T> void unionItemHelp(Object entity, List<T> list, BaseMapper baseMapper) {
        final KotTableInfo.TableInfo tableInfo = KotTableInfo.get(entity);
        final Map<String, KotTableInfo.FieldWrapper> fieldWrapperMap = tableInfo.getFieldWrapperMap();


        fieldWrapperMap.forEach((name, fieldWrapper) -> {
            // 获取注解
            final UnionItem unionItem = fieldWrapper.getField().getAnnotation(UnionItem.class);
            if (unionItem == null) {
                return;
            }
            if (isBlank(unionItem.fkColumn())) {
                throw new RuntimeException("@UnionItem.fkColumn不能为空");
            }
            try {
                // 子表信息
                final Object itemEntity = unionItem.clazz().newInstance();
                final KotTableInfo.TableInfo itemTableInfo = KotTableInfo.get(itemEntity);
                for (Object oriEntity : list) {
                    final Object fkVal = KotBeanUtils.getFieldVal(unionItem.fkColumn(), oriEntity);
                    if (fkVal == null) {
                        continue;
                    }
                    // 外键赋值
                    KotBeanUtils.setField(itemTableInfo.getPrimaryKey(), itemEntity, fkVal);
                    // 属性类型赋值
                    if (fieldWrapper.getField().getType().toString().contains("java.util.List")) {
                        KotBeanUtils.setField(fieldWrapper, oriEntity, toBeanList(baseMapper.kotUnionFindAll(itemTableInfo.getTableName(), itemTableInfo.getPrimaryKey().getColumn(), fkVal), itemEntity));
                    } else if (fieldWrapper.getField().getType().equals(itemEntity.getClass())) {
                        final List<Map<String, Object>> items = baseMapper.kotUnionFindAll(itemTableInfo.getTableName(), itemTableInfo.getPrimaryKey().getColumn(), fkVal);
                        Map<String, Object> map = items.size() == 0 ? null : items.get(0);
                        KotBeanUtils.setField(fieldWrapper, oriEntity, toBean(itemEntity, map));
                    } else {
                        throw new KotException("关联对象不匹配：" + "@UnionItem.clazz=" + fieldWrapper.getField().getType().toString() + "<==>" + itemEntity.getClass().toString());
                    }
                }

            } catch (InstantiationException | IllegalAccessException e) {
                throw new KotException(e);
            }
        });


    }

    private static Object toBean(Object bean, Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        try {
            BeanUtils.populate(bean, map);
        } catch (Exception e) {
            throw new KotException("Map转成Bean错误", e);
        }
        return bean;
    }

    private static List toBeanList(List<Map<String, Object>> maps, Object bean) {
        if (CollectionUtils.isEmpty(maps)) {
            return maps;
        }
        return maps.stream().map(m -> toBean(bean, m)).collect(toList());
    }
}
