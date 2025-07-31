package com.yu.ledger.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yu.ledger.entity.po.LoanContracts;
import com.yu.ledger.entity.po.RepaymentSchedule;
import com.yu.ledger.mapper.LoanContractsMapper;
import com.yu.ledger.mapper.RepaymentScheduleMapper;
import com.yu.ledger.service.IDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import com.yu.ledger.entity.po.Customers;
import com.yu.ledger.mapper.CustomersMapper;

@Service
public class DashboardServiceImpl implements IDashboardService {
    @Autowired
    private LoanContractsMapper loanContractsMapper;
    @Autowired
    private RepaymentScheduleMapper repaymentScheduleMapper;
    @Autowired
    private CustomersMapper customersMapper;

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        // 1. 总借贷金额
        BigDecimal totalLoanAmount = loanContractsMapper.selectList(null).stream()
                .map(LoanContracts::getLoanAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("totalLoanAmount", totalLoanAmount);

        // 2. 本月利息收入
        LocalDate now = LocalDate.now();
        BigDecimal monthlyInterestIncome = repaymentScheduleMapper.selectList(
                new QueryWrapper<RepaymentSchedule>()
                        .eq("period_status", "paid")
                        //.ge("payment_date", now.withDayOfMonth(1))
                        //.le("payment_date", now)
        ).stream().map(RepaymentSchedule::getCalculatedInterest)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("monthlyInterestIncome", monthlyInterestIncome);

        // 3. 逾期合同数
        long overdueContracts = repaymentScheduleMapper.selectList(
                new QueryWrapper<RepaymentSchedule>()
                        .eq("period_status", "overdue")
        ).stream().map(RepaymentSchedule::getContractId).distinct().count();
        stats.put("overdueContracts", overdueContracts);

        // 4. 逾期金额
        BigDecimal overdueAmount = repaymentScheduleMapper.selectList(
                new QueryWrapper<RepaymentSchedule>()
                        .eq("period_status", "overdue")
        ).stream().map(RepaymentSchedule::getCalculatedInterest)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("overdueAmount", overdueAmount);

        // 5. 环比增长（mock数据，实际应对比上月）
        stats.put("totalLoanTrend", 12.5);
        stats.put("monthlyInterestTrend", 8.3);
        stats.put("overdueTrend", 2.1);
        stats.put("overdueAmountTrend", 5.2);
        return stats;
    }

    @Override
    public List<Map<String, Object>> getTodayReminders() {
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);
        
        // 查询最近30天内到期的还款计划
        List<RepaymentSchedule> dueList = repaymentScheduleMapper.selectList(
                new QueryWrapper<RepaymentSchedule>()
                        .ge("due_date", today)
                        .le("due_date", thirtyDaysLater)
                        .in("period_status", Arrays.asList("pending", "overdue"))
                        .orderByAsc("due_date")
        );
        
        List<Map<String, Object>> result = new ArrayList<>();
        for (RepaymentSchedule schedule : dueList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", schedule.getScheduleId());
            map.put("contractId", schedule.getContractId());
            map.put("amount", schedule.getCalculatedInterest());
            
            // 计算剩余天数
            long remainingDays = ChronoUnit.DAYS.between(today, schedule.getDueDate());
            map.put("remaining_date", remainingDays);
            
            // 判断状态：如果已逾期则为overdue，否则为due
            if (schedule.getDueDate().isBefore(today)) {
                map.put("status", "overdue");
            } else {
                map.put("status", "due");
            }
            
            // 查询客户姓名
            LoanContracts contract = loanContractsMapper.selectById(schedule.getContractId());
            if (contract != null) {
                Customers customer = customersMapper.selectById(contract.getCustomerId());
                map.put("customerName", customer != null ? customer.getFullName() : "未知客户");
            } else {
                map.put("customerName", "未知客户");
            }
            
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getRecentActivities() {
        // 最近活动：取最近10条已还款、逾期、合同创建
        List<Map<String, Object>> activities = new ArrayList<>();
        // 1. 最近还款
        List<RepaymentSchedule> paidList = repaymentScheduleMapper.selectList(
                new QueryWrapper<RepaymentSchedule>()
                        .eq("period_status", "paid")
                        .orderByDesc("payment_date")
                        .last("limit 5")
        );
        for (RepaymentSchedule schedule : paidList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", schedule.getScheduleId());
            map.put("type", "payment");
            map.put("title", "合同" + schedule.getContractId() + "完成利息支付 ¥" + schedule.getCalculatedInterest());
            map.put("time", schedule.getPaymentDate() != null ? schedule.getPaymentDate().toString() : "");
            activities.add(map);
        }
        // 2. 最近逾期
        List<RepaymentSchedule> overdueList = repaymentScheduleMapper.selectList(
                new QueryWrapper<RepaymentSchedule>()
                        .eq("period_status", "overdue")
                        .orderByDesc("due_date")
                        .last("limit 3")
        );
        for (RepaymentSchedule schedule : overdueList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", schedule.getScheduleId());
            map.put("type", "overdue");
            map.put("title", "合同" + schedule.getContractId() + "逾期，金额 ¥" + schedule.getCalculatedInterest());
            map.put("time", schedule.getDueDate() != null ? schedule.getDueDate().toString() : "");
            activities.add(map);
        }
        // 3. 最近合同
        List<LoanContracts> contractList = loanContractsMapper.selectList(
                new QueryWrapper<LoanContracts>()
                        .orderByDesc("created_at")
                        .last("limit 2")
        );
        for (LoanContracts contract : contractList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", contract.getContractId());
            map.put("type", "contract");
            map.put("title", "新合同 " + contract.getContractId() + " 创建，客户ID " + contract.getCustomerId());
            map.put("time", contract.getCreatedAt() != null ? contract.getCreatedAt().toString() : "");
            activities.add(map);
        }
        // 按时间倒序，取前10条
        activities.sort((a, b) -> b.getOrDefault("time", "").toString().compareTo(a.getOrDefault("time", "").toString()));
        return activities.size() > 10 ? activities.subList(0, 10) : activities;
    }
} 