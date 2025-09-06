<script setup>
import { ref, computed } from 'vue'
import Dashboard from './components/Dashboard.vue'
import Contracts from './components/Contracts.vue'
import Repayments from './components/Repayments.vue'
import Reminders from './components/Reminders.vue'

const activeMenu = ref('dashboard')

const currentComponent = computed(() => {
  const componentMap = {
    dashboard: Dashboard,
    contracts: Contracts,
    repayments: Repayments,
    reminders: Reminders
  }
  return componentMap[activeMenu.value] || Dashboard
})

const handleMenuSelect = (index) => {
  activeMenu.value = index
}

const getPageTitle = () => {
  const titleMap = {
    dashboard: '📊 数据仪表盘',
    contracts: '📋 借款合同管理',
    repayments: '💰 还款计划跟踪',
    reminders: '🔔 智能提醒系统'
  }
  return titleMap[activeMenu.value] || '📊 数据仪表盘'
}
</script>

<template>
  <div id="app">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside width="280px" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); overflow: hidden;">
        <div class="logo-container">
          <div class="logo-icon">
            <el-icon><Bank /></el-icon>
          </div>
          <h2 class="logo-text">银行借贷管理</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="el-menu-vertical"
          background-color="transparent"
          text-color="#ffffff"
          active-text-color="#ffd700"
          @select="handleMenuSelect"
        >
          <el-menu-item index="dashboard" class="menu-item">
            <el-icon><DataBoard /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          <el-menu-item index="contracts" class="menu-item">
            <el-icon><Document /></el-icon>
            <span>借款合同</span>
          </el-menu-item>
          <el-menu-item index="repayments" class="menu-item">
            <el-icon><Money /></el-icon>
            <span>还款计划</span>
          </el-menu-item>
          <el-menu-item index="reminders" class="menu-item">
            <el-icon><Bell /></el-icon>
            <span>提醒管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区域 -->
      <el-container>
        <!-- 顶部导航栏 -->
        <el-header style="background: linear-gradient(90deg, #ffffff 0%, #f8f9fa 100%); border-bottom: 2px solid #e9ecef; display: flex; align-items: center; justify-content: space-between;">
          <div class="header-left">
            <h3 class="page-title">{{ getPageTitle() }}</h3>
          </div>
          <div class="header-right">
            <el-avatar size="small" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
            <span class="user-name">管理员</span>
          </div>
        </el-header>

        <!-- 主要内容 -->
        <el-main style="background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);">
          <component :is="currentComponent" />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
.el-menu-vertical {
  border-right: none;
  margin-top: 20px;
  overflow: visible;
  height: calc(100vh - 140px) !important;
}

.menu-item {
  margin: 8px 16px;
  border-radius: 12px;
  transition: all 0.3s ease;
  height: 50px !important;
  line-height: 50px !important;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.1) !important;
  transform: translateX(8px);
}

.menu-item.is-active {
  background: rgba(255, 215, 0, 0.2) !important;
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.3);
}

.logo-container {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  height: 120px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.logo-icon {
  width: 50px;
  height: 50px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
}

.logo-icon .el-icon {
  font-size: 24px;
  color: #ffffff;
}

.logo-text {
  color: #ffffff;
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.header-left {
  display: flex;
  align-items: center;
}

.page-title {
  margin: 0;
  color: #2c3e50;
  font-size: 24px;
  font-weight: 600;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-name {
  color: #606266;
  font-weight: 500;
}
</style>
