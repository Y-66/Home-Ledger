package com.yu.ledger.entity.enums;

/**
 * 提醒状态枚举
 */
public enum ReminderStatusEnum {
    SUCCESS("success", "成功"),
    FAILED("failed", "失败"),
    PENDING("pending", "待发送");

    private final String code;
    private final String description;

    ReminderStatusEnum(String code, String description) {
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