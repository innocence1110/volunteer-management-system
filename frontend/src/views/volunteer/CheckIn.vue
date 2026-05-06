<template>
  <div>
    <div class="page-header"><h2>活动签到</h2></div>
    <el-card>
      <el-table :data="activities" stripe v-loading="loading">
        <el-table-column prop="name" label="活动名称" min-width="150" />
        <el-table-column prop="location" label="地点" width="150" />
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
        <el-table-column label="签到方式" width="120" align="center">
          <template #default="{ row }">
            {{ row.checkInType === 'button' ? '按钮' : row.checkInType === 'code' ? '数字码' : '图片' }}
          </template>
        </el-table-column>
        <el-table-column label="签到状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row._checkedIn" type="success" size="small">已签到</el-tag>
            <el-tag v-else type="info" size="small">未签到</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="{ row }">
            <el-button v-if="canCheckIn(row) && !row._checkedIn" size="small" type="primary"
              @click="openCheckIn(row)">签到</el-button>
            <el-button v-else size="small" disabled>签到</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 签到弹窗 -->
    <el-dialog v-model="checkinVisible" :title="currentActivity.name + ' - 签到'" width="450px">
      <!-- 按钮签到 -->
      <div v-if="currentActivity.checkInType === 'button'">
        <p style="color: #606266; margin-bottom: 10px;">点击签到按钮，系统将尝试获取GPS定位。若定位失败，可手动输入签到地址。</p>
        <el-input v-model="manualAddress" placeholder="手动输入签到地址（可选）" clearable style="margin-bottom: 15px;" />
        <div style="text-align: center;">
          <el-button type="primary" size="large" :loading="checkinLoading" @click="handleButtonCheckIn">
            <el-icon><CircleCheck /></el-icon> 点击签到
          </el-button>
        </div>
      </div>

      <!-- 数字码签到 -->
      <div v-if="currentActivity.checkInType === 'code'">
        <p style="color: #606266;">请输入管理员提供的4位数字验证码</p>
        <el-input v-model="checkInCode" placeholder="请输入验证码" maxlength="4" size="large" />
        <div style="text-align: right; margin-top: 20px;">
          <el-button @click="checkinVisible = false">取消</el-button>
          <el-button type="primary" :loading="checkinLoading" @click="handleCodeCheckIn">确认签到</el-button>
        </div>
      </div>

      <!-- 图片签到 -->
      <div v-if="currentActivity.checkInType === 'image'">
        <p style="color: #606266;">上传现场拍摄的照片完成签到</p>
        <el-upload
          ref="uploadRef"
          action="#"
          :auto-upload="false"
          :limit="1"
          accept="image/*"
          :on-change="handleFileChange"
          list-type="picture"
        >
          <el-button type="primary">选择照片</el-button>
          <template #tip>
            <div style="color: #909399; font-size: 12px;">支持 JPG/PNG 格式，不超过 5MB</div>
          </template>
        </el-upload>
        <div style="text-align: right; margin-top: 20px;">
          <el-button @click="checkinVisible = false">取消</el-button>
          <el-button type="primary" :loading="checkinLoading" @click="handleImageCheckIn">上传并签到</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyRegistrations } from '../../api/modules/registration'
import { getActivityDetail } from '../../api/modules/activity'
import { buttonCheckIn, codeCheckIn, imageCheckIn, checkCheckInStatus } from '../../api/modules/checkin'

const activities = ref([])
const loading = ref(false)
const checkinVisible = ref(false)
const checkinLoading = ref(false)
const currentActivity = ref({})
const checkInCode = ref('')
const uploadFile = ref(null)
const manualAddress = ref('')

async function loadData() {
  loading.value = true
  activities.value = [] // 先清空旧数据，避免重复
  try {
    const res = await getMyRegistrations({ page: 1, size: 50 })
    if (res.code === 200) {
      const records = res.data.records || []
      // 获取活动详情
      for (const reg of records) {
        try {
          const actRes = await getActivityDetail(reg.activityId)
          if (actRes.code === 200) {
            const act = actRes.data
            act._registered = true
            // 检查签到状态
            try {
              const ciRes = await checkCheckInStatus(act.id)
              act._checkedIn = ciRes.data === true
            } catch { act._checkedIn = false }
            activities.value.push(act)
          }
        } catch (e) { console.error(e) }
      }
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function canCheckIn(row) {
  if (!row.startTime || !row.endTime) return false
  const now = new Date().getTime()
  const start = new Date(row.startTime).getTime()
  const end = new Date(row.endTime).getTime()
  return now >= start && now <= end && row.status === 'ongoing'
}

function openCheckIn(row) {
  currentActivity.value = row
  checkInCode.value = ''
  uploadFile.value = null
  checkinVisible.value = true
}

function handleFileChange(file) {
  uploadFile.value = file.raw
}

async function handleButtonCheckIn() {
  checkinLoading.value = true
  try {
    // 优先使用手动输入的地址
    let gps = manualAddress.value.trim()
    if (!gps && navigator.geolocation) {
      // 尝试自动获取GPS
      try {
        const pos = await new Promise((resolve, reject) => {
          navigator.geolocation.getCurrentPosition(resolve, reject, { timeout: 5000 })
        })
        gps = `${pos.coords.latitude}, ${pos.coords.longitude}`
      } catch {
        gps = 'GPS未获取-请手动输入地址'
      }
    } else if (!gps) {
      gps = '未提供定位信息'
    }
    const res = await buttonCheckIn(currentActivity.value.id, { gpsAddress: gps })
    if (res.code === 200) {
      ElMessage.success('签到成功')
      checkinVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) { ElMessage.error('签到失败') }
  finally { checkinLoading.value = false }
}

async function handleCodeCheckIn() {
  if (!checkInCode.value || checkInCode.value.length !== 4) {
    ElMessage.warning('请输入4位数字验证码')
    return
  }
  checkinLoading.value = true
  try {
    const res = await codeCheckIn(currentActivity.value.id, { checkInCode: checkInCode.value })
    if (res.code === 200) {
      ElMessage.success('签到成功')
      checkinVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) { ElMessage.error('签到失败') }
  finally { checkinLoading.value = false }
}

async function handleImageCheckIn() {
  if (!uploadFile.value) {
    ElMessage.warning('请选择照片')
    return
  }
  if (uploadFile.value.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过5MB')
    return
  }
  checkinLoading.value = true
  try {
    const res = await imageCheckIn(currentActivity.value.id, uploadFile.value)
    if (res.code === 200) {
      ElMessage.success('签到成功')
      checkinVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) { ElMessage.error('签到失败') }
  finally { checkinLoading.value = false }
}

onMounted(loadData)
</script>
