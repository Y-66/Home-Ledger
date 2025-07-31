package com.yu.ledger.service;

import java.util.List;
import java.util.Map;

public interface IDashboardService {
    Map<String, Object> getStats();
    List<Map<String, Object>> getTodayReminders();
    List<Map<String, Object>> getRecentActivities();
} 