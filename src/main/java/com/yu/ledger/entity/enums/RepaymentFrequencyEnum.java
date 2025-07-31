package com.yu.ledger.entity.enums;

/**
 * 还款频率枚举
 */
public enum RepaymentFrequencyEnum {
    MONTHLY("monthly", "月付"),
    QUARTERLY("quarterly", "季付"),
    ANNUALLY("annually", "年付");

    private final String code;
    private final String description;

    RepaymentFrequencyEnum(String code, String description) {
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