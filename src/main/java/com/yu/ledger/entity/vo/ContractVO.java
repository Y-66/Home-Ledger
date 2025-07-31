package com.yu.ledger.entity.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 合同VO
 */
@Data
public class ContractVO {
    private Integer contractId;
    private String contractName;
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
} 