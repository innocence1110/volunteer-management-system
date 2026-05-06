<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2>📝 用户注册</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input v-model="form.phone" placeholder="请输入电话号码" prefix-icon="Phone" />
        </el-form-item>
        <el-form-item prop="account">
          <el-input v-model="form.account" placeholder="请输入登录账号" prefix-icon="UserFilled" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码（至少6位）"
            prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="志愿者" value="volunteer" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="handleRegister">
            确认注册
          </el-button>
        </el-form-item>
      </el-form>
      <div style="text-align: center;">
        <span style="color: #909399;">已有账号？</span>
        <el-link type="primary" @click="$router.push('/login')">返回登录</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '../../api/modules/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  name: '',
  phone: '',
  account: '',
  password: '',
  role: 'volunteer'
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入电话号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '电话号码格式不正确', trigger: 'blur' }
  ],
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

async function handleRegister() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await register(form)
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.message)
    }
  } catch (e) {
    ElMessage.error('注册失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>
