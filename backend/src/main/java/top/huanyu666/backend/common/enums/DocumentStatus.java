package top.huanyu666.backend.common.enums;

/**
 * 单据状态枚举 —— 统一管理所有业务单据的状态码。
 * <p>
 * 用法：{@code order.setStatus(DocumentStatus.APPROVED.getCode())}
 * <br>
 * 比对：{@code DocumentStatus.DRAFT.eq(order.getStatus())}
 * </p>
 */
public enum DocumentStatus {

    // ==== 单据生命周期 ====

    DRAFT("DRAFT", "草稿"),
    SUBMITTED("SUBMITTED", "已提交"),
    APPROVED("APPROVED", "已审核"),
    RELEASED("RELEASED", "已下达"),
    CONFIRMED("CONFIRMED", "已确认"),
    SHIPPED("SHIPPED", "已发货"),
    COMPLETED("COMPLETED", "已完成"),
    CLOSED("CLOSED", "已关闭"),

    // ==== 采购申请专用 ====
    ORDERED("ORDERED", "已下单"),

    // ==== 应收应付核销 ====
    PENDING("PENDING", "待收款"),
    UNPAID("UNPAID", "未付"),
    PARTIALLY_PAID("PARTIALLY_PAID", "部分已付"),
    FULLY_PAID("FULLY_PAID", "已付清"),

    // ==== 旧版核销状态（兼容） ====
    PARTIAL_PAID("PARTIAL_PAID", "部分已付"),
    PAID("PAID", "已付清");

    private final String code;
    private final String label;

    DocumentStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() { return code; }
    public String getLabel() { return label; }

    /** 与 Entity 的 String 字段比对 */
    public boolean eq(String other) {
        return this.code.equals(other);
    }
}
