package kot.bootstarter.kotmybatis.mapper;

import kot.bootstarter.kotmybatis.common.CT;
import kot.bootstarter.kotmybatis.properties.KotMybatisProperties;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author YangYu
 */
public interface BaseMapper<T> {

    /**
     * 批量插入操作
     */
    @InsertProvider(type = BaseProvider.class)
    int batchInsert(@Param(CT.KOT_LIST) List<T> list, @Param(CT.PROPERTIES) KotMybatisProperties properties);

    /**
     * 查询操作
     */

    @Select("SELECT ${columns} FROM ${relatedTableName} WHERE ${pkColumn} = #{pkVal}")
    Map<String, Object> relatedFindOne(@Param("relatedTableName") String assTableName, @Param("columns") String columns, @Param("pkColumn") String pkColumn, @Param("pkVal") Object pkVal);

    @Select("<script>"
            + "SELECT ${columns} FROM ${relatedTableName} WHERE ${pkColumn} IN "
            + "<foreach item='item' index='index' collection='pkVals' open='(' separator=',' close=')'>"
            + "#{item}"
            + "</foreach>"
            + "</script>")
    List<Map<String, Object>> relatedFindAll(@Param("relatedTableName") String assTableName, @Param("columns") String columns, @Param("pkColumn") String pkColumn, @Param("pkVals") List pkVals);

    @SelectProvider(type = BaseProvider.class)
    List<T> list(@Param(CT.COLUMNS) Set<String> columns, @Param(CT.SQL_CONDITION) String conditionList, @Param(CT.ALIAS_CONDITION) Map<String, Object> conditionMap, @Param(CT.ALIAS_ENTITY) T entity);

    @SelectProvider(type = BaseProvider.class)
    int count(@Param(CT.SQL_CONDITION) String conditionList, @Param(CT.ALIAS_CONDITION) Map<String, Object> conditionMap, @Param(CT.ALIAS_ENTITY) T entity);


    @DeleteProvider(type = BaseProvider.class)
    int delete(@Param(CT.SQL_CONDITION) String conditionList, @Param(CT.ALIAS_CONDITION) Map<String, Object> conditionMap, @Param(CT.ALIAS_ENTITY) T entity);

    /**
     * 更新操作
     */

    @UpdateProvider(type = BaseProvider.class)
    int update(@Param(CT.COLUMNS) Set<String> columns, @Param(CT.SQL_CONDITION) String conditionList, @Param(CT.ALIAS_CONDITION) Map<String, Object> conditionMap, @Param(CT.ALIAS_ENTITY) T entity, @Param(CT.SET_ENTITY) T setEntity, @Param("setNull") boolean setNull);


}
