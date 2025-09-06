<template>
  <div class="repayments">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>💰 还款计划跟踪</h1>
        <p>实时跟踪所有还款计划，确保按时收取利息</p>
      </div>
      <div class="header-stats">
        <div class="stat-item">
          <div class="stat-number">{{ stats.pending }}</div>
          <div class="stat-label">待还款</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ stats.overdue }}</div>
          <div class="stat-label">逾期</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ stats.paid }}</div>
          <div class="stat-label">已还款</div>
        </div>
      </div>
    </div>

    <!-- 筛选器 -->
    <div class="filter-section">
      <div class="filter-group">
        
        
        <el-tabs v-model="searchForm.status" class="status-tabs">
          <el-tab-pane label="全部" name=""></el-tab-pane>
          <el-tab-pane label="待还款" name="pending"></el-tab-pane>
          <el-tab-pane label="已还款" name="paid"></el-tab-pane>
          <el-tab-pane label="逾期" name="overdue"></el-tab-pane>
        </el-tabs>
        <el-input
          v-model="searchForm.keyword"
          placeholder="🔍 搜索合同号/合同名称/客户姓名"
          clearable
          @keyup.enter="handleSearch"
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        
        <el-date-picker
          v-model="searchForm.dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="date-picker"
        />
        
        <el-button type="primary" @click="handleSearch" class="search-btn">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        
        <el-button type="success" @click="handleBatchRemind" class="batch-btn">
          <el-icon><Bell /></el-icon>
          批量提醒
        </el-button>
      </div>
    </div>

    <!-- 还款计划时间线 -->
    <div class="timeline-container">
      <div class="timeline-main">
        <div v-for="group in groupedRepayments" :key="group.yearMonth" class="timeline-group">
          <div class="timeline-divider">
            <span class="divider-text">{{ group.yearMonth }}</span>
            <span class="main-timeline-marker"></span>
          </div>
          <div class="timeline-row">
            <div class="sub-timeline">
              <div
                v-for="(repayment, idx) in getSubTimelinePositions(group)"
                :key="idx"
                class="sub-timeline-marker-wrap"
                :style="{ left: idx === 0 ? '16.67%' : idx === 1 ? '50%' : '83.33%' }"
              >
                <div v-if="repayment" class="sub-timeline-date">{{ formatDate(repayment.due_date) }}</div>
                <div class="sub-timeline-marker" :class="{ empty: !repayment }"></div>
              </div>
            </div>
            <div v-for="(repayment, idx) in group.repayments" :key="repayment.schedule_id" class="timeline-item-row">
              <div class="timeline-card-wrap">
                <div class="repayment-card new-header" :class="repayment.period_status">
                  <div class="card-header new-header">
                    <div class="contract-meta">
                      <span class="contract-id">#{{ repayment.contract_id }}</span>
                      <span class="contract-name">{{ repayment.contract_name }}</span>
                      <span class="customer-name">{{ repayment.customer_name }}</span>
                    </div>
                    <el-tag :type="getStatusType(repayment.period_status)" class="repay-status-tag" effect="dark">
                      {{ getStatusText(repayment.period_status) }}
                    </el-tag>
                  </div>
                  <div class="repayment-details">
                    <div class="detail-row">
                      <div class="detail-item">
                        <span class="label">计息期间</span>
                        <span class="value">{{ repayment.period_start }} 至 <br/>{{ repayment.period_end }}</span>
                      </div>
                      <div class="detail-item">
                        <span class="label">到期日期</span>
                        <span class="value due-date">{{ repayment.due_date }}</span>
                      </div>
                    </div>
                    <div class="detail-row">
                      <div class="detail-item">
                        <span class="label">应付利息</span>
                        <span class="value amount">¥{{ formatMoney(repayment.calculated_interest) }}</span>
                      </div>
                      <div class="detail-item">
                        <span class="label">还款日期</span>
                        <span class="value">{{ repayment.payment_date || '未还款' }}</span>
                      </div>
                    </div>
                  </div>
                  <div class="card-actions">
                    <el-button size="small" type="primary" @click="handleMarkPaid(repayment)">
                      <el-icon><Check /></el-icon>
                      标记已还
                    </el-button>
                    <el-button size="small" type="warning" @click="handleSendReminder(repayment)">
                      <el-icon><Bell /></el-icon>
                      发送提醒
                    </el-button>
                    <el-button size="small" @click="handleViewDetails(repayment)">
                      <el-icon><View /></el-icon>
                      详情
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 标记还款对话框 -->
    <el-dialog
      v-model="paymentDialogVisible"
      title="标记还款"
      width="500px"
      class="payment-dialog"
    >
      <el-form
        ref="paymentFormRef"
        :model="paymentForm"
        :rules="paymentRules"
        label-width="100px"
      >
        <el-form-item label="还款日期" prop="payment_date">
          <el-date-picker
            v-model="paymentForm.payment_date"
            type="date"
            style="width: 100%"
            placeholder="选择还款日期"
          />
        </el-form-item>
        <el-form-item label="还款金额" prop="payment_amount">
          <el-input-number
            v-model="paymentForm.payment_amount"
            :min="0"
            :precision="2"
            style="width: 100%"
            placeholder="请输入还款金额"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="paymentForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="paymentDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handlePaymentSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 还款详情对话框 -->
    <el-dialog
      v-model="detailsDialogVisible"
      title="还款详情"
      width="600px"
      class="details-dialog"
    >
      <div v-if="selectedRepayment" class="repayment-details-modal">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="计划ID">{{ selectedRepayment.schedule_id }}</el-descriptions-item>
          <el-descriptions-item label="合同号">{{ selectedRepayment.contract_id }}</el-descriptions-item>
          <el-descriptions-item label="合同名称">{{ selectedRepayment.contract_name }}</el-descriptions-item>
          <el-descriptions-item label="客户姓名">{{ selectedRepayment.customer_name }}</el-descriptions-item>
          <el-descriptions-item label="计息期间">{{ selectedRepayment.period_start }} 至 {{ selectedRepayment.period_end }}</el-descriptions-item>
          <el-descriptions-item label="到期日期">{{ selectedRepayment.due_date }}</el-descriptions-item>
          <el-descriptions-item label="应付利息">¥{{ formatMoney(selectedRepayment.calculated_interest) }}</el-descriptions-item>
          <el-descriptions-item label="还款状态">
            <el-tag :type="getStatusType(selectedRepayment.period_status)">
              {{ getStatusText(selectedRepayment.period_status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="还款日期">{{ selectedRepayment.payment_date || '未还款' }}</el-descriptions-item>
          <el-descriptions-item label="最后提醒">{{ selectedRepayment.last_reminder_date || '未提醒' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { repaymentsAPI } from '../service/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { processDateRangeParams } from '../utils/request'

const loading = ref(false)
const paymentDialogVisible = ref(false)
const detailsDialogVisible = ref(false)
const selectedRepayment = ref(null)
const paymentFormRef = ref()

const searchForm = reactive({
  keyword: '',
  status: 'pending', // 默认选择待还款
  dateRange: null
})

const paymentForm = reactive({
  schedule_id: null,
  payment_date: null,
  payment_amount: null,
  remark: ''
})

const paymentRules = {
  payment_date: [
    { required: true, message: '请选择还款日期', trigger: 'change' }
  ],
  payment_amount: [
    { required: true, message: '请输入还款金额', trigger: 'blur' }
  ]
}

const repaymentList = ref([])

const stats = computed(() => {
  const pending = repaymentList.value.filter(item => item.period_status === 'pending').length
  const overdue = repaymentList.value.filter(item => item.period_status === 'overdue').length
  const paid = repaymentList.value.filter(item => item.period_status === 'paid').length
  return { pending, overdue, paid }
})

const formatMoney = (amount) => {
  return new Intl.NumberFormat('zh-CN').format(amount)
}

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

const formatYearMonth = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long' })
}

const getStatusType = (status) => {
  const typeMap = {
    pending: 'warning',
    paid: 'success',
    overdue: 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    pending: '待还款',
    paid: '已还款',
    overdue: '逾期'
  }
  return textMap[status] || status
}

// 计算属性：按年份月份分组的还款计划
const groupedRepayments = computed(() => {
  const groups = []
  let currentGroup = null
  
  repaymentList.value.forEach(repayment => {
    const yearMonth = formatYearMonth(repayment.due_date)
    
    if (!currentGroup || currentGroup.yearMonth !== yearMonth) {
      currentGroup = {
        yearMonth,
        repayments: []
      }
      groups.push(currentGroup)
    }
    
    currentGroup.repayments.push(repayment)
  })
  
  return groups
})

const handleSearch = async () => {
  loading.value = true
  
  // 处理日期范围参数，避免 axios 发送 dateRange[] 格式
  const params = processDateRangeParams(searchForm)
  
  repaymentList.value = await repaymentsAPI.getRepayments(params)
  loading.value = false
}



// 监听状态变化，自动搜索
watch(() => searchForm.status, (newStatus) => {
  console.log('状态变化:', newStatus)
  handleSearch()
})

const handleBatchRemind = () => {
  ElMessage.success('批量提醒功能执行中...')
}

const handleMarkPaid = (row) => {
  paymentForm.schedule_id = row.schedule_id
  paymentForm.payment_amount = row.calculated_interest
  paymentForm.payment_date = new Date()
  paymentForm.remark = ''
  paymentDialogVisible.value = true
}

const handleSendReminder = async (row) => {
  await ElMessageBox.confirm(
    `确定要向客户 ${row.customer_name} 发送还款提醒吗？`,
    '发送提醒',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
  await repaymentsAPI.sendReminder(row.schedule_id)
  ElMessage.success('提醒发送成功')
  handleSearch()
}

const handleViewDetails = async (row) => {
  selectedRepayment.value = await repaymentsAPI.getRepaymentDetail(row.schedule_id)
  detailsDialogVisible.value = true
}

const handlePaymentSubmit = async () => {
  try {
    await paymentFormRef.value.validate()
    await repaymentsAPI.markPaid(paymentForm.schedule_id, paymentForm)
    ElMessage.success('还款记录已更新')
    paymentDialogVisible.value = false
    handleSearch()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

function getSubTimelinePositions(group) {
  const arr = Array(3).fill(null);
  group.repayments.forEach((item, idx) => {
    arr[idx] = item;
  });
  return arr;
}

onMounted(async () => {
  await handleSearch()
})
</script>

<style scoped>
.repayments {
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

.header-stats {
  display: flex;
  gap: 30px;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.8;
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
  gap: 12px;
  align-items: center;
  flex-wrap: wrap;
  padding: 5px 0;
}

.search-input {
  flex: 2;
  min-width: 270px;
  max-width: 500px;
}

.filter-select, .date-picker {
  min-width: 90px;
  max-width: 110px;
  flex-shrink: 0;
}

.search-btn, .batch-btn {
  border-radius: 12px;
  padding: 10px 20px;
  flex-shrink: 0;
  white-space: nowrap;
}

/* 状态选项卡样式 */
.status-tabs {
  flex-shrink: 0;
  pointer-events: auto;
  margin: 0 20px;
}

.status-tabs :deep(.el-tabs__header) {
  margin: 0;
  border-bottom: none;
}

.status-tabs :deep(.el-tabs__nav-wrap) {
  padding: 0;
}

.status-tabs :deep(.el-tabs__item) {
  padding: 6px 12px;
  font-size: 13px;
  border-radius: 6px 6px 0 0;
  margin-right: 2px;
  transition: all 0.3s ease;
  cursor: pointer;
  user-select: none;
}

.status-tabs :deep(.el-tabs__item.is-active) {
  background: linear-gradient(135deg, #409EFF, #36a3f7);
  color: white;
  font-weight: 600;
}

.status-tabs :deep(.el-tabs__item:hover) {
  background: rgba(64, 158, 255, 0.1);
  color: #409EFF;
}

.status-tabs :deep(.el-tabs__active-bar) {
  display: none;
}

.status-tabs :deep(.el-tabs__nav) {
  border: none;
}

.status-tabs :deep(.el-tabs__content) {
  display: none;
}

.timeline-container {
  display: flex;
  align-items: flex-start;
  position: relative;
  padding-left: 60px;
}

.timeline-main {
  width: 100%;
  position: relative;
}

.timeline-main::before {
  content: '';
  position: absolute;
  left: -30px;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(to bottom, #667eea, #764ba2);
  border-radius: 2px;
  z-index: 1;
}

.main-timeline-marker {
  position: absolute;
  left: -30px;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF, #36a3f7);
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(64,158,255,0.10);
  margin: -4px 2px;
  z-index: 3;
}

.timeline-divider {
  position: relative;
  margin: 30px 0 10px 0;
  text-align: left;
  font-weight: bold;
  font-size: 16px;
  color: #409EFF;
  min-height: 32px;
}

.timeline-row {
  display: flex;
  flex-wrap: wrap;
  gap: 24px 2%;
  margin-bottom: 40px;
  position: relative;
}

.timeline-item-row {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  width: 32%;
  min-width: 320px;
  max-width: 33.33%;
  position: relative;
}

.timeline-marker {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF, #36a3f7);
  border: 3px solid #fff;
  box-shadow: 0 2px 8px rgba(64,158,255,0.10);
  position: absolute;
  left: -50px;
  top: 32px;
  z-index: 2;
}

.timeline-marker.pending {
  background: linear-gradient(135deg, #E6A23C, #ebb563);
}

.timeline-marker.paid {
  background: linear-gradient(135deg, #67C23A, #85ce61);
}

.timeline-marker.overdue {
  background: linear-gradient(135deg, #F56C6C, #f78989);
}

.timeline-card-wrap {
  width: 100%;
}

.repayment-card {
  background: white;
  border-radius: 16px;
  padding: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border-left: 4px solid transparent;
}

.repayment-card:hover {
  transform: translateX(5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
}

.repayment-card.pending {
  border-left-color: #E6A23C;
}

.repayment-card.paid {
  border-left-color: #67C23A;
}

.repayment-card.overdue {
  border-left-color: #F56C6C;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  gap: 15px;
}

.card-header.new-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1.5px solid #f0f0f5;
  background: linear-gradient(90deg, #f8fafc 60%, #eaf1fb 100%);
  border-radius: 16px 16px 0 0;
  margin-bottom: 18px;
  box-shadow: 0 2px 8px rgba(64,158,255,0.06);
}

.contract-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.contract-id {
  font-size: 13px;
  color: #b0b4ba;
  font-weight: 500;
  letter-spacing: 1px;
}

.contract-name {
  font-size: 18px;
  font-weight: 700;
  color: #409EFF;
  background: linear-gradient(90deg, #409EFF 60%, #36a3f7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.customer-name {
  font-size: 14px;
  color: #060606;
  margin-top: 2px;
}

.repay-status-tag {
  font-size: 15px !important;
  padding: 6px 14px !important;
  border-radius: 5px !important;
  box-shadow: 0 2px 8px rgba(64,158,255,0.10);
  font-weight: 600;
  letter-spacing: 1px;
}

.repayment-details {
  margin-bottom: 20px;
}

.detail-row {
  display: flex;
  gap: 30px;
  margin-bottom: 15px;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-item {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.label {
  font-size: 12px;
  color: #7f8c8d;
  margin-bottom: 5px;
}

.value {
  font-size: 14px;
  font-weight: 600;
  color: #2c3e50;
}

.value.amount {
  color: #409EFF;
  font-size: 16px;
}

.value.due-date {
  color: #E6A23C;
}

.card-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.card-actions .el-button {
  border-radius: 8px;
  font-size: 12px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.repayment-details-modal {
  padding: 20px 0;
}

.sub-timeline {
  position: absolute;
  top: -28px;
  left: 0;
  width: 100%;
  height: 32px;
  z-index: 1;
}

.sub-timeline::before {
  content: '';
  position: absolute;
  top: 16px;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, #e0e7ef 60%, #c6dafc 100%);
  z-index: 1;
  border-radius: 1px;
}

.sub-timeline-marker-wrap {
  position: absolute;
  top: -8px;
  width: 0;
  text-align: center;
  transform: translateX(-50%);
  z-index: 3;
}

.sub-timeline-date {
  font-size: 12px;
  color: #409EFF;
  margin-bottom: 2px;
  font-weight: 600;
  letter-spacing: 1px;
  position: relative;
  z-index: 2;
  margin: 0px -14px;
  background: transparent;
  padding: 0 0px;
}

.sub-timeline-marker {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409EFF, #36a3f7);
  border: 2px solid #fff;
  margin: 2px -5px;
  box-shadow: 0 2px 8px rgba(64,158,255,0.10);
  z-index: 3;
  position: relative;
}

.sub-timeline-marker.empty {
  background: transparent;
  border: none;
  box-shadow: none;
}
</style> 