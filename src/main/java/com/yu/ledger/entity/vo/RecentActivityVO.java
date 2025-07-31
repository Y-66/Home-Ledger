package com.yu.ledger.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * 最近活动VO
 */
@Data
public class RecentActivityVO {
    private String activityType;
    private String description;
    private LocalDateTime activityTime;
    private BigDecimal amount;
    private String customerName;
} 