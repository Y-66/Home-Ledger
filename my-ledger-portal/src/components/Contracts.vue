<template>
  <div class="contracts">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>📋 借款合同管理</h1>
        <p>管理所有借贷合同，跟踪合同状态和还款情况</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" size="large" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增合同
        </el-button>
      </div>
    </div>

    <!-- 筛选器 -->
    <div class="filter-section">
      <div class="filter-group">
        <el-tabs v-model="searchForm.status" class="status-tabs">
          <el-tab-pane :label="tabLabels.all" name=""></el-tab-pane>
          <el-tab-pane :label="tabLabels.active" name="active"></el-tab-pane>
          <el-tab-pane :label="tabLabels.closed" name="closed"></el-tab-pane>
          <el-tab-pane :label="tabLabels.default" name="default"></el-tab-pane>
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

        <el-select v-model="searchForm.frequency" placeholder="还款频率" clearable class="filter-select">
          <el-option label="📅 月付" value="monthly" />
          <el-option label="📅 季付" value="quarterly" />
          <el-option label="📅 年付" value="annually" />
        </el-select>
        
        <el-button type="primary" @click="handleSearch" class="search-btn">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </div>
    </div>

    <!-- 合同卡片网格 -->
    <div class="contracts-grid">
      <div v-for="contract in contractList" :key="contract.contract_id" class="contract-card" @click="handleViewContract(contract)">
        <div class="card-header new-header">
          <div class="contract-meta">
            <span class="contract-id">#{{ contract.contract_id }}</span>
            <span class="contract-name">{{ contract.contract_name }}</span>
          </div>
          <el-tag :type="getStatusType(contract.contract_status)" class="contract-status-tag" effect="dark" round>
            {{ getStatusText(contract.contract_status) }}
          </el-tag>
        </div>
        
        <div class="customer-info">
          <div class="customer-avatar">
            {{ contract.customer_name.charAt(0) }}
          </div>
          <div class="customer-details">
            <div class="customer-name">{{ contract.customer_name }}</div>
            <div class="contract-type">{{ getFrequencyText(contract.repayment_frequency) }}</div>
          </div>
        </div>
        
        <div class="contract-details">
          <div class="detail-item">
            <span class="label">借贷金额</span>
            <span class="value amount">¥{{ formatMoney(contract.loan_amount) }}</span>
          </div>
          <div class="detail-item">
            <span class="label">年利率</span>
            <span class="value rate">{{ contract.annual_interest_rate }}%</span>
          </div>
          <div class="detail-item">
            <span class="label">开始日期</span>
            <span class="value date">{{ contract.start_date }}</span>
          </div>
          <div class="detail-item">
            <span class="label">结束日期</span>
            <span class="value date">{{ contract.end_date }}</span>
          </div>
        </div>
        
        <div class="card-actions">
          <el-button size="small" type="primary" @click.stop="handleEdit(contract)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button size="small" type="success" @click.stop="handleViewSchedule(contract)">
            <el-icon><Calendar /></el-icon>
            还款计划
          </el-button>
          <el-button size="small" type="warning" @click.stop="handleClose(contract)">
            <el-icon><Close /></el-icon>
            关闭
          </el-button>
        </div>
      </div>
    </div>

    <!-- 新增/编辑合同对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="700px"
      @close="handleDialogClose"
      class="contract-dialog"
    >
      <el-form
        ref="contractFormRef"
        :model="contractForm"
        :rules="contractRules"
        label-width="120px"
      >
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="合同名称" prop="contract_name">
              <el-input v-model="contractForm.contract_name" placeholder="请输入合同名称" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="客户" prop="customer_id">
              <el-select
                v-model="contractForm.customer_id"
                placeholder="请选择客户"
                style="width: 100%"
                filterable
              >
                <el-option
                  v-for="customer in customerOptions"
                  :key="customer.customer_id"
                  :label="customer.full_name"
                  :value="customer.customer_id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="借贷金额" prop="loan_amount">
              <el-input-number
                v-model="contractForm.loan_amount"
                :min="0"
                :precision="2"
                style="width: 100%"
                placeholder="请输入借贷金额"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="年利率(%)" prop="annual_interest_rate">
              <el-input-number
                v-model="contractForm.annual_interest_rate"
                :min="0"
                :max="100"
                :precision="2"
                style="width: 100%"
                placeholder="请输入年利率"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="利息类型" prop="interest_type">
              <el-select v-model="contractForm.interest_type" style="width: 100%">
                <el-option label="固定利率" value="fixed" />
                <el-option label="浮动利率" value="variable" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="start_date">
              <el-date-picker
                v-model="contractForm.start_date"
                type="date"
                style="width: 100%"
                placeholder="选择开始日期"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="end_date">
              <el-date-picker
                v-model="contractForm.end_date"
                type="date"
                style="width: 100%"
                placeholder="选择结束日期"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="还款频率" prop="repayment_frequency">
              <el-select v-model="contractForm.repayment_frequency" style="width: 100%">
                <el-option label="月付" value="monthly" />
                <el-option label="季付" value="quarterly" />
                <el-option label="年付" value="annually" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同状态" prop="contract_status">
              <el-select v-model="contractForm.contract_status" style="width: 100%">
                <el-option label="活跃" value="active" />
                <el-option label="已关闭" value="closed" />
                <el-option label="违约" value="default" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 合同详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="合同详情"
      width="800px"
      class="detail-dialog"
    >
      <div v-if="contractDetail" class="contract-detail">
        <div class="detail-section">
          <h3>基本信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="合同编号">{{ contractDetail.contract_id }}</el-descriptions-item>
            <el-descriptions-item label="合同名称">{{ contractDetail.contract_name }}</el-descriptions-item>
            <el-descriptions-item label="客户姓名">{{ contractDetail.customer_name }}</el-descriptions-item>
            <el-descriptions-item label="借贷金额">¥{{ formatMoney(contractDetail.loan_amount) }}</el-descriptions-item>
            <el-descriptions-item label="年利率">{{ contractDetail.annual_interest_rate }}%</el-descriptions-item>
            <el-descriptions-item label="开始日期">{{ contractDetail.start_date }}</el-descriptions-item>
            <el-descriptions-item label="结束日期">{{ contractDetail.end_date }}</el-descriptions-item>
            <el-descriptions-item label="利息类型">{{ contractDetail.interest_type === 'fixed' ? '固定利率' : '浮动利率' }}</el-descriptions-item>
            <el-descriptions-item label="还款频率">{{ getFrequencyText(contractDetail.repayment_frequency) }}</el-descriptions-item>
            <el-descriptions-item label="合同状态">
              <el-tag :type="getStatusType(contractDetail.contract_status)">
                {{ getStatusText(contractDetail.contract_status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="已还本金">¥{{ formatMoney(contractDetail.principal_paid || 0) }}</el-descriptions-item>
          </el-descriptions>
        </div>
        
        <div class="detail-section">
          <h3>客户信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="身份证号">{{ contractDetail.customer?.id_number }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ contractDetail.customer?.contact_phone }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ contractDetail.customer?.email }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ contractDetail.customer?.created_at }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-dialog>

    <!-- 还款计划对话框 -->
    <el-dialog
      v-model="scheduleDialogVisible"
      title="还款计划"
      width="1000px"
      class="schedule-dialog"
    >
      <div v-if="currentContract" class="schedule-header">
        <h3>合同 {{ currentContract.contract_id }} - {{ currentContract.customer_name }}</h3>
        <p>借贷金额: ¥{{ formatMoney(currentContract.loan_amount) }} | 年利率: {{ currentContract.annual_interest_rate }}%</p>
      </div>
      
      <el-table :data="repaymentSchedules" style="width: 100%" border>
        <el-table-column prop="schedule_id" label="计划ID" width="80" />
        <el-table-column prop="period_start" label="计息开始" width="120" />
        <el-table-column prop="period_end" label="计息结束" width="120" />
        <el-table-column prop="due_date" label="到期日期" width="120" />
        <el-table-column prop="calculated_interest" label="应付利息" width="120">
          <template #default="scope">
            ¥{{ formatMoney(scope.row.calculated_interest) }}
          </template>
        </el-table-column>
        <el-table-column prop="period_status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getPeriodStatusType(scope.row.period_status)">
              {{ getPeriodStatusText(scope.row.period_status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="payment_date" label="实际付款日期" width="120" />
        <el-table-column prop="last_reminder_date" label="最后提醒日期" width="120" />
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { contractsAPI } from '../service/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('新增合同')
const contractFormRef = ref()

// 新增详情对话框相关状态
const detailDialogVisible = ref(false)
const scheduleDialogVisible = ref(false)
const currentContract = ref(null)
const contractDetail = ref(null)
const repaymentSchedules = ref([])

const searchForm = reactive({
  keyword: '',
  status: 'active', // 默认选择活跃状态
  frequency: ''
})

const contractForm = reactive({
  contract_id: null,
  contract_name: '',
  customer_id: null,
  loan_amount: null,
  annual_interest_rate: null,
  start_date: null,
  end_date: null,
  interest_type: 'fixed',
  repayment_frequency: 'monthly',
  contract_status: 'active',
  principal_paid: 0,
  principal_payment_date: null
})

const contractRules = {
  contract_name: [
    { required: true, message: '请输入合同名称', trigger: 'blur' }
  ],
  customer_id: [
    { required: true, message: '请选择客户', trigger: 'change' }
  ],
  loan_amount: [
    { required: true, message: '请输入借贷金额', trigger: 'blur' }
  ],
  annual_interest_rate: [
    { required: true, message: '请输入年利率', trigger: 'blur' }
  ],
  start_date: [
    { required: true, message: '请选择开始日期', trigger: 'change' }
  ],
  end_date: [
    { required: true, message: '请选择结束日期', trigger: 'change' }
  ],
  repayment_frequency: [
    { required: true, message: '请选择还款频率', trigger: 'change' }
  ]
}

const customerOptions = ref([])
const contractList = ref([])

// 状态统计数据
const statusStats = ref({
  all: 0,
  active: 0,
  closed: 0,
  default: 0
})

// 计算属性：格式化选项卡标签
const tabLabels = computed(() => ({
  all: `全部 (${statusStats.value.all})`,
  active: `活跃 (${statusStats.value.active})`,
  closed: `已关闭 (${statusStats.value.closed})`,
  default: `违约 (${statusStats.value.default})`
}))

const formatMoney = (amount) => {
  return new Intl.NumberFormat('zh-CN').format(amount)
}

const getFrequencyText = (frequency) => {
  const textMap = {
    monthly: '月付',
    quarterly: '季付',
    annually: '年付'
  }
  return textMap[frequency] || frequency
}

const getStatusType = (status) => {
  const typeMap = {
    active: 'success',
    closed: 'info',
    default: 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    active: '活跃',
    closed: '已关闭',
    default: '违约'
  }
  return textMap[status] || status
}

const getPeriodStatusType = (status) => {
  const typeMap = {
    pending: 'warning',
    paid: 'success',
    overdue: 'danger'
  }
  return typeMap[status] || 'info'
}

const getPeriodStatusText = (status) => {
  const textMap = {
    pending: '待付款',
    paid: '已付款',
    overdue: '已逾期'
  }
  return textMap[status] || status
}

const handleSearch = async () => {
  loading.value = true
  contractList.value = await contractsAPI.getContracts(searchForm)
  loading.value = false
}

// 更新统计数据（当数据发生变化时调用）
const updateStats = () => {
  const allContracts = contractList.value
  statusStats.value = {
    all: allContracts.length,
    active: allContracts.filter(c => c.contract_status === 'active').length,
    closed: allContracts.filter(c => c.contract_status === 'closed').length,
    default: allContracts.filter(c => c.contract_status === 'default').length
  }
}

// 获取状态统计数据
const loadStatusStats = async () => {
  try {
    // 获取所有合同数据，然后在前端统计
    const allContracts = await contractsAPI.getContracts({})
    
    statusStats.value = {
      all: allContracts.length,
      active: allContracts.filter(c => c.contract_status === 'active').length,
      closed: allContracts.filter(c => c.contract_status === 'closed').length,
      default: allContracts.filter(c => c.contract_status === 'default').length
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

// 监听状态变化，自动搜索
watch(() => searchForm.status, (newStatus) => {
  console.log('合同状态变化:', newStatus)
  handleSearch()
})

const handleAdd = () => {
  dialogTitle.value = '新增合同'
  resetForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑合同'
  
  // 如果没有customer_id，根据customer_name查找
  let customerId = row.customer_id
  if (!customerId && row.customer_name) {
    const customer = customerOptions.value.find(c => c.full_name === row.customer_name)
    customerId = customer ? customer.customer_id : null
  }
  
  // 复制合同数据，确保客户ID正确设置
  Object.assign(contractForm, {
    contract_id: row.contract_id,
    contract_name: row.contract_name,
    customer_id: customerId,
    loan_amount: row.loan_amount,
    annual_interest_rate: row.annual_interest_rate,
    start_date: row.start_date,
    end_date: row.end_date,
    interest_type: row.interest_type,
    repayment_frequency: row.repayment_frequency,
    contract_status: row.contract_status,
    principal_paid: row.principal_paid || 0,
    principal_payment_date: row.principal_payment_date
  })
  
  dialogVisible.value = true
}

const handleViewContract = async (contract) => {
  try {
    currentContract.value = contract
    // 获取合同详细信息
    contractDetail.value = await contractsAPI.getContractDetail(contract.contract_id)
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取合同详情失败')
    console.error('获取合同详情失败:', error)
  }
}

const handleViewSchedule = async (row) => {
  try {
    currentContract.value = row
    // 获取还款计划列表
    repaymentSchedules.value = await contractsAPI.getRepaymentSchedules(row.contract_id)
    scheduleDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取还款计划失败')
    console.error('获取还款计划失败:', error)
  }
}

const handleClose = async (row) => {
  await ElMessageBox.confirm(
    `确定要关闭合同 ${row.contract_id} 吗？`,
    '确认关闭',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
  await contractsAPI.closeContract(row.contract_id)
  ElMessage.success('合同已关闭')
  await loadStatusStats() // 重新加载统计数据
  handleSearch()
}

const handleSubmit = async () => {
  try {
    await contractFormRef.value.validate()
    if (contractForm.contract_id) {
      await contractsAPI.updateContract(contractForm.contract_id, contractForm)
      ElMessage.success('编辑成功')
    } else {
      await contractsAPI.addContract(contractForm)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadStatusStats() // 重新加载统计数据
    handleSearch()
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

const handleDialogClose = () => {
  resetForm()
}

const resetForm = () => {
  Object.assign(contractForm, {
    contract_id: null,
    contract_name: '',
    customer_id: null,
    loan_amount: null,
    annual_interest_rate: null,
    start_date: null,
    end_date: null,
    interest_type: 'fixed',
    repayment_frequency: 'monthly',
    contract_status: 'active',
    principal_paid: 0,
    principal_payment_date: null
  })
  contractFormRef.value?.resetFields()
}

onMounted(async () => {
  customerOptions.value = await contractsAPI.getCustomerOptions()
  await loadStatusStats() // 先加载统计数据
  await handleSearch()
})
</script>

<style scoped>
.contracts {
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

.filter-section {
  background: white;
  border-radius: 16px;
  padding: 25px;
  margin-bottom: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
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
  min-width: 200px;
  max-width: 300px;
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
  position: relative;
}

.status-tabs :deep(.el-tabs__item .count-badge) {
  position: absolute;
  top: -2px;
  right: -2px;
  background: #f56c6c;
  color: white;
  border-radius: 8px;
  padding: 1px 4px;
  font-size: 10px;
  min-width: 16px;
  text-align: center;
  line-height: 1.2;
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

.contracts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.contract-card {
  background: white;
  border-radius: 16px;
  padding: 25px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  border: 2px solid transparent;
}

.contract-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
  border-color: #409EFF;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header.new-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 18px;
  border-bottom: 1.5px solid #f0f0f5;
  background: linear-gradient(90deg, #f8fafc 60%, #eaf1fb 100%);
  border-radius: 16px 16px 0 0;
  margin-bottom: 18px;
  box-shadow: 0 2px 8px rgba(64,158,255,0.06);
}

.contract-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.contract-id {
  font-size: 13px;
  color: #b0b4ba;
  font-weight: 500;
  letter-spacing: 1px;
}

.contract-name {
  font-size: 20px;
  font-weight: 700;
  color: #409EFF;
  background: linear-gradient(90deg, #409EFF 60%, #36a3f7 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.contract-status-tag {
  font-size: 15px !important;
  padding: 6px 18px !important;
  border-radius: 10px !important;
  box-shadow: 0 2px 8px rgba(64,158,255,0.10);
  font-weight: 600;
  letter-spacing: 1px;
}

.customer-info {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.customer-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 18px;
  margin-right: 15px;
}

.customer-name {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 5px;
}

.contract-type {
  font-size: 14px;
  color: #7f8c8d;
}

.contract-details {
  margin-bottom: 20px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
  border-bottom: none;
}

.label {
  font-size: 14px;
  color: #7f8c8d;
}

.value {
  font-weight: 600;
  color: #2c3e50;
}

.value.amount {
  color: #409EFF;
  font-size: 16px;
}

.value.rate {
  color: #67C23A;
}

.value.date {
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

/* 详情对话框样式 */
.detail-dialog .contract-detail {
  padding: 20px 0;
}

.detail-section {
  margin-bottom: 30px;
}

.detail-section h3 {
  color: #2c3e50;
  margin-bottom: 15px;
  font-size: 18px;
  font-weight: 600;
}

/* 还款计划对话框样式 */
.schedule-dialog .schedule-header {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.schedule-header h3 {
  color: #2c3e50;
  margin-bottom: 8px;
  font-size: 16px;
  font-weight: 600;
}

.schedule-header p {
  color: #7f8c8d;
  margin: 0;
  font-size: 14px;
}
</style> 