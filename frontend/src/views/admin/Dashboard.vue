<template>
  <div>
    <div class="page-header">
      <h2>系统概览</h2>
    </div>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center;">
            <el-icon :size="40" color="#409eff"><User /></el-icon>
            <div style="margin-top: 10px;">
              <div style="font-size: 28px; font-weight: bold; color: #409eff;">{{ stats.totalUsers }}</div>
              <div style="color: #909399; margin-top: 5px;">注册用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center;">
            <el-icon :size="40" color="#67c23a"><Flag /></el-icon>
            <div style="margin-top: 10px;">
              <div style="font-size: 28px; font-weight: bold; color: #67c23a;">{{ stats.totalActivities }}</div>
              <div style="color: #909399; margin-top: 5px;">活动总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center;">
            <el-icon :size="40" color="#e6a23c"><Edit /></el-icon>
            <div style="margin-top: 10px;">
              <div style="font-size: 28px; font-weight: bold; color: #e6a23a;">{{ stats.totalRegistrations }}</div>
              <div style="color: #909399; margin-top: 5px;">总报名数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="text-align: center;">
            <el-icon :size="40" color="#f56c6c"><CircleCheck /></el-icon>
            <div style="margin-top: 10px;">
              <div style="font-size: 28px; font-weight: bold; color: #f56c6c;">{{ stats.totalCheckIns }}</div>
              <div style="color: #909399; margin-top: 5px;">总签到数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header>
        <span>最近活动</span>
      </template>
      <el-table :data="recentActivities" stripe style="width: 100%">
        <el-table-column prop="name" label="活动名称" />
        <el-table-column prop="location" label="活动地点" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ongoing' ? 'success' : row.status === 'ended' ? 'info' : 'warning'">
              {{ row.status === 'ongoing' ? '进行中' : row.status === 'ended' ? '已结束' : '待审核' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报名情况" width="120">
          <template #default="{ row }">
            {{ row.currentParticipants || 0 }} / {{ row.maxParticipants }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getActivities } from '../../api/modules/activity'
import { getStats } from '../../api/modules/stats'

const stats = ref({
  totalUsers: 0,
  totalActivities: 0,
  totalRegistrations: 0,
  totalCheckIns: 0
})

const recentActivities = ref([])

onMounted(async () => {
  try {
    const [statsRes, actRes] = await Promise.all([
      getStats(),
      getActivities({ page: 1, size: 5 })
    ])
    if (statsRes.code === 200) {
      stats.value = statsRes.data
    }
    if (actRes.code === 200) {
      recentActivities.value = actRes.data.records || []
    }
  } catch (e) {
    console.error(e)
  }
})
</script>
