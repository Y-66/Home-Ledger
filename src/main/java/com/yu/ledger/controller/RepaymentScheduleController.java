package com.yu.ledger.controller;

import com.yu.ledger.service.IRepaymentScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;

@RestController
@RequestMapping("/repayments")
@Tag(name = "还款计划管理", description = "还款计划相关接口")
public class RepaymentScheduleController {
    @Autowired
    private IRepaymentScheduleService repaymentScheduleService;

    // 1. 获取还款计划列表
    @GetMapping("")
    @Operation(summary = "获取还款计划列表", description = "根据关键字、状态、日期范围等条件查询还款计划")
    public ResponseEntity<?> listRepayments(@RequestParam Map<String, Object> params) {
        return ResponseEntity.ok(repaymentScheduleService.listRepayments(params));
    }

    // 2. 标记还款
    @PostMapping("/{scheduleId}/pay")
    @Operation(summary = "标记还款", description = "标记指定还款计划为已还款状态")
    public ResponseEntity<?> payRepayment(
            @Parameter(description = "还款计划ID") @PathVariable Long scheduleId, 
            @RequestBody Map<String, Object> payInfo) {
        return ResponseEntity.ok(repaymentScheduleService.payRepayment(scheduleId, payInfo));
    }

    // 3. 发送还款提醒
    @PostMapping("/{scheduleId}/remind")
    @Operation(summary = "发送还款提醒", description = "为指定还款计划发送提醒")
    public ResponseEntity<?> remindRepayment(
            @Parameter(description = "还款计划ID") @PathVariable Long scheduleId) {
        return ResponseEntity.ok(repaymentScheduleService.remindRepayment(scheduleId));
    }

    // 4. 获取还款详情
    @GetMapping("/{scheduleId}")
    @Operation(summary = "获取还款详情", description = "获取指定还款计划的详细信息")
    public ResponseEntity<?> getRepaymentDetail(
            @Parameter(description = "还款计划ID") @PathVariable Long scheduleId) {
        return ResponseEntity.ok(repaymentScheduleService.getRepaymentDetail(scheduleId));
    }
}
