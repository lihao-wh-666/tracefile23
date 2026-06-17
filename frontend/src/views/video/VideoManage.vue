<template>
  <div class="page-container">
    <h2 class="page-title">视频管理</h2>

    <el-card shadow="hover" class="search-card">
      <el-form :model="searchForm" class="search-form">
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="视频标题" class="search-item">
              <el-input v-model="searchForm.keyword" placeholder="请输入视频标题" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="视频分类" class="search-item">
              <el-select v-model="searchForm.categoryId" placeholder="全部分类" clearable style="width: 100%">
                <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="视频状态" class="search-item">
              <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 100%">
                <el-option label="草稿" :value="0" />
                <el-option label="已发布" :value="1" />
                <el-option label="已下架" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <div class="search-actions">
              <el-button type="primary" @click="handleSearch">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="handleReset">重置</el-button>
              <el-button type="success" @click="handleAdd">
                <el-icon><Plus /></el-icon>
                上传视频
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-card shadow="hover" class="table-card mobile-hidden">
      <div class="responsive-table">
        <el-table :data="tableData" border stripe style="width: 100%" v-loading="loading">
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column label="封面" width="120" align="center">
            <template #default="{ row }">
              <el-image
                v-if="row.coverUrl"
                :src="getImageUrl(row.coverUrl)"
                fit="cover"
                style="width: 100px; height: 60px; border-radius: 4px"
                :preview-src-list="[getImageUrl(row.coverUrl)]"
              />
              <span v-else class="no-cover">无封面</span>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="视频标题" min-width="180" show-overflow-tooltip />
          <el-table-column prop="categoryName" label="分类" width="100" align="center" />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">{{ row.statusName }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="duration" label="时长" width="90" align="center" />
          <el-table-column prop="viewCount" label="播放量" width="90" align="center" />
          <el-table-column prop="rating" label="评分" width="80" align="center" />
          <el-table-column prop="createTime" label="创建时间" width="170" align="center" />
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
              <el-button type="success" link size="small" @click="handlePreview(row)">预览</el-button>
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
      <div class="list-item" v-for="item in tableData" :key="item.id" v-loading="loading">
        <div class="item-cover">
          <el-image
            v-if="item.coverUrl"
            :src="getImageUrl(item.coverUrl)"
            fit="cover"
            style="width: 100%; height: 100%"
          />
          <span v-else class="no-cover">无封面</span>
        </div>
        <div class="item-content">
          <div class="item-header">
            <span class="item-title">{{ item.title }}</span>
            <el-tag :type="getStatusType(item.status)" size="small">{{ item.statusName }}</el-tag>
          </div>
          <div class="item-info">
            <div class="info-row">
              <span class="info-label">分类:</span>
              <span class="info-value">{{ item.categoryName || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">时长:</span>
              <span class="info-value">{{ item.duration || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">播放量:</span>
              <span class="info-value">{{ item.viewCount || 0 }}</span>
            </div>
          </div>
          <div class="item-actions">
            <el-button type="primary" size="small" @click="handleEdit(item)">编辑</el-button>
            <el-button type="success" size="small" @click="handlePreview(item)">预览</el-button>
            <el-button type="danger" size="small" @click="handleDelete(item)">删除</el-button>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" class="responsive-dialog" destroy-on-close width="700px">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px" class="responsive-form">
        <el-form-item label="视频标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入视频标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="视频分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择视频分类" style="width: 100%">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="视频描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入视频描述（选填）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="视频文件" prop="videoUrl">
          <el-upload
            :auto-upload="true"
            :show-file-list="false"
            :before-upload="beforeVideoUpload"
            :http-request="uploadVideoFile"
            accept="video/*"
          >
            <div class="upload-area">
              <div v-if="videoUploading" class="upload-progress">
                <el-progress :percentage="videoUploadPercent" :status="videoUploadPercent === 100 ? 'success' : ''" />
                <span class="upload-text">上传中... {{ videoUploadPercent }}%</span>
              </div>
              <div v-else-if="form.videoUrl" class="uploaded-video">
                <el-icon class="video-icon"><VideoPlay /></el-icon>
                <span class="video-name">{{ getVideoName(form.videoUrl) }}</span>
                <el-button type="danger" link size="small" @click.stop="clearVideo">
                  <el-icon><Delete /></el-icon>
                  移除
                </el-button>
              </div>
              <div v-else class="upload-placeholder">
                <el-icon class="upload-icon"><UploadFilled /></el-icon>
                <span>点击上传视频文件</span>
                <span class="upload-tip">支持 MP4、AVI、MOV 等格式，最大 500MB</span>
              </div>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="视频封面" prop="coverUrl">
          <el-upload
            :auto-upload="true"
            :show-file-list="false"
            :before-upload="beforeCoverUpload"
            :http-request="uploadCoverFile"
            accept="image/*"
          >
            <div class="cover-upload-area">
              <div v-if="coverUploading" class="upload-progress">
                <el-progress :percentage="coverUploadPercent" :status="coverUploadPercent === 100 ? 'success' : ''" />
              </div>
              <div v-else-if="form.coverUrl" class="uploaded-cover">
                <el-image
                  :src="getImageUrl(form.coverUrl)"
                  fit="cover"
                  style="width: 200px; height: 120px; border-radius: 4px"
                  :preview-src-list="[getImageUrl(form.coverUrl)]"
                />
                <el-button type="danger" link size="small" @click.stop="clearCover">
                  <el-icon><Delete /></el-icon>
                  移除
                </el-button>
              </div>
              <div v-else class="upload-placeholder">
                <el-icon class="upload-icon"><Picture /></el-icon>
                <span>点击上传封面图片</span>
                <span class="upload-tip">支持 JPG、PNG 等格式，建议 16:9 比例</span>
              </div>
            </div>
          </el-upload>
        </el-form-item>
        <el-form-item label="视频时长" prop="duration">
          <el-input v-model="form.duration" placeholder="例如：10:30" maxlength="20" />
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-select
            v-model="tagList"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入标签后按回车添加"
            style="width: 100%"
            @change="handleTagsChange"
          >
          </el-select>
        </el-form-item>
        <el-form-item label="发布状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">保存草稿</el-radio>
            <el-radio :value="1">立即发布</el-radio>
            <el-radio :value="2">已下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="previewVisible" title="视频预览" width="800px" destroy-on-close>
      <div v-if="previewVideo" class="preview-container">
        <video
          v-if="previewVideo.videoUrl"
          :src="getVideoUrl(previewVideo.videoUrl)"
          controls
          style="width: 100%; max-height: 500px"
          :poster="previewVideo.coverUrl ? getImageUrl(previewVideo.coverUrl) : ''"
        >
          您的浏览器不支持视频播放
        </video>
        <div v-else class="no-video">
          <el-icon class="no-video-icon"><Warning /></el-icon>
          <span>暂无视频文件</span>
        </div>
        <div class="preview-info">
          <h3 class="preview-title">{{ previewVideo.title }}</h3>
          <p class="preview-desc">{{ previewVideo.description }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Plus,
  Delete,
  VideoPlay,
  UploadFilled,
  Picture,
  Warning
} from '@element-plus/icons-vue'
import {
  getVideoManagePage,
  getVideoInfo,
  addVideo,
  updateVideo,
  deleteVideo,
  uploadVideo,
  uploadVideoCover
} from '../../api/video'
import { getVideoCategories } from '../../api/video'

const searchForm = reactive({
  keyword: '',
  categoryId: null,
  status: null
})
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref([])
const loading = ref(false)
const categories = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('上传视频')
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const submitLoading = ref(false)
const tagList = ref([])

const videoUploading = ref(false)
const videoUploadPercent = ref(0)
const coverUploading = ref(false)
const coverUploadPercent = ref(0)

const previewVisible = ref(false)
const previewVideo = ref(null)

const getDefaultForm = () => ({
  title: '',
  categoryId: null,
  description: '',
  videoUrl: '',
  coverUrl: '',
  duration: '',
  fileSize: null,
  tags: '',
  status: 0
})

const form = reactive(getDefaultForm())

const formRules = {
  title: [
    { required: true, message: '请输入视频标题', trigger: 'blur' },
    { min: 2, max: 200, message: '视频标题长度在 2 到 200 个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择视频分类', trigger: 'change' }
  ],
  videoUrl: [
    { required: true, message: '请上传视频文件', trigger: 'change' }
  ],
  description: [
    { max: 500, message: '视频描述长度不能超过 500 个字符', trigger: 'blur' }
  ],
  duration: [
    { max: 20, message: '时长格式不正确', trigger: 'blur' }
  ]
}

const getStatusType = (status) => {
  const typeMap = {
    0: 'info',
    1: 'success',
    2: 'warning'
  }
  return typeMap[status] || 'info'
}

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/api')) return url
  return '/api' + url
}

const getVideoUrl = (url) => {
  return getImageUrl(url)
}

const getVideoName = (url) => {
  if (!url) return ''
  const parts = url.split('/')
  return parts[parts.length - 1]
}

const loadCategories = async () => {
  const res = await getVideoCategories()
  categories.value = res.data || []
}

const fetchData = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.page,
      size: pagination.size,
      keyword: searchForm.keyword || undefined,
      categoryId: searchForm.categoryId || undefined,
      status: searchForm.status != null ? searchForm.status : undefined
    }
    const res = await getVideoManagePage(params)
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.categoryId = null
  searchForm.status = null
  pagination.page = 1
  fetchData()
}

const handleAdd = () => {
  isEdit.value = false
  editingId.value = null
  dialogTitle.value = '上传视频'
  Object.assign(form, getDefaultForm())
  tagList.value = []
  videoUploadPercent.value = 0
  coverUploadPercent.value = 0
  dialogVisible.value = true
}

const handleEdit = async (row) => {
  isEdit.value = true
  editingId.value = row.id
  dialogTitle.value = '编辑视频'
  const res = await getVideoInfo(row.id)
  const data = res.data || {}
  Object.assign(form, {
    title: data.title || '',
    categoryId: data.categoryId || null,
    description: data.description || '',
    videoUrl: data.videoUrl || '',
    coverUrl: data.coverUrl || '',
    duration: data.duration || '',
    fileSize: data.fileSize || null,
    tags: data.tags || '',
    status: data.status ?? 0
  })
  tagList.value = data.tags ? data.tags.split(',') : []
  videoUploadPercent.value = form.videoUrl ? 100 : 0
  coverUploadPercent.value = form.coverUrl ? 100 : 0
  dialogVisible.value = true
}

const handlePreview = (row) => {
  previewVideo.value = row
  previewVisible.value = true
}

const handleTagsChange = (val) => {
  form.tags = val.join(',')
}

const beforeVideoUpload = (file) => {
  const isVideo = file.type.startsWith('video/')
  if (!isVideo) {
    ElMessage.error('只能上传视频文件!')
    return false
  }
  const isLt500M = file.size / 1024 / 1024 < 500
  if (!isLt500M) {
    ElMessage.error('视频大小不能超过 500MB!')
    return false
  }
  return true
}

const uploadVideoFile = async (options) => {
  videoUploading.value = true
  videoUploadPercent.value = 0
  try {
    const res = await uploadVideo(options.file, (percent) => {
      videoUploadPercent.value = percent
    })
    form.videoUrl = res.data
    form.fileSize = options.file.size
    ElMessage.success('视频上传成功')
  } catch (e) {
    ElMessage.error('视频上传失败')
  } finally {
    videoUploading.value = false
  }
}

const clearVideo = () => {
  form.videoUrl = ''
  form.fileSize = null
  videoUploadPercent.value = 0
}

const beforeCoverUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('封面图片大小不能超过 10MB!')
    return false
  }
  return true
}

const uploadCoverFile = async (options) => {
  coverUploading.value = true
  coverUploadPercent.value = 0
  try {
    const res = await uploadVideoCover(options.file, (percent) => {
      coverUploadPercent.value = percent
    })
    form.coverUrl = res.data
    ElMessage.success('封面上传成功')
  } catch (e) {
    ElMessage.error('封面上传失败')
  } finally {
    coverUploading.value = false
  }
}

const clearCover = () => {
  form.coverUrl = ''
  coverUploadPercent.value = 0
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    const formData = { ...form }
    if (isEdit.value) {
      await updateVideo(editingId.value, formData)
      ElMessage.success('修改成功')
    } else {
      await addVideo(formData)
      ElMessage.success('上传成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除视频 "${row.title}" 吗？`, '提示', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    distinguishCancelAndClose: true
  })
    .then(async () => {
      await deleteVideo(row.id)
      ElMessage.success('删除成功')
      fetchData()
    })
    .catch(() => {})
}

onMounted(() => {
  loadCategories()
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

.no-cover {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 60px;
  background: var(--bg-tertiary);
  border-radius: 4px;
  font-size: 12px;
  color: var(--text-secondary);
}

.mobile-list {
  margin-top: 8px;
}

.list-item {
  display: flex;
  gap: 12px;
  background: var(--bg-card);
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.item-cover {
  width: 120px;
  height: 70px;
  flex-shrink: 0;
  border-radius: 4px;
  overflow: hidden;
  background: var(--bg-tertiary);
}

.item-cover .no-cover {
  width: 100%;
  height: 100%;
}

.item-content {
  flex: 1;
  min-width: 0;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 8px;
}

.item-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.item-info {
  margin-bottom: 8px;
}

.info-row {
  display: flex;
  margin-bottom: 4px;
  font-size: 12px;
}

.info-label {
  color: var(--text-secondary);
  min-width: 50px;
  flex-shrink: 0;
}

.info-value {
  color: var(--text-primary);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-actions {
  display: flex;
  gap: 8px;
  padding-top: 8px;
  border-top: 1px solid var(--border-primary);
}

.upload-area,
.cover-upload-area {
  width: 100%;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px;
  border: 2px dashed var(--border-primary);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: var(--bg-tertiary);
}

.upload-placeholder:hover {
  border-color: var(--color-primary);
  background: var(--bg-primary);
}

.upload-icon {
  font-size: 32px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.upload-placeholder span {
  color: var(--text-secondary);
  font-size: 14px;
}

.upload-tip {
  font-size: 12px !important;
  color: var(--text-secondary) !important;
  margin-top: 4px;
}

.upload-progress {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px;
  border: 2px dashed var(--color-primary);
  border-radius: 8px;
  background: var(--bg-primary);
}

.upload-text {
  font-size: 14px;
  color: var(--text-primary);
}

.uploaded-video {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 1px solid var(--border-primary);
  border-radius: 8px;
  background: var(--bg-tertiary);
}

.video-icon {
  font-size: 32px;
  color: var(--color-primary);
}

.video-name {
  flex: 1;
  color: var(--text-primary);
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.uploaded-cover {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border: 1px solid var(--border-primary);
  border-radius: 8px;
  background: var(--bg-tertiary);
}

.preview-container {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.no-video {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: var(--bg-tertiary);
  border-radius: 8px;
}

.no-video-icon {
  font-size: 48px;
  color: var(--text-secondary);
  margin-bottom: 12px;
}

.no-video span {
  color: var(--text-secondary);
  font-size: 14px;
}

.preview-info {
  padding: 16px;
  background: var(--bg-tertiary);
  border-radius: 8px;
}

.preview-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.preview-desc {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
  line-height: 1.6;
}

@media screen and (max-width: 768px) {
  .search-actions .el-button {
    flex: 1;
    min-width: calc(33.33% - 6px);
  }

  .item-actions .el-button {
    flex: 1;
  }

  .list-item {
    flex-direction: column;
  }

  .item-cover {
    width: 100%;
    height: 180px;
  }
}
</style>
