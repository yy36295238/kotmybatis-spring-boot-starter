package kot.bootstarter.kotmybatis.plugin;

import kot.bootstarter.kotmybatis.common.CT;
import kot.bootstarter.kotmybatis.utils.MapUtils;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;

import java.util.Properties;


/**
 * @author YangYu
 */
@Intercepts({@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = java.sql.Statement.class)})
public class MapResultToCamelPlugin implements Interceptor {

    private boolean underSoreToCamel;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        if (underSoreToCamel && !isSkip(invocation)) {
            return MapUtils.toCamel(invocation.proceed());
        }
        return invocation.proceed();

    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        underSoreToCamel = Boolean.parseBoolean(properties.getProperty(CT.UNDER_SORE_TO_CAMEL));

    }

    private boolean isSkip(Invocation invocation) {
        final MetaObject metaObject = SystemMetaObject.forObject(invocation.getTarget());
        final MappedStatement ms = (MappedStatement) metaObject.getValue("mappedStatement");
        return "com.kot.kotmybatis.biz.mapper.OrderMapper.relatedFindAll".equals(ms.getId());

    }


}
