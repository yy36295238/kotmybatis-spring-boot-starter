package kot.bootstarter.kotmybatis.service;

import kot.bootstarter.kotmybatis.lambda.Property;

import java.util.Collection;
import java.util.List;

/**
 * @author yangyu
 */
public interface BaseMapperManager<T> {

    /**
     * 返回指定字段
     */
    MapperService<T> fields(String... field);

    MapperService<T> fields(Property... property);

    MapperService<T> fields(List<String> fields);

    MapperService<T> fieldsByLambda(List<Property> fields);

    MapperService<T> orderBy(String orderBy);

    MapperService<T> orderByIdAsc();

    MapperService<T> orderByIdDesc();

    MapperService<T> eq(String key, Object value);

    MapperService<T> eq(Property property, Object value);

    MapperService<T> neq(String key, Object value);

    MapperService<T> neq(Property property, Object value);

    MapperService<T> in(String key, String values);

    MapperService<T> in(Property property, String value);

    MapperService<T> in(String key, Object[] values);

    MapperService<T> in(Property property, Object[] values);

    MapperService<T> in(String key, Collection<?> values);

    MapperService<T> in(Property property, Collection<?> values);

    MapperService<T> nin(String key, Object[] values);

    MapperService<T> nin(Property property, Object[] values);

    MapperService<T> nin(String key, Collection<?> values);

    MapperService<T> nin(Property property, Collection<?> values);

    MapperService<T> lt(String key, Object value);

    MapperService<T> lt(Property property, Object value);

    MapperService<T> gt(String key, Object value);

    MapperService<T> gt(Property property, Object value);

    MapperService<T> lte(String key, Object value);

    MapperService<T> lte(Property property, Object value);

    MapperService<T> gte(String key, Object value);

    MapperService<T> gte(Property property, Object value);

    MapperService<T> or(String key, Object value);

    MapperService<T> or(Property property, Object value);

    MapperService<T> like(String key, Object value);

    MapperService<T> like(Property property, Object value);

    MapperService<T> between(String key, Object left, Object right);

    MapperService<T> between(Property property, Object left, Object right);

    MapperService<T> isNull(String key);

    MapperService<T> isNull(Property property);

    MapperService<T> notNull(String key);

    MapperService<T> notNull(Property property);

    MapperService<T> activeLike();

    MapperService<T> activeRelated();

    MapperService<T> activeUnion();
}
