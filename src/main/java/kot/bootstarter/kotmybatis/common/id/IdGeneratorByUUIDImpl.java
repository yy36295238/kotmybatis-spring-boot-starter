package kot.bootstarter.kotmybatis.common.id;

import java.util.UUID;

public class IdGeneratorByUUIDImpl implements IdGenerator {
    @Override
    public Object gen() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
