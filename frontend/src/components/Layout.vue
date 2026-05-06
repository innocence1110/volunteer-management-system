<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <div class="layout-aside">
      <div class="menu-title">
        <el-icon><Flag /></el-icon>
        &nbsp;志愿活动管理
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <template v-if="userStore.isAdmin">
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>系统概览</span>
          </el-menu-item>
          <el-menu-item index="/admin/activity-publish">
            <el-icon><Plus /></el-icon>
            <span>活动发布</span>
          </el-menu-item>
          <el-menu-item index="/admin/activity-manage">
            <el-icon><Setting /></el-icon>
            <span>活动管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/activity-browse">
            <el-icon><List /></el-icon>
            <span>活动浏览</span>
          </el-menu-item>
        </template>
        <template v-if="userStore.isVolunteer">
          <el-menu-item index="/volunteer/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <span>我的主页</span>
          </el-menu-item>
          <el-menu-item index="/volunteer/activity-browse">
            <el-icon><List /></el-icon>
            <span>活动浏览</span>
          </el-menu-item>
          <el-menu-item index="/volunteer/activity-register">
            <el-icon><Edit /></el-icon>
            <span>活动报名</span>
          </el-menu-item>
          <el-menu-item index="/volunteer/checkin">
            <el-icon><CircleCheck /></el-icon>
            <span>活动签到</span>
          </el-menu-item>
        </template>
        <!-- 通用菜单 -->
        <el-menu-item :index="profilePath">
          <el-icon><User /></el-icon>
          <span>信息管理</span>
        </el-menu-item>
        <el-menu-item :index="notifPath">
          <el-icon><Bell /></el-icon>
          <span>通知消息</span>
          <el-badge v-if="userStore.unreadCount > 0" :value="userStore.unreadCount" :max="99"
            style="margin-left: 8px" />
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 主内容区 -->
    <div class="layout-main">
      <!-- 顶部导航栏 -->
      <div class="layout-header">
        <h3 style="margin: 0; color: #303133;">{{ currentTitle }}</h3>
        <div style="display: flex; align-items: center; gap: 16px;">
          <span style="color: #606266;">
            <el-icon><User /></el-icon>
            {{ userStore.user?.name }}
            <el-tag size="small" :type="userStore.isAdmin ? 'danger' : 'success'">
              {{ userStore.isAdmin ? '管理员' : '志愿者' }}
            </el-tag>
          </span>
          <el-button type="danger" text @click="handleLogout">
            <el-icon><SwitchButton /></el-icon> 退出
          </el-button>
        </div>
      </div>

      <!-- 页面内容 -->
      <div class="layout-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../store'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '志愿活动管理系统')

const profilePath = computed(() => '/' + userStore.user?.role + '/profile')
const notifPath = computed(() => '/' + userStore.user?.role + '/notifications')

function handleLogout() {
  userStore.logout()
  router.push('/login')
}

onMounted(() => {
  userStore.fetchUnreadCount()
  // 每30秒刷新未读数
  setInterval(() => userStore.fetchUnreadCount(), 30000)
})
</script>
