package com.yu.ledger.service.impl;

import com.yu.ledger.service.ICustomersService;
import com.yu.ledger.entity.po.Customers;
import com.yu.ledger.mapper.CustomersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class CustomersServiceImpl implements ICustomersService {
    @Autowired
    private CustomersMapper customersMapper;

    @Override
    public List<Map<String, Object>> customerOptions() {
        List<Customers> list = customersMapper.selectList(null);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Customers c : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("customer_id", c.getCustomerId());
            map.put("full_name", c.getFullName());
            result.add(map);
        }
        return result;
    }
}
