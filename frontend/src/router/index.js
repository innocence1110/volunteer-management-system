import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/auth/Register.vue'),
    meta: { requiresAuth: false }
  },
  // 管理员路由
  {
    path: '/admin',
    component: () => import('../components/Layout.vue'),
    meta: { requiresAuth: true, role: 'admin' },
    redirect: '/admin/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: { title: '系统概览' }
      },
      {
        path: 'activity-publish',
        name: 'ActivityPublish',
        component: () => import('../views/admin/ActivityPublish.vue'),
        meta: { title: '活动发布' }
      },
      {
        path: 'activity-manage',
        name: 'ActivityManage',
        component: () => import('../views/admin/ActivityManage.vue'),
        meta: { title: '活动管理' }
      },
      {
        path: 'activity-browse',
        name: 'AdminActivityBrowse',
        component: () => import('../views/admin/ActivityBrowse.vue'),
        meta: { title: '活动浏览' }
      },
      {
        path: 'profile',
        name: 'AdminProfile',
        component: () => import('../views/admin/Profile.vue'),
        meta: { title: '信息管理' }
      },
      {
        path: 'notifications',
        name: 'AdminNotifications',
        component: () => import('../views/admin/Notifications.vue'),
        meta: { title: '通知消息' }
      }
    ]
  },
  // 志愿者路由
  {
    path: '/volunteer',
    component: () => import('../components/Layout.vue'),
    meta: { requiresAuth: true, role: 'volunteer' },
    redirect: '/volunteer/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'VolunteerDashboard',
        component: () => import('../views/volunteer/Dashboard.vue'),
        meta: { title: '我的主页' }
      },
      {
        path: 'activity-browse',
        name: 'VolunteerActivityBrowse',
        component: () => import('../views/volunteer/ActivityBrowse.vue'),
        meta: { title: '活动浏览' }
      },
      {
        path: 'activity-register',
        name: 'ActivityRegister',
        component: () => import('../views/volunteer/ActivityRegister.vue'),
        meta: { title: '活动报名' }
      },
      {
        path: 'checkin',
        name: 'CheckIn',
        component: () => import('../views/volunteer/CheckIn.vue'),
        meta: { title: '活动签到' }
      },
      {
        path: 'profile',
        name: 'VolunteerProfile',
        component: () => import('../views/volunteer/Profile.vue'),
        meta: { title: '信息管理' }
      },
      {
        path: 'notifications',
        name: 'VolunteerNotifications',
        component: () => import('../views/volunteer/Notifications.vue'),
        meta: { title: '通知消息' }
      }
    ]
  },
  {
    path: '/',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')

  if (to.meta.requiresAuth !== false && !token) {
    next('/login')
  } else if (to.meta.role && user && user.role !== to.meta.role) {
    next('/' + user.role + '/dashboard')
  } else {
    next()
  }
})

export default router
