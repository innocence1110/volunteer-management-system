<template>
  <div>
    <div class="page-header"><h2>活动浏览</h2></div>
    <el-card>
      <el-form :inline="true" style="margin-bottom: 20px;">
        <el-form-item>
          <el-input v-model="keyword" placeholder="搜索活动名称" clearable @clear="loadData"
            @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="activities" stripe v-loading="loading">
        <el-table-column prop="name" label="活动名称" min-width="150" />
        <el-table-column prop="location" label="地点" width="150" />
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
        <el-table-column label="报名" width="120" align="center">
          <template #default="{ row }">
            {{ row.currentParticipants || 0 }} / {{ row.maxParticipants }}
          </template>
        </el-table-column>
        <el-table-column label="奖励" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="warning">{{ row.pointsReward || 0 }}分</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ongoing' ? 'success' : 'info'" size="small">
              {{ row.status === 'ongoing' ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="showDetail(row)">详情</el-button>
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

    <!-- 活动详情弹窗 -->
    <el-dialog v-model="detailVisible" title="活动详情" width="550px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="活动名称">{{ detail.name }}</el-descriptions-item>
        <el-descriptions-item label="活动描述">{{ detail.description || '无' }}</el-descriptions-item>
        <el-descriptions-item label="活动地点">{{ detail.location }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ detail.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ detail.endTime }}</el-descriptions-item>
        <el-descriptions-item label="报名情况">
          {{ detail.currentParticipants || 0 }} / {{ detail.maxParticipants }}
        </el-descriptions-item>
        <el-descriptions-item label="奖励积分">{{ detail.pointsReward }} 分</el-descriptions-item>
        <el-descriptions-item label="签到方式">
          {{ detail.checkInType === 'button' ? '按钮签到' : detail.checkInType === 'code' ? '数字码签到' : '图片签到' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getActivities } from '../../api/modules/activity'

const activities = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const loading = ref(false)
const detailVisible = ref(false)
const detail = ref({})

async function loadData() {
  loading.value = true
  try {
    const res = await getActivities({ page: currentPage.value, size: pageSize.value, keyword: keyword.value })
    if (res.code === 200) {
      activities.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function showDetail(row) {
  detail.value = row
  detailVisible.value = true
}

onMounted(loadData)
</script>
