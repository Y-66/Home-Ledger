package com.yu.ledger.entity.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同详情VO
 */
@Data
public class ContractDetailVO {
    private Integer contractId;
    private Integer customerId;
    private String customerName;
    private BigDecimal loanAmount;
    private BigDecimal annualInterestRate;
    private LocalDate startDate;
    private LocalDate endDate;
    private String interestType;
    private String repaymentFrequency;
    private String contractStatus;
    private BigDecimal principalPaid;
    private LocalDate principalPaymentDate;
    private LocalDateTime createdAt;
    private CustomerVO customer;
} 