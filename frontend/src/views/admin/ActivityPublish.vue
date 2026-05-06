<template>
  <div>
    <div class="page-header">
      <h2>活动发布</h2>
    </div>

    <el-card>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px" style="max-width: 700px;">
        <el-form-item label="活动名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="活动描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入活动描述" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间"
            value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间"
            value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="form.location" placeholder="请输入活动地点" />
        </el-form-item>
        <el-form-item label="招募人数" prop="maxParticipants">
          <el-input-number v-model="form.maxParticipants" :min="1" :max="9999" />
        </el-form-item>
        <el-form-item label="签到方式" prop="checkInType">
          <el-radio-group v-model="form.checkInType">
            <el-radio label="button">按钮签到</el-radio>
            <el-radio label="code">数字码签到</el-radio>
            <el-radio label="image">图片签到</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.checkInType === 'code'" label="验证码" prop="checkInCode">
          <el-input v-model="form.checkInCode" placeholder="请输入4位数字验证码" maxlength="4" />
        </el-form-item>
        <el-form-item label="奖励积分" prop="pointsReward">
          <el-input-number v-model="form.pointsReward" :min="0" :max="100" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handlePublish">确认发布</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { publishActivity } from '../../api/modules/activity'

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  name: '',
  description: '',
  startTime: '',
  endTime: '',
  location: '',
  maxParticipants: 30,
  checkInType: 'button',
  checkInCode: '',
  pointsReward: 10
})

const rules = {
  name: [{ required: true, message: '请输入活动名称', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  maxParticipants: [{ required: true, message: '请输入招募人数', trigger: 'change' }],
  checkInType: [{ required: true, message: '请选择签到方式', trigger: 'change' }]
}

async function handlePublish() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await publishActivity(form)
    if (res.code === 200) {
      ElMessage.success('活动发布成功')
      resetForm()
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error('发布失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  formRef.value?.resetFields()
}
</script>
