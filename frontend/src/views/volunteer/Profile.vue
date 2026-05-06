<template>
  <div>
    <div class="page-header"><h2>信息管理</h2></div>
    <el-card style="max-width: 600px;">
      <el-form :model="form" label-width="80px" v-loading="loading">
        <el-form-item label="姓名">
          <el-input :value="form.name" disabled />
        </el-form-item>
        <el-form-item label="角色">
          <el-tag type="success">志愿者</el-tag>
        </el-form-item>
        <el-form-item label="积分">
          <el-tag type="warning" size="large">{{ form.points || 0 }} 分</el-tag>
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="form.major" placeholder="选填" />
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="form.age" :min="10" :max="100" />
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="form.studentId" placeholder="选填" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getProfile, updateProfile } from '../../api/modules/user'

const form = ref({})
const loading = ref(false)
const saving = ref(false)

async function loadProfile() {
  loading.value = true
  try {
    const res = await getProfile()
    if (res.code === 200) form.value = res.data
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function handleSave() {
  saving.value = true
  try {
    const res = await updateProfile({
      phone: form.value.phone,
      major: form.value.major,
      age: form.value.age,
      studentId: form.value.studentId
    })
    if (res.code === 200) ElMessage.success('修改成功')
    else ElMessage.error(res.message)
  } catch (e) { ElMessage.error('修改失败') }
  finally { saving.value = false }
}

onMounted(loadProfile)
</script>
