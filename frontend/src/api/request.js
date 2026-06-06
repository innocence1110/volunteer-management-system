import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '',
  timeout: 15000,
})

// 请求拦截器 - 自动添加 Token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器 - 统一处理错误
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 401) {
      ElMessage.error(res.message || '登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  (error) => {
    const msg = error.response?.data?.message
      || (typeof error.response?.data === 'string' ? error.response.data : null)
      || error.message
      || '网络请求失败'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request
