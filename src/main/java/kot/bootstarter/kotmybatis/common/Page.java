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
    private String orderBy;
    private String sort;

    public Page(int pageIndex, int pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    public Page(int pageIndex, int pageSize, String orderBy, String sort) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.orderBy = orderBy;
        this.sort = sort;
    }
}
