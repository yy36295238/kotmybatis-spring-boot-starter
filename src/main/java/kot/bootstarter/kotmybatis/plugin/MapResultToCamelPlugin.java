package kot.bootstarter.kotmybatis.plugin;

import kot.bootstarter.kotmybatis.common.CT;
import kot.bootstarter.kotmybatis.utils.MapUtils;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;

import java.util.Properties;


/**
 * @author YangYu
 */
@Intercepts({@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = java.sql.Statement.class)})
public class MapResultToCamelPlugin implements Interceptor {

    private boolean underSoreToCamel;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (!underSoreToCamel) {
            return invocation.proceed();
        }
        return MapUtils.toCamel(invocation.proceed());
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        underSoreToCamel = Boolean.parseBoolean(properties.getProperty(CT.UNDER_SORE_TO_CAMEL));

    }


}
