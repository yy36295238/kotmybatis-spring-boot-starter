package kot.bootstarter.kotmybatis.service;

public interface MapperManagerService<T> {
    MapperService<T> newQuery();

    MapperService<T> newUpdate();

    MapperService<T> ops();
}
