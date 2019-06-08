package kot.bootstarter.kotmybatis.service;


import kot.bootstarter.kotmybatis.common.Page;

import java.util.List;
import java.util.Map;

public interface MapperService<T> extends BaseMapperManager<T> {


    /**
     * 插入
     */
    int insert(T entity);

    /**
     * 批量插入
     */
    int batchInsert(List<T> list);

    /**
     * 保存，主键存在则更新
     */
    int save(T entity);

    /**
     * 查询操作
     */
    T findOne(T entity);

    List<T> list(T entity);

    int count(T entity);

    /**
     * 分页查询
     */
    Page<T> selectPage(Page<T> page, T entity);

    /**
     * 删除操作
     */
    int delete(T entity);

    /**
     * 逻辑删除
     */
    int logicDelete(T entity);

    /**
     * 更新操作
     */
    int updateById(T entity);

    /**
     * 更新所有字段，无值则设置为null
     */
    int updateById(T entity, boolean setNull);

    int update(T setEntity, T whereEntity);

    /**
     * 更新所有字段，无值则设置为null
     */
    int update(T setEntity, T whereEntity, boolean setNull);

    /**
     * 判断实体中，注解@Column(unique=tue)该字段数据库中是否已存在
     */
    Map<String, Object> columnExist(T entity);

}
