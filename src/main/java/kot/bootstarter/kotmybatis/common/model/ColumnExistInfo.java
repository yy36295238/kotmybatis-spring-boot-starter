package kot.bootstarter.kotmybatis.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @Author yangyu
 * @create 2019/11/21 14:38
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColumnExistInfo {
    private boolean exist;
    private Set<String> existSet;
    private Integer count;

    public ColumnExistInfo(Integer count) {
        this.count = count;
    }

    public ColumnExistInfo(boolean exist, Set<String> existSet) {
        this.exist = exist;
        this.existSet = existSet;
    }
}
