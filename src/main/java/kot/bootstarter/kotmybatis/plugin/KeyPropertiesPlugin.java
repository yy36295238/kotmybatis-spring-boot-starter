package kot.bootstarter.kotmybatis.plugin;

import kot.bootstarter.kotmybatis.annotation.TableName;
import kot.bootstarter.kotmybatis.config.KotTableInfo;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.sql.Statement;
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
            if (param == null) {
                return wrap;
            }
            final TableName tableNameAnno = param.getClass().getAnnotation(TableName.class);
            if (tableNameAnno == null) {
                return wrap;
            }
            final KotTableInfo.FieldWrapper fieldWrapper = KotTableInfo.get(param).getPrimaryKey();
            metaObject.setValue("delegate.mappedStatement.keyGenerator", new Jdbc3KeyGenerator());
            final String[] keyProperties = new String[1];
            keyProperties[0] = fieldWrapper.getFieldName();
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
