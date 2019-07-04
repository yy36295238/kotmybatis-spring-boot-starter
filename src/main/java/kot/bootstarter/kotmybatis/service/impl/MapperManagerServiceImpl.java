package kot.bootstarter.kotmybatis.service.impl;

import kot.bootstarter.kotmybatis.common.id.IdGeneratorFactory;
import kot.bootstarter.kotmybatis.mapper.BaseMapper;
import kot.bootstarter.kotmybatis.properties.KotMybatisProperties;
import kot.bootstarter.kotmybatis.service.MapperManagerService;
import kot.bootstarter.kotmybatis.service.MapperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author YangYu
 */
@Service
public class MapperManagerServiceImpl<T> implements MapperManagerService<T> {

    @Autowired
    private BaseMapper<T> baseMapper;

    @Resource
    private KotMybatisProperties properties;

    @Autowired
    private IdGeneratorFactory idGeneratorFactory;

    @Override
    public MapperService<T> newQuery() {
        return new MapperServiceImpl<>(baseMapper, properties, idGeneratorFactory);
    }

    @Override
    public MapperService<T> newUpdate() {
        return new MapperServiceImpl<>(baseMapper, properties, idGeneratorFactory);
    }
}
