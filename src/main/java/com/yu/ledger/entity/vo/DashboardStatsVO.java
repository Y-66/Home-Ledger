package com.yu.ledger.entity.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 仪表板统计VO
 */
@Data
public class DashboardStatsVO {
    private BigDecimal totalLoanAmount;
    private BigDecimal monthlyInterest;
    private Integer overdueContracts;
    private BigDecimal overdueAmount;
} 