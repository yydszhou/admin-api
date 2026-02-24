/**
 * Vue Router 路由配置
 * 包含登录、注册、忘记密码等路由及路由守卫
 */

import { createRouter, createWebHistory } from 'vue-router'

/**
 * 路由配置
 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'layout',
      component: () => import('@/components/layout/Layout.vue'),
      children: [
        {
          path: '',
          name: 'home',
          component: () => import('@/views/dashboard/index.vue'),
          meta: {
            title: '仪表盘'
          }
        },
        {
          path: '/data',
          name: 'data',
          component: () => import('@/views/dashboard/index.vue'),
          meta: {
            title: '数据'
          }
        },
        {
          path: '/about',
          name: 'about',
          component: () => import('@/views/AboutView.vue'),
          meta: {
            title: '关于'
          }
        }
      ]
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/login/index.vue'),
      meta: {
        title: '用户登录'
      }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/register/index.vue'),
      meta: {
        title: '用户注册'
      }
    },
    {
      path: '/forgot-password',
      name: 'forgot-password',
      component: () => import('@/views/forgot-password/index.vue'),
      meta: {
        title: '忘记密码'
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
 * 处理页面标题设置
 */
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  const title = to.meta.title as string
  if (title) {
    document.title = `${title} - Admin UI`
  }
  
  // 直接放行，不需要登录验证
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
