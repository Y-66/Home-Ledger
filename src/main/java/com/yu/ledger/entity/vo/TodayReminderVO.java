package com.yu.ledger.entity.vo;

import lombok.Data;
import java.time.LocalDate;
import java.math.BigDecimal;

/**
 * 今日提醒VO
 */
@Data
public class TodayReminderVO {
    private Integer scheduleId;
    private String customerName;
    private String contractId;
    private LocalDate dueDate;
    private BigDecimal calculatedInterest;
    private Integer remainingDate;
} 