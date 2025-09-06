import axios from 'axios'

const BASE_URL = 'http://localhost:8080'

// 仪表盘相关接口
export const dashboardAPI = {
  // 获取仪表盘统计数据
  async getStats() {
    const { data } = await axios.get(`${BASE_URL}/dashboard/stats`)
    return data
  },
  // 获取今日到期提醒
  async getTodayReminders() {
    const { data } = await axios.get(`${BASE_URL}/dashboard/today-reminders`)
    return data
  },
  // 获取最近活动
  async getRecentActivities() {
    const { data } = await axios.get(`${BASE_URL}/dashboard/recent-activities`)
    return data
  }
}

// 借款合同相关接口
export const contractsAPI = {
  // 获取合同列表
  async getContracts(params) {
    const { data } = await axios.get(`${BASE_URL}/contracts`, { params })
    return data
  },
  // 新增合同
  async addContract(contract) {
    const { data } = await axios.post(`${BASE_URL}/contracts`, contract)
    return data
  },
  // 编辑合同
  async updateContract(contractId, contract) {
    const { data } = await axios.put(`${BASE_URL}/contracts/${contractId}`, contract)
    return data
  },
  // 关闭合同
  async closeContract(contractId) {
    const { data } = await axios.post(`${BASE_URL}/contracts/${contractId}/close`)
    return data
  },
  // 获取客户选项
  async getCustomerOptions() {
    const { data } = await axios.get(`${BASE_URL}/customers/options`)
    return data
  },
  // 获取合同详情
  async getContractDetail(contractId) {
    const { data } = await axios.get(`${BASE_URL}/contracts/${contractId}/detail`)
    return data
  },
  // 获取合同还款计划
  async getRepaymentSchedules(contractId) {
    const { data } = await axios.get(`${BASE_URL}/contracts/${contractId}/schedules`)
    return data
  }
}

// 还款计划相关接口
export const repaymentsAPI = {
  // 获取还款计划列表
  // 注意：前端会将 dateRange 数组转换为 startDate 和 endDate 参数
  async getRepayments(params) {
    const { data } = await axios.get(`${BASE_URL}/repayments`, { params })
    return data
  },
  // 标记还款
  async markPaid(scheduleId, payment) {
    const { data } = await axios.post(`${BASE_URL}/repayments/${scheduleId}/pay`, payment)
    return data
  },
  // 发送还款提醒
  async sendReminder(scheduleId) {
    const { data } = await axios.post(`${BASE_URL}/repayments/${scheduleId}/remind`)
    return data
  },
  // 获取还款详情
  async getRepaymentDetail(scheduleId) {
    const { data } = await axios.get(`${BASE_URL}/repayments/${scheduleId}`)
    return data
  }
}

// 提醒管理相关接口
export const remindersAPI = {
  // 获取提醒日志列表
  async getReminders(params) {
    const { data } = await axios.get(`${BASE_URL}/reminders`, { params })
    return data
  },
  // 发送提醒
  async sendReminder(reminder) {
    const { data } = await axios.post(`${BASE_URL}/reminders`, reminder)
    return data
  },
  // 重新发送提醒
  async resendReminder(reminderId) {
    const { data } = await axios.post(`${BASE_URL}/reminders/${reminderId}/resend`)
    return data
  },
  // 删除提醒记录
  async deleteReminder(reminderId) {
    const { data } = await axios.delete(`${BASE_URL}/reminders/${reminderId}`)
    return data
  },
  // 获取提醒详情
  async getReminderDetail(reminderId) {
    const { data } = await axios.get(`${BASE_URL}/reminders/${reminderId}`)
    return data
  },
  // 获取客户选项
  async getCustomerOptions() {
    const { data } = await axios.get(`${BASE_URL}/customers/options`)
    return data
  },
  // 获取合同选项（根据客户）
  async getContractOptions(customerId) {
    const { data } = await axios.get(`${BASE_URL}/contracts/options`, { params: { customerId } })
    return data
  }
} 