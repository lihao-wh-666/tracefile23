import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../components/Layout.vue'
import { useUserStore } from '../store/user'
import { ElMessage } from 'element-plus'

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
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/dashboard/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'question',
        name: 'QuestionList',
        component: () => import('../views/question/QuestionList.vue'),
        meta: { title: '题库管理' }
      },
      {
        path: 'paper',
        name: 'PaperList',
        component: () => import('../views/paper/PaperList.vue'),
        meta: { title: '试卷管理' }
      },
      {
        path: 'exam',
        name: 'ExamList',
        component: () => import('../views/exam/ExamList.vue'),
        meta: { title: '考试管理' }
      },
      {
        path: 'exam/take/:id',
        name: 'ExamTake',
        component: () => import('../views/exam/ExamTake.vue'),
        meta: { title: '参加考试' }
      },
      {
        path: 'score',
        name: 'ScoreList',
        component: () => import('../views/score/ScoreList.vue'),
        meta: { title: '成绩统计' }
      },
      {
        path: 'personal-score',
        name: 'PersonalScore',
        component: () => import('../views/score/PersonalScore.vue'),
        meta: { title: '个人成绩台账' }
      },
      {
        path: 'user',
        name: 'UserList',
        component: () => import('../views/user/UserList.vue'),
        meta: { title: '用户管理', roles: [1] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')
  const publicPaths = ['/login', '/register', '/forgot-password']
  
  if (!publicPaths.includes(to.path) && !token) {
    next('/login')
    return
  }

  if (token && !publicPaths.includes(to.path)) {
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
        ElMessage.error('无权限访问')
        next('/dashboard')
        return
      }
    }
  }
  
  next()
})

export default router
