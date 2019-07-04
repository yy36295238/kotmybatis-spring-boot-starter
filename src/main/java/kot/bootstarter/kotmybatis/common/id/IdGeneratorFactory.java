package kot.bootstarter.kotmybatis.common.id;

import kot.bootstarter.kotmybatis.annotation.ID;
import kot.bootstarter.kotmybatis.exception.KotException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IdGeneratorFactory {

    @Autowired(required = false)
    private IdGenerator idGenerator;


    public IdGenerator get(ID.IdType idType) {
        if (ID.IdType.UUID == idType) {
            return new IdGeneratorByUUIDImpl();
        } else if (ID.IdType.SNOW_FLAKE == idType) {
            return new IdGeneratorBySnowflakeImpl();
        } else if (ID.IdType.CUSTOMIZE == idType) {
            if (idGenerator == null) {
                throw new KotException("自定义主键策略，必须声明 [IdGenerator]");
            }
            return idGenerator;
        } else {
            return null;
        }
    }
}
