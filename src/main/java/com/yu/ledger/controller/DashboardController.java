package com.yu.ledger.controller;

import com.yu.ledger.service.IDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "仪表盘", description = "仪表盘相关接口")
public class DashboardController {
    @Autowired
    private IDashboardService dashboardService;

    // 1. 获取仪表盘统计数据
    @GetMapping("/stats")
    @Operation(summary = "获取仪表盘统计数据", description = "获取借贷总额、月利息收入、逾期合同数等统计数据")
    public ResponseEntity<?> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    // 2. 获取最近期提醒
    @GetMapping("/today-reminders")
    @Operation(summary = "获取最近期提醒", description = "获取最近30天内快到期的还款提醒，包含剩余天数")
    public ResponseEntity<?> getTodayReminders() {
        return ResponseEntity.ok(dashboardService.getTodayReminders());
    }

    // 3. 获取最近活动
    @GetMapping("/recent-activities")
    @Operation(summary = "获取最近活动", description = "获取最近的还款、逾期、新合同等活动记录")
    public ResponseEntity<?> getRecentActivities() {
        return ResponseEntity.ok(dashboardService.getRecentActivities());
    }
} 