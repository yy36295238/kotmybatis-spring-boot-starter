package kot.bootstarter.kotmybatis.common;

import kot.bootstarter.kotmybatis.annotation.Related;
import kot.bootstarter.kotmybatis.config.KotTableInfo;
import kot.bootstarter.kotmybatis.exception.KotException;
import kot.bootstarter.kotmybatis.mapper.BaseMapper;
import kot.bootstarter.kotmybatis.utils.KotBeanUtils;
import kot.bootstarter.kotmybatis.utils.KotStringUtils;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

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
            final String pkColumn = KotStringUtils.isBlank(related.fkColumn()) ? tableInfo.getPrimaryKey().getColumn() : related.fkColumn();

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
}
