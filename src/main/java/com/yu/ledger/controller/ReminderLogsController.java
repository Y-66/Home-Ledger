package com.yu.ledger.controller;

import com.yu.ledger.service.IReminderLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@RestController
@RequestMapping("/reminders")
public class ReminderLogsController {
    @Autowired
    private IReminderLogsService reminderLogsService;

    // 1. 获取提醒日志列表
    @GetMapping("")
    public ResponseEntity<?> listReminders(@RequestParam Map<String, Object> params) {
        return ResponseEntity.ok(reminderLogsService.listReminders(params));
    }

    // 2. 发送提醒
    @PostMapping("")
    public ResponseEntity<?> sendReminder(@RequestBody Map<String, Object> reminder) {
        return ResponseEntity.ok(reminderLogsService.sendReminder(reminder));
    }

    // 3. 重新发送提醒
    @PostMapping("/{reminderId}/resend")
    public ResponseEntity<?> resendReminder(@PathVariable Long reminderId) {
        return ResponseEntity.ok(reminderLogsService.resendReminder(reminderId));
    }

    // 4. 获取提醒详情
    @GetMapping("/{reminderId}")
    public ResponseEntity<?> getReminderDetail(@PathVariable Long reminderId) {
        return ResponseEntity.ok(reminderLogsService.getReminderDetail(reminderId));
    }

    // 5. 删除提醒记录
    @DeleteMapping("/{reminderId}")
    public ResponseEntity<?> deleteReminder(@PathVariable Long reminderId) {
        return ResponseEntity.ok(reminderLogsService.deleteReminder(reminderId));
    }
}
