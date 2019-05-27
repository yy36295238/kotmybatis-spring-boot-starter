package kot.bootstarter.kotmybatis.config;


import kot.bootstarter.kotmybatis.properties.KotMybatisProperties;
import kot.bootstarter.kotmybatis.service.MapperManagerService;
import kot.bootstarter.kotmybatis.service.impl.MapperManagerServiceImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * @author YangYu
 */
@Configuration
@EnableConfigurationProperties(KotMybatisProperties.class)
@ConditionalOnClass(MapperManagerService.class)
public class KotMybatisAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MapperManagerService.class)
    public MapperManagerService kotMongoTemplate() {
        return new MapperManagerServiceImpl();
    }
}
