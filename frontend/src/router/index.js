import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../components/Layout.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/Login.vue')
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
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router
