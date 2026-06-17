<template>
  <div class="video-list-page">
    <h2 class="page-title">{{ t('video.title') }}</h2>

    <el-card shadow="hover" class="search-card">
      <div class="search-bar">
        <div class="search-input-wrap">
          <el-input
            v-model="searchKeyword"
            :placeholder="t('video.searchPlaceholder')"
            clearable
            size="large"
            @input="handleSearchInput"
            @clear="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        <div class="filter-actions">
          <el-select v-model="selectedCategory" placeholder="全部分类" size="large" style="width: 140px" @change="handleCategoryChange">
            <el-option :label="t('video.allCategories')" :value="null" />
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
          <el-select v-model="sortType" size="large" style="width: 120px" @change="handleSortChange">
            <el-option label="最新发布" value="latest" />
            <el-option label="最热门" value="hot" />
            <el-option label="评分最高" value="rating" />
          </el-select>
        </div>
      </div>
    </el-card>

    <div v-loading="loading" class="video-grid">
      <div
        v-for="video in videoList"
        :key="video.id"
        class="video-card"
        @click="goToDetail(video.id)"
      >
        <div class="video-cover">
          <img :src="video.cover" :alt="video.title" class="cover-img" />
          <div class="video-duration">{{ video.duration }}</div>
          <div class="video-overlay">
            <el-icon class="play-icon"><VideoPlay /></el-icon>
          </div>
        </div>
        <div class="video-info">
          <h3 class="video-title" :title="video.title">{{ video.title }}</h3>
          <div class="video-meta">
            <span class="meta-item">
              <el-icon><View /></el-icon>
              <span>{{ formatCount(video.viewCount) }}</span>
            </span>
            <span class="meta-item">
              <el-icon><Star /></el-icon>
              <span>{{ video.rating }}</span>
            </span>
          </div>
          <div class="video-footer">
            <span class="video-category">{{ video.categoryName }}</span>
            <span class="video-date">{{ video.publishDate }}</span>
          </div>
          <div class="video-tags">
            <el-tag v-for="tag in video.tags.slice(0, 3)" :key="tag" size="small" type="info" effect="light">
              {{ tag }}
            </el-tag>
          </div>
        </div>
      </div>

      <el-empty v-if="!loading && videoList.length === 0" :description="t('video.noVideo')" />
    </div>

    <div class="pagination-wrap" v-if="total > 0">
      <el-pagination
        v-model:current-page="pagination.current"
        v-model:page-size="pagination.size"
        :total="total"
        :page-sizes="[12, 24, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Search, VideoPlay, View, Star } from '@element-plus/icons-vue'
import { getVideoPage, getVideoCategories } from '../../api/video'

const { t } = useI18n()
const router = useRouter()

const videoList = ref([])
const categories = ref([])
const loading = ref(false)
const total = ref(0)
const searchKeyword = ref('')
const selectedCategory = ref(null)
const sortType = ref('latest')

const pagination = reactive({
  current: 1,
  size: 12
})

let searchTimer = null

const loadVideos = async () => {
  loading.value = true
  try {
    const params = {
      current: pagination.current,
      size: pagination.size,
      keyword: searchKeyword.value,
      categoryId: selectedCategory.value,
      sort: sortType.value
    }
    const res = await getVideoPage(params)
    videoList.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  const res = await getVideoCategories()
  categories.value = res.data
}

const handleSearchInput = () => {
  if (searchTimer) clearTimeout(searchTimer)
  searchTimer = setTimeout(() => {
    handleSearch()
  }, 300)
}

const handleSearch = () => {
  pagination.current = 1
  loadVideos()
}

const handleCategoryChange = () => {
  pagination.current = 1
  loadVideos()
}

const handleSortChange = () => {
  pagination.current = 1
  loadVideos()
}

const handlePageChange = () => {
  loadVideos()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleSizeChange = () => {
  pagination.current = 1
  loadVideos()
}

const goToDetail = (id) => {
  router.push(`/video/${id}`)
}

const formatCount = (count) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count.toLocaleString()
}

onMounted(() => {
  loadCategories()
  loadVideos()
})
</script>

<style scoped>
.video-list-page {
  width: 100%;
}

.search-card {
  border-radius: 8px;
  margin-bottom: 20px;
}

.search-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.search-input-wrap {
  flex: 1;
  min-width: 250px;
}

.filter-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.video-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
  min-height: 400px;
}

.video-card {
  background: var(--bg-card);
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.video-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.video-cover {
  position: relative;
  width: 100%;
  padding-top: 56.25%;
  overflow: hidden;
  background: var(--bg-tertiary);
}

.cover-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.video-card:hover .cover-img {
  transform: scale(1.05);
}

.video-duration {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background: rgba(0, 0, 0, 0.75);
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.video-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.video-card:hover .video-overlay {
  opacity: 1;
}

.play-icon {
  font-size: 48px;
  color: #fff;
}

.video-info {
  padding: 14px 14px 16px;
}

.video-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 10px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 42px;
}

.video-meta {
  display: flex;
  gap: 14px;
  margin-bottom: 10px;
  font-size: 13px;
  color: var(--text-secondary);
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.meta-item .el-icon {
  font-size: 14px;
}

.video-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: var(--text-secondary);
  margin-bottom: 10px;
}

.video-category {
  color: var(--color-primary);
  font-weight: 500;
}

.video-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media screen and (max-width: 1400px) {
  .video-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
  }
}

@media screen and (max-width: 1024px) {
  .video-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 14px;
  }
}

@media screen and (max-width: 768px) {
  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-actions {
    width: 100%;
  }

  .filter-actions .el-select {
    flex: 1;
    width: auto !important;
  }

  .video-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    margin-bottom: 16px;
  }

  .video-info {
    padding: 10px 10px 12px;
  }

  .video-title {
    font-size: 13px;
    min-height: 36px;
  }

  .video-meta {
    font-size: 11px;
    gap: 10px;
  }

  .video-footer {
    font-size: 11px;
  }

  .pagination-wrap :deep(.el-pagination) {
    justify-content: center;
  }
}

@media screen and (max-width: 480px) {
  .video-grid {
    grid-template-columns: 1fr;
  }
}
</style>
