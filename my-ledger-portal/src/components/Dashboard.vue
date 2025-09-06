<template>
  <div class="dashboard">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h1>👋 欢迎回来，管理员！</h1>
        <p>今天是 {{ currentDate }}，让我们来看看今天的业务情况</p>
      </div>
      <div class="welcome-illustration">
        <div class="floating-card card-1">📈</div>
        <div class="floating-card card-2">💰</div>
        <div class="floating-card card-3">📊</div>
      </div>
    </div>

    <!-- 关键指标卡片 -->
    <div class="metrics-grid">
      <div class="metric-card primary" @click="handleMetricClick('total')">
        <div class="metric-icon">
          <el-icon><Money /></el-icon>
        </div>
        <div class="metric-content">
          <div class="metric-value">¥{{ stats.totalLoanAmount || 'N/A' }}</div>
          <div class="metric-label">总借贷金额</div>
          <div class="metric-trend positive">+{{ stats.totalLoanTrend || '0' }}% ↗</div>
        </div>
        <div class="metric-bg"></div>
      </div>

      <div class="metric-card success" @click="handleMetricClick('income')">
        <div class="metric-icon">
          <el-icon><TrendCharts /></el-icon>
        </div>
        <div class="metric-content">
          <div class="metric-value">¥{{ stats.monthlyInterestIncome || 'N/A' }}</div>
          <div class="metric-label">累计利息收入</div>
          <div class="metric-trend positive">+{{ stats.monthlyInterestTrend || '0' }}% ↗</div>
        </div>
        <div class="metric-bg"></div>
      </div>

      <div class="metric-card warning" @click="handleMetricClick('overdue')">
        <div class="metric-icon">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="metric-content">
          <div class="metric-value">{{ stats.overdueContracts || 'N/A' }}</div>
          <div class="metric-label">逾期合同数</div>
          <div class="metric-trend negative">+{{ stats.overdueTrend || '0' }}% ↗</div>
        </div>
        <div class="metric-bg"></div>
      </div>

      <div class="metric-card danger" @click="handleMetricClick('amount')">
        <div class="metric-icon">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="metric-content">
          <div class="metric-value">¥{{ stats.overdueAmount || 'N/A' }}</div>
          <div class="metric-label">逾期金额</div>
          <div class="metric-trend negative">+{{ stats.overdueAmountTrend || '0' }}% ↗</div>
        </div>
        <div class="metric-bg"></div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="content-grid">
      <!-- 今日到期提醒 -->
      <div class="content-card due-reminders">
        <div class="card-header">
          <h3>⏰ 近期到期提醒</h3>
          <el-button type="primary" size="small" round>查看全部</el-button>
        </div>
        <div class="reminder-list">
          <div v-for="reminder in todayReminders" :key="reminder.id" class="reminder-item" @click="handleReminderClick(reminder)">
            <div class="reminder-avatar" :class="reminder.status">
              {{ reminder.customerName.charAt(0) }}
            </div>
            <div class="reminder-info">
              <div class="reminder-name">{{ reminder.customerName }}</div>
              <div class="reminder-details">
                <span class="amount">¥{{ formatMoney(reminder.amount) }}</span>
              </div>
            </div>
            <div class="reminder-status">
              <el-tag :type="reminder.status === 'overdue' ? 'danger' : 'warning'" size="large" >
                {{ reminder.status === 'overdue' ? '逾期' : reminder.remaining_date + '天内到期' }}
              </el-tag>

            </div>
          </div>
        </div>
      </div>

      <!-- 最近活动 -->
      <div class="content-card recent-activities">
        <div class="card-header">
          <h3>📋 最近活动</h3>
        </div>
        <div class="activity-timeline">
          <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
            <div class="activity-icon" :class="activity.type">
              <el-icon>
                <CircleCheck v-if="activity.type === 'payment'" />
                <Warning v-else-if="activity.type === 'overdue'" />
                <Document v-else />
              </el-icon>
            </div>
            <div class="activity-content">
              <div class="activity-title">{{ activity.title }}</div>
              <div class="activity-time">{{ activity.time }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快速操作 -->
    <div class="quick-actions">
      <el-button type="primary" size="large" @click="handleQuickAction('new-contract')">
        <el-icon><Plus /></el-icon>
        新增合同
      </el-button>
      <el-button type="success" size="large" @click="handleQuickAction('send-reminder')">
        <el-icon><Bell /></el-icon>
        发送提醒
      </el-button>
      <el-button type="warning" size="large" @click="handleQuickAction('generate-report')">
        <el-icon><Document /></el-icon>
        生成报表
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { dashboardAPI } from '../service/api'
import { ElMessage } from 'element-plus'

const currentDate = ref('')
const stats = ref({})
const todayReminders = ref([])
const recentActivities = ref([])

const handleMetricClick = (metric) => {
  ElMessage.success(`点击了${metric}指标`)
}

const handleReminderClick = (reminder) => {
  ElMessage.info(`查看${reminder.customerName || reminder.customer_name}的提醒详情`)
}

const handleQuickAction = (action) => {
  const actionMap = {
    'new-contract': '新增合同',
    'send-reminder': '发送提醒',
    'generate-report': '生成报表'
  }
  ElMessage.success(`执行操作：${actionMap[action]}`)
}

const formatMoney = (amount) => {
  return new Intl.NumberFormat('zh-CN').format(amount)
}

onMounted(async () => {
  const now = new Date()
  currentDate.value = now.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
  stats.value = await dashboardAPI.getStats()
  todayReminders.value = await dashboardAPI.getTodayReminders()
  recentActivities.value = await dashboardAPI.getRecentActivities()
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
  height: 100%;
  overflow-y: auto;
}

.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  padding: 30px;
  margin-bottom: 30px;
  color: white;
  position: relative;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.3);
}

.welcome-content h1 {
  font-size: 28px;
  margin-bottom: 10px;
  font-weight: 600;
}

.welcome-content p {
  font-size: 16px;
  opacity: 0.9;
}

.welcome-illustration {
  position: absolute;
  right: 30px;
  top: 50%;
  transform: translateY(-50%);
}

.floating-card {
  position: absolute;
  font-size: 24px;
  animation: float 3s ease-in-out infinite;
}

.card-1 { right: 0; animation-delay: 0s; }
.card-2 { right: 40px; top: -20px; animation-delay: 1s; }
.card-3 { right: 80px; top: 20px; animation-delay: 2s; }

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-10px); }
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.metric-card {
  background: white;
  border-radius: 16px;
  padding: 25px;
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.metric-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.metric-card.primary { border-left: 4px solid #409EFF; }
.metric-card.success { border-left: 4px solid #67C23A; }
.metric-card.warning { border-left: 4px solid #E6A23C; }
.metric-card.danger { border-left: 4px solid #F56C6C; }

.metric-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 15px;
  font-size: 24px;
}

.metric-card.primary .metric-icon { background: linear-gradient(135deg, #409EFF, #36a3f7); color: white; }
.metric-card.success .metric-icon { background: linear-gradient(135deg, #67C23A, #85ce61); color: white; }
.metric-card.warning .metric-icon { background: linear-gradient(135deg, #E6A23C, #ebb563); color: white; }
.metric-card.danger .metric-icon { background: linear-gradient(135deg, #F56C6C, #f78989); color: white; }

.metric-value {
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 5px;
}

.metric-label {
  font-size: 14px;
  color: #7f8c8d;
  margin-bottom: 8px;
}

.metric-trend {
  font-size: 12px;
  font-weight: 500;
}

.metric-trend.positive { color: #67C23A; }
.metric-trend.negative { color: #F56C6C; }

.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 30px;
}

.content-card {
  background: white;
  border-radius: 16px;
  padding: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  color: #2c3e50;
  font-size: 18px;
}

.reminder-list {
  max-height: 300px;
  overflow-y: auto;
}

.reminder-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s ease;
  gap: 15px;
}

.reminder-item:hover {
  background: #f8f9fa;
  border-radius: 8px;
  padding-left: 10px;
  padding-right: 10px;
}

.reminder-item:last-child {
  border-bottom: none;
}

.reminder-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  color: white;
  margin-right: 15px;
}

.reminder-avatar.due { background: linear-gradient(135deg, #E6A23C, #ebb563); }
.reminder-avatar.overdue { background: linear-gradient(135deg, #F56C6C, #f78989); }

.reminder-info {
  flex: 1;
  min-width: 0;
}

.reminder-name {
  font-weight: 600;
  margin-bottom: 5px;
  color: #2c3e50;
}

.reminder-details {
  font-size: 14px;
  color: #7f8c8d;
  display: flex;
  align-items: center;
  gap: 10px;
}

.reminder-details .amount {
  font-size: 16px;
  font-weight: 600;
  color: #409EFF;
  background: linear-gradient(135deg, #409EFF, #36a3f7);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.reminder-status {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.activity-timeline {
  max-height: 300px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  font-size: 16px;
}

.activity-icon.payment { background: linear-gradient(135deg, #67C23A, #85ce61); color: white; }
.activity-icon.overdue { background: linear-gradient(135deg, #F56C6C, #f78989); color: white; }
.activity-icon.contract { background: linear-gradient(135deg, #409EFF, #36a3f7); color: white; }

.activity-content {
  flex: 1;
}

.activity-title {
  font-weight: 500;
  margin-bottom: 5px;
  color: #2c3e50;
}

.activity-time {
  font-size: 12px;
  color: #7f8c8d;
}

.quick-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
  padding: 20px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.quick-actions .el-button {
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 500;
}
</style> 