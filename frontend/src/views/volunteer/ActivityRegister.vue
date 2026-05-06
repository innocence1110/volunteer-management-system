<template>
  <div>
    <div class="page-header"><h2>活动报名</h2></div>
    <el-card>
      <el-form :inline="true" style="margin-bottom: 20px;">
        <el-form-item>
          <el-input v-model="keyword" placeholder="输入活动名称搜索" clearable @keyup.enter="loadData" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="activities" stripe v-loading="loading">
        <el-table-column prop="name" label="活动名称" min-width="150" />
        <el-table-column prop="location" label="地点" width="150" />
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column label="报名" width="120" align="center">
          <template #default="{ row }">
            {{ row.currentParticipants || 0 }} / {{ row.maxParticipants }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ongoing' ? 'success' : 'info'" size="small">
              {{ row.status === 'ongoing' ? '进行中' : '已结束' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="showDetail(row)">详情</el-button>
            <el-button v-if="canRegister(row)" size="small" type="success"
              @click="handleRegister(row)">报名</el-button>
            <el-button v-if="canCancel(row)" size="small" type="warning"
              @click="handleCancel(row)">取消</el-button>
            <el-tag v-if="row._registered" size="small" type="success">已报名</el-tag>
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
      <template #footer>
        <el-button v-if="canRegister(detail)" type="primary" @click="handleRegister(detail)">报名</el-button>
        <el-button v-if="canCancel(detail)" type="warning" @click="handleCancel(detail)">取消报名</el-button>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getActivities } from '../../api/modules/activity'
import { registerActivity, cancelRegistration, checkRegistration } from '../../api/modules/registration'

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
      const records = res.data.records || []
      // 检查每个活动的报名状态
      for (const act of records) {
        try {
          const regRes = await checkRegistration(act.id)
          act._registered = regRes.data === true
        } catch { act._registered = false }
      }
      activities.value = records
      total.value = res.data.total || 0
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function canRegister(row) {
  return row.status === 'ongoing' && !row._registered &&
    (row.currentParticipants || 0) < row.maxParticipants
}

function canCancel(row) {
  if (!row._registered) return false
  if (!row.startTime) return false
  const diff = new Date(row.startTime).getTime() - Date.now()
  return diff > 2 * 60 * 60 * 1000 // 超过2小时
}

function showDetail(row) {
  detail.value = { ...row }
  detailVisible.value = true
}

async function handleRegister(row) {
  try {
    await ElMessageBox.confirm('确认报名该活动？', '确认报名', { type: 'info' })
    const res = await registerActivity(row.id)
    if (res.code === 200) {
      ElMessage.success('报名成功')
      loadData()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) { /* 取消 */ }
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm('确认取消报名？', '确认取消', { type: 'warning' })
    const res = await cancelRegistration(row.id)
    if (res.code === 200) {
      ElMessage.success('取消成功')
      loadData()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) { /* 取消 */ }
}

onMounted(loadData)
</script>
