import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../components/Layout.vue'
import { useUserStore } from '../store/user'
import { usePreferencesStore } from '../store/preferences'
import i18n from '../locales'
import errorHandler from '../utils/errorHandler'
import { startSessionTimeoutCheck, stopSessionTimeoutCheck } from '../utils/sessionTimeout'
import ErrorBoundary from '../views/error/ErrorBoundary.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/login/Register.vue')
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('../views/login/ForgotPassword.vue')
  },
  {
    path: '/error/500',
    name: 'Error500',
    component: () => import('../views/error/Error500.vue')
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/Dashboard.vue'),
        meta: { title: 'dashboard', roles: [1, 2, 3] }
      },
      {
        path: 'question',
        name: 'QuestionList',
        component: () => import('../views/question/QuestionList.vue'),
        meta: { title: 'question', roles: [1, 2] }
      },
      {
        path: 'subject',
        name: 'SubjectList',
        component: () => import('../views/subject/SubjectList.vue'),
        meta: { title: 'subject', roles: [1, 2] }
      },
      {
        path: 'paper',
        name: 'PaperList',
        component: () => import('../views/paper/PaperList.vue'),
        meta: { title: 'paper', roles: [1, 2] }
      },
      {
        path: 'exam',
        name: 'ExamList',
        component: () => import('../views/exam/ExamList.vue'),
        meta: { title: 'exam', roles: [1, 2, 3] }
      },
      {
        path: 'exam/take/:id',
        name: 'ExamTake',
        component: () => import('../views/exam/ExamTake.vue'),
        meta: { title: 'examTake', roles: [1, 2, 3] }
      },
      {
        path: 'score',
        name: 'ScoreList',
        component: () => import('../views/score/ScoreList.vue'),
        meta: { title: 'score', roles: [1, 2] }
      },
      {
        path: 'personal-score',
        name: 'PersonalScore',
        component: () => import('../views/score/PersonalScore.vue'),
        meta: { title: 'personalScore', roles: [1, 2, 3] }
      },
      {
        path: 'question-analysis',
        name: 'QuestionAnalysis',
        component: () => import('../views/questionAnalysis/QuestionAnalysis.vue'),
        meta: { title: 'questionAnalysis', roles: [1, 2] }
      },
      {
        path: 'user',
        name: 'UserList',
        component: () => import('../views/user/UserList.vue'),
        meta: { title: 'user', roles: [1] }
      },
      {
        path: 'system-config',
        name: 'SystemConfigList',
        component: () => import('../views/systemConfig/SystemConfigList.vue'),
        meta: { title: 'systemConfig', roles: [1] }
      },
      {
        path: 'operation-log',
        name: 'OperationLogList',
        component: () => import('../views/operationLog/OperationLogList.vue'),
        meta: { title: 'operationLog', roles: [1] }
      },
      {
        path: 'log-archive',
        name: 'LogArchiveList',
        component: () => import('../views/logArchive/LogArchiveList.vue'),
        meta: { title: 'logArchive', roles: [1] }
      },
      {
        path: 'log-masking',
        name: 'LogMasking',
        component: () => import('../views/logMasking/LogMasking.vue'),
        meta: { title: 'logMasking', roles: [1] }
      },
      {
        path: 'video',
        name: 'VideoList',
        component: () => import('../views/video/VideoList.vue'),
        meta: { title: 'video', roles: [1, 2, 3] }
      },
      {
        path: 'video/detail',
        name: 'VideoDetail',
        component: () => import('../views/video/VideoDetail.vue'),
        meta: { title: 'videoDetail', roles: [1, 2, 3] }
      },
      {
        path: 'video-manage',
        name: 'VideoManage',
        component: () => import('../views/video/VideoManage.vue'),
        meta: { title: 'videoManage', roles: [1, 2] }
      },
      {
        path: 'video-category',
        name: 'VideoCategoryManage',
        component: () => import('../views/video/VideoCategoryManage.vue'),
        meta: { title: 'videoCategory', roles: [1, 2] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/profile/Profile.vue'),
        meta: { title: 'profile', roles: [1, 2, 3] }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('../views/settings/Settings.vue'),
        meta: { title: 'settings', roles: [1, 2, 3] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'Error404',
    component: () => import('../views/error/Error404.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')
  const publicPaths = ['/login', '/register', '/forgot-password', '/error/500']

  if (!publicPaths.includes(to.path) && !token) {
    stopSessionTimeoutCheck()
    next('/login')
    return
  }

  if (token && !publicPaths.includes(to.path) && to.name !== 'Error404') {
    const userStore = useUserStore()
    const preferencesStore = usePreferencesStore()
    if (!userStore.token) {
      userStore.token = token
    }
    if (!userStore.userInfo) {
      try {
        await userStore.getUserInfo()
      } catch (err) {
        userStore.logout()
        stopSessionTimeoutCheck()
        next('/login')
        return
      }
    }
    if (!preferencesStore.loaded) {
      try {
        await preferencesStore.fetchPreferences()
      } catch (err) {
        console.error('Load preferences failed:', err)
      }
    }

    if (to.meta?.roles && to.meta.roles.length > 0) {
      if (!to.meta.roles.includes(userStore.userInfo?.role)) {
        errorHandler.showErrorToast('无权限访问')
        next('/dashboard')
        return
      }
    }

    startSessionTimeoutCheck()
  } else if (publicPaths.includes(to.path)) {
    stopSessionTimeoutCheck()
  }

  next()
})

router.afterEach((to, from, failure) => {
  if (failure) {
    errorHandler.handleRouterError(failure, to, from)
  }
  const systemName = i18n.global.t('login.title')
  if (to.meta?.title) {
    const titleKey = `menu.${to.meta.title}`
    const pageTitle = i18n.global.te(titleKey) ? i18n.global.t(titleKey) : to.meta.title
    document.title = pageTitle + ' - ' + systemName
  } else {
    document.title = systemName
  }
})

router.onError((error) => {
  errorHandler.handleRouterError(error, router.currentRoute.value, router.currentRoute.value)
  if (error.message.includes('Failed to fetch dynamically imported module') ||
      error.message.includes('Loading chunk')) {
    errorHandler.showErrorDialog(
      '页面加载失败，可能是版本更新导致，请刷新页面',
      null,
      '加载错误'
    ).then(() => {
      window.location.reload()
    }).catch(() => {
      window.location.reload()
    })
  }
})

export default router
