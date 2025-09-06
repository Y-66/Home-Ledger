<template>
  <div class="reminders">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>🔔 智能提醒系统</h1>
        <p>自动化提醒管理，确保客户按时还款</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" size="large" @click="handleSendReminder">
          <el-icon><Bell /></el-icon>
          发送提醒
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card success">
        <div class="stat-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.success }}</div>
          <div class="stat-label">发送成功</div>
        </div>
      </div>
      <div class="stat-card warning">
        <div class="stat-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.pending }}</div>
          <div class="stat-label">待发送</div>
        </div>
      </div>
      <div class="stat-card danger">
        <div class="stat-icon">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.failed }}</div>
          <div class="stat-label">发送失败</div>
        </div>
      </div>
      <div class="stat-card info">
        <div class="stat-icon">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-number">{{ stats.total }}</div>
          <div class="stat-label">总提醒数</div>
        </div>
      </div>
    </div>

    <!-- 筛选器 -->
    <div class="filter-section">
      <div class="filter-group">
        <el-input
          v-model="searchForm.keyword"
          placeholder="🔍 搜索客户姓名/合同号/合同名称"
          clearable
          @keyup.enter="handleSearch"
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-select v-model="searchForm.type" placeholder="提醒类型" clearable class="filter-select">
          <el-option label="📧 初始提醒" value="initial" />
          <el-option label="📞 跟进提醒" value="follow_up" />
          <el-option label="⚠️ 逾期提醒" value="overdue" />
        </el-select>
        
        <el-select v-model="searchForm.channel" placeholder="提醒渠道" clearable class="filter-select">
          <el-option label="📱 短信" value="sms" />
          <el-option label="📧 邮件" value="email" />
          <el-option label="📱 应用内" value="app" />
          <el-option label="📄 信函" value="letter" />
        </el-select>
        
        <el-select v-model="searchForm.status" placeholder="发送状态" clearable class="filter-select">
          <el-option label="✅ 成功" value="success" />
          <el-option label="⏱ 待发送" value="pending" />
          <el-option label="❌ 失败" value="failure" />
        </el-select>
        
        <el-button type="primary" @click="handleSearch" class="search-btn">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
    </div>

    <!-- 提醒日志聊天界面 -->
    <div class="chat-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
        <el-skeleton :rows="3" animated />
        <el-skeleton :rows="3" animated />
      </div>
      
      <!-- 空数据提示 -->
      <div v-else-if="reminderList.length === 0" class="empty-container">
        <el-empty description="暂无提醒记录" />
      </div>
      
      <!-- 提醒列表 -->
      <div v-else v-for="reminder in reminderList" :key="reminder.reminder_id" class="chat-item" :class="reminder.status === 'success' ? 'success' : 'failed'">
        <div class="chat-avatar">
          <div class="avatar-icon" :class="reminder.status === 'success' ? 'success' : 'failed'">
            <el-icon>
              <CircleCheck v-if="reminder.status === 'success'" />
              <Warning v-else />
            </el-icon>
          </div>
        </div>
        
        <div class="chat-content">
          <div class="chat-header">
            <div class="chat-info">
              <div class="customer-name">{{ reminder.customer_name }}</div>
              <div class="reminder-details">
                <span class="contract-id">{{ reminder.contract_id }}</span>
                <span class="contract-name" v-if="reminder.contract_name">{{ reminder.contract_name }}</span>
                <span class="reminder-type">{{ getTypeText(reminder.reminder_type) }}</span>
                <span class="reminder-channel">{{ getChannelText(reminder.reminder_channel) }}</span>
              </div>
            </div>
            <div class="chat-time">{{ reminder.sent_at }}</div>
          </div>
          
          <div class="chat-message">
            <div class="message-content">
              <div class="message-text">
                <strong>{{ reminder.customer_name }}</strong> 的还款提醒已通过 
                <strong>{{ getChannelText(reminder.reminder_channel) }}</strong> 发送至 
                <strong>{{ reminder.recipient }}</strong>
              </div>
              <div class="message-status">
                <div class="status-indicator" :class="getStatusClass(reminder.status)">
                  <div class="status-dot"></div>
                  <span class="status-label">{{ getStatusText(reminder.status) }}</span>
                </div>
                <span v-if="reminder.status === 'failed'" class="failure-reason">
                  失败原因: {{ reminder.failure_reason }}
                </span>
              </div>
            </div>
          </div>
          
          <div class="chat-actions">
            <el-button size="small" type="primary" @click="handleResend(reminder)">
              <el-icon><Refresh /></el-icon>
              重新发送
            </el-button>
            <el-button size="small" @click="handleViewDetails(reminder)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(reminder)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 发送提醒对话框 -->
    <el-dialog
      v-model="sendDialogVisible"
      title="发送提醒"
      width="600px"
      class="send-dialog"
    >
      <el-form
        ref="sendFormRef"
        :model="sendForm"
        :rules="sendRules"
        label-width="100px"
      >
        <el-form-item label="选择客户" prop="customer_id">
          <el-select
            v-model="sendForm.customer_id"
            placeholder="请选择客户"
            style="width: 100%"
            filterable
            @change="handleCustomerChange"
          >
            <el-option
              v-for="customer in customerOptions"
              :key="customer.customer_id"
              :label="customer.full_name"
              :value="customer.customer_id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择合同" prop="contract_id">
          <el-select
            v-model="sendForm.contract_id"
            placeholder="请选择合同"
            style="width: 100%"
            :disabled="!sendForm.customer_id"
          >
            <el-option
              v-for="contract in contractOptions"
              :key="contract.contract_id"
              :label="`${contract.contract_id} - ¥${formatMoney(contract.loan_amount)}`"
              :value="contract.contract_id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒类型" prop="reminder_type">
          <el-select v-model="sendForm.reminder_type" style="width: 100%">
            <el-option label="初始提醒" value="initial" />
            <el-option label="跟进提醒" value="follow_up" />
            <el-option label="逾期提醒" value="overdue" />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒渠道" prop="channels">
          <el-checkbox-group v-model="sendForm.channels">
            <el-checkbox label="sms">📱 短信</el-checkbox>
            <el-checkbox label="email">📧 邮件</el-checkbox>
            <el-checkbox label="app">📱 应用内</el-checkbox>
            <el-checkbox label="letter">📄 信函</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="提醒内容" prop="content">
          <el-input
            v-model="sendForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入提醒内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="sendDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSendSubmit">发送</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 提醒详情对话框 -->
    <el-dialog
      v-model="detailsDialogVisible"
      title="提醒详情"
      width="600px"
      class="details-dialog"
    >
      <div v-if="selectedReminder" class="reminder-details-modal">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="提醒ID">{{ selectedReminder.reminder_id }}</el-descriptions-item>
          <el-descriptions-item label="客户姓名">{{ selectedReminder.customer_name || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="合同号">{{ selectedReminder.contract_id || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="合同名称">{{ selectedReminder.contract_name || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="提醒类型">
            <el-tag :type="getTypeColor(selectedReminder.reminder_type)">
              {{ getTypeText(selectedReminder.reminder_type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="提醒渠道">
            <el-tag :type="getChannelColor(selectedReminder.reminder_channel)">
              {{ getChannelText(selectedReminder.reminder_channel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="接收人">{{ selectedReminder.recipient }}</el-descriptions-item>
          <el-descriptions-item label="发送时间">{{ selectedReminder.sent_at }}</el-descriptions-item>
          <el-descriptions-item label="发送状态">
            <div class="status-indicator" :class="getStatusClass(selectedReminder.status)">
              <div class="status-dot"></div>
              <span class="status-label">{{ getStatusText(selectedReminder.status) }}</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="失败原因" v-if="selectedReminder.status === 'failed'">
            {{ selectedReminder.failure_reason || '无' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { remindersAPI } from '../service/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const sendDialogVisible = ref(false)
const detailsDialogVisible = ref(false)
const selectedReminder = ref(null)
const sendFormRef = ref()

const searchForm = reactive({
  keyword: '',
  type: '',
  channel: '',
  status: ''
})

const sendForm = reactive({
  customer_id: null,
  contract_id: null,
  reminder_type: 'initial',
  channels: ['sms'],
  content: ''
})

const sendRules = {
  customer_id: [
    { required: true, message: '请选择客户', trigger: 'change' }
  ],
  contract_id: [
    { required: true, message: '请选择合同', trigger: 'change' }
  ],
  reminder_type: [
    { required: true, message: '请选择提醒类型', trigger: 'change' }
  ],
  channels: [
    { required: true, message: '请选择提醒渠道', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入提醒内容', trigger: 'blur' }
  ]
}

const customerOptions = ref([])
const contractOptions = ref([])
const reminderList = ref([])

const stats = computed(() => {
  const success = reminderList.value.filter(item => item.status === 'success').length
  const pending = reminderList.value.filter(item => item.status === 'pending').length
  const failed = reminderList.value.filter(item => item.status === 'failed').length
  const total = reminderList.value.length
  return { success, failed, total, pending }
})

const formatMoney = (amount) => {
  return new Intl.NumberFormat('zh-CN').format(amount)
}

const getTypeColor = (type) => {
  const colorMap = {
    initial: 'primary',
    follow_up: 'warning',
    overdue: 'danger'
  }
  return colorMap[type] || 'info'
}

const getTypeText = (type) => {
  const textMap = {
    initial: '初始提醒',
    follow_up: '跟进提醒',
    overdue: '逾期提醒'
  }
  return textMap[type] || type
}

const getChannelColor = (channel) => {
  const colorMap = {
    sms: 'success',
    email: 'primary',
    app: 'warning',
    letter: 'info'
  }
  return colorMap[channel] || 'info'
}

const getChannelText = (channel) => {
  const textMap = {
    sms: '短信',
    email: '邮件',
    app: '应用内',
    letter: '信函'
  }
  return textMap[channel] || channel
}

const getStatusClass = (status) => {
  if (status === 'success') {
    return 'sent'
  } else if (status === 'failed') {
    return 'failed'
  } else {
    return 'pending'
  }
}

const getStatusText = (status) => {
  if (status === 'success') {
    return '已发送'
  } else if (status === 'failed') {
    return '发送失败'
  } else {
    return '待发送'
  }
}

const handleSearch = async () => {
  try {
    loading.value = true
    console.log('搜索参数:', searchForm)
    const data = await remindersAPI.getReminders(searchForm)
    console.log('获取到的提醒数据:', data)
    reminderList.value = data || []
    loading.value = false
  } catch (error) {
    console.error('获取提醒数据失败:', error)
    ElMessage.error('获取提醒数据失败')
    reminderList.value = []
    loading.value = false
  }
}

const handleSendReminder = () => {
  sendDialogVisible.value = true
  resetSendForm()
}

const handleCustomerChange = async (customerId) => {
  sendForm.contract_id = null
  contractOptions.value = await remindersAPI.getContractOptions(customerId)
}

const handleResend = async (row) => {
  await ElMessageBox.confirm(
    `确定要重新发送提醒给 ${row.customer_name} 吗？`,
    '重新发送',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
  await remindersAPI.resendReminder(row.reminder_id)
  ElMessage.success('提醒重新发送成功')
  handleSearch()
}

const handleViewDetails = async (row) => {
  selectedReminder.value = await remindersAPI.getReminderDetail(row.reminder_id)
  detailsDialogVisible.value = true
}

const handleSendSubmit = async () => {
  try {
    await sendFormRef.value.validate()
    await remindersAPI.sendReminder(sendForm)
    ElMessage.success('提醒发送成功')
    sendDialogVisible.value = false
    handleSearch()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 ${row.customer_name} 的提醒记录吗？此操作不可逆。`,
      '删除提醒',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    await remindersAPI.deleteReminder(row.reminder_id)
    ElMessage.success('提醒记录删除成功')
    handleSearch() // 刷新列表
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除提醒失败:', error)
      ElMessage.error('删除提醒失败')
    }
  }
}

const resetSendForm = () => {
  Object.assign(sendForm, {
    customer_id: null,
    contract_id: null,
    reminder_type: 'initial',
    channels: ['sms'],
    content: ''
  })
  contractOptions.value = []
  sendFormRef.value?.resetFields()
}

onMounted(async () => {
  try {
    // 获取客户选项
    console.log('正在获取客户选项...')
    customerOptions.value = await remindersAPI.getCustomerOptions()
    console.log('客户选项:', customerOptions.value)
    
    // 获取提醒数据
    console.log('正在获取提醒数据...')
    await handleSearch()
  } catch (error) {
    console.error('初始化失败:', error)
    ElMessage.error('页面初始化失败')
  }
})
</script>

<style scoped>
.reminders {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 30px;
  margin-bottom: 30px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.3);
}

.header-content h1 {
  font-size: 28px;
  margin-bottom: 10px;
  font-weight: 600;
}

.header-content p {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 25px;
  display: flex;
  align-items: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.stat-card.success { border-left: 4px solid #67C23A; }
.stat-card.warning { border-left: 4px solid #E6A23C; }
.stat-card.danger { border-left: 4px solid #F56C6C; }
.stat-card.info { border-left: 4px solid #409EFF; }

.stat-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  font-size: 24px;
  color: white;
}

.stat-card.success .stat-icon { background: linear-gradient(135deg, #67C23A, #85ce61); }
.stat-card.warning .stat-icon { background: linear-gradient(135deg, #E6A23C, #ebb563); }
.stat-card.danger .stat-icon { background: linear-gradient(135deg, #F56C6C, #f78989); }
.stat-card.info .stat-icon { background: linear-gradient(135deg, #409EFF, #36a3f7); }

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #7f8c8d;
}

.filter-section {
  background: white;
  border-radius: 16px;
  padding: 25px;
  margin-bottom: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.filter-group {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: nowrap;
}

.search-input {
  flex: 1;
  min-width: 250px;
  max-width: 280px;
}

.filter-select {
  min-width: 100px;
  max-width: 120px;
  flex-shrink: 0;
}

.search-btn {
  border-radius: 12px;
  padding: 10px 20px;
  flex-shrink: 0;
  white-space: nowrap;
}

.chat-container {
  background: white;
  border-radius: 16px;
  padding: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.loading-container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.empty-container {
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px; /* Adjust as needed */
}

.chat-item {
  display: flex;
  margin-bottom: 25px;
  padding: 20px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.chat-item:hover {
  background: #f8f9fa;
}

.chat-item.success {
  border-left: 4px solid #67C23A;
}

.chat-item.failed {
  border-left: 4px solid #F56C6C;
}

.chat-avatar {
  margin-right: 20px;
}

.avatar-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.avatar-icon.success {
  background: linear-gradient(135deg, #67C23A, #85ce61);
}

.avatar-icon.failed {
  background: linear-gradient(135deg, #F56C6C, #f78989);
}

.chat-content {
  flex: 1;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.customer-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 5px;
}

.reminder-details {
  font-size: 12px;
  color: #7f8c8d;
}

.reminder-details span {
  margin-right: 15px;
}

.contract-name {
  color: #409EFF;
  font-weight: 500;
}

.chat-time {
  font-size: 12px;
  color: #7f8c8d;
}

.chat-message {
  margin-bottom: 15px;
}

.message-content {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 15px;
}

.message-text {
  font-size: 14px;
  color: #2c3e50;
  margin-bottom: 10px;
  line-height: 1.5;
}

.message-status {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-indicator {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid;
  transition: all 0.2s ease;
}

.status-indicator.sent {
  background-color: #f0f9ff;
  border-color: #67C23A;
  color: #67C23A;
}

.status-indicator.pending {
  background-color: #fff7ed;
  border-color: #E6A23C;
  color: #E6A23C;
}

.status-indicator.failed {
  background-color: #ffebee;
  border-color: #F56C6C;
  color: #F56C6C;
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  flex-shrink: 0;
}

.status-indicator.sent .status-dot {
  background-color: #67C23A;
}

.status-indicator.pending .status-dot {
  background-color: #E6A23C;
}

.status-indicator.failed .status-dot {
  background-color: #F56C6C;
}

.status-label {
  font-size: 13px;
  font-weight: 500;
}

.failure-reason {
  font-size: 12px;
  color: #F56C6C;
}

.chat-actions {
  display: flex;
  gap: 10px;
}

.chat-actions .el-button {
  border-radius: 8px;
  font-size: 12px;
}

.chat-actions .el-button--danger {
  border-color: #f56c6c !important;
  color: #f56c6c !important;
  background-color: transparent !important;
}

.chat-actions .el-button--danger:hover {
  background-color: #f56c6c !important;
  border-color: #f56c6c !important;
  color: white !important;
}

.chat-actions .el-button--danger:not(:hover) {
  color: #f56c6c !important;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.reminder-details-modal {
  padding: 20px 0;
}
</style> 