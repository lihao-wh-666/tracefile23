<template>
  <div class="page-container">
    <h2 class="page-title">题库管理</h2>

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
            <el-form-item label="题型" class="search-item">
              <el-select v-model="searchForm.type" placeholder="请选择题型" clearable style="width: 100%">
                <el-option label="单选" value="单选" />
                <el-option label="多选" value="多选" />
                <el-option label="判断" value="判断" />
                <el-option label="填空" value="填空" />
                <el-option label="问答" value="问答" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="24" :md="8">
            <div class="search-actions">
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleReset">重置</el-button>
              <el-button type="success" @click="handleAdd">
                <el-icon><Plus /></el-icon>
                新增
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
          <el-table-column prop="subjectName" label="科目" width="100" align="center" />
          <el-table-column prop="type" label="题型" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="typeTagMap[row.type]" size="small">{{ row.type }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="content" label="题目内容" min-width="200" show-overflow-tooltip />
          <el-table-column prop="score" label="分值" width="70" align="center" />
          <el-table-column prop="difficulty" label="难度" width="80" align="center">
            <template #default="{ row }">
              <el-tag :type="difficultyTagMap[row.difficulty]" size="small">{{ row.difficulty }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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
          <div class="item-tags">
            <el-tag :type="typeTagMap[item.type]" size="small">{{ item.type }}</el-tag>
            <el-tag :type="difficultyTagMap[item.difficulty]" size="small">{{ item.difficulty }}</el-tag>
          </div>
          <span class="item-score">{{ item.score }}分</span>
        </div>
        <div class="item-content">{{ item.content }}</div>
        <div class="item-meta">
          <span>科目: {{ item.subjectName }}</span>
        </div>
        <div class="item-actions">
          <el-button type="primary" size="small" @click="handleEdit(item)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(item)">删除</el-button>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" class="responsive-dialog" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px" class="responsive-form">
        <el-row :gutter="16">
          <el-col :span="24">
            <el-form-item label="科目" prop="subjectId">
              <el-select v-model="form.subjectId" placeholder="请选择科目" style="width: 100%">
                <el-option v-for="s in subjectList" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="题型" prop="type">
              <el-select v-model="form.type" placeholder="请选择题型" style="width: 100%" @change="onTypeChange">
                <el-option label="单选" value="单选" />
                <el-option label="多选" value="多选" />
                <el-option label="判断" value="判断" />
                <el-option label="填空" value="填空" />
                <el-option label="问答" value="问答" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="分值" prop="score">
              <el-input-number v-model="form.score" :min="1" :max="100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="难度" prop="difficulty">
              <el-select v-model="form.difficulty" placeholder="难度" style="width: 100%">
                <el-option label="简单" value="简单" />
                <el-option label="中等" value="中等" />
                <el-option label="困难" value="困难" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="题目内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入题目内容" />
        </el-form-item>
        <template v-if="form.type === '单选' || form.type === '多选'">
          <el-form-item label="选项A" prop="optionA">
            <el-input v-model="form.optionA" placeholder="请输入选项A" />
          </el-form-item>
          <el-form-item label="选项B" prop="optionB">
            <el-input v-model="form.optionB" placeholder="请输入选项B" />
          </el-form-item>
          <el-form-item label="选项C" prop="optionC">
            <el-input v-model="form.optionC" placeholder="请输入选项C" />
          </el-form-item>
          <el-form-item label="选项D" prop="optionD">
            <el-input v-model="form.optionD" placeholder="请输入选项D" />
          </el-form-item>
        </template>
        <el-form-item label="答案" prop="answer">
          <el-input v-model="form.answer" placeholder="请输入答案" />
        </el-form-item>
        <el-form-item label="解析">
          <el-input v-model="form.analysis" type="textarea" :rows="2" placeholder="请输入解析" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus } from '@element-plus/icons-vue'
import { getQuestionPage, getQuestionDetail, addQuestion, updateQuestion, deleteQuestion } from '../../api/question'
import { getSubjectList } from '../../api/subject'

const typeTagMap = { '单选': '', '多选': 'success', '判断': 'warning', '填空': 'info', '问答': 'danger' }
const difficultyTagMap = { '简单': 'success', '中等': 'warning', '困难': 'danger' }

const searchForm = reactive({ subjectId: '', type: '', difficulty: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])
const subjectList = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增题目')
const editingId = ref(null)
const formRef = ref(null)

const getDefaultForm = () => ({
  subjectId: '',
  type: '',
  content: '',
  optionA: '',
  optionB: '',
  optionC: '',
  optionD: '',
  answer: '',
  analysis: '',
  score: 5,
  difficulty: '中等'
})

const form = reactive(getDefaultForm())

const formRules = {
  subjectId: [{ required: true, message: '请选择科目', trigger: 'change' }],
  type: [{ required: true, message: '请选择题型', trigger: 'change' }],
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  answer: [{ required: true, message: '请输入答案', trigger: 'blur' }],
  score: [{ required: true, message: '请输入分值', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }]
}

const onTypeChange = () => {
  if (form.type !== '单选' && form.type !== '多选') {
    form.optionA = ''
    form.optionB = ''
    form.optionC = ''
    form.optionD = ''
  }
}

const fetchData = async () => {
  const params = {
    page: pagination.page,
    size: pagination.size,
    ...searchForm
  }
  const res = await getQuestionPage(params)
  tableData.value = res.data?.records || []
  pagination.total = res.data?.total || 0
}

const fetchSubjects = async () => {
  const res = await getSubjectList()
  subjectList.value = res.data || []
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.subjectId = ''
  searchForm.type = ''
  searchForm.difficulty = ''
  pagination.page = 1
  fetchData()
}

const handleAdd = () => {
  editingId.value = null
  dialogTitle.value = '新增题目'
  Object.assign(form, getDefaultForm())
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  editingId.value = row.id
  dialogTitle.value = '编辑题目'
  const res = await getQuestionDetail(row.id)
  Object.assign(form, res.data)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  if (editingId.value) {
    await updateQuestion(editingId.value, { ...form })
    ElMessage.success('修改成功')
  } else {
    await addQuestion({ ...form })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchData()
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该题目吗？', '提示', { type: 'warning' })
    .then(async () => {
      await deleteQuestion(row.id)
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

.item-tags {
  display: flex;
  gap: 6px;
}

.item-score {
  font-size: 15px;
  font-weight: 600;
  color: #e6a23c;
}

.item-content {
  font-size: 14px;
  color: #303133;
  line-height: 1.6;
  margin-bottom: 10px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-meta {
  font-size: 13px;
  color: #909399;
  margin-bottom: 12px;
}

.item-actions {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

@media screen and (max-width: 768px) {
  .search-actions .el-button {
    flex: 1;
    min-width: calc(33.33% - 6px);
  }

  .item-actions .el-button {
    flex: 1;
  }
}
</style>
