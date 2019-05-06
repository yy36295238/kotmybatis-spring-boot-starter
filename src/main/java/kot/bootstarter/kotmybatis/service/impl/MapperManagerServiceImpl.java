package kot.bootstarter.kotmybatis.service.impl;

import kot.bootstarter.kotmybatis.mapper.BaseMapper;
import kot.bootstarter.kotmybatis.service.MapperManagerService;
import kot.bootstarter.kotmybatis.service.MapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author YangYu
 */
@Service
public class MapperManagerServiceImpl<T> implements MapperManagerService<T> {

    @Autowired
    private BaseMapper<T> baseMapper;

    @Override
    public MapperService<T> newQuery() {
        return new MapperServiceImpl<>(baseMapper);
    }

    @Override
    public MapperService<T> newUpdate() {
        return new MapperServiceImpl<>(baseMapper);
    }
}
