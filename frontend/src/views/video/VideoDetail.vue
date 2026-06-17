<template>
  <div class="video-detail-page" v-loading="loading">
    <h2 class="page-title">{{ t('video.videoDetail') }}</h2>
    <div class="back-btn" @click="goBack">
      <el-icon><ArrowLeft /></el-icon>
      <span>{{ t('video.backToList') }}</span>
    </div>

    <div class="detail-content" v-if="videoDetail">
      <div class="main-content">
        <div class="video-player-card">
          <div class="video-player" ref="playerContainerRef">
            <div class="player-wrapper">
              <video
                v-if="isPlaying && videoDetail.videoUrl"
                :src="getVideoUrl(videoDetail.videoUrl)"
                controls
                autoplay
                style="width: 100%; height: 100%; position: absolute; top: 0; left: 0; background: #000"
                :poster="getImageUrl(videoDetail.coverUrl)"
              >
                您的浏览器不支持视频播放
              </video>
              <img
                v-else
                :src="getImageUrl(videoDetail.coverUrl)"
                :alt="videoDetail.title"
                class="poster-img"
                :class="{ blurred: isPlaying }"
              />
              <div class="player-overlay" @click="togglePlay" v-if="!isPlaying">
                <div class="play-button">
                  <el-icon :size="64"><VideoPlay /></el-icon>
                </div>
              </div>

              <div class="resume-dialog" v-if="showResumeDialog && savedProgress > 0" @click.stop>
                <div class="resume-content">
                  <div class="resume-icon">
                    <el-icon :size="32"><VideoPlay /></el-icon>
                  </div>
                  <div class="resume-text">
                    <div class="resume-title">{{ t('video.resumePlayback') }}</div>
                    <div class="resume-desc">{{ t('video.resumeFrom', { time: formatTime(savedProgress) }) }}</div>
                  </div>
                  <div class="resume-actions">
                    <el-button type="primary" @click.stop="resumePlayback">{{ t('video.resumePlayback') }}</el-button>
                    <el-button @click.stop="playFromStart">{{ t('video.playFromStart') }}</el-button>
                  </div>
                </div>
              </div>

              <div class="player-controls" v-show="isPlaying || showControls">
                <div class="progress-bar" @click="seekProgress">
                  <div class="progress-buffered" :style="{ width: bufferedPercent + '%' }"></div>
                  <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
                  <div class="progress-dot" :style="{ left: progressPercent + '%' }"></div>
                </div>
                <div class="control-row">
                  <div class="control-left">
                    <el-icon class="control-btn" @click.stop="togglePlay">
                      <VideoPlay v-if="!isPlaying" />
                      <VideoPause v-else />
                    </el-icon>
                    <span class="time-text">{{ currentTimeText }} / {{ totalTimeText }}</span>
                  </div>
                  <div class="control-right">
                    <div class="control-btn-group">
                      <el-dropdown trigger="click" @command="handleSpeedChange">
                        <span class="control-btn speed-btn">
                          {{ playbackSpeed }}x
                        </span>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item
                              v-for="speed in speedOptions"
                              :key="speed"
                              :command="speed"
                              :class="{ active: playbackSpeed === speed }"
                            >
                              {{ speed }}x
                            </el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>

                    <div class="control-btn-group">
                      <el-dropdown trigger="click" @command="handleQualityChange">
                        <span class="control-btn quality-btn">
                          {{ currentQualityLabel }}
                        </span>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item
                              v-for="q in qualityOptions"
                              :key="q.key"
                              :command="q.key"
                              :class="{ active: currentQuality === q.key }"
                            >
                              {{ t(`video.quality${q.key}`) }}
                            </el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>

                    <el-icon class="control-btn" @click.stop="toggleFullscreen">
                      <FullScreen v-if="!isFullscreen" />
                      <Aim v-else />
                    </el-icon>
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

        <el-card shadow="hover" class="section-card" v-if="false">
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
              <el-tag v-for="tag in (videoDetail.tagList || [])" :key="tag" size="small" effect="plain">
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </el-card>

        <el-card shadow="hover" class="section-card" v-if="videoDetail.categoryName">
          <template #header>
            <span class="section-title">{{ t('video.category') }}</span>
          </template>
          <div class="category-info">
            <el-tag type="primary" size="large">{{ videoDetail.categoryName }}</el-tag>
            <div class="video-stats" style="margin-top: 16px">
              <div class="stat-item">
                <span class="stat-number">{{ formatCount(videoDetail.viewCount) }}</span>
                <span class="stat-label">{{ t('video.views') }}</span>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <span class="stat-number">{{ formatCount(videoDetail.likeCount) }}</span>
                <span class="stat-label">{{ t('video.likes') }}</span>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <span class="stat-number">{{ videoDetail.rating }}</span>
                <span class="stat-label">{{ t('video.rating') }}</span>
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
                <img :src="getImageUrl(item.coverUrl)" :alt="item.title" />
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
import { ref, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  ArrowLeft, VideoPlay, VideoPause, View, Star, StarFilled,
  Share, UserFilled, FullScreen, Aim
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getVideoDetail, getRelatedVideos } from '../../api/video'

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
const showControls = ref(false)
const isFullscreen = ref(false)
const playbackSpeed = ref(1)
const currentQuality = ref('720p')
const showResumeDialog = ref(false)
const savedProgress = ref(0)

const playerContainerRef = ref(null)
let playTimer = null
let saveProgressTimer = null

const speedOptions = [0.5, 0.75, 1, 1.25, 1.5, 2]
const qualityOptions = [
  { key: '360p', resolution: '360P' },
  { key: '720p', resolution: '720P' },
  { key: '1080p', resolution: '1080P' },
  { key: '4k', resolution: '4K' }
]

const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  if (url.startsWith('/api')) return url
  return '/api' + url
}

const getVideoUrl = (url) => {
  return getImageUrl(url)
}

const progressPercent = computed(() => {
  if (totalTime.value === 0) return 0
  return (currentTime.value / totalTime.value) * 100
})

const bufferedPercent = computed(() => {
  return Math.min(progressPercent.value + 10, 100)
})

const currentTimeText = computed(() => formatTime(currentTime.value))
const totalTimeText = computed(() => formatTime(totalTime.value))

const currentQualityLabel = computed(() => {
  const q = qualityOptions.find(item => item.key === currentQuality.value)
  return q ? q.resolution : '720P'
})

const difficultyType = computed(() => {
  const map = {
    '入门': 'success',
    '进阶': 'warning',
    '高级': 'danger'
  }
  return map[videoDetail.value?.difficulty] || 'info'
})

const STORAGE_KEY_PREFIX = 'video_progress_'

const getStorageKey = () => {
  return STORAGE_KEY_PREFIX + (route.query.id || 'default')
}

const loadSavedProgress = () => {
  try {
    const saved = localStorage.getItem(getStorageKey())
    if (saved) {
      const data = JSON.parse(saved)
      savedProgress.value = data.currentTime || 0
      if (data.quality) {
        currentQuality.value = data.quality
      }
      if (data.speed) {
        playbackSpeed.value = data.speed
      }
      return savedProgress.value > 0 && savedProgress.value < totalTime.value - 5
    }
  } catch (e) {
    console.warn('Failed to load video progress:', e)
  }
  return false
}

const saveProgress = () => {
  try {
    const data = {
      currentTime: currentTime.value,
      quality: currentQuality.value,
      speed: playbackSpeed.value,
      updatedAt: Date.now()
    }
    localStorage.setItem(getStorageKey(), JSON.stringify(data))
  } catch (e) {
    console.warn('Failed to save video progress:', e)
  }
}

const startProgressSaver = () => {
  stopProgressSaver()
  saveProgressTimer = setInterval(() => {
    if (isPlaying.value) {
      saveProgress()
    }
  }, 5000)
}

const stopProgressSaver = () => {
  if (saveProgressTimer) {
    clearInterval(saveProgressTimer)
    saveProgressTimer = null
  }
}

const loadVideoDetail = async () => {
  loading.value = true
  try {
    const id = route.query.id
    const res = await getVideoDetail(id)
    videoDetail.value = res.data
    if (videoDetail.value?.outline?.length > 0) {
      totalTime.value = videoDetail.value.durationSeconds || 3600
    }
    await nextTick()
    const hasSaved = loadSavedProgress()
    if (hasSaved) {
      showResumeDialog.value = true
    }
  } finally {
    loading.value = false
  }
}

const loadRelatedVideos = async () => {
  const id = route.query.id
  const res = await getRelatedVideos(id, 8)
  relatedVideos.value = res.data
}

const togglePlay = () => {
  if (showResumeDialog.value) {
    showResumeDialog.value = false
  }
  isPlaying.value = !isPlaying.value
  if (isPlaying.value) {
    startPlayback()
    startProgressSaver()
  } else {
    stopPlayback()
    saveProgress()
  }
}

const resumePlayback = () => {
  showResumeDialog.value = false
  currentTime.value = savedProgress.value
  isPlaying.value = true
  startPlayback()
  startProgressSaver()
}

const playFromStart = () => {
  showResumeDialog.value = false
  currentTime.value = 0
  isPlaying.value = true
  startPlayback()
  startProgressSaver()
}

const startPlayback = () => {
  stopPlayback()
  const interval = 1000 / playbackSpeed.value
  playTimer = setInterval(() => {
    if (currentTime.value < totalTime.value) {
      currentTime.value += 1
    } else {
      stopPlayback()
      isPlaying.value = false
      saveProgress()
    }
  }, interval)
}

const stopPlayback = () => {
  if (playTimer) {
    clearInterval(playTimer)
    playTimer = null
  }
}

const handleSpeedChange = (speed) => {
  playbackSpeed.value = speed
  if (isPlaying.value) {
    startPlayback()
  }
  saveProgress()
  ElMessage.success(`${speed}x`)
}

const handleQualityChange = (quality) => {
  currentQuality.value = quality
  saveProgress()
  ElMessage.success(t(`video.quality${quality}`))
}

const toggleFullscreen = () => {
  if (!playerContainerRef.value) return

  if (!isFullscreen.value) {
    if (playerContainerRef.value.requestFullscreen) {
      playerContainerRef.value.requestFullscreen()
    } else if (playerContainerRef.value.webkitRequestFullscreen) {
      playerContainerRef.value.webkitRequestFullscreen()
    } else if (playerContainerRef.value.msRequestFullscreen) {
      playerContainerRef.value.msRequestFullscreen()
    }
    isFullscreen.value = true
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
    } else if (document.webkitExitFullscreen) {
      document.webkitExitFullscreen()
    } else if (document.msExitFullscreen) {
      document.msExitFullscreen()
    }
    isFullscreen.value = false
  }
}

const handleFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement || 
    !!document.webkitFullscreenElement || 
    !!document.msFullscreenElement
}

const seekProgress = (e) => {
  const rect = e.currentTarget.getBoundingClientRect()
  const percent = (e.clientX - rect.left) / rect.width
  currentTime.value = Math.floor(percent * totalTime.value)
  saveProgress()
}

const selectChapter = (chapter) => {
  currentChapter.value = chapter
  currentTime.value = 0
  if (!isPlaying.value) {
    isPlaying.value = true
    startPlayback()
    startProgressSaver()
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
  stopProgressSaver()
  saveProgress()
  currentTime.value = 0
  isPlaying.value = false
  currentChapter.value = 1
  showResumeDialog.value = false
  savedProgress.value = 0
  router.push({ path: '/video/detail', query: { id } })
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

watch(
  () => route.query.id,
  (newId) => {
    if (newId) {
      loadVideoDetail()
      loadRelatedVideos()
    }
  }
)

onMounted(() => {
  loadVideoDetail()
  loadRelatedVideos()
  document.addEventListener('fullscreenchange', handleFullscreenChange)
  document.addEventListener('webkitfullscreenchange', handleFullscreenChange)
  document.addEventListener('msfullscreenchange', handleFullscreenChange)
})

onUnmounted(() => {
  stopPlayback()
  stopProgressSaver()
  saveProgress()
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
  document.removeEventListener('webkitfullscreenchange', handleFullscreenChange)
  document.removeEventListener('msfullscreenchange', handleFullscreenChange)
})
</script>

<style scoped>
.video-detail-page {
  width: 100%;
  position: relative;
}

.page-title {
  margin: 0 0 16px 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
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
  box-shadow: var(--shadow-card);
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
  transition: filter 0.3s;
}

.poster-img.blurred {
  filter: brightness(0.7);
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

.resume-dialog {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.6);
  z-index: 10;
}

.resume-content {
  background: var(--bg-card);
  border-radius: 12px;
  padding: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  max-width: 360px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

.resume-icon {
  color: var(--color-primary);
}

.resume-text {
  text-align: center;
}

.resume-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.resume-desc {
  font-size: 14px;
  color: var(--text-secondary);
}

.resume-actions {
  display: flex;
  gap: 12px;
}

.player-controls {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.7));
  color: #fff;
  z-index: 5;
}

.progress-bar {
  position: relative;
  height: 4px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
  cursor: pointer;
  margin-bottom: 10px;
}

.progress-buffered {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 2px;
}

.progress-fill {
  position: absolute;
  top: 0;
  left: 0;
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

.control-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.control-btn {
  font-size: 20px;
  cursor: pointer;
  transition: color 0.2s;
  color: #fff;
}

.control-btn:hover {
  color: var(--color-primary);
}

.control-btn-group {
  display: flex;
  align-items: center;
}

.speed-btn,
.quality-btn {
  font-size: 14px;
  font-weight: 500;
  padding: 2px 6px;
  border-radius: 4px;
  transition: background 0.2s;
}

.speed-btn:hover,
.quality-btn:hover {
  background: rgba(255, 255, 255, 0.2);
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

:deep(.el-dropdown-menu__item.active) {
  color: var(--color-primary);
  font-weight: 500;
}

.video-player:fullscreen {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-player:fullscreen .player-wrapper {
  padding-top: 0;
  width: 100%;
  height: 100%;
}

.video-player:fullscreen .poster-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
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

  .control-right {
    gap: 12px;
  }

  .speed-btn,
  .quality-btn {
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

  .resume-content {
    padding: 20px;
    max-width: 300px;
  }

  .resume-actions {
    flex-direction: column;
    width: 100%;
  }

  .resume-actions .el-button {
    width: 100%;
  }
}
</style>
