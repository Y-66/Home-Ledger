package com.yu.ledger.service;

import java.util.List;
import java.util.Map;

public interface IReminderLogsService {
    List<Map<String, Object>> listReminders(Map<String, Object> params);
    Map<String, Object> sendReminder(Map<String, Object> reminder);
    Map<String, Object> resendReminder(Long reminderId);
    Map<String, Object> getReminderDetail(Long reminderId);

    /**
     * 删除提醒记录
     */
    Map<String, Object> deleteReminder(Long reminderId);
}
