package com.yu.ledger.service.impl;

import com.yu.ledger.service.ILoanContractsService;
import com.yu.ledger.util.MyRepaymentScheduleCalculator;
import com.yu.ledger.util.RepaymentScheduleCalculator;
import org.springframework.stereotype.Service;
import java.util.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yu.ledger.entity.po.LoanContracts;
import com.yu.ledger.entity.po.Customers;
import com.yu.ledger.entity.po.RepaymentSchedule;
import com.yu.ledger.mapper.LoanContractsMapper;
import com.yu.ledger.mapper.CustomersMapper;
import com.yu.ledger.mapper.RepaymentScheduleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import com.yu.ledger.entity.po.LoanContracts;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Service
public class LoanContractsServiceImpl implements ILoanContractsService {
    @Autowired
    private LoanContractsMapper loanContractsMapper;
    @Autowired
    private CustomersMapper customersMapper;
    @Autowired
    private RepaymentScheduleMapper repaymentScheduleMapper;

    private LocalDate parseToLocalDate(String dateStr) {
        if (dateStr == null) return null;
        if (dateStr.length() == 10) {
            return LocalDate.parse(dateStr);
        } else {
            return OffsetDateTime.parse(dateStr).toLocalDate();
        }
    }

    @Override
    public List<Map<String, Object>> listContracts(Map<String, Object> params) {
        QueryWrapper<LoanContracts> wrapper = new QueryWrapper<>();
        // 关键字（合同号/合同名/客户名）
        if (params.get("keyword") != null && !params.get("keyword").toString().trim().isEmpty()) {
            String keyword = params.get("keyword").toString().trim();
            // 先查合同名
            QueryWrapper<LoanContracts> contractNameWrapper = new QueryWrapper<>();
            contractNameWrapper.like("contract_name", keyword);
            List<LoanContracts> contractNameList = loanContractsMapper.selectList(contractNameWrapper);
            if (!contractNameList.isEmpty()) {
                List<Integer> contractIds = contractNameList.stream()
                        .map(LoanContracts::getContractId)
                        .collect(java.util.stream.Collectors.toList());
                wrapper.in("contract_id", contractIds);
            } else {
                // 再查客户名
                QueryWrapper<Customers> customerWrapper = new QueryWrapper<>();
                customerWrapper.like("full_name", keyword);
                List<Customers> customers = customersMapper.selectList(customerWrapper);
                if (!customers.isEmpty()) {
                    List<Integer> customerIds = customers.stream()
                            .map(Customers::getCustomerId)
                            .collect(java.util.stream.Collectors.toList());
                    wrapper.in("customer_id", customerIds);
                } else {
                    // 最后查合同ID
                    try {
                        Integer contractId = Integer.valueOf(keyword);
                        wrapper.eq("contract_id", contractId);
                    } catch (NumberFormatException e) {
                        wrapper.eq("contract_id", -1);
                    }
                }
            }
        }
        // 合同状态
        if (params.get("status") != null && !params.get("status").toString().trim().isEmpty()) {
            wrapper.eq("contract_status", params.get("status"));
        }
        // 还款频率
        if (params.get("frequency") != null && !params.get("frequency").toString().trim().isEmpty()) {
            wrapper.eq("repayment_frequency", params.get("frequency"));
        }
        wrapper.orderByDesc("contract_id");
        List<LoanContracts> list = loanContractsMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (LoanContracts c : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("contract_id", c.getContractId());
            map.put("contract_name", c.getContractName());
            Customers customer = customersMapper.selectById(c.getCustomerId());
            map.put("customer_name", customer != null ? customer.getFullName() : null);
            map.put("loan_amount", c.getLoanAmount());
            map.put("annual_interest_rate", c.getAnnualInterestRate());
            map.put("start_date", c.getStartDate());
            map.put("end_date", c.getEndDate());
            map.put("interest_type", c.getInterestType());
            map.put("repayment_frequency", c.getRepaymentFrequency());
            map.put("contract_status", c.getContractStatus());
            map.put("principal_paid", c.getPrincipalPaid());
            map.put("created_at", c.getCreatedAt());
            result.add(map);
        }
        return result;
    }

    @Override
    public Map<String, Object> addContract(Map<String, Object> contract) {
        // 1. 创建合同
        LoanContracts entity = new LoanContracts();
        entity.setContractName((String) contract.get("contract_name"));
        entity.setCustomerId((Integer) contract.get("customer_id"));
        entity.setLoanAmount(new BigDecimal(contract.get("loan_amount").toString()));
        entity.setAnnualInterestRate(new BigDecimal(contract.get("annual_interest_rate").toString()));
        entity.setStartDate(parseToLocalDate(contract.get("start_date").toString()));
        entity.setEndDate(parseToLocalDate(contract.get("end_date").toString()));
        entity.setInterestType(contract.get("interest_type").toString());
        entity.setRepaymentFrequency(contract.get("repayment_frequency").toString());
        entity.setContractStatus(contract.getOrDefault("contract_status", "active").toString());
        entity.setPrincipalPaid(BigDecimal.ZERO);
        entity.setCreatedAt(LocalDateTime.now());
        
        // 插入合同
        loanContractsMapper.insert(entity);
        Integer contractId = entity.getContractId();
        
        // 2. 生成还款计划
        try {
            List<RepaymentSchedule> schedules = MyRepaymentScheduleCalculator.generateRepaymentSchedules(
                contractId,
                entity.getLoanAmount(),
                entity.getAnnualInterestRate(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getRepaymentFrequency()
            );
            
            // 批量插入还款计划
            for (RepaymentSchedule schedule : schedules) {
                repaymentScheduleMapper.insert(schedule);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("contract_id", contractId);
            result.put("schedule_count", schedules.size());
            result.put("message", "合同创建成功，已生成 " + schedules.size() + " 个还款计划");
            return result;
            
        } catch (Exception e) {
            // 如果生成还款计划失败，记录错误但合同创建成功
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("contract_id", contractId);
            result.put("schedule_count", 0);
            result.put("warning", "合同创建成功，但还款计划生成失败：" + e.getMessage());
            return result;
        }
    }

    @Override
    public Map<String, Object> updateContract(String contractId, Map<String, Object> contract) {
        LoanContracts entity = loanContractsMapper.selectById(contractId);
        if (entity == null) {
            return Map.of("success", false, "message", "合同不存在");
        }
        
        // 记录原始值，用于判断是否需要重新生成还款计划
        BigDecimal originalLoanAmount = entity.getLoanAmount();
        BigDecimal originalAnnualRate = entity.getAnnualInterestRate();
        LocalDate originalStartDate = entity.getStartDate();
        LocalDate originalEndDate = entity.getEndDate();
        String originalFrequency = entity.getRepaymentFrequency();
        
        // 更新合同信息
        if (contract.get("contract_name") != null) entity.setContractName(contract.get("contract_name").toString());
        if (contract.get("customer_id") != null) entity.setCustomerId((Integer) contract.get("customer_id"));
        if (contract.get("loan_amount") != null) entity.setLoanAmount(new BigDecimal(contract.get("loan_amount").toString()));
        if (contract.get("annual_interest_rate") != null) entity.setAnnualInterestRate(new BigDecimal(contract.get("annual_interest_rate").toString()));
        if (contract.get("start_date") != null) entity.setStartDate(parseToLocalDate(contract.get("start_date").toString()));
        if (contract.get("end_date") != null) entity.setEndDate(parseToLocalDate(contract.get("end_date").toString()));
        if (contract.get("interest_type") != null) entity.setInterestType(contract.get("interest_type").toString());
        if (contract.get("repayment_frequency") != null) entity.setRepaymentFrequency(contract.get("repayment_frequency").toString());
        if (contract.get("contract_status") != null) entity.setContractStatus(contract.get("contract_status").toString());
        
        // 更新合同
        loanContractsMapper.updateById(entity);
        
        // 检查是否需要重新生成还款计划
        boolean needRegenerateSchedules = false;
        if (contract.get("loan_amount") != null && !originalLoanAmount.equals(entity.getLoanAmount())) {
            needRegenerateSchedules = true;
        }
        if (contract.get("annual_interest_rate") != null && !originalAnnualRate.equals(entity.getAnnualInterestRate())) {
            needRegenerateSchedules = true;
        }
        if (contract.get("start_date") != null && !originalStartDate.equals(entity.getStartDate())) {
            needRegenerateSchedules = true;
        }
        if (contract.get("end_date") != null && !originalEndDate.equals(entity.getEndDate())) {
            needRegenerateSchedules = true;
        }
        if (contract.get("repayment_frequency") != null && !originalFrequency.equals(entity.getRepaymentFrequency())) {
            needRegenerateSchedules = true;
        }
        
        // 如果需要重新生成还款计划
        if (needRegenerateSchedules) {
            try {
                // 删除原有还款计划
                QueryWrapper<RepaymentSchedule> deleteWrapper = new QueryWrapper<>();
                deleteWrapper.eq("contract_id", contractId);
                repaymentScheduleMapper.delete(deleteWrapper);
                
                // 生成新的还款计划
                List<RepaymentSchedule> schedules = MyRepaymentScheduleCalculator.generateRepaymentSchedules(
                    entity.getContractId(),
                    entity.getLoanAmount(),
                    entity.getAnnualInterestRate(),
                    entity.getStartDate(),
                    entity.getEndDate(),
                    entity.getRepaymentFrequency()
                );
                
                // 批量插入新的还款计划
                for (RepaymentSchedule schedule : schedules) {
                    repaymentScheduleMapper.insert(schedule);
                }
                
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("contract_id", entity.getContractId());
                result.put("schedule_count", schedules.size());
                result.put("message", "合同更新成功，已重新生成 " + schedules.size() + " 个还款计划");
                return result;
                
            } catch (Exception e) {
                // 如果重新生成还款计划失败，记录错误但合同更新成功
                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("contract_id", entity.getContractId());
                result.put("schedule_count", 0);
                result.put("warning", "合同更新成功，但还款计划重新生成失败：" + e.getMessage());
                return result;
            }
        } else {
            // 不需要重新生成还款计划
            return Map.of("success", true, "contract_id", entity.getContractId(), "message", "合同更新成功");
        }
    }

    @Override
    public Map<String, Object> closeContract(String contractId) {
        LoanContracts entity = loanContractsMapper.selectById(contractId);
        if (entity == null) {
            return Map.of("success", false, "message", "合同不存在");
        }
        entity.setContractStatus("closed");
        loanContractsMapper.updateById(entity);
        // 删除该合同下所有还款计划
        QueryWrapper<RepaymentSchedule> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("contract_id", contractId);
        repaymentScheduleMapper.delete(deleteWrapper);
        return Map.of("success", true, "contract_id", entity.getContractId());
    }

    @Override
    public Map<String, Object> getContractDetail(String contractId) {
        LoanContracts contract = loanContractsMapper.selectById(contractId);
        if (contract == null) {
            return Map.of("success", false, "message", "合同不存在");
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("contract_id", contract.getContractId());
        result.put("contract_name", contract.getContractName());
        result.put("customer_id", contract.getCustomerId());
        result.put("loan_amount", contract.getLoanAmount());
        result.put("annual_interest_rate", contract.getAnnualInterestRate());
        result.put("start_date", contract.getStartDate());
        result.put("end_date", contract.getEndDate());
        result.put("interest_type", contract.getInterestType());
        result.put("repayment_frequency", contract.getRepaymentFrequency());
        result.put("contract_status", contract.getContractStatus());
        result.put("principal_paid", contract.getPrincipalPaid());
        result.put("principal_payment_date", contract.getPrincipalPaymentDate());
        result.put("created_at", contract.getCreatedAt());
        
        // 查询客户信息
        Customers customer = customersMapper.selectById(contract.getCustomerId());
        if (customer != null) {
            result.put("customer_name", customer.getFullName());
            
            // 客户详细信息
            Map<String, Object> customerDetail = new HashMap<>();
            customerDetail.put("customer_id", customer.getCustomerId());
            customerDetail.put("full_name", customer.getFullName());
            customerDetail.put("id_number", customer.getIdNumber());
            customerDetail.put("contact_phone", customer.getContactPhone());
            customerDetail.put("email", customer.getEmail());
            customerDetail.put("created_at", customer.getCreatedAt());
            result.put("customer", customerDetail);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getContractSchedules(String contractId) {
        QueryWrapper<RepaymentSchedule> wrapper = new QueryWrapper<>();
        wrapper.eq("contract_id", contractId);
        wrapper.orderByAsc("period_start");
        
        List<RepaymentSchedule> list = repaymentScheduleMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (RepaymentSchedule schedule : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("schedule_id", schedule.getScheduleId());
            map.put("contract_id", schedule.getContractId());
            map.put("period_start", schedule.getPeriodStart());
            map.put("period_end", schedule.getPeriodEnd());
            map.put("due_date", schedule.getDueDate());
            map.put("calculated_interest", schedule.getCalculatedInterest());
            map.put("period_status", schedule.getPeriodStatus());
            map.put("payment_date", schedule.getPaymentDate());
            map.put("last_reminder_date", schedule.getLastReminderDate());
            map.put("created_at", schedule.getCreatedAt());
            result.add(map);
        }
        
        return result;
    }

    @Override
    public List<Map<String, Object>> contractOptions(Integer customerId) {
        QueryWrapper<LoanContracts> wrapper = new QueryWrapper<>();
        if (customerId != null) {
            wrapper.eq("customer_id", customerId);
        }
        List<LoanContracts> list = loanContractsMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (LoanContracts c : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("contract_id", c.getContractId());
            map.put("contract_name", c.getContractName());
            map.put("loan_amount", c.getLoanAmount());
            result.add(map);
        }
        return result;
    }
}
