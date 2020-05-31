package kot.bootstarter.kotmybatis.activerecord;

import kot.bootstarter.kotmybatis.service.MapperManagerService;
import kot.bootstarter.kotmybatis.service.MapperService;
import kot.bootstarter.kotmybatis.utils.SpringUtils;

import static kot.bootstarter.kotmybatis.utils.KotStringUtils.toLowerCaseFirstOne;

/**
 * Os
 *
 * @Author yangyu
 * @create 2020/5/31 下午7:11
 */
public class ActiveRecord<T extends ActiveRecord> {


    private MapperManagerService<T> mapperManagerService = SpringUtils.getBean(serviceName(), MapperManagerService.class);

    private MapperService<T> mapperService() {
        return mapperManagerService.ops();
    }

    public int insert() {
        return mapperService().insert((T) this);
    }

    private String serviceName() {
        return toLowerCaseFirstOne(this.getClass().getSimpleName()) + "ServiceImpl";
    }


}
