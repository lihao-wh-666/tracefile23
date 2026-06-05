<template>
  <div class="page-container">
    <h2 class="page-title">试卷管理</h2>

    <el-card shadow="hover" class="search-card">
      <el-form :model="searchForm" class="search-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="科目" class="search-item">
              <el-select v-model="searchForm.subjectId" placeholder="请选择科目" clearable style="width: 100%">
                <el-option v-for="s in subjectList" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="状态" class="search-item">
              <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 100%">
                <el-option label="草稿" value="草稿" />
                <el-option label="已发布" value="已发布" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="8">
            <div class="search-actions">
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button type="success" @click="handleAdd">
                <el-icon><Plus /></el-icon>
                新增试卷
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="table-card mobile-hidden">
      <div class="responsive-table">
        <el-table :data="tableData" border stripe style="width: 100%">
          <el-table-column prop="id" label="ID" width="60" align="center" />
          <el-table-column prop="name" label="试卷名称" min-width="160" show-overflow-tooltip />
          <el-table-column prop="subjectName" label="科目" width="100" align="center" />
          <el-table-column prop="totalScore" label="总分" width="80" align="center" />
          <el-table-column prop="passScore" label="及格分" width="80" align="center" />
          <el-table-column prop="duration" label="时长" width="80" align="center">
            <template #default="{ row }">{{ row.duration }}分钟</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.status === '已发布' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="220" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
              <el-button type="warning" link size="small" @click="handleEdit(row)" :disabled="row.status === '已发布'">编辑</el-button>
              <el-button type="success" link size="small" @click="handlePublish(row)" :disabled="row.status === '已发布'">发布</el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row)" :disabled="row.status === '已发布'">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="pagination-wrap"
        @size-change="fetchData"
        @current-change="fetchData"
      />
    </el-card>

    <div class="mobile-list mobile-only">
      <div class="list-item" v-for="item in tableData" :key="item.id">
        <div class="item-header">
          <span class="item-title">{{ item.name }}</span>
          <el-tag :type="item.status === '已发布' ? 'success' : 'info'" size="small">{{ item.status }}</el-tag>
        </div>
        <div class="item-info">
          <span class="info-label">科目:</span>
          <span class="info-value">{{ item.subjectName }}</span>
        </div>
        <div class="item-info-row">
          <span class="info-pill">总分: {{ item.totalScore }}</span>
          <span class="info-pill">及格: {{ item.passScore }}</span>
          <span class="info-pill">{{ item.duration }}分钟</span>
        </div>
        <div class="item-actions">
          <el-button type="primary" size="small" @click="handleView(item)">查看</el-button>
          <el-button type="warning" size="small" @click="handleEdit(item)" :disabled="item.status === '已发布'">编辑</el-button>
          <el-button type="success" size="small" @click="handlePublish(item)" :disabled="item.status === '已发布'">发布</el-button>
          <el-button type="danger" size="small" @click="handleDelete(item)" :disabled="item.status === '已发布'">删除</el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" class="responsive-dialog paper-dialog" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px" class="responsive-form">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="试卷名称" prop="name">
              <el-input v-model="form.name" placeholder="请输入试卷名称" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="科目" prop="subjectId">
              <el-select v-model="form.subjectId" placeholder="请选择" style="width: 100%" @change="onSubjectChange">
                <el-option v-for="s in subjectList" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="及格分" prop="passScore">
              <el-input-number v-model="form.passScore" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="时长" prop="duration">
              <el-input-number v-model="form.duration" :min="10" :max="300" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="题目选择">
          <div class="question-selector">
            <div class="selector-left">
              <div class="selector-header">可选题目</div>
              <div class="responsive-table">
                <el-table :data="availableQuestions" border size="small" height="320" @selection-change="onAvailableSelect">
                  <el-table-column type="selection" width="40" />
                  <el-table-column prop="type" label="题型" width="70">
                    <template #default="{ row }">
                      <el-tag size="small" :type="typeTagMap[row.type]">{{ row.type }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="content" label="内容" show-overflow-tooltip />
                  <el-table-column prop="score" label="分值" width="60" align="center" />
                </el-table>
              </div>
            </div>
            <div class="selector-actions">
              <el-button type="primary" :icon="ArrowRight" @click="addToSelected" :disabled="availableSelection.length === 0" circle />
              <el-button type="primary" :icon="ArrowLeft" @click="removeFromSelected" :disabled="selectedSelection.length === 0" circle style="margin-top: 8px" />
            </div>
            <div class="selector-right">
              <div class="selector-header">已选题目 (总分: {{ totalScore }})</div>
              <div class="responsive-table">
                <el-table :data="form.questions" border size="small" height="320" @selection-change="onSelectedSelect">
                  <el-table-column type="selection" width="40" />
                  <el-table-column prop="type" label="题型" width="70">
                    <template #default="{ row }">
                      <el-tag size="small" :type="typeTagMap[row.type]">{{ row.type }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="content" label="内容" show-overflow-tooltip />
                  <el-table-column prop="score" label="分值" width="60" align="center" />
                </el-table>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewVisible" title="试卷详情" class="responsive-dialog" destroy-on-close>
      <el-descriptions :column="1" border size="small">
        <el-descriptions-item label="试卷名称">{{ viewData.name }}</el-descriptions-item>
        <el-descriptions-item label="科目">{{ viewData.subjectName }}</el-descriptions-item>
        <el-descriptions-item label="总分">{{ viewData.totalScore }}</el-descriptions-item>
        <el-descriptions-item label="及格分">{{ viewData.passScore }}</el-descriptions-item>
        <el-descriptions-item label="时长">{{ viewData.duration }}分钟</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewData.status === '已发布' ? 'success' : 'info'">{{ viewData.status }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <div style="margin-top: 16px">
        <h4 style="margin-bottom: 12px">题目列表</h4>
        <div class="responsive-table">
          <el-table :data="viewData.questions || []" border size="small">
            <el-table-column type="index" label="序号" width="60" align="center" />
            <el-table-column prop="type" label="题型" width="80">
              <template #default="{ row }">
                <el-tag size="small" :type="typeTagMap[row.type]">{{ row.type }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="content" label="题目内容" show-overflow-tooltip />
            <el-table-column prop="score" label="分值" width="60" align="center" />
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowRight, ArrowLeft, Search, Plus } from '@element-plus/icons-vue'
import { getPaperPage, getPaperDetail, addPaper, updatePaper, deletePaper, publishPaper } from '../../api/paper'
import { getQuestionPage } from '../../api/question'
import { getSubjectList } from '../../api/subject'

const typeTagMap = { '单选': '', '多选': 'success', '判断': 'warning', '填空': 'info', '问答': 'danger' }

const searchForm = reactive({ subjectId: '', status: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])
const subjectList = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增试卷')
const editingId = ref(null)
const formRef = ref(null)

const getDefaultForm = () => ({
  name: '',
  subjectId: '',
  passScore: 60,
  duration: 120,
  questions: []
})

const form = reactive(getDefaultForm())

const formRules = {
  name: [{ required: true, message: '请输入试卷名称', trigger: 'blur' }],
  subjectId: [{ required: true, message: '请选择科目', trigger: 'change' }],
  passScore: [{ required: true, message: '请输入及格分', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入时长', trigger: 'blur' }]
}

const availableQuestions = ref([])
const availableSelection = ref([])
const selectedSelection = ref([])

const totalScore = computed(() => form.questions.reduce((sum, q) => sum + q.score, 0))

const viewVisible = ref(false)
const viewData = ref({})

const fetchData = async () => {
  const params = {
    page: pagination.page,
    size: pagination.size,
    ...searchForm
  }
  const res = await getPaperPage(params)
  tableData.value = res.data?.records || []
  pagination.total = res.data?.total || 0
}

const fetchSubjects = async () => {
  const res = await getSubjectList()
  subjectList.value = res.data || []
}

const fetchAvailableQuestions = async (subjectId) => {
  if (!subjectId) {
    availableQuestions.value = []
    return
  }
  const res = await getQuestionPage({ subjectId, size: 500 })
  const allQuestions = res.data?.records || []
  const selectedIds = new Set(form.questions.map(q => q.id))
  availableQuestions.value = allQuestions.filter(q => !selectedIds.has(q.id))
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const onSubjectChange = (subjectId) => {
  form.questions = []
  fetchAvailableQuestions(subjectId)
}

const onAvailableSelect = (selection) => {
  availableSelection.value = selection
}

const onSelectedSelect = (selection) => {
  selectedSelection.value = selection
}

const addToSelected = () => {
  const addIds = new Set(availableSelection.value.map(q => q.id))
  form.questions.push(...availableSelection.value)
  availableQuestions.value = availableQuestions.value.filter(q => !addIds.has(q.id))
  availableSelection.value = []
}

const removeFromSelected = () => {
  const removeIds = new Set(selectedSelection.value.map(q => q.id))
  availableQuestions.value.push(...selectedSelection.value)
  form.questions = form.questions.filter(q => !removeIds.has(q.id))
  selectedSelection.value = []
}

const handleAdd = () => {
  editingId.value = null
  dialogTitle.value = '新增试卷'
  Object.assign(form, getDefaultForm())
  availableQuestions.value = []
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  editingId.value = row.id
  dialogTitle.value = '编辑试卷'
  const res = await getPaperDetail(row.id)
  Object.assign(form, {
    name: res.data.name,
    subjectId: res.data.subjectId,
    passScore: res.data.passScore,
    duration: res.data.duration,
    questions: res.data.questions || []
  })
  await fetchAvailableQuestions(res.data.subjectId)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  if (form.questions.length === 0) {
    ElMessage.warning('请至少选择一道题目')
    return
  }
  const payload = {
    ...form,
    totalScore: totalScore.value,
    questionIds: form.questions.map(q => q.id)
  }
  if (editingId.value) {
    await updatePaper(editingId.value, payload)
    ElMessage.success('修改成功')
  } else {
    await addPaper(payload)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchData()
}

const handleView = async (row) => {
  const res = await getPaperDetail(row.id)
  viewData.value = res.data
  viewVisible.value = true
}

const handlePublish = (row) => {
  ElMessageBox.confirm('确定要发布该试卷吗？发布后将无法编辑和删除。', '发布确认', { type: 'warning' })
    .then(async () => {
      await publishPaper(row.id)
      ElMessage.success('发布成功')
      fetchData()
    })
    .catch(() => {})
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该试卷吗？', '提示', { type: 'warning' })
    .then(async () => {
      await deletePaper(row.id)
      ElMessage.success('删除成功')
      fetchData()
    })
    .catch(() => {})
}

onMounted(() => {
  fetchSubjects()
  fetchData()
})
</script>

<style scoped>
.page-container {
  width: 100%;
}

.search-card {
  border-radius: 8px;
  margin-bottom: 16px;
}

.search-form {
  margin: 0;
}

.search-item {
  margin-bottom: 0;
}

.search-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-start;
}

.table-card {
  border-radius: 8px;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.mobile-list {
  margin-top: 8px;
}

.list-item {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  flex: 1;
  margin-right: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-info {
  display: flex;
  margin-bottom: 8px;
  font-size: 13px;
}

.info-label {
  color: #909399;
  width: 50px;
  flex-shrink: 0;
}

.info-value {
  color: #606266;
  flex: 1;
}

.item-info-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.info-pill {
  background: #f5f7fa;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  color: #606266;
}

.item-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.question-selector {
  display: flex;
  width: 100%;
  gap: 12px;
  align-items: stretch;
  flex-wrap: wrap;
}

.selector-left,
.selector-right {
  flex: 1;
  min-width: 200px;
}

.selector-header {
  font-weight: bold;
  margin-bottom: 8px;
  font-size: 14px;
  color: #333;
}

.selector-actions {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 0 4px;
}

@media screen and (max-width: 768px) {
  .search-actions .el-button {
    flex: 1;
    min-width: calc(50% - 4px);
  }

  .item-actions .el-button {
    flex: 1;
    min-width: calc(25% - 6px);
  }

  .question-selector {
    flex-direction: column;
  }

  .selector-actions {
    flex-direction: row;
    gap: 8px;
  }
}

@media screen and (max-width: 480px) {
  .item-actions .el-button {
    min-width: calc(50% - 4px);
  }
}
</style>
