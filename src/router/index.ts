/**
 * Vue Router 路由配置
 * 包含登录、注册、忘记密码等路由及路由守卫
 */

import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

/**
 * 路由配置
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/HomeView.vue'),
      meta: {
        title: '首页',
        requiresAuth: true // 需要登录
      }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/index.vue'),
      meta: {
        title: '用户登录',
        guestOnly: true // 仅未登录用户可访问
      }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/register/index.vue'),
      meta: {
        title: '用户注册',
        guestOnly: true
      }
    },
    {
      path: '/forgot-password',
      name: 'forgot-password',
      component: () => import('@/views/forgot-password/index.vue'),
      meta: {
        title: '忘记密码',
        guestOnly: true
      }
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('@/views/AboutView.vue'),
      meta: {
        title: '关于',
        requiresAuth: true
      }
    }
  ],
  // 滚动行为配置
  scrollBehavior() {
    return { top: 0 }
  }
})

/**
 * 路由守卫 - 全局前置守卫
 * 处理登录状态检查、权限验证、页面标题设置
 */
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  const title = to.meta.title as string
  if (title) {
    document.title = `${title} - Admin UI`
  }
  
  // 获取用户 Store
  const userStore = useUserStore()
  
  // 检查是否已登录（优先从本地存储恢复）
  const isLoggedIn = userStore.isLoggedIn || userStore.restoreLoginState()
  
  // 1. 处理需要登录的页面
  if (to.meta.requiresAuth && !isLoggedIn) {
    // 未登录，重定向到登录页，并携带原目标地址
    next({
      path: '/login',
      query: { redirect: to.fullPath }
    })
    return
  }
  
  // 2. 处理仅游客可访问的页面（登录页、注册页等）
  if (to.meta.guestOnly && isLoggedIn) {
    // 已登录，重定向到首页或原目标页
    const redirect = to.query.redirect as string
    next(redirect || '/')
    return
  }
  
  // 3. 正常放行
  next()
})

/**
 * 路由守卫 - 全局后置钩子
 * 可用于页面统计、日志记录等
 */
router.afterEach((to) => {
  // 可在此处添加页面访问日志
  // console.log(`Navigated to: ${to.fullPath}`)
})

/**
 * 路由错误处理
 */
router.onError((error) => {
  console.error('Router error:', error)
})

export default router
