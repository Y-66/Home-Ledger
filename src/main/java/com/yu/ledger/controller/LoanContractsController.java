package com.yu.ledger.controller;

import com.yu.ledger.service.ILoanContractsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;

@RestController
@RequestMapping("/contracts")
@Tag(name = "合同管理", description = "合同相关接口")
public class LoanContractsController {
    @Autowired
    private ILoanContractsService loanContractsService;

    // 1. 获取合同列表
    @GetMapping("")
    @Operation(summary = "获取合同列表", description = "根据条件查询合同列表")
    public ResponseEntity<?> listContracts(@RequestParam Map<String, Object> params) {
        return ResponseEntity.ok(loanContractsService.listContracts(params));
    }

    // 2. 新增合同
    @PostMapping("")
    @Operation(summary = "新增合同", description = "创建新的借贷合同")
    public ResponseEntity<?> addContract(@RequestBody Map<String, Object> contract) {
        // contract中应包含contract_name字段
        return ResponseEntity.ok(loanContractsService.addContract(contract));
    }

    // 3. 编辑合同
    @PutMapping("/{contractId}")
    @Operation(summary = "编辑合同", description = "更新合同信息")
    public ResponseEntity<?> updateContract(
            @Parameter(description = "合同ID") @PathVariable String contractId, 
            @RequestBody Map<String, Object> contract) {
        // contract中应包含contract_name字段
        return ResponseEntity.ok(loanContractsService.updateContract(contractId, contract));
    }

    // 4. 关闭合同
    @PostMapping("/{contractId}/close")
    @Operation(summary = "关闭合同", description = "将合同状态设置为关闭")
    public ResponseEntity<?> closeContract(
            @Parameter(description = "合同ID") @PathVariable String contractId) {
        return ResponseEntity.ok(loanContractsService.closeContract(contractId));
    }

    // 5. 获取合同详情
    @GetMapping("/{contractId}/detail")
    @Operation(summary = "获取合同详情", description = "获取指定合同的详细信息，包括客户信息")
    public ResponseEntity<?> getContractDetail(
            @Parameter(description = "合同ID") @PathVariable String contractId) {
        return ResponseEntity.ok(loanContractsService.getContractDetail(contractId));
    }

    // 6. 获取合同还款计划
    @GetMapping("/{contractId}/schedules")
    @Operation(summary = "获取合同还款计划", description = "获取指定合同的所有还款计划")
    public ResponseEntity<?> getContractSchedules(
            @Parameter(description = "合同ID") @PathVariable String contractId) {
        return ResponseEntity.ok(loanContractsService.getContractSchedules(contractId));
    }

    // 7. 获取合同选项（根据客户）
    @GetMapping("/options")
    @Operation(summary = "获取合同选项", description = "获取合同选项列表，可选择性按客户筛选")
    public ResponseEntity<?> contractOptions(
            @Parameter(description = "客户ID（可选）") @RequestParam(required = false) Integer customerId) {
        return ResponseEntity.ok(loanContractsService.contractOptions(customerId));
    }
}
