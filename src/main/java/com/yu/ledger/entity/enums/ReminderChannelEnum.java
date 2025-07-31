package com.yu.ledger.entity.enums;

/**
 * 提醒渠道枚举
 */
public enum ReminderChannelEnum {
    SMS("sms", "短信"),
    EMAIL("email", "邮件"),
    APP("app", "应用内"),
    LETTER("letter", "信函");

    private final String code;
    private final String description;

    ReminderChannelEnum(String code, String description) {
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