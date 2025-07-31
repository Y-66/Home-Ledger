package com.yu.ledger.entity.enums;

/**
 * 提醒类型枚举
 */
public enum ReminderTypeEnum {
    INITIAL("initial", "初始提醒"),
    FOLLOW_UP("follow_up", "跟进提醒"),
    OVERDUE("overdue", "逾期提醒");

    private final String code;
    private final String description;

    ReminderTypeEnum(String code, String description) {
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