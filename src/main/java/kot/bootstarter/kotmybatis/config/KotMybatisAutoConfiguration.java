package kot.bootstarter.kotmybatis.config;


import kot.bootstarter.kotmybatis.common.CT;
import kot.bootstarter.kotmybatis.plugin.KeyPropertiesPlugin;
import kot.bootstarter.kotmybatis.plugin.MapResultToCamelPlugin;
import kot.bootstarter.kotmybatis.properties.KotMybatisProperties;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Properties;


/**
 * @author YangYu
 */
@Configuration
@ConditionalOnClass(SqlSessionFactory.class)
@EnableConfigurationProperties(KotMybatisProperties.class)
public class KotMybatisAutoConfiguration {

    @Resource
    private KotMybatisProperties kotMybatisProperties;

    /**
     * 将插件加入到mybatis插件拦截链中
     */
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> {
            //插件拦截链采用了责任链模式，执行顺序和加入连接链的顺序有关
            Properties mrtcpProperties = new Properties();
            mrtcpProperties.setProperty(CT.UNDER_SORE_TO_CAMEL, String.valueOf(kotMybatisProperties.isUnderSoreToCamel()));
            configuration.addInterceptor(new MapResultToCamelPlugin(mrtcpProperties));
            configuration.addInterceptor(new KeyPropertiesPlugin());
        };
    }
}
