package kot.bootstarter.kotmybatis.mapper;

import kot.bootstarter.kotmybatis.common.CT;
import kot.bootstarter.kotmybatis.common.Page;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author YangYu
 */
public interface BaseMapper<T> {

    /**
     * 插入操作
     */
    @InsertProvider(type = BaseProvider.class)
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    int insert(T entity);

    /**
     * 批量插入操作
     */
    @InsertProvider(type = BaseProvider.class)
    int batchInsert(@Param("list") List<T> list);

    /**
     * 查询操作
     */
    @SelectProvider(type = BaseProvider.class)
    T findOne(@Param(CT.COLUMNS) Set<String> columns, @Param(CT.SQL_CONDITION) String conditionList, @Param(CT.ALIAS_CONDITION) Map<String, Object> conditionMap, @Param(CT.ALIAS_ENTITY) T entity);

    @SelectProvider(type = BaseProvider.class)
    List<T> list(@Param(CT.COLUMNS) Set<String> columns, @Param(CT.SQL_CONDITION) String conditionList, @Param(CT.ALIAS_CONDITION) Map<String, Object> conditionMap, @Param(CT.ALIAS_ENTITY) T entity);

    @SelectProvider(type = BaseProvider.class)
    int count(@Param(CT.SQL_CONDITION) String conditionList, @Param(CT.ALIAS_CONDITION) Map<String, Object> conditionMap, @Param(CT.ALIAS_ENTITY) T entity);

    @SelectProvider(type = BaseProvider.class)
    List<T> selectPage(@Param(CT.COLUMNS) Set<String> columns, @Param(CT.SQL_CONDITION) String conditionList, @Param("page") Page page, @Param(CT.ALIAS_CONDITION) Map<String, Object> conditionMap, @Param(CT.ALIAS_ENTITY) T entity);

    @DeleteProvider(type = BaseProvider.class)
    int delete(@Param(CT.COLUMNS) Set<String> columns, @Param(CT.SQL_CONDITION) String conditionList, @Param(CT.ALIAS_CONDITION) Map<String, Object> conditionMap, @Param(CT.ALIAS_ENTITY) T entity);

    /**
     * 更新操作
     */
    @UpdateProvider(type = BaseProvider.class)
    int updateById(@Param(CT.ALIAS_ENTITY) T entity, @Param("setNull") boolean setNull);

    @UpdateProvider(type = BaseProvider.class)
    int update(@Param(CT.COLUMNS) Set<String> columns, @Param(CT.SQL_CONDITION) String conditionList, @Param(CT.ALIAS_CONDITION) Map<String, Object> conditionMap, @Param(CT.ALIAS_ENTITY) T entity, @Param(CT.SET_ENTITY) T setEntity, @Param("setNull") boolean setNull);


}
