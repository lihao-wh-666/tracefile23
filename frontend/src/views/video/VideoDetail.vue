<template>
  <div class="video-detail-page" v-loading="loading">
    <div class="back-btn" @click="goBack">
      <el-icon><ArrowLeft /></el-icon>
      <span>{{ t('video.backToList') }}</span>
    </div>

    <div class="detail-content" v-if="videoDetail">
      <div class="main-content">
        <div class="video-player-card">
          <div class="video-player">
            <div class="player-wrapper">
              <img :src="videoDetail.cover" :alt="videoDetail.title" class="poster-img" />
              <div class="player-overlay" @click="togglePlay">
                <div class="play-button" v-if="!isPlaying">
                  <el-icon :size="64"><VideoPlay /></el-icon>
                </div>
              </div>
              <div class="player-controls" v-if="isPlaying">
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
                  <div class="progress-dot" :style="{ left: progressPercent + '%' }"></div>
                </div>
                <div class="control-row">
                  <div class="control-left">
                    <el-icon class="control-btn" @click.stop="togglePlay"><VideoPlay /></el-icon>
                    <span class="time-text">{{ currentTimeText }} / {{ totalTimeText }}</span>
                  </div>
                  <div class="control-right">
                    <el-icon class="control-btn"><DataBoard /></el-icon>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <h1 class="video-title">{{ videoDetail.title }}</h1>
          <div class="video-meta-row">
            <div class="meta-left">
              <el-tag :type="difficultyType" size="small" effect="light">{{ videoDetail.difficulty }}</el-tag>
              <span class="meta-divider">|</span>
              <span class="meta-text">
                <el-icon><View /></el-icon>
                {{ formatCount(videoDetail.viewCount) }} {{ t('video.views') }}
              </span>
              <span class="meta-divider">|</span>
              <span class="meta-text">
                <el-icon><Star /></el-icon>
                {{ videoDetail.rating }}
              </span>
            </div>
            <div class="meta-right">
              <el-button type="primary" @click="handleLike">
                <el-icon><StarFilled /></el-icon>
                {{ formatCount(videoDetail.likeCount) }}
              </el-button>
              <el-button :type="isCollected ? 'warning' : 'default'" @click="handleCollect">
                <el-icon><StarFilled v-if="isCollected" /><Star v-else /></el-icon>
                {{ t('video.collect') }}
              </el-button>
              <el-button>
                <el-icon><Share /></el-icon>
                {{ t('video.share') }}
              </el-button>
            </div>
          </div>
        </div>

        <el-card shadow="hover" class="section-card">
          <template #header>
            <div class="card-header">
              <span class="section-title">{{ t('video.courseOutline') }}</span>
              <span class="chapter-count">{{ videoDetail.chapters }} {{ t('video.chapters') }}</span>
            </div>
          </template>
          <div class="outline-list">
            <div
              v-for="(item, index) in videoDetail.outline"
              :key="item.chapter"
              class="outline-item"
              :class="{ active: currentChapter === item.chapter }"
              @click="selectChapter(item.chapter)"
            >
              <div class="chapter-left">
                <div class="chapter-number">
                  <el-icon v-if="currentChapter === item.chapter" class="playing-icon"><VideoPlay /></el-icon>
                  <span v-else>{{ String(item.chapter).padStart(2, '0') }}</span>
                </div>
                <span class="chapter-title">{{ item.title }}</span>
              </div>
              <div class="chapter-right">
                <el-tag v-if="item.free" size="small" type="success" effect="light">{{ t('video.free') }}</el-tag>
                <span class="chapter-duration">{{ item.duration }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <el-card shadow="hover" class="section-card">
          <template #header>
            <span class="section-title">{{ t('video.courseIntro') }}</span>
          </template>
          <div class="course-intro">
            <p>{{ videoDetail.description }}</p>
            <div class="tag-list">
              <el-tag v-for="tag in videoDetail.tags" :key="tag" size="small" effect="plain">
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </el-card>

        <el-card shadow="hover" class="section-card">
          <template #header>
            <span class="section-title">{{ t('video.teacherInfo') }}</span>
          </template>
          <div class="teacher-info" v-if="videoDetail.teacher">
            <div class="teacher-avatar">
              <el-avatar :size="64">
                <el-icon :size="32"><UserFilled /></el-icon>
              </el-avatar>
            </div>
            <div class="teacher-detail">
              <div class="teacher-header">
                <h3 class="teacher-name">{{ videoDetail.teacher.name }}</h3>
                <el-tag size="small" type="primary" effect="light">{{ videoDetail.teacher.title }}</el-tag>
              </div>
              <p class="teacher-desc">{{ videoDetail.teacher.description }}</p>
              <div class="teacher-stats">
                <div class="stat-item">
                  <span class="stat-number">{{ videoDetail.teacher.courses }}</span>
                  <span class="stat-label">{{ t('video.courses') }}</span>
                </div>
                <div class="stat-divider"></div>
                <div class="stat-item">
                  <span class="stat-number">{{ formatCount(videoDetail.teacher.students) }}</span>
                  <span class="stat-label">{{ t('video.students') }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <div class="sidebar">
        <el-card shadow="hover" class="sidebar-card">
          <template #header>
            <span class="section-title">{{ t('video.relatedVideos') }}</span>
          </template>
          <div class="related-list">
            <div
              v-for="item in relatedVideos"
              :key="item.id"
              class="related-item"
              @click="goToVideo(item.id)"
            >
              <div class="related-cover">
                <img :src="item.cover" :alt="item.title" />
                <span class="related-duration">{{ item.duration }}</span>
              </div>
              <div class="related-info">
                <h4 class="related-title">{{ item.title }}</h4>
                <div class="related-meta">
                  <span class="meta-item">
                    <el-icon><View /></el-icon>
                    {{ formatCount(item.viewCount) }}
                  </span>
                  <span class="meta-item">
                    <el-icon><Star /></el-icon>
                    {{ item.rating }}
                  </span>
                </div>
              </div>
            </div>
            <el-empty v-if="relatedVideos.length === 0" :description="t('video.noRelated')" :image-size="80" />
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  ArrowLeft, VideoPlay, View, Star, StarFilled,
  Share, UserFilled, DataBoard
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getVideoDetail, getRelatedVideoList } from '../../api/video'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const loading = ref(false)
const videoDetail = ref(null)
const relatedVideos = ref([])
const isPlaying = ref(false)
const currentChapter = ref(1)
const isLiked = ref(false)
const isCollected = ref(false)
const currentTime = ref(0)
const totalTime = ref(100)

let playTimer = null

const progressPercent = computed(() => {
  if (totalTime.value === 0) return 0
  return (currentTime.value / totalTime.value) * 100
})

const currentTimeText = computed(() => formatTime(currentTime.value))
const totalTimeText = computed(() => formatTime(totalTime.value))

const difficultyType = computed(() => {
  const map = {
    '入门': 'success',
    '进阶': 'warning',
    '高级': 'danger'
  }
  return map[videoDetail.value?.difficulty] || 'info'
})

const loadVideoDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const res = await getVideoDetail(id)
    videoDetail.value = res.data
    if (videoDetail.value?.outline?.length > 0) {
      totalTime.value = videoDetail.value.durationSeconds || 3600
    }
  } finally {
    loading.value = false
  }
}

const loadRelatedVideos = async () => {
  const id = route.params.id
  const res = await getRelatedVideoList(id, 8)
  relatedVideos.value = res.data
}

const togglePlay = () => {
  isPlaying.value = !isPlaying.value
  if (isPlaying.value) {
    startPlayback()
  } else {
    stopPlayback()
  }
}

const startPlayback = () => {
  playTimer = setInterval(() => {
    if (currentTime.value < totalTime.value) {
      currentTime.value += 1
    } else {
      stopPlayback()
      isPlaying.value = false
    }
  }, 1000)
}

const stopPlayback = () => {
  if (playTimer) {
    clearInterval(playTimer)
    playTimer = null
  }
}

const selectChapter = (chapter) => {
  currentChapter.value = chapter
  currentTime.value = 0
  if (!isPlaying.value) {
    isPlaying.value = true
    startPlayback()
  }
}

const handleLike = () => {
  if (!isLiked.value) {
    isLiked.value = true
    videoDetail.value.likeCount++
    ElMessage.success(t('video.likeSuccess'))
  } else {
    isLiked.value = false
    videoDetail.value.likeCount--
    ElMessage.info(t('video.cancelLike'))
  }
}

const handleCollect = () => {
  if (!isCollected.value) {
    isCollected.value = true
    videoDetail.value.collectCount++
    ElMessage.success(t('video.collectSuccess'))
  } else {
    isCollected.value = false
    videoDetail.value.collectCount--
    ElMessage.info(t('video.cancelCollect'))
  }
}

const goBack = () => {
  router.push('/video')
}

const goToVideo = (id) => {
  stopPlayback()
  currentTime.value = 0
  isPlaying.value = false
  currentChapter.value = 1
  router.push(`/video/${id}`)
  loadVideoDetail()
  loadRelatedVideos()
}

const formatCount = (count) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + '万'
  }
  return count.toLocaleString()
}

const formatTime = (seconds) => {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = Math.floor(seconds % 60)
  if (h > 0) {
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  }
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

onMounted(() => {
  loadVideoDetail()
  loadRelatedVideos()
})

onUnmounted(() => {
  stopPlayback()
})
</script>

<style scoped>
.video-detail-page {
  width: 100%;
  position: relative;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-regular);
  cursor: pointer;
  margin-bottom: 16px;
  font-size: 14px;
  transition: color 0.2s;
}

.back-btn:hover {
  color: var(--color-primary);
}

.detail-content {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.main-content {
  flex: 1;
  min-width: 0;
}

.sidebar {
  width: 320px;
  flex-shrink: 0;
}

.video-player-card {
  background: var(--bg-card);
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.video-player {
  position: relative;
  width: 100%;
  background: #000;
}

.player-wrapper {
  position: relative;
  width: 100%;
  padding-top: 56.25%;
  overflow: hidden;
}

.poster-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.player-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: rgba(0, 0, 0, 0.2);
  transition: background 0.3s;
}

.player-overlay:hover {
  background: rgba(0, 0, 0, 0.35);
}

.play-button {
  color: #fff;
  opacity: 0.9;
  transition: transform 0.3s;
}

.player-overlay:hover .play-button {
  transform: scale(1.1);
  opacity: 1;
}

.player-controls {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  color: #fff;
}

.progress-bar {
  position: relative;
  height: 4px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
  cursor: pointer;
  margin-bottom: 10px;
}

.progress-fill {
  height: 100%;
  background: var(--color-primary);
  border-radius: 2px;
}

.progress-dot {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 12px;
  height: 12px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.control-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.control-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.control-btn {
  font-size: 20px;
  cursor: pointer;
  transition: color 0.2s;
}

.control-btn:hover {
  color: var(--color-primary);
}

.time-text {
  font-size: 13px;
  font-family: monospace;
}

.video-title {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 16px 20px 12px;
  line-height: 1.4;
}

.video-meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.meta-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.meta-divider {
  color: var(--text-placeholder);
}

.meta-text {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
}

.meta-text .el-icon {
  font-size: 14px;
}

.meta-right {
  display: flex;
  gap: 8px;
}

.section-card {
  border-radius: 10px;
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.chapter-count {
  font-size: 13px;
  color: var(--text-secondary);
}

.outline-list {
  max-height: 400px;
  overflow-y: auto;
}

.outline-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  margin-bottom: 4px;
}

.outline-item:hover {
  background: var(--bg-hover);
}

.outline-item.active {
  background: var(--bg-hover);
}

.chapter-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  min-width: 0;
}

.chapter-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--bg-tertiary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  flex-shrink: 0;
}

.outline-item.active .chapter-number {
  background: var(--color-primary);
  color: #fff;
}

.playing-icon {
  font-size: 14px;
}

.chapter-title {
  font-size: 14px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.outline-item.active .chapter-title {
  color: var(--color-primary);
  font-weight: 500;
}

.chapter-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.chapter-duration {
  font-size: 13px;
  color: var(--text-secondary);
  font-family: monospace;
}

.course-intro {
  font-size: 14px;
  color: var(--text-regular);
  line-height: 1.8;
}

.course-intro p {
  margin: 0 0 16px 0;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.teacher-info {
  display: flex;
  gap: 20px;
}

.teacher-avatar {
  flex-shrink: 0;
}

.teacher-detail {
  flex: 1;
  min-width: 0;
}

.teacher-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.teacher-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.teacher-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin: 0 0 12px 0;
}

.teacher-stats {
  display: flex;
  align-items: center;
  gap: 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 20px;
}

.stat-item:first-child {
  padding-left: 0;
}

.stat-number {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 2px;
}

.stat-divider {
  width: 1px;
  height: 30px;
  background: var(--border-primary);
}

.sidebar-card {
  border-radius: 10px;
  position: sticky;
  top: 0;
}

.related-list {
  max-height: 600px;
  overflow-y: auto;
}

.related-item {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  cursor: pointer;
  transition: opacity 0.2s;
  border-bottom: 1px solid var(--border-tertiary);
}

.related-item:last-child {
  border-bottom: none;
}

.related-item:hover {
  opacity: 0.8;
}

.related-cover {
  position: relative;
  width: 120px;
  height: 68px;
  flex-shrink: 0;
  border-radius: 6px;
  overflow: hidden;
  background: var(--bg-tertiary);
}

.related-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.related-duration {
  position: absolute;
  bottom: 4px;
  right: 4px;
  background: rgba(0, 0, 0, 0.75);
  color: #fff;
  font-size: 11px;
  padding: 1px 5px;
  border-radius: 3px;
}

.related-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.related-title {
  font-size: 13px;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.related-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--text-secondary);
}

.related-meta .meta-item {
  display: flex;
  align-items: center;
  gap: 3px;
}

.related-meta .el-icon {
  font-size: 12px;
}

@media screen and (max-width: 1200px) {
  .detail-content {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
  }

  .sidebar-card {
    position: static;
  }

  .related-list {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    max-height: none;
  }

  .related-item {
    flex-direction: column;
    border-bottom: none;
  }

  .related-cover {
    width: 100%;
    padding-top: 56.25%;
    height: 0;
  }

  .related-cover img {
    position: absolute;
    top: 0;
    left: 0;
  }
}

@media screen and (max-width: 768px) {
  .video-title {
    font-size: 16px;
    margin: 12px 14px 10px;
  }

  .video-meta-row {
    padding: 0 14px 14px;
    flex-direction: column;
    align-items: flex-start;
  }

  .meta-right {
    width: 100%;
  }

  .meta-right .el-button {
    flex: 1;
  }

  .section-title {
    font-size: 15px;
  }

  .teacher-info {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .teacher-header {
    justify-content: center;
  }

  .teacher-stats {
    justify-content: center;
  }

  .related-list {
    grid-template-columns: 1fr;
  }

  .control-btn {
    font-size: 18px;
  }

  .time-text {
    font-size: 12px;
  }
}

@media screen and (max-width: 480px) {
  .meta-right .el-button {
    font-size: 12px;
    padding: 8px 10px;
  }

  .chapter-number {
    width: 28px;
    height: 28px;
    font-size: 12px;
  }

  .chapter-title {
    font-size: 13px;
  }
}
</style>
