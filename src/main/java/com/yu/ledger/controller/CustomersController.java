package com.yu.ledger.controller;

import com.yu.ledger.service.ICustomersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yu
 * @since 2025-07-27
 */
@RestController
@RequestMapping("/customers")
public class CustomersController {
    @Autowired
    private ICustomersService customersService;

    // 5. 获取客户选项
    @GetMapping("/options")
    public ResponseEntity<?> customerOptions() {
        return ResponseEntity.ok(customersService.customerOptions());
    }
}
