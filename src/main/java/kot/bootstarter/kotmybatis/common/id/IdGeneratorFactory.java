package kot.bootstarter.kotmybatis.common.id;

import kot.bootstarter.kotmybatis.annotation.ID;
import kot.bootstarter.kotmybatis.exception.KotException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class IdGeneratorFactory {

    /**
     * 用户自定义主键策略
     */
    @Autowired(required = false)
    @Qualifier("idGenerator")
    private IdGenerator customizeIdGenerator;

    @Autowired
    @Qualifier("idGeneratorBySnowflake")
    private IdGenerator idGeneratorBySnowflake;

    @Autowired
    @Qualifier("idGeneratorByUUID")
    private IdGenerator idGeneratorByUUID;

    public IdGenerator get(ID.IdType idType) {
        if (ID.IdType.UUID == idType) {
            return idGeneratorByUUID;
        } else if (ID.IdType.SNOW_FLAKE == idType) {
            return idGeneratorBySnowflake;
        } else if (ID.IdType.CUSTOMIZE == idType) {
            if (customizeIdGenerator == null) {
                throw new KotException("自定义主键策略，必须声明 [IdGenerator]");
            }
            return customizeIdGenerator;
        } else {
            return null;
        }
    }
}
