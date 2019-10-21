package kot.bootstarter.kotmybatis.plugin;

import kot.bootstarter.kotmybatis.annotation.ID;
import kot.bootstarter.kotmybatis.annotation.TableName;
import kot.bootstarter.kotmybatis.common.CT;
import kot.bootstarter.kotmybatis.config.KotTableInfo;
import kot.bootstarter.kotmybatis.properties.KotMybatisProperties;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * @author YangYu
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "update", args = {Statement.class})})
public class KeyPropertiesPlugin implements Interceptor {


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        final Object wrap = Plugin.wrap(target, this);
        if (target instanceof StatementHandler) {
            final MetaObject metaObject = SystemMetaObject.forObject(target);
            final Object param = metaObject.getValue("delegate.parameterHandler.parameterObject");
            final Object sqlCommandType = metaObject.getValue("delegate.mappedStatement.sqlCommandType");
            if (sqlCommandType == null || !SqlCommandType.INSERT.name().equals(sqlCommandType.toString())) {
                return wrap;
            }
            if (!(param instanceof Map)) {
                return wrap;
            }
            final Map map = (Map) param;
            if ((!map.containsKey(CT.ALIAS_ENTITY) && !map.containsKey(CT.KOT_LIST)) || !map.containsKey(CT.PROPERTIES)) {
                return wrap;
            }
            boolean isBatchInsert = map.containsKey(CT.KOT_LIST);
            Object entity = isBatchInsert ? ((List) map.get(CT.KOT_LIST)).get(0) : map.get(CT.ALIAS_ENTITY);
            final TableName tableNameAnno = entity.getClass().getAnnotation(TableName.class);
            if (tableNameAnno == null) {
                return wrap;
            }

            // 数据库自动生成主键
            final KotTableInfo.FieldWrapper fieldWrapper = KotTableInfo.get(entity).getPrimaryKey();
            KotMybatisProperties kotMybatisProperties = (KotMybatisProperties) map.get(CT.PROPERTIES);

            ID.IdType idType = fieldWrapper.getIdType() == ID.IdType.NONE ? kotMybatisProperties.getIdType() : fieldWrapper.getIdType();
            if (idType != ID.IdType.NONE && idType != ID.IdType.AUTO) {
                return wrap;
            }

            metaObject.setValue("delegate.mappedStatement.keyGenerator", new Jdbc3KeyGenerator());
            final String[] keyProperties = new String[1];
            keyProperties[0] = (isBatchInsert ? CT.KOT_LIST : CT.ALIAS_ENTITY) + CT.DOT + fieldWrapper.getFieldName();
            final String[] keyColumns = new String[1];
            keyColumns[0] = fieldWrapper.getColumn();
            metaObject.setValue("delegate.mappedStatement.keyProperties", keyProperties);
            metaObject.setValue("delegate.mappedStatement.keyColumns", keyColumns);
        }
        return wrap;
    }

    @Override
    public void setProperties(Properties properties) {

    }


}
