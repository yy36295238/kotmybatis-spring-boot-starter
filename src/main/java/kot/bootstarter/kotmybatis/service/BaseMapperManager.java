package kot.bootstarter.kotmybatis.service;

import java.util.Collection;
import java.util.List;

/**
 * @author yangyu
 */
public interface BaseMapperManager<T> {

    /**
     * 返回指定字段
     */
    MapperService<T> fields(String field);

    MapperService<T> fields(List<String> fields);

    MapperService<T> orderBy(String orderBy);

    MapperService<T> eq(String key, Object value);

    MapperService<T> neq(String key, Object value);

    MapperService<T> in(String key, String values);

    MapperService<T> in(String key, Object[] values);

    MapperService<T> in(String key, Collection<?> values);

    MapperService<T> nin(String key, Object[] values);

    MapperService<T> nin(String key, Collection<?> values);

    MapperService<T> lt(String key, Object value);

    MapperService<T> gt(String key, Object value);

    MapperService<T> lte(String key, Object value);

    MapperService<T> gte(String key, Object value);

    MapperService<T> or(String key, Object value);

    MapperService<T> like(String key, Object value);

    MapperService<T> between(String key, Object left, Object right);

    MapperService<T> isNull(String key);
}
