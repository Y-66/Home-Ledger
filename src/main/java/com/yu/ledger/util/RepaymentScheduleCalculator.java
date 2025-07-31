package com.yu.ledger.util;

import com.yu.ledger.entity.po.RepaymentSchedule;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 还款计划计算工具类
 */
public class RepaymentScheduleCalculator {
    
    /**
     * 根据合同信息生成还款计划
     * @param contractId 合同ID
     * @param loanAmount 借贷金额
     * @param annualInterestRate 年利率
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param repaymentFrequency 还款频率
     * @return 还款计划列表
     */
    public static List<RepaymentSchedule> generateRepaymentSchedules(
            Integer contractId, 
            BigDecimal loanAmount, 
            BigDecimal annualInterestRate, 
            LocalDate startDate, 
            LocalDate endDate, 
            String repaymentFrequency) {
        
        List<RepaymentSchedule> schedules = new ArrayList<>();
        
        // 计算还款期数
        int periods = calculatePeriods(startDate, endDate, repaymentFrequency);
        
        // 计算每期利息
        BigDecimal periodInterest = calculatePeriodInterest(loanAmount, annualInterestRate, repaymentFrequency);
        
        // 生成每期还款计划
        LocalDate currentPeriodStart = startDate;
        LocalDate currentPeriodEnd;
        LocalDate dueDate;
        
        for (int i = 1; i <= periods; i++) {
            RepaymentSchedule schedule = new RepaymentSchedule();
            schedule.setContractId(contractId);
            schedule.setPeriodStart(currentPeriodStart);
            
            // 计算计息结束日期
            currentPeriodEnd = calculatePeriodEnd(currentPeriodStart, repaymentFrequency);
            // 如果超过合同结束日期，则使用合同结束日期
            if (currentPeriodEnd.isAfter(endDate)) {
                currentPeriodEnd = endDate;
            }
            schedule.setPeriodEnd(currentPeriodEnd);
            
            // 计算到期日期（计息结束日期后一天）
            dueDate = currentPeriodEnd.plusDays(1);
            schedule.setDueDate(dueDate);
            
            // 计算本期利息（按实际计息天数计算）
            BigDecimal actualInterest = calculateActualInterest(
                loanAmount, 
                annualInterestRate, 
                currentPeriodStart, 
                currentPeriodEnd
            );
            schedule.setCalculatedInterest(actualInterest);
            
            // 设置状态为待付
            schedule.setPeriodStatus("pending");
            
            // 设置创建时间
            schedule.setCreatedAt(java.time.LocalDateTime.now());
            
            schedules.add(schedule);
            
            // 下一期的开始日期
            currentPeriodStart = currentPeriodEnd.plusDays(1);
            
            // 如果已经超过结束日期，退出循环
            if (currentPeriodStart.isAfter(endDate)) {
                break;
            }
        }
        
        return schedules;
    }
    
    /**
     * 计算还款期数
     */
    private static int calculatePeriods(LocalDate startDate, LocalDate endDate, String frequency) {
        long days = ChronoUnit.DAYS.between(startDate, endDate);
        
        switch (frequency.toLowerCase()) {
            case "monthly":
                return (int) Math.ceil(days / 30.0);
            case "quarterly":
                return (int) Math.ceil(days / 90.0);
            case "annually":
                return (int) Math.ceil(days / 365.0);
            default:
                return (int) Math.ceil(days / 30.0); // 默认按月计算
        }
    }
    
    /**
     * 计算每期利息（简化计算）
     */
    private static BigDecimal calculatePeriodInterest(BigDecimal loanAmount, BigDecimal annualRate, String frequency) {
        BigDecimal monthlyRate = annualRate.divide(new BigDecimal("1200"), 6, RoundingMode.HALF_UP); // 月利率
        
        switch (frequency.toLowerCase()) {
            case "monthly":
                return loanAmount.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
            case "quarterly":
                return loanAmount.multiply(monthlyRate).multiply(new BigDecimal("3")).setScale(2, RoundingMode.HALF_UP);
            case "annually":
                return loanAmount.multiply(annualRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            default:
                return loanAmount.multiply(monthlyRate).setScale(2, RoundingMode.HALF_UP);
        }
    }
    
    /**
     * 计算实际利息（按实际计息天数）
     */
    private static BigDecimal calculateActualInterest(
            BigDecimal loanAmount, 
            BigDecimal annualRate, 
            LocalDate periodStart, 
            LocalDate periodEnd) {
        
        long days = ChronoUnit.DAYS.between(periodStart, periodEnd) + 1; // 包含开始和结束日期
        BigDecimal dailyRate = annualRate.divide(new BigDecimal("36500"), 8, RoundingMode.HALF_UP); // 日利率
        
        return loanAmount.multiply(dailyRate).multiply(new BigDecimal(days))
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算计息结束日期
     */
    private static LocalDate calculatePeriodEnd(LocalDate startDate, String frequency) {
        switch (frequency.toLowerCase()) {
            case "monthly":
                return startDate.plusMonths(1).minusDays(1);
            case "quarterly":
                return startDate.plusMonths(3).minusDays(1);
            case "annually":
                return startDate.plusYears(1).minusDays(1);
            default:
                return startDate.plusMonths(1).minusDays(1);
        }
    }
} 