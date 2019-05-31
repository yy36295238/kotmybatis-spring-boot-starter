package kot.bootstarter.kotmybatis.service;


import kot.bootstarter.kotmybatis.common.Page;

import java.util.List;

public interface MapperService<T> extends BaseMapperManager<T> {


    /**
     * 保存操作
     */
    int insert(T entity);

    /**
     * 批量插入
     */
    int batchInsert(List<T> list);

    int save(T entity);

    /**
     * 查询操作
     */
    T findOne(T entity);

    List<T> list(T entity);

    Integer count(T entity);

    Page<T> selectPage(Page<T> page, T entity);

    /**
     * 删除操作
     */
    int delete(T entity);

    int logicDelete(T entity);

    /**
     * 更新操作
     */
    int updateById(T entity);

    int updateById(T entity, boolean setNull);

    int update(T setEntity, T whereEntity);

    int update(T setEntity, T whereEntity, boolean setNull);

}
