package kot.bootstarter.kotmybatis.service;


import kot.bootstarter.kotmybatis.common.Page;

import java.util.List;

public interface MapperService<T> extends BaseMapperManager<T> {

//    BaseMapperManager gt(String col, Object val);

    /**
     * 保存操作
     */
    int insert(T entity);

    int save(T entity) throws IllegalAccessException;

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

    /**
     * 更新操作
     */
    int updateById(T entity);

    int update(T setEntity, T whereEntity);

}
