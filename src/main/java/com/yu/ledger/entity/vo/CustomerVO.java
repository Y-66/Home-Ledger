package com.yu.ledger.entity.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 客户VO
 */
@Data
public class CustomerVO {
    private Integer customerId;
    private String fullName;
    private String idNumber;
    private String contactPhone;
    private String email;
    private LocalDateTime createdAt;
} 