package com.yu.ledger.entity.enums;

/**
 * 合同状态枚举
 */
public enum ContractStatusEnum {
    ACTIVE("active", "活跃"),
    CLOSED("closed", "关闭"),
    DEFAULT("default", "违约");

    private final String code;
    private final String description;

    ContractStatusEnum(String code, String description) {
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