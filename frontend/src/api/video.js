import { getVideoList, getVideoById, getRelatedVideos, getCategories } from '../mock/videoData'

const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms))

export const getVideoPage = async (params) => {
  await delay(200)
  const result = getVideoList(params)
  return {
    code: 200,
    message: 'success',
    data: {
      records: result.records,
      total: result.total,
      current: result.current,
      size: result.size,
      pages: result.pages
    }
  }
}

export const getVideoDetail = async (id) => {
  await delay(200)
  const video = getVideoById(id)
  if (!video) {
    return { code: 404, message: '视频不存在', data: null }
  }
  return { code: 200, message: 'success', data: video }
}

export const getVideoCategories = async () => {
  await delay(100)
  return { code: 200, message: 'success', data: getCategories() }
}

export const getRelatedVideoList = async (videoId, limit = 6) => {
  await delay(150)
  return { code: 200, message: 'success', data: getRelatedVideos(videoId, limit) }
}

export const getHotVideos = async (limit = 10) => {
  await delay(150)
  const result = getVideoList({ current: 1, size: limit, sort: 'hot' })
  return { code: 200, message: 'success', data: result.records }
}
