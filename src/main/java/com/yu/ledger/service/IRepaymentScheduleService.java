package com.yu.ledger.service;

import java.util.List;
import java.util.Map;

public interface IRepaymentScheduleService {
    List<Map<String, Object>> listRepayments(Map<String, Object> params);
    Map<String, Object> payRepayment(Long scheduleId, Map<String, Object> payInfo);
    Map<String, Object> remindRepayment(Long scheduleId);
    Map<String, Object> getRepaymentDetail(Long scheduleId);
}
