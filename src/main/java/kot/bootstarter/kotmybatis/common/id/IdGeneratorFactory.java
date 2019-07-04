package kot.bootstarter.kotmybatis.common.id;

import kot.bootstarter.kotmybatis.annotation.ID;

public class IdGeneratorFactory {

    public static IdGenerator get(ID.IdType idType) {
        if (ID.IdType.UUID == idType) {
            return new IdGeneratorByUUIDImpl();
        } else if (ID.IdType.SNOW_FLAKE == idType) {
            return new IdGeneratorBySnowflakeImpl();
        } else {
            return null;
        }
    }
}
