package kot.bootstarter.kotmybatis.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "kot.mybatis")
public class KotMybatisProperties {
    /**
     * 逻辑删除：默认关闭
     */
    private boolean logicDelete = false;

    /**
     * Map类型下划线转驼峰
     */
    private boolean underSoreToCamel = false;
}
