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
public class MyRepaymentScheduleCalculator {

    public static List<RepaymentSchedule> generateRepaymentSchedules(
            Integer contractId,
            BigDecimal loanAmount,
            BigDecimal annualInterestRate,
            LocalDate startDate,
            LocalDate endDate,
            String repaymentFrequency) {

        List<RepaymentSchedule> schedules = new ArrayList<>();

        int periods = calculatePeriods(startDate, endDate, repaymentFrequency);

        LocalDate currentPeriodStart = startDate;
        LocalDate currentPeriodEnd;

        for (int i = 1; i <= periods; i++) {
            RepaymentSchedule schedule = new RepaymentSchedule();
            schedule.setContractId(contractId);
            schedule.setPeriodStart(currentPeriodStart);

            // 每期结束日期为当前期开始时间加一个周期（不减一天）
            currentPeriodEnd = calculatePeriodEnd(currentPeriodStart, repaymentFrequency);

            // 如果结束日期超出合同结束，则截断
            if (currentPeriodEnd.isAfter(endDate)) {
                currentPeriodEnd = endDate;
            }
            schedule.setPeriodEnd(currentPeriodEnd);

            // 到期日就是本期结束日
            schedule.setDueDate(currentPeriodEnd);

            // 按实际天数计算利息
            BigDecimal actualInterest = calculateActualInterest(
                    loanAmount,
                    annualInterestRate,
                    currentPeriodStart,
                    currentPeriodEnd
            );
            schedule.setCalculatedInterest(actualInterest);
            schedule.setPeriodStatus("pending");
            schedule.setCreatedAt(java.time.LocalDateTime.now());

            schedules.add(schedule);

            // 下一期的开始时间 = 当前期结束时间
            currentPeriodStart = currentPeriodEnd;

            if (currentPeriodStart.isAfter(endDate)) {
                break;
            }
        }

        return schedules;
    }

    private static int calculatePeriods(LocalDate startDate, LocalDate endDate, String frequency) {
        switch (frequency.toLowerCase()) {
            case "monthly":
                return (int) Math.ceil(ChronoUnit.MONTHS.between(startDate, endDate.plusDays(1)));
            case "quarterly":
                return (int) Math.ceil(ChronoUnit.MONTHS.between(startDate, endDate.plusDays(1)) / 3.0);
            case "annually":
                return (int) Math.ceil(ChronoUnit.YEARS.between(startDate, endDate.plusDays(1)));
            default:
                return (int) Math.ceil(ChronoUnit.MONTHS.between(startDate, endDate.plusDays(1))); // 默认按月
        }
    }

    private static BigDecimal calculateActualInterest(
            BigDecimal loanAmount,
            BigDecimal annualRate,
            LocalDate periodStart,
            LocalDate periodEnd) {

        long days = ChronoUnit.DAYS.between(periodStart, periodEnd); // 不含 periodEnd 当天
        BigDecimal dailyRate = annualRate.divide(new BigDecimal("36500"), 8, RoundingMode.HALF_UP);
        return loanAmount.multiply(dailyRate).multiply(BigDecimal.valueOf(days))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static LocalDate calculatePeriodEnd(LocalDate startDate, String frequency) {
        switch (frequency.toLowerCase()) {
            case "monthly":
                return startDate.plusMonths(1);
            case "quarterly":
                return startDate.plusMonths(3);
            case "annually":
                return startDate.plusYears(1);
            default:
                return startDate.plusMonths(1);
        }
    }
}
