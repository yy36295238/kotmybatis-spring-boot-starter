package kot.bootstarter.kotmybatis.enums;


public enum ConditionEnum {

    /**
     * 筛选条件
     */
    EQ(" = "),
    NEQ(" <> "),
    GT(" > "),
    LT(" < "),
    GTE(" >= "),
    LTE(" <= "),
    IN(" IN "),
    NIN(" NOT IN "),
    OR(" = "),
    LIKE(" LIKE "),
    NULL(" IS NULL "),
    NOT_NULL(" IS NOT NULL ");

    public String oper;

    ConditionEnum(String oper) {
        this.oper = oper;
    }
}
