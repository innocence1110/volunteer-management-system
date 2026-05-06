import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '../api/request'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const unreadCount = ref(0)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 'admin')
  const isVolunteer = computed(() => user.value?.role === 'volunteer')

  function setToken(newToken) {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  function setUser(userData) {
    user.value = userData
    localStorage.setItem('user', JSON.stringify(userData))
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  async function fetchUnreadCount() {
    try {
      const res = await request.get('/api/notifications/unread-count')
      if (res.code === 200) {
        unreadCount.value = res.data
      }
    } catch (e) {
      console.error('获取未读数失败', e)
    }
  }

  return { token, user, unreadCount, isLoggedIn, isAdmin, isVolunteer, setToken, setUser, logout, fetchUnreadCount }
})
