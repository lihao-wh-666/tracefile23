<template>
  <div class="dashboard-container">
    <h2 class="page-title">系统概览</h2>

    <div class="stat-grid">
      <el-card shadow="hover" class="stat-card" :style="{ borderTop: `3px solid ${item.color}` }" v-for="item in statCards" :key="item.label">
        <div class="stat-card-body">
          <div class="stat-icon" :style="{ backgroundColor: item.color }">
            <el-icon :size="28"><component :is="item.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-count">{{ item.count }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </div>
      </el-card>
    </div>

    <el-card shadow="hover" class="table-card">
      <template #header>
        <span class="table-title">最近考试</span>
      </template>
      <div class="responsive-table">
        <el-table :data="recentExams" stripe style="width: 100%">
          <el-table-column prop="name" label="考试名称" min-width="160" />
          <el-table-column prop="paperName" label="试卷" min-width="140" class="mobile-hidden" />
          <el-table-column prop="startTime" label="开始时间" min-width="170" class="mobile-hidden" />
          <el-table-column prop="endTime" label="结束时间" min-width="170" class="mobile-hidden" />
          <el-table-column label="状态" min-width="100">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <div class="mobile-exam-list mobile-only">
      <div class="exam-item" v-for="item in recentExams" :key="item.id">
        <div class="exam-header">
          <span class="exam-name">{{ item.name }}</span>
          <el-tag :type="statusTagType(item.status)" size="small">{{ statusLabel(item.status) }}</el-tag>
        </div>
        <div class="exam-info">
          <span class="info-item">试卷: {{ item.paperName }}</span>
        </div>
        <div class="exam-info">
          <span class="info-item">开始: {{ item.startTime }}</span>
        </div>
        <div class="exam-info">
          <span class="info-item">结束: {{ item.endTime }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Reading, Document, Notebook, EditPen } from '@element-plus/icons-vue'
import { getDashboardData } from '../../api/dashboard'

const subjectCount = ref(0)
const questionCount = ref(0)
const paperCount = ref(0)
const examCount = ref(0)
const recentExams = ref([])

const statCards = computed(() => [
  { label: '科目总数', count: subjectCount.value, icon: Reading, color: '#409eff' },
  { label: '题目总数', count: questionCount.value, icon: Document, color: '#67c23a' },
  { label: '试卷总数', count: paperCount.value, icon: Notebook, color: '#e6a23c' },
  { label: '考试总数', count: examCount.value, icon: EditPen, color: '#909399' }
])

const statusLabel = (status) => {
  const map = { 0: '未开始', 1: '进行中', 2: '已结束' }
  return map[status] ?? '未知'
}

const statusTagType = (status) => {
  const map = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] ?? 'info'
}

onMounted(async () => {
  const res = await getDashboardData()
  subjectCount.value = res.data.subjectCount
  questionCount.value = res.data.questionCount
  paperCount.value = res.data.paperCount
  examCount.value = res.data.examCount
  recentExams.value = res.data.recentExams || []
})
</script>

<style scoped>
.dashboard-container {
  width: 100%;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.stat-card-body {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  transition: transform 0.3s;
}

.stat-card:hover .stat-icon {
  transform: scale(1.1);
}

.stat-info {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.stat-count {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.table-card {
  border-radius: 8px;
}

.table-title {
  font-size: 16px;
  font-weight: bold;
}

.mobile-exam-list {
  margin-top: 16px;
}

.exam-item {
  background: #fff;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.exam-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.exam-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  flex: 1;
  margin-right: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.exam-info {
  margin-bottom: 4px;
}

.info-item {
  font-size: 13px;
  color: #606266;
}

@media screen and (max-width: 1200px) {
  .stat-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media screen and (max-width: 768px) {
  .stat-grid {
    grid-template-columns: 1fr;
    gap: 12px;
    margin-bottom: 16px;
  }

  .stat-card {
    border-radius: 8px;
  }

  .stat-card-body {
    gap: 12px;
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: 10px;
  }

  .stat-icon .el-icon {
    font-size: 24px;
  }

  .stat-count {
    font-size: 24px;
  }

  .stat-label {
    font-size: 13px;
  }

  .table-card {
    border-radius: 8px;
  }

  .table-title {
    font-size: 15px;
  }
}

@media screen and (max-width: 480px) {
  .stat-count {
    font-size: 22px;
  }

  .stat-label {
    font-size: 12px;
  }
}

@media screen and (min-width: 769px) and (max-width: 1024px) {
  .stat-icon {
    width: 52px;
    height: 52px;
  }

  .stat-count {
    font-size: 26px;
  }
}
</style>
