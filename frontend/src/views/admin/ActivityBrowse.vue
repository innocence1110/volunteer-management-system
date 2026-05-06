<template>
  <div>
    <div class="page-header">
      <h2>活动浏览</h2>
    </div>
    <el-card>
      <el-table :data="activities" stripe v-loading="loading">
        <el-table-column prop="name" label="活动名称" min-width="150" />
        <el-table-column prop="location" label="地点" width="150" />
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
        <el-table-column label="报名情况" width="120" align="center">
          <template #default="{ row }">
            {{ row.currentParticipants || 0 }} / {{ row.maxParticipants }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ongoing' ? 'success' : 'info'">
              {{ row.status === 'ongoing' ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布者" width="100" />
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
import { getActivities } from '../../api/modules/activity'

const activities = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

async function loadData() {
  loading.value = true
  try {
    const res = await getActivities({ page: currentPage.value, size: pageSize.value })
    if (res.code === 200) {
      activities.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

onMounted(loadData)
</script>
