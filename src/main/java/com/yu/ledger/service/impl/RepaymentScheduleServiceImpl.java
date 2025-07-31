package com.yu.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yu.ledger.entity.po.RepaymentSchedule;
import com.yu.ledger.entity.po.LoanContracts;
import com.yu.ledger.entity.po.Customers;
import com.yu.ledger.entity.po.ReminderLogs;
import com.yu.ledger.mapper.RepaymentScheduleMapper;
import com.yu.ledger.mapper.LoanContractsMapper;
import com.yu.ledger.mapper.CustomersMapper;
import com.yu.ledger.mapper.ReminderLogsMapper;
import com.yu.ledger.service.IRepaymentScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RepaymentScheduleServiceImpl implements IRepaymentScheduleService {

    private final RepaymentScheduleMapper repaymentScheduleMapper;

    private final LoanContractsMapper loanContractsMapper;

    private final CustomersMapper customersMapper;

    private final ReminderLogsMapper reminderLogsMapper;

    private LocalDate parseToLocalDate(String dateStr) {
        if (dateStr == null) return null;
        if (dateStr.length() == 10) {
            return LocalDate.parse(dateStr);
        } else {
            return OffsetDateTime.parse(dateStr).toLocalDate();
        }
    }

    @Override
    public List<Map<String, Object>> listRepayments(Map<String, Object> params) {
        LambdaQueryWrapper<RepaymentSchedule> wrapper = new LambdaQueryWrapper<>();
        // 关键字（合同ID/合同名/客户名）
        if (params.get("keyword") != null && !params.get("keyword").toString().trim().isEmpty()) {
            String keyword = params.get("keyword").toString().trim();
            // 先查合同名
            List<LoanContracts> contractNameList = loanContractsMapper.selectList(new LambdaQueryWrapper<LoanContracts>()
                    .like(LoanContracts::getContractName, keyword));
            if (!contractNameList.isEmpty()) {
                List<Integer> contractIds = contractNameList.stream()
                        .map(LoanContracts::getContractId)
                        .collect(java.util.stream.Collectors.toList());
                wrapper.in(RepaymentSchedule::getContractId, contractIds);
            } else {
                // 再查客户名
                List<Customers> customers = customersMapper.selectList(new LambdaQueryWrapper<Customers>()
                        .like(Customers::getFullName, keyword));
                if (!customers.isEmpty()) {
                    List<Integer> customerIds = customers.stream()
                            .map(Customers::getCustomerId)
                            .collect(java.util.stream.Collectors.toList());
                    List<LoanContracts> contracts = loanContractsMapper.selectList(new LambdaQueryWrapper<LoanContracts>()
                            .in(LoanContracts::getCustomerId, customerIds));
                    if (!contracts.isEmpty()) {
                        List<Integer> contractIds = contracts.stream()
                                .map(LoanContracts::getContractId)
                                .collect(java.util.stream.Collectors.toList());
                        wrapper.in(RepaymentSchedule::getContractId, contractIds);
                    } else {
                        wrapper.eq(RepaymentSchedule::getContractId, -1);
                    }
                } else {
                    // 最后查合同ID
                    try {
                        Integer contractId = Integer.valueOf(keyword);
                        wrapper.eq(RepaymentSchedule::getContractId, contractId);
                    } catch (NumberFormatException e) {
                        wrapper.eq(RepaymentSchedule::getContractId, -1);
                    }
                }
            }
        }
        // 状态
        if (params.get("status") != null && !params.get("status").toString().trim().isEmpty()) {
            wrapper.eq(RepaymentSchedule::getPeriodStatus, params.get("status"));
        }
        // 日期范围查询（使用新的startDate和endDate参数）
        if (params.get("startDate") != null && !params.get("startDate").toString().trim().isEmpty()) {
            LocalDate startDate = parseToLocalDate(params.get("startDate").toString());
            if (startDate != null) {
                wrapper.ge(RepaymentSchedule::getDueDate, startDate);
            }
        }
        if (params.get("endDate") != null && !params.get("endDate").toString().trim().isEmpty()) {
            LocalDate endDate = parseToLocalDate(params.get("endDate").toString());
            if (endDate != null) {
                wrapper.le(RepaymentSchedule::getDueDate, endDate);
            }
        }
        wrapper.orderByAsc(RepaymentSchedule::getDueDate);
        return  repaymentScheduleMapper.selectList(wrapper).stream()
                .map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("schedule_id", r.getScheduleId());
                    map.put("contract_id", r.getContractId());
                    // 查合同名和客户名
                    LoanContracts contract = loanContractsMapper.selectById(r.getContractId());
                    map.put("contract_name", contract != null ? contract.getContractName() : null);
                    Customers customer = contract != null ? customersMapper.selectById(contract.getCustomerId()) : null;
                    map.put("customer_name", customer != null ? customer.getFullName() : null);
                    map.put("period_start", r.getPeriodStart());
                    map.put("period_end", r.getPeriodEnd());
                    map.put("due_date", r.getDueDate());
                    map.put("calculated_interest", r.getCalculatedInterest());
                    map.put("period_status", r.getPeriodStatus());
                    map.put("payment_date", r.getPaymentDate());
                    map.put("last_reminder_date", r.getLastReminderDate());
                    return map;
                }).toList();
    }

    @Override
    public Map<String, Object> payRepayment(Long scheduleId, Map<String, Object> payInfo) {
        RepaymentSchedule r = repaymentScheduleMapper.selectById(scheduleId);
        if (r == null) return Map.of("success", false, "message", "还款计划不存在");
        if (payInfo.get("payment_date") != null) {
            r.setPaymentDate(parseToLocalDate(payInfo.get("payment_date").toString()));
        }
        if (payInfo.get("payment_amount") != null) {
            // 可扩展：校验金额
        }
        if (payInfo.get("remark") != null) {
            // 可扩展：保存备注
        }
        r.setPeriodStatus("paid");
        repaymentScheduleMapper.updateById(r);
        return Map.of("success", true, "schedule_id", r.getScheduleId());
    }

    @Override
    public Map<String, Object> remindRepayment(Long scheduleId) {
        RepaymentSchedule r = repaymentScheduleMapper.selectById(scheduleId);
        if (r == null) return Map.of("success", false, "message", "还款计划不存在");
        r.setLastReminderDate(LocalDate.now());
        repaymentScheduleMapper.updateById(r);

        // 生成提醒日志
        try {
            // 获取合同和客户信息
            LoanContracts contract = loanContractsMapper.selectById(r.getContractId());
            if (contract != null) {
                Customers customer = customersMapper.selectById(contract.getCustomerId());
                if (customer != null) {
                    // 创建初始提醒日志
                    ReminderLogs reminder = new ReminderLogs();
                    reminder.setScheduleId(r.getScheduleId());
                    reminder.setReminderType("initial");
                    reminder.setReminderChannel("email");
                    reminder.setRecipient(customer.getContactPhone());
                    reminder.setSentAt(LocalDateTime.now());
                    reminder.setReminderStatus("pending");
                    reminder.setFailureReason(null);

                    reminderLogsMapper.insert(reminder);
                }
            }
        } catch (Exception e) {
            // 记录错误但不影响主流程
            System.err.println("生成提醒日志失败: " + e.getMessage());
        }

        return Map.of("success", true, "schedule_id", r.getScheduleId());
    }

    @Override
    public Map<String, Object> getRepaymentDetail(Long scheduleId) {
        RepaymentSchedule r = repaymentScheduleMapper.selectById(scheduleId);
        if (r == null) return Map.of("success", false, "message", "还款计划不存在");
        Map<String, Object> map = new HashMap<>();
        map.put("schedule_id", r.getScheduleId());
        map.put("contract_id", r.getContractId());
        LoanContracts contract = loanContractsMapper.selectById(r.getContractId());
        map.put("contract_name", contract != null ? contract.getContractName() : null);
        Customers customer = contract != null ? customersMapper.selectById(contract.getCustomerId()) : null;
        map.put("customer_name", customer != null ? customer.getFullName() : null);
        map.put("period_start", r.getPeriodStart());
        map.put("period_end", r.getPeriodEnd());
        map.put("due_date", r.getDueDate());
        map.put("calculated_interest", r.getCalculatedInterest());
        map.put("period_status", r.getPeriodStatus());
        map.put("payment_date", r.getPaymentDate());
        map.put("last_reminder_date", r.getLastReminderDate());
        return map;
    }
}
