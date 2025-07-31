package com.yu.ledger.entity.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author yu
 * @since 2025-07-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("loan_contracts")
@ApiModel(value="LoanContracts对象", description="")
public class LoanContracts implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "contract_id", type = IdType.AUTO)
    private Integer contractId;

    private String contractName;

    private Integer customerId;

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


}
