<template>
  <div>
    <div class="page-header"><h2>我的主页</h2></div>

    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="8">
        <el-card shadow="hover">
          <div style="text-align: center;">
            <el-icon :size="40" color="#409eff"><Trophy /></el-icon>
            <div style="margin-top: 10px;">
              <div style="font-size: 36px; font-weight: bold; color: #409eff;">{{ profile.points || 0 }}</div>
              <div style="color: #909399; margin-top: 5px;">我的积分</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div style="text-align: center;">
            <el-icon :size="40" color="#67c23a"><Edit /></el-icon>
            <div style="margin-top: 10px;">
              <div style="font-size: 36px; font-weight: bold; color: #67c23a;">{{ regCount }}</div>
              <div style="color: #909399; margin-top: 5px;">已报名活动</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div style="text-align: center;">
            <el-icon :size="40" color="#e6a23c"><CircleCheck /></el-icon>
            <div style="margin-top: 10px;">
              <div style="font-size: 36px; font-weight: bold; color: #e6a23c;">{{ checkCount }}</div>
              <div style="color: #909399; margin-top: 5px;">已完成签到</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header><span>我报名的活动</span></template>
      <el-table :data="myRegistrations" stripe>
        <el-table-column prop="activityName" label="活动名称" />
        <el-table-column prop="createTime" label="报名时间" width="180" />
        <el-table-column label="状态" width="100" align="center">
          <template #default>
            <el-tag type="success" size="small">已报名</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProfile } from '../../api/modules/user'
import { getMyRegistrations } from '../../api/modules/registration'

const profile = ref({})
const myRegistrations = ref([])
const regCount = ref(0)
const checkCount = ref(0)

onMounted(async () => {
  try {
    const [profileRes, regRes] = await Promise.all([
      getProfile(),
      getMyRegistrations({ page: 1, size: 10 })
    ])
    if (profileRes.code === 200) profile.value = profileRes.data
    if (regRes.code === 200) {
      myRegistrations.value = regRes.data.records || []
      regCount.value = regRes.data.total || 0
    }
  } catch (e) { console.error(e) }
})
</script>
