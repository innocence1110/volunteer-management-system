<template>
  <div>
    <div class="page-header">
      <h2>活动管理</h2>
    </div>

    <el-card>
      <el-table :data="activities" stripe v-loading="loading">
        <el-table-column prop="name" label="活动名称" min-width="150" />
        <el-table-column prop="location" label="地点" width="150" />
        <el-table-column prop="startTime" label="开始时间" width="170" />
        <el-table-column prop="endTime" label="结束时间" width="170" />
        <el-table-column label="报名" width="100" align="center">
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
        <el-table-column label="操作" width="250" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="openEditDialog(row)">修改</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
            <el-button size="small" @click="viewRegistrations(row)">报名</el-button>
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

    <!-- 修改活动弹窗 -->
    <el-dialog v-model="editVisible" title="修改活动" width="600px">
      <el-form ref="editFormRef" :model="editForm" :rules="rules" label-width="100px">
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="活动描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="editForm.startTime" type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="editForm.endTime" type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="editForm.location" />
        </el-form-item>
        <el-form-item label="招募人数" prop="maxParticipants">
          <el-input-number v-model="editForm.maxParticipants" :min="1" />
        </el-form-item>
        <el-form-item label="签到方式">
          <el-radio-group v-model="editForm.checkInType">
            <el-radio label="button">按钮</el-radio>
            <el-radio label="code">数字码</el-radio>
            <el-radio label="image">图片</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="editForm.checkInType === 'code'" label="验证码">
          <el-input v-model="editForm.checkInCode" maxlength="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="editLoading" @click="handleEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- 报名列表弹窗 -->
    <el-dialog v-model="regVisible" title="报名与签到情况" width="900px">
      <el-table :data="registrations" stripe>
        <el-table-column prop="userName" label="姓名" width="100" />
        <el-table-column prop="userPhone" label="电话" width="120" />
        <el-table-column prop="createTime" label="报名时间" width="170" />
        <el-table-column label="签到状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.checkedIn" type="success" size="small">已签到</el-tag>
            <el-tag v-else type="danger" size="small">未签到</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="签到方式" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.checkedIn">
              {{ row.checkInType === 'button' ? '按钮' : row.checkInType === 'code' ? '数字码' : '图片' }}
            </span>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="签到时间" width="170">
          <template #default="{ row }">
            <span v-if="row.checkInTime">{{ row.checkInTime }}</span>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="签到地址" min-width="150">
          <template #default="{ row }">
            <span v-if="row.checkInAddress">{{ row.checkInAddress }}</span>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="签到图片" width="100" align="center">
          <template #default="{ row }">
            <el-button v-if="row.checkInImage" size="small" type="primary" text
              @click="previewImage(row.checkInImage)">查看</el-button>
            <span v-else style="color: #c0c4cc;">-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 图片预览弹窗 -->
    <el-dialog v-model="imagePreviewVisible" title="签到图片" width="500px">
      <div style="text-align: center;">
        <img :src="previewImageUrl" style="max-width: 100%; max-height: 400px; border-radius: 4px;" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyActivities, updateActivity, deleteActivity } from '../../api/modules/activity'
import { getActivityRegistrations } from '../../api/modules/registration'

const activities = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 编辑相关
const editVisible = ref(false)
const editLoading = ref(false)
const editFormRef = ref(null)
const editingId = ref(null)
const editForm = reactive({
  name: '', description: '', startTime: '', endTime: '',
  location: '', maxParticipants: 30, checkInType: 'button', checkInCode: '',
  pointsReward: 10
})

// 报名列表相关
const regVisible = ref(false)
const registrations = ref([])

// 图片预览相关
const imagePreviewVisible = ref(false)
const previewImageUrl = ref('')

function previewImage(path) {
  previewImageUrl.value = path
  imagePreviewVisible.value = true
}

const rules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择时间', trigger: 'change' }],
  location: [{ required: true, message: '请输入地点', trigger: 'blur' }],
  maxParticipants: [{ required: true, message: '请输入人数', trigger: 'change' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await getMyActivities({ page: currentPage.value, size: pageSize.value })
    if (res.code === 200) {
      activities.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

function openEditDialog(row) {
  editingId.value = row.id
  Object.assign(editForm, {
    name: row.name, description: row.description || '',
    startTime: row.startTime, endTime: row.endTime,
    location: row.location, maxParticipants: row.maxParticipants,
    checkInType: row.checkInType, checkInCode: row.checkInCode || '',
    pointsReward: row.pointsReward || 10
  })
  editVisible.value = true
}

async function handleEdit() {
  const valid = await editFormRef.value.validate().catch(() => false)
  if (!valid) return
  editLoading.value = true
  try {
    const res = await updateActivity(editingId.value, editForm)
    if (res.code === 200) {
      ElMessage.success('修改成功')
      editVisible.value = false
      loadData()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) { ElMessage.error('修改失败') }
  finally { editLoading.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该活动？删除后不可恢复。', '确认删除', {
      confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning'
    })
    const res = await deleteActivity(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadData()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) { /* 取消 */ }
}

async function viewRegistrations(row) {
  try {
    const res = await getActivityRegistrations(row.id, { page: 1, size: 50 })
    if (res.code === 200) {
      registrations.value = res.data.records || []
      regVisible.value = true
    }
  } catch (e) { ElMessage.error('获取报名列表失败') }
}

onMounted(loadData)
</script>
