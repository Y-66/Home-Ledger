package com.yu.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yu.ledger.entity.po.ReminderLogs;
import com.yu.ledger.entity.po.Customers;
import com.yu.ledger.entity.po.LoanContracts;
import com.yu.ledger.entity.po.RepaymentSchedule;
import com.yu.ledger.mapper.ReminderLogsMapper;
import com.yu.ledger.mapper.CustomersMapper;
import com.yu.ledger.mapper.LoanContractsMapper;
import com.yu.ledger.mapper.RepaymentScheduleMapper;
import com.yu.ledger.service.IReminderLogsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.yu.ledger.entity.enums.ReminderStatusEnum.*;

@Service
@RequiredArgsConstructor
public class ReminderLogsServiceImpl implements IReminderLogsService {

    private final ReminderLogsMapper reminderLogsMapper;

    private final CustomersMapper customersMapper;

    private final LoanContractsMapper loanContractsMapper;

    private final RepaymentScheduleMapper repaymentScheduleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listReminders(Map<String, Object> params) {
        LambdaQueryWrapper<ReminderLogs> wrapper = new LambdaQueryWrapper<>();

        // 1. 模糊搜索关键字（支持客户姓名、合同号、合同名称）
        String keyword = (params.get("keyword") != null) ? params.get("keyword").toString().trim() : null;
        if (StringUtils.hasText(keyword)) {
            // 先通过关键字查找相关的客户和合同
            List<Integer> relatedScheduleIds = new ArrayList<>();
            
            // 通过客户姓名查找
            List<Customers> customers = customersMapper.selectList(new LambdaQueryWrapper<Customers>()
                    .like(Customers::getFullName, keyword));
            
            for (Customers customer : customers) {
                // 查找该客户的合同
                List<LoanContracts> contracts = loanContractsMapper.selectList(new LambdaQueryWrapper<LoanContracts>()
                        .eq(LoanContracts::getCustomerId, customer.getCustomerId()));
                
                for (LoanContracts contract : contracts) {
                    // 查找该合同的还款计划
                    List<RepaymentSchedule> schedules = repaymentScheduleMapper.selectList(new LambdaQueryWrapper<RepaymentSchedule>()
                            .eq(RepaymentSchedule::getContractId, contract.getContractId()));
                    
                    for (RepaymentSchedule schedule : schedules) {
                        relatedScheduleIds.add(schedule.getScheduleId());
                    }
                }
            }
            
            // 通过合同号查找（假设合同号就是contract_id）
            try {
                Integer contractId = Integer.valueOf(keyword);
                List<RepaymentSchedule> schedules = repaymentScheduleMapper.selectList(new LambdaQueryWrapper<RepaymentSchedule>()
                        .eq(RepaymentSchedule::getContractId, contractId));
                for (RepaymentSchedule schedule : schedules) {
                    relatedScheduleIds.add(schedule.getScheduleId());
                }
            } catch (NumberFormatException e) {
                // 如果不是数字，尝试通过合同名称查找
                List<LoanContracts> contractsByName = loanContractsMapper.selectList(new LambdaQueryWrapper<LoanContracts>()
                        .like(LoanContracts::getContractName, keyword));
                
                for (LoanContracts contract : contractsByName) {
                    // 查找该合同的还款计划
                    List<RepaymentSchedule> schedules = repaymentScheduleMapper.selectList(new LambdaQueryWrapper<RepaymentSchedule>()
                            .eq(RepaymentSchedule::getContractId, contract.getContractId()));
                    
                    for (RepaymentSchedule schedule : schedules) {
                        relatedScheduleIds.add(schedule.getScheduleId());
                    }
                }
            }
            
            // 如果找到了相关的schedule_id，则添加到查询条件中
            if (!relatedScheduleIds.isEmpty()) {
                wrapper.in(ReminderLogs::getScheduleId, relatedScheduleIds);
            } else {
                // 如果没有找到相关记录，返回空结果
                return new ArrayList<>();
            }
        }

        // 2. 精确匹配 reminder_type
        if (params.get("type") != null && StringUtils.hasText(params.get("type").toString())) {
            wrapper.eq(ReminderLogs::getReminderType, params.get("type").toString());
        }

        // 3. 精确匹配 reminder_channel
        if (params.get("channel") != null && StringUtils.hasText(params.get("channel").toString())) {
            wrapper.eq(ReminderLogs::getReminderChannel, params.get("channel").toString());
        }

        // 4. 匹配状态
        if (params.get("status") != null) {
            String status = params.get("status").toString();
            if ("success".equalsIgnoreCase(status)) {
                wrapper.eq(ReminderLogs::getReminderStatus, SUCCESS);
            } else if ("failure".equalsIgnoreCase(status)) {
                wrapper.eq(ReminderLogs::getReminderStatus, FAILED);
            } else if ("pending".equalsIgnoreCase(status)) {
                wrapper.eq(ReminderLogs::getReminderStatus, PENDING);
            }
        }

        // 5. 按发送时间降序排序（最新到最旧）
        wrapper.orderByDesc(ReminderLogs::getSentAt);

        // 查询数据
        List<ReminderLogs> list = reminderLogsMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();

        for (ReminderLogs r : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("reminder_id", r.getReminderId());

            String contractId = "";
            String customerName = "";
            String contractName = "";

            if (r.getScheduleId() != null) {
                try {
                    RepaymentSchedule schedule = repaymentScheduleMapper.selectById(r.getScheduleId());
                    if (schedule != null) {
                        contractId = schedule.getContractId() != null ? schedule.getContractId().toString() : "";
                        LoanContracts contract = loanContractsMapper.selectById(schedule.getContractId());
                        if (contract != null && contract.getCustomerId() != null) {
                            Customers customer = customersMapper.selectById(contract.getCustomerId());
                            customerName = (customer != null) ? customer.getFullName() : "";
                            
                            // 获取合同名称
                            contractName = contract.getContractName();
                        }
                    }
                } catch (Exception e) {
                    // 记录错误但不影响主流程
                    System.err.println("获取客户信息失败: " + e.getMessage());
                }
            }

            map.put("customer_name", customerName);
            map.put("contract_id", contractId);
            map.put("contract_name", contractName);
            map.put("reminder_type", r.getReminderType());
            map.put("reminder_channel", r.getReminderChannel());
            map.put("recipient", r.getRecipient());
            map.put("sent_at", r.getSentAt());
            map.put("status", r.getReminderStatus());
            map.put("failure_reason", r.getFailureReason());

            result.add(map);
        }

        return result;
    }



    @Override
    public Map<String, Object> sendReminder(Map<String, Object> reminder) {
        ReminderLogs entity = new ReminderLogs();
        entity.setReminderType(reminder.get("reminder_type").toString());
        // channels为数组，拼接为逗号分隔
        Object channelsObj = reminder.get("channels");
        if (channelsObj instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> channels = (List<String>) channelsObj;
            entity.setReminderChannel(String.join(",", channels));
        } else {
            entity.setReminderChannel(channelsObj != null ? channelsObj.toString() : "");
        }
        // recipient为手机号/邮箱
        entity.setRecipient(reminder.getOrDefault("recipient", "").toString());
        // scheduleId、contractId、customerId
        if (reminder.get("schedule_id") != null) {
            try { entity.setScheduleId(Integer.valueOf(reminder.get("schedule_id").toString())); } catch (Exception ignore) {}
        }
        entity.setSentAt(LocalDateTime.now());
        entity.setReminderStatus("pending");
        entity.setFailureReason(null);
        reminderLogsMapper.insert(entity);
        return Map.of("success", true, "reminder_id", entity.getReminderId());
    }

    @Override
    public Map<String, Object> resendReminder(Long reminderId) {
        ReminderLogs entity = reminderLogsMapper.selectById(reminderId);
        if (entity == null) return Map.of("success", false, "message", "提醒不存在");
        entity.setSentAt(LocalDateTime.now());
        entity.setReminderStatus("success");
        entity.setFailureReason(null);
        reminderLogsMapper.updateById(entity);
        return Map.of("success", true, "reminder_id", entity.getReminderId());
    }

    @Override
    public Map<String, Object> getReminderDetail(Long reminderId) {
        ReminderLogs r = reminderLogsMapper.selectById(reminderId);
        if (r == null) return Map.of("success", false, "message", "提醒不存在");
        
        Map<String, Object> map = new HashMap<>();
        map.put("reminder_id", r.getReminderId());
        map.put("reminder_type", r.getReminderType());
        map.put("reminder_channel", r.getReminderChannel());
        map.put("recipient", r.getRecipient());
        map.put("sent_at", r.getSentAt());
        map.put("status", r.getReminderStatus());
        map.put("failure_reason", r.getFailureReason());

        // 获取客户和合同信息
        String contractId = "";
        String customerName = "";
        String contractName = "";

        if (r.getScheduleId() != null) {
            try {
                RepaymentSchedule schedule = repaymentScheduleMapper.selectById(r.getScheduleId());
                if (schedule != null) {
                    contractId = schedule.getContractId() != null ? schedule.getContractId().toString() : "";
                    LoanContracts contract = loanContractsMapper.selectById(schedule.getContractId());
                    if (contract != null && contract.getCustomerId() != null) {
                        // 获取客户信息
                        Customers customer = customersMapper.selectById(contract.getCustomerId());
                        customerName = (customer != null) ? customer.getFullName() : "";
                        
                        // 生成合同名称（可以根据实际需求调整格式）
                        contractName = contract.getContractName();
                    }
                }
            } catch (Exception e) {
                // 记录错误但不影响主流程
                System.err.println("获取客户信息失败: " + e.getMessage());
            }
        }

        map.put("customer_name", customerName);
        map.put("contract_id", contractId);
        map.put("contract_name", contractName);

        return map;
    }

    @Override
    public Map<String, Object> deleteReminder(Long reminderId) {
        ReminderLogs reminder = reminderLogsMapper.selectById(reminderId);
        if (reminder == null) {
            return Map.of("success", false, "message", "提醒记录不存在");
        }
        
        try {
            reminderLogsMapper.deleteById(reminderId);
            return Map.of("success", true, "message", "提醒记录删除成功", "reminder_id", reminderId);
        } catch (Exception e) {
            return Map.of("success", false, "message", "删除失败: " + e.getMessage());
        }
    }
}
