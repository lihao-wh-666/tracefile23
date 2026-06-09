import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../components/Layout.vue'
import { useUserStore } from '../store/user'
import errorHandler from '../utils/errorHandler'
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
        meta: { title: '首页', roles: [1, 2, 3] }
      },
      {
        path: 'question',
        name: 'QuestionList',
        component: () => import('../views/question/QuestionList.vue'),
        meta: { title: '题库管理', roles: [1, 2] }
      },
      {
        path: 'paper',
        name: 'PaperList',
        component: () => import('../views/paper/PaperList.vue'),
        meta: { title: '试卷管理', roles: [1, 2] }
      },
      {
        path: 'exam',
        name: 'ExamList',
        component: () => import('../views/exam/ExamList.vue'),
        meta: { title: '考试管理', roles: [1, 2, 3] }
      },
      {
        path: 'exam/take/:id',
        name: 'ExamTake',
        component: () => import('../views/exam/ExamTake.vue'),
        meta: { title: '参加考试', roles: [1, 2, 3] }
      },
      {
        path: 'score',
        name: 'ScoreList',
        component: () => import('../views/score/ScoreList.vue'),
        meta: { title: '成绩统计', roles: [1, 2] }
      },
      {
        path: 'personal-score',
        name: 'PersonalScore',
        component: () => import('../views/score/PersonalScore.vue'),
        meta: { title: '个人成绩台账', roles: [1, 2, 3] }
      },
      {
        path: 'user',
        name: 'UserList',
        component: () => import('../views/user/UserList.vue'),
        meta: { title: '用户管理', roles: [1] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/profile/Profile.vue'),
        meta: { title: '个人中心', roles: [1, 2, 3] }
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
    next('/login')
    return
  }

  if (token && !publicPaths.includes(to.path) && to.name !== 'Error404') {
    const userStore = useUserStore()
    if (!userStore.userInfo) {
      try {
        await userStore.getUserInfo()
      } catch (err) {
        userStore.logout()
        next('/login')
        return
      }
    }

    if (to.meta?.roles && to.meta.roles.length > 0) {
      if (!to.meta.roles.includes(userStore.userInfo?.role)) {
        errorHandler.showErrorToast('无权限访问')
        next('/dashboard')
        return
      }
    }
  }

  next()
})

router.afterEach((to, from, failure) => {
  if (failure) {
    errorHandler.handleRouterError(failure, to, from)
  }
  if (to.meta?.title) {
    document.title = to.meta.title + ' - 在线考试系统'
  } else {
    document.title = '在线考试系统'
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
