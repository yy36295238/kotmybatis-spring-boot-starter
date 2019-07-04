package kot.bootstarter.kotmybatis.common.id;

import kot.bootstarter.kotmybatis.annotation.ID;
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
        } else if (ID.IdType.CUSTOMIZE == idType && idGenerator != null) {
            return idGenerator;
        } else {
            return null;
        }
    }
}
