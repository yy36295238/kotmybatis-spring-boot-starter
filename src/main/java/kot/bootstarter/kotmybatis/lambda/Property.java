package kot.bootstarter.kotmybatis.lambda;

import java.io.Serializable;

/**
 * @author YangYu
 */
@FunctionalInterface
public interface Property<T> extends Serializable {
    T apply();
}