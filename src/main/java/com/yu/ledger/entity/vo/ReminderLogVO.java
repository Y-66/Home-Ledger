package com.yu.ledger.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 提醒日志VO
 */
@Data
public class ReminderLogVO {
    private Integer reminderId;
    private String customerName;
    private String contractId;
    private String contractName;
    private String reminderType;
    private String reminderChannel;
    private String recipient;
    private LocalDateTime sentAt;
    private String status;
    private String failureReason;
} 