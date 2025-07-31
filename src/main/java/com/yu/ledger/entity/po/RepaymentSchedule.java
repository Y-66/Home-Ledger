package com.yu.ledger.entity.po;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
@TableName("repayment_schedule")
@ApiModel(value="RepaymentSchedule对象", description="")
public class RepaymentSchedule implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "schedule_id", type = IdType.AUTO)
    private Integer scheduleId;

    @TableField("contract_id")
    private Integer contractId;

    @TableField("period_start")
    private LocalDate periodStart;

    @TableField("period_end")
    private LocalDate periodEnd;

    @TableField("due_date")
    private LocalDate dueDate;

    @TableField("calculated_interest")
    private BigDecimal calculatedInterest;

    @TableField("period_status")
    private String periodStatus;

    @TableField("last_reminder_date")
    private LocalDate lastReminderDate;

    @TableField("payment_date")
    private LocalDate paymentDate;

    @TableField("created_at")
    private LocalDateTime createdAt;

}
