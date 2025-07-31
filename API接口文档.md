# 银行借贷管理系统 API 接口文档

**基础地址**：`http://localhost:8080`

---

## 一、仪表盘相关

### 1. 获取仪表盘统计数据
- **接口**：`/dashboard/stats`
- **请求类型**：GET
- **请求参数**：无
- **返回示例**：
  ```json
  {
    "totalLoanAmount": 125600000,
    "totalLoanTrend": 12.5,
    "monthlyInterestIncome": 8200000,
    "monthlyInterestTrend": 8.3,
    "overdueContracts": 23,
    "overdueTrend": 2.1,
    "overdueAmount": 1800000,
    "overdueAmountTrend": 5.2
  }
  ```
- **字段说明**：
  - `totalLoanAmount` (number)：总借贷金额
  - `totalLoanTrend` (number)：总借贷金额环比增长（%）
  - `monthlyInterestIncome` (number)：本月利息收入
  - `monthlyInterestTrend` (number)：本月利息收入环比增长（%）
  - `overdueContracts` (number)：逾期合同数
  - `overdueTrend` (number)：逾期合同数环比增长（%）
  - `overdueAmount` (number)：逾期金额
  - `overdueAmountTrend` (number)：逾期金额环比增长（%）

---

### 2. 获取今日到期提醒
- **接口**：`/dashboard/today-reminders`
- **请求类型**：GET
- **请求参数**：无
- **返回示例**：
  ```json
  [
    {
      "id": 1,
      "customerName": "张三",
      "contractId": "CT001",
      "amount": "15000",
      "status": "due" // 或 overdue
    }
  ]
  ```
- **字段说明**：
  - `id` (number)：提醒ID
  - `customerName` (string)：客户姓名
  - `contractId` (string)：合同号
  - `amount` (string/number)：应还金额
  - `status` (string)：状态（due=今日到期，overdue=逾期）

---

### 3. 获取最近活动
- **接口**：`/dashboard/recent-activities`
- **请求类型**：GET
- **请求参数**：无
- **返回示例**：
  ```json
  [
    {
      "id": 1,
      "type": "payment", // payment/overdue/contract
      "title": "张三完成利息支付 ¥1,500",
      "time": "2小时前"
    }
  ]
  ```
- **字段说明**：
  - `id` (number)：活动ID
  - `type` (string)：类型（payment=还款，overdue=逾期，contract=合同）
  - `title` (string)：活动描述
  - `time` (string)：时间描述

---

## 二、借款合同相关

### 1. 获取合同列表
- **接口**：`/contracts`
- **请求类型**：GET
- **Query参数**：
  | 参数名      | 类型   | 必填 | 说明           |
  | ----------- | ------ | ---- | -------------- |
  | keyword     | string | 否   | 关键字（合同号/客户名） |
  | status      | string | 否   | 合同状态（active/closed/default） |
  | frequency   | string | 否   | 还款频率（monthly/quarterly/annually） |
- **返回示例**：
  ```json
  [
    {
      "contract_id": "CT001",
      "customer_name": "张三",
      "loan_amount": 100000,
      "annual_interest_rate": 5.5,
      "start_date": "2024-01-01",
      "end_date": "2025-01-01",
      "interest_type": "fixed",
      "repayment_frequency": "monthly",
      "contract_status": "active",
      "principal_paid": 0,
      "created_at": "2024-01-01 10:00:00"
    }
  ]
  ```
- **字段说明**：
  - `contract_id` (string)：合同号
  - `customer_name` (string)：客户姓名
  - `loan_amount` (number)：借贷金额
  - `annual_interest_rate` (number)：年利率
  - `start_date` (string)：开始日期
  - `end_date` (string)：结束日期
  - `interest_type` (string)：利息类型（fixed/variable）
  - `repayment_frequency` (string)：还款频率
  - `contract_status` (string)：合同状态
  - `principal_paid` (number)：已还本金
  - `created_at` (string)：创建时间

---

### 2. 新增合同
- **接口**：`/contracts`
- **请求类型**：POST
- **Body参数**（JSON）：
  | 参数名               | 类型    | 必填 | 说明         |
  | -------------------- | ------- | ---- | ------------ |
  | customer_id          | number  | 是   | 客户ID       |
  | loan_amount          | number  | 是   | 借贷金额     |
  | annual_interest_rate | number  | 是   | 年利率       |
  | start_date           | string  | 是   | 开始日期     |
  | end_date             | string  | 是   | 结束日期     |
  | interest_type        | string  | 是   | 利息类型     |
  | repayment_frequency  | string  | 是   | 还款频率     |
  | contract_status      | string  | 否   | 合同状态     |
- **返回**：新增合同对象

---

### 3. 编辑合同
- **接口**：`/contracts/{contractId}`
- **请求类型**：PUT
- **路径参数**：
  - `contractId` (string)：合同号
- **Body参数**：同上
- **返回**：更新后的合同对象

---

### 4. 关闭合同
- **接口**：`/contracts/{contractId}/close`
- **请求类型**：POST
- **路径参数**：
  - `contractId` (string)：合同号
- **返回**：操作结果

---

### 5. 获取客户选项
- **接口**：`/customers/options`
- **请求类型**：GET
- **返回示例**：
  ```json
  [
    { "customer_id": 1, "full_name": "张三" }
  ]
  ```
- **字段说明**：
  - `customer_id` (number)：客户ID
  - `full_name` (string)：客户姓名

---

### 6. 获取合同选项（根据客户）
- **接口**：`/contracts/options`
- **请求类型**：GET
- **Query参数**：
  - `customerId` (number)：客户ID
- **返回示例**：
  ```json
  [
    { "contract_id": "CT001", "loan_amount": 100000 }
  ]
  ```
- **字段说明**：
  - `contract_id` (string)：合同号
  - `loan_amount` (number)：借贷金额

---

## 三、还款计划相关

### 1. 获取还款计划列表
- **接口**：`/repayments`
- **请求类型**：GET
- **Query参数**：
  | 参数名    | 类型   | 必填 | 说明           |
  | --------- | ------ | ---- | -------------- |
  | keyword   | string | 否   | 关键字         |
  | status    | string | 否   | 状态（pending/paid/overdue） |
  | dateRange | array  | 否   | [开始日期, 结束日期] |
- **返回示例**：
  ```json
  [
    {
      "schedule_id": 1,
      "contract_id": "CT001",
      "customer_name": "张三",
      "period_start": "2024-01-01",
      "period_end": "2024-01-31",
      "due_date": "2024-02-01",
      "calculated_interest": 458.33,
      "period_status": "pending",
      "payment_date": null,
      "last_reminder_date": null
    }
  ]
  ```
- **字段说明**：
  - `schedule_id` (number)：还款计划ID
  - `contract_id` (string)：合同号
  - `customer_name` (string)：客户姓名
  - `period_start` (string)：计息开始
  - `period_end` (string)：计息结束
  - `due_date` (string)：到期日期
  - `calculated_interest` (number)：应付利息
  - `period_status` (string)：状态
  - `payment_date` (string/null)：还款日期
  - `last_reminder_date` (string/null)：最后提醒日期

---

### 2. 标记还款
- **接口**：`/repayments/{scheduleId}/pay`
- **请求类型**：POST
- **路径参数**：
  - `scheduleId` (number)：还款计划ID
- **Body参数**（JSON）：
  | 参数名        | 类型   | 必填 | 说明     |
  | ------------- | ------ | ---- | -------- |
  | payment_date  | string | 是   | 还款日期 |
  | payment_amount| number | 是   | 还款金额 |
  | remark        | string | 否   | 备注     |
- **返回**：操作结果

---

### 3. 发送还款提醒
- **接口**：`/repayments/{scheduleId}/remind`
- **请求类型**：POST
- **路径参数**：
  - `scheduleId` (number)：还款计划ID
- **返回**：操作结果

---

### 4. 获取还款详情
- **接口**：`/repayments/{scheduleId}`
- **请求类型**：GET
- **路径参数**：
  - `scheduleId` (number)：还款计划ID
- **返回**：还款计划对象

---

## 四、提醒管理相关

### 1. 获取提醒日志列表
- **接口**：`/reminders`
- **请求类型**：GET
- **Query参数**：
  | 参数名   | 类型   | 必填 | 说明           |
  | -------- | ------ | ---- | -------------- |
  | keyword  | string | 否   | 关键字         |
  | type     | string | 否   | 提醒类型（initial/follow_up/overdue） |
  | channel  | string | 否   | 渠道（sms/email/app/letter） |
  | status   | string | 否   | 状态（success/failure） |
- **返回示例**：
  ```json
  [
    {
      "reminder_id": 1,
      "customer_name": "张三",
      "contract_id": "CT001",
      "reminder_type": "initial",
      "reminder_channel": "sms",
      "recipient": "13800138001",
      "sent_at": "2024-01-15 10:30:00",
      "is_success": true,
      "failure_reason": null
    }
  ]
  ```
- **字段说明**：
  - `reminder_id` (number)：提醒ID
  - `customer_name` (string)：客户姓名
  - `contract_id` (string)：合同号
  - `reminder_type` (string)：提醒类型
  - `reminder_channel` (string)：提醒渠道
  - `recipient` (string)：接收人
  - `sent_at` (string)：发送时间
  - `is_success` (boolean)：是否成功
  - `failure_reason` (string/null)：失败原因

---

### 2. 发送提醒
- **接口**：`/reminders`
- **请求类型**：POST
- **Body参数**（JSON）：
  | 参数名        | 类型     | 必填 | 说明         |
  | ------------- | -------- | ---- | ------------ |
  | customer_id   | number   | 是   | 客户ID       |
  | contract_id   | string   | 是   | 合同号       |
  | reminder_type | string   | 是   | 提醒类型     |
  | channels      | string[] | 是   | 渠道数组     |
  | content       | string   | 是   | 提醒内容     |
- **返回**：操作结果

---

### 3. 重新发送提醒
- **接口**：`/reminders/{reminderId}/resend`
- **请求类型**：POST
- **路径参数**：
  - `reminderId` (number)：提醒ID
- **返回**：操作结果

---

### 4. 获取提醒详情
- **接口**：`/reminders/{reminderId}`
- **请求类型**：GET
- **路径参数**：
  - `reminderId` (number)：提醒ID
- **返回**：提醒详情对象

---

> 如需分页、排序、导出、批量操作等功能，可在参数中扩展。
> 如需更详细的字段类型、校验规则、错误码说明等，也可随时补充！
> 后续如有新接口需求，请继续告知，我会帮你详细补充文档。 