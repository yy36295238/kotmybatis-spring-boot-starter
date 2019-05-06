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
    NULL(" IS NULL ");

    public String oper;

    ConditionEnum(String oper) {
        this.oper = oper;
    }

    public static boolean contains(ConditionEnum conditionEnum) {
        for (ConditionEnum anEnum : ConditionEnum.values()) {
            if (anEnum == conditionEnum) {
                return true;
            }
        }
        return false;
    }
}
