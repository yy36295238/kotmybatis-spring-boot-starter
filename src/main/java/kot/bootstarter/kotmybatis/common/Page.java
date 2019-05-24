package kot.bootstarter.kotmybatis.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Page<T> {


    private int pageIndex = 1;
    private int pageSize = 30;
    private int total;
    private List<T> data;

    public Page(int pageIndex, int pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }
}
