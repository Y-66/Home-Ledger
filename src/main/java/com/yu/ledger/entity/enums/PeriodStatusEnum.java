package com.yu.ledger.entity.enums;

/**
 * 还款计划状态枚举
 */
public enum PeriodStatusEnum {
    PENDING("pending", "待付"),
    PAID("paid", "已付"),
    OVERDUE("overdue", "逾期");

    private final String code;
    private final String description;

    PeriodStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
} 