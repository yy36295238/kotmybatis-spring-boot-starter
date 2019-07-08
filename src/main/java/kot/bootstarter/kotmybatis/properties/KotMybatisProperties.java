package kot.bootstarter.kotmybatis.properties;


import kot.bootstarter.kotmybatis.annotation.ID;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "kot.mybatis")
public class KotMybatisProperties {
    /**
     * 逻辑删除：默认关闭
     */
    private boolean logicDelete;

    /**
     * Map类型下划线转驼峰
     */
    private boolean mapResultUnderSoreToCamel;

    /**
     * 实体结果集下划线转驼峰
     */
    private boolean entityResultUnderSoreToCamel;

    /**
     * 主键策略
     */
    private ID.IdType idType = ID.IdType.NONE;

    /**
     * 雪花算法 - 终端id
     */
    private long workId = 1;
    /**
     * 雪花算法 - 数据中心id
     */
    private long dataCenterId = 1;
}
