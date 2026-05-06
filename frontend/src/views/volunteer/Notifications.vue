<template>
  <div>
    <div class="page-header" style="display: flex; justify-content: space-between; align-items: center;">
      <h2>通知消息</h2>
      <el-button type="primary" text @click="handleReadAll">全部已读</el-button>
    </div>
    <el-card>
      <el-table :data="notifications" stripe v-loading="loading">
        <el-table-column width="60" align="center">
          <template #default="{ row }">
            <el-badge :hidden="row.isRead === 1" dot>
              <el-icon v-if="row.isRead === 0" color="#f56c6c"><Bell /></el-icon>
              <el-icon v-else color="#c0c4cc"><Bell /></el-icon>
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column prop="content" label="内容" min-width="300" />
        <el-table-column prop="createTime" label="时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button v-if="row.isRead === 0" size="small" type="primary" text
              @click="handleRead(row)">已读</el-button>
            <span v-else style="color: #909399; font-size: 12px;">已读</span>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top: 20px; justify-content: center;"
        background layout="prev, pager, next"
        :total="total" :page-size="pageSize"
        v-model:current-page="currentPage"
        @current-change="loadData"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getNotifications, markAsRead, markAllAsRead } from '../../api/modules/notification'
import { useUserStore } from '../../store'

const userStore = useUserStore()
const notifications = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const loading = ref(false)

async function loadData() {
  loading.value = true
  try {
    const res = await getNotifications({ page: currentPage.value, size: pageSize.value })
    if (res.code === 200) {
      notifications.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function handleRead(row) {
  await markAsRead(row.id)
  row.isRead = 1
  userStore.fetchUnreadCount()
}

async function handleReadAll() {
  await markAllAsRead()
  loadData()
  userStore.fetchUnreadCount()
  ElMessage.success('已全部标记为已读')
}

onMounted(loadData)
</script>
