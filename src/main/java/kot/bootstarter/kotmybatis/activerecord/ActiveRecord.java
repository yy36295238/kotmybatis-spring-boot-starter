package kot.bootstarter.kotmybatis.activerecord;

import com.github.pagehelper.PageInfo;
import kot.bootstarter.kotmybatis.annotation.TableName;
import kot.bootstarter.kotmybatis.common.Page;
import kot.bootstarter.kotmybatis.common.model.ColumnExistInfo;
import kot.bootstarter.kotmybatis.lambda.Property;
import kot.bootstarter.kotmybatis.service.MapperManagerService;
import kot.bootstarter.kotmybatis.service.MapperService;
import kot.bootstarter.kotmybatis.utils.SpringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static kot.bootstarter.kotmybatis.utils.KotStringUtils.toLowerCaseFirstOne;

/**
 * @author yangyu
 * @date 2020/5/31 下午7:11
 */
public class ActiveRecord<T> {


    private MapperManagerService<T> mapperManagerService = mapperManagerService();

    private MapperService<T> mapperService() {
        return mapperManagerService.ops();
    }

    private MapperManagerService<T> mapperManagerService() {
        TableName tableName = this.getClass().getAnnotation(TableName.class);
        if (tableName.service() != Void.class) {
            return (MapperManagerService) SpringUtils.getBean(tableName.service());
        }
        return SpringUtils.getBean(toLowerCaseFirstOne(this.getClass().getSimpleName()) + "ServiceImpl", MapperManagerService.class);
    }


    public int insert() {
        return mapperService().insert((T) this);
    }

    public ColumnExistInfo insertWithCheckColumns(String... columns) {
        return mapperService().insertWithCheckColumns((T) this, columns);
    }

    public int save() {
        return mapperService().save((T) this);
    }

    public T findOne() {
        return mapperService().findOne((T) this);
    }

    public List<T> list() {
        return mapperService().list((T) this);
    }

    public int count() {
        return mapperService().count((T) this);
    }

    public PageInfo<T> selectPage(Page page) {
        return mapperService().selectPage(page, (T) this);
    }

    public int delete() {
        return mapperService().delete((T) this);
    }

    public int logicDelete() {
        return mapperService().logicDelete((T) this);
    }

    public int updateById() {
        return mapperService().updateById((T) this);
    }

    public ColumnExistInfo updateByIdWithCheckColumns(String... columns) {
        return mapperService().updateByIdWithCheckColumns((T) this, columns);
    }

    public int updateById(boolean setNull) {
        return mapperService().updateById((T) this, setNull);
    }

    public int update(T whereEntity) {
        return mapperService().update((T) this, whereEntity);
    }

    public int update(T whereEntity, boolean setNull) {
        return mapperService().update((T) this, whereEntity, setNull);
    }

    public Map<String, Object> columnExist() {
        return mapperService().columnExist((T) this);
    }

    public boolean exist() {
        return mapperService().exist((T) this);
    }

    public ActiveRecord<T> fields(String... field) {
        mapperService().fields(field);
        return this;
    }

    public ActiveRecord<T> fields(Property... property) {
        mapperService().fields(property);
        return this;
    }

    public ActiveRecord<T> orderBy(String orderBy) {
        mapperService().orderBy(orderBy);
        return this;
    }

    public ActiveRecord<T> orderByIdAsc() {
        mapperService().orderByIdAsc();
        return this;
    }

    public ActiveRecord<T> orderByIdDesc() {
        mapperService().orderByIdDesc();
        return this;
    }

    public ActiveRecord<T> eq(String key, Object value) {
        mapperService().eq(key, value);
        return this;
    }

    public ActiveRecord<T> eq(Property property, Object value) {
        mapperService().eq(property, value);
        return this;
    }

    public ActiveRecord<T> neq(String key, Object value) {
        mapperService().neq(key, value);
        return this;
    }

    public ActiveRecord<T> neq(Property property, Object value) {
        mapperService().neq(property, value);
        return this;
    }

    public ActiveRecord<T> in(String key, String values) {
        mapperService().in(key, values);
        return this;
    }

    public ActiveRecord<T> in(Property property, String value) {
        mapperService().in(property, value);
        return this;
    }

    public ActiveRecord<T> in(String key, Object[] values) {
        mapperService().in(key, values);
        return this;
    }

    public ActiveRecord<T> in(Property property, Object[] values) {
        mapperService().in(property, values);
        return this;
    }

    public ActiveRecord<T> nin(String key, Object[] values) {
        mapperService().nin(key, values);
        return this;
    }

    public ActiveRecord<T> nin(Property property, Object[] values) {
        mapperService().nin(property, values);
        return this;
    }

    public ActiveRecord<T> lt(String key, Object value) {
        mapperService().lt(key, value);
        return this;
    }

    public ActiveRecord<T> lt(Property property, Object value) {
        mapperService().lt(property, value);
        return this;
    }

    public ActiveRecord<T> gt(String key, Object value) {
        mapperService().gt(key, value);
        return this;
    }

    public ActiveRecord<T> gt(Property property, Object value) {
        mapperService().gt(property, value);
        return this;
    }

    public ActiveRecord<T> lte(String key, Object value) {
        mapperService().lte(key, value);
        return this;
    }

    public ActiveRecord<T> lte(Property property, Object value) {
        mapperService().lte(property, value);
        return this;
    }

    public ActiveRecord<T> gte(String key, Object value) {
        mapperService().gte(key, value);
        return this;
    }

    public ActiveRecord<T> gte(Property property, Object value) {
        mapperService().gte(property, value);
        return this;
    }

    public ActiveRecord<T> or(String key, Object value) {
        mapperService().or(key, value);
        return this;
    }

    public ActiveRecord<T> or(Property property, Object value) {
        mapperService().or(property, value);
        return this;
    }

    public ActiveRecord<T> like(String key, Object value) {
        mapperService().like(key, value);
        return this;
    }

    public ActiveRecord<T> like(Property property, Object value) {
        mapperService().like(property, value);
        return this;
    }

    public ActiveRecord<T> between(String key, Object left, Object right) {
        mapperService().between(key, left, right);
        return this;
    }

    public ActiveRecord<T> between(Property property, Object left, Object right) {
        mapperService().between(property, left, right);
        return this;
    }

    public ActiveRecord<T> isNull(String key) {
        mapperService().isNull(key);
        return this;
    }

    public ActiveRecord<T> isNull(Property property) {
        mapperService().isNull(property);
        return this;
    }

    public ActiveRecord<T> notNull(String key) {
        mapperService().notNull(key);
        return this;
    }

    public ActiveRecord<T> notNull(Property property) {
        mapperService().notNull(property);
        return this;
    }

    public ActiveRecord<T> activeLike() {
        mapperService().activeLike();
        return this;
    }

    public ActiveRecord<T> activeRelated() {
        mapperService().activeRelated();
        return this;
    }

    public ActiveRecord<T> activeUnion() {
        mapperService().activeUnion();
        return this;
    }

    public ActiveRecord<T> nin(Property property, Collection<?> values) {
        mapperService().nin(property, values);
        return this;
    }

    public ActiveRecord<T> nin(String key, Collection<?> values) {
        mapperService().nin(key, values);
        return this;
    }

    public ActiveRecord<T> in(Property property, Collection<?> values) {
        mapperService().in(property, values);
        return this;
    }

    public ActiveRecord<T> in(String key, Collection<?> values) {
        mapperService().in(key, values);
        return this;
    }

    public ActiveRecord<T> fieldsByLambda(List<Property> fields) {
        mapperService().fieldsByLambda(fields);
        return this;
    }

    public ActiveRecord<T> fields(List<String> fields) {
        mapperService().fields(fields);
        return this;
    }
}
