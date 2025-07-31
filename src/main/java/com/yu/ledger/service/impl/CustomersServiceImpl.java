package com.yu.ledger.service.impl;

import com.yu.ledger.service.ICustomersService;
import com.yu.ledger.entity.po.Customers;
import com.yu.ledger.mapper.CustomersMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomersServiceImpl implements ICustomersService {
    private final CustomersMapper customersMapper;

    @Override
    public List<Map<String, Object>> customerOptions() {
        return customersMapper.selectList(null).stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("customer_id", c.getCustomerId());
            map.put("full_name", c.getFullName());
            return map;
        }).collect(Collectors.toList());
    }

}
