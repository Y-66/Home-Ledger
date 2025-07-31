package com.yu.ledger.entity.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName("reminder_logs")
@ApiModel(value="ReminderLogs对象", description="")
public class ReminderLogs implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "reminder_id", type = IdType.AUTO)
    private Integer reminderId;

    private Integer scheduleId;

    private String reminderType;

    private String reminderChannel;

    private LocalDateTime sentAt;

    private String recipient;

    private String reminderStatus;

    private String failureReason;


}
