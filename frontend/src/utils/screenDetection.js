import { recordSwitch, incrementWarningCount, getSwitchStatistics } from '../api/switchRecord'

export const SwitchType = {
  TAB_SWITCH: 1,
  WINDOW_SWITCH: 2,
  APP_SWITCH: 3,
  SCREENSHOT: 4,
  SCREEN_RECORD: 5
}

export const SwitchTypeLabel = {
  1: '标签页切换',
  2: '窗口切换',
  3: '应用切换',
  4: '截图操作',
  5: '屏幕录制'
}

const SCREENSHOT_KEYS = [
  'PrintScreen',
  'Meta+Shift+3',
  'Meta+Shift+4',
  'Meta+Shift+5',
  'Ctrl+Shift+A',
  'Ctrl+Alt+A',
  'Alt+PrintScreen',
  'Ctrl+PrintScreen',
  'Shift+PrintScreen',
  'Fn+PrintScreen',
  'Meta+Shift+S',
  'Ctrl+Shift+S'
]

export class ScreenDetection {
  constructor(options = {}) {
    this.recordId = options.recordId || null
    this.enabled = options.enabled !== false
    this.onWarning = options.onWarning || null
    this.onSwitch = options.onSwitch || null
    this.onAutoSubmit = options.onAutoSubmit || null

    this.isMonitoring = false
    this.switchStartTime = null
    this.currentSwitchType = null
    this.switchCount = 0
    this.totalSwitchDuration = 0
    this.screenshotCount = 0
    this.screenRecordCount = 0
    this.warningCount = 0

    this.maxSwitchCount = 3
    this.maxSingleSwitchDuration = 30
    this.maxTotalSwitchDuration = 60
    this.screenshotDetectionEnabled = true
    this.screenRecordDetectionEnabled = true
    this.autoSubmitOnExceed = false

    this.boundHandleVisibilityChange = this.handleVisibilityChange.bind(this)
    this.boundHandleWindowBlur = this.handleWindowBlur.bind(this)
    this.boundHandleWindowFocus = this.handleWindowFocus.bind(this)
    this.boundHandleKeyDown = this.handleKeyDown.bind(this)
    this.boundHandleMouseLeave = this.handleMouseLeave.bind(this)
    this.boundHandleMouseEnter = this.handleMouseEnter.bind(this)
    this.boundHandleDevTools = this.handleDevTools.bind(this)
  }

  async init() {
    if (this.recordId) {
      try {
        const res = await getSwitchStatistics(this.recordId)
        const stats = res.data
        this.switchCount = stats.switchCount || 0
        this.totalSwitchDuration = stats.totalSwitchDuration || 0
        this.screenshotCount = stats.screenshotCount || 0
        this.screenRecordCount = stats.screenRecordCount || 0
        this.warningCount = stats.warningCount || 0
        this.maxSwitchCount = stats.maxSwitchCount || 3
        this.maxSingleSwitchDuration = stats.maxSingleSwitchDuration || 30
        this.maxTotalSwitchDuration = stats.maxTotalSwitchDuration || 60
        this.screenshotDetectionEnabled = stats.screenshotDetectionEnabled !== false
        this.screenRecordDetectionEnabled = stats.screenRecordDetectionEnabled !== false
        this.autoSubmitOnExceed = stats.autoSubmitOnExceed === true
      } catch (e) {
        console.warn('Failed to load switch statistics:', e)
      }
    }
  }

  start() {
    if (!this.enabled || this.isMonitoring) return

    this.isMonitoring = true

    document.addEventListener('visibilitychange', this.boundHandleVisibilityChange)
    window.addEventListener('blur', this.boundHandleWindowBlur)
    window.addEventListener('focus', this.boundHandleWindowFocus)
    window.addEventListener('keydown', this.boundHandleKeyDown)
    document.addEventListener('mouseleave', this.boundHandleMouseLeave)
    document.addEventListener('mouseenter', this.boundHandleMouseEnter)

    this.devToolsInterval = setInterval(this.boundHandleDevTools, 1000)

    if (this.screenRecordDetectionEnabled) {
      this.detectScreenRecording()
    }
  }

  stop() {
    if (!this.isMonitoring) return

    this.isMonitoring = false

    document.removeEventListener('visibilitychange', this.boundHandleVisibilityChange)
    window.removeEventListener('blur', this.boundHandleWindowBlur)
    window.removeEventListener('focus', this.boundHandleWindowFocus)
    window.removeEventListener('keydown', this.boundHandleKeyDown)
    document.removeEventListener('mouseleave', this.boundHandleMouseLeave)
    document.removeEventListener('mouseenter', this.boundHandleMouseEnter)

    if (this.devToolsInterval) {
      clearInterval(this.devToolsInterval)
      this.devToolsInterval = null
    }

    if (this.switchStartTime) {
      this.endSwitch(Date.now())
    }
  }

  handleVisibilityChange() {
    if (document.hidden) {
      const type = this.detectSwitchType()
      this.startSwitch(type, 'visibilitychange')
    } else {
      this.endSwitch(Date.now())
    }
  }

  handleWindowBlur(e) {
    if (!this.switchStartTime) {
      this.startSwitch(SwitchType.WINDOW_SWITCH, 'window blur')
    }
  }

  handleWindowFocus(e) {
    if (this.switchStartTime) {
      this.endSwitch(Date.now())
    }
  }

  handleKeyDown(e) {
    if (!this.screenshotDetectionEnabled) return

    const keyCombo = this.getKeyCombo(e)

    if (SCREENSHOT_KEYS.includes(keyCombo)) {
      e.preventDefault()
      this.detectScreenshot(keyCombo)
    }

    if ((e.ctrlKey && e.shiftKey && e.key === 'I') ||
        (e.ctrlKey && e.shiftKey && e.key === 'J') ||
        (e.ctrlKey && e.key === 'u') ||
        (e.key === 'F12')) {
      e.preventDefault()
      this.startSwitch(SwitchType.APP_SWITCH, 'devtools shortcut')
    }
  }

  getKeyCombo(e) {
    const keys = []
    if (e.ctrlKey) keys.push('Ctrl')
    if (e.altKey) keys.push('Alt')
    if (e.shiftKey) keys.push('Shift')
    if (e.metaKey) keys.push('Meta')
    keys.push(e.key)
    return keys.join('+')
  }

  handleMouseLeave(e) {
    if (e.clientY <= 0 || e.clientX <= 0 ||
        e.clientX >= window.innerWidth || e.clientY >= window.innerHeight) {
      if (!this.switchStartTime) {
        this.startSwitch(SwitchType.WINDOW_SWITCH, 'mouse leave')
      }
    }
  }

  handleMouseEnter(e) {
    if (this.switchStartTime && this.currentSwitchType === SwitchType.WINDOW_SWITCH) {
      this.endSwitch(Date.now())
    }
  }

  handleDevTools() {
    const threshold = 160
    const widthThreshold = window.outerWidth - window.innerWidth > threshold
    const heightThreshold = window.outerHeight - window.innerHeight > threshold

    if (widthThreshold || heightThreshold) {
      if (!this.devToolsOpen) {
        this.devToolsOpen = true
        this.startSwitch(SwitchType.APP_SWITCH, 'devtools open')
      }
    } else {
      if (this.devToolsOpen) {
        this.devToolsOpen = false
        this.endSwitch(Date.now())
      }
    }
  }

  detectSwitchType() {
    const navigator = window.navigator
    if (navigator.userAgent.includes('Chrome')) {
      return SwitchType.TAB_SWITCH
    } else if (navigator.userAgent.includes('Firefox')) {
      return SwitchType.TAB_SWITCH
    } else if (navigator.userAgent.includes('Safari')) {
      return SwitchType.TAB_SWITCH
    } else if (navigator.userAgent.includes('Edg')) {
      return SwitchType.TAB_SWITCH
    }
    return SwitchType.WINDOW_SWITCH
  }

  startSwitch(type, reason) {
    if (!this.isMonitoring || this.switchStartTime) return

    this.switchStartTime = Date.now()
    this.currentSwitchType = type

    const details = JSON.stringify({
      reason,
      userAgent: navigator.userAgent,
      timestamp: new Date().toISOString()
    })

    if (this.onSwitch) {
      this.onSwitch({
        type,
        typeLabel: SwitchTypeLabel[type],
        startTime: this.switchStartTime,
        details
      })
    }
  }

  async endSwitch(endTime) {
    if (!this.switchStartTime) return

    const duration = Math.round((endTime - this.switchStartTime) / 1000)
    const type = this.currentSwitchType
    const startTime = this.switchStartTime

    this.switchStartTime = null
    this.currentSwitchType = null

    if (duration < 1) return

    this.switchCount++
    this.totalSwitchDuration += duration

    try {
      await recordSwitch({
        recordId: this.recordId,
        switchType: type,
        duration,
        appName: this.getAppName(),
        screenshotDetected: 0,
        screenRecordDetected: 0,
        details: JSON.stringify({
          duration,
          startTime: new Date(startTime).toISOString(),
          endTime: new Date(endTime).toISOString(),
          userAgent: navigator.userAgent
        })
      })
    } catch (e) {
      console.warn('Failed to record switch:', e)
    }

    this.checkThresholds(type, duration)
  }

  async detectScreenshot(keyCombo) {
    this.screenshotCount++

    try {
      await recordSwitch({
        recordId: this.recordId,
        switchType: SwitchType.SCREENSHOT,
        duration: 0,
        appName: 'Screenshot',
        screenshotDetected: 1,
        screenRecordDetected: 0,
        details: JSON.stringify({
          keyCombo,
          timestamp: new Date().toISOString(),
          userAgent: navigator.userAgent
        })
      })
    } catch (e) {
      console.warn('Failed to record screenshot:', e)
    }

    this.triggerWarning(SwitchType.SCREENSHOT, '检测到截图操作')
  }

  async detectScreenRecording() {
    if (!navigator.mediaDevices || !navigator.mediaDevices.getDisplayMedia) {
      return
    }

    const originalGetDisplayMedia = navigator.mediaDevices.getDisplayMedia.bind(navigator.mediaDevices)

    navigator.mediaDevices.getDisplayMedia = async (constraints) => {
      this.screenRecordCount++

      try {
        await recordSwitch({
          recordId: this.recordId,
          switchType: SwitchType.SCREEN_RECORD,
          duration: 0,
          appName: 'Screen Recorder',
          screenshotDetected: 0,
          screenRecordDetected: 1,
          details: JSON.stringify({
            timestamp: new Date().toISOString(),
            constraints: JSON.stringify(constraints),
            userAgent: navigator.userAgent
          })
        })
      } catch (e) {
        console.warn('Failed to record screen record:', e)
      }

      this.triggerWarning(SwitchType.SCREEN_RECORD, '检测到屏幕录制操作')

      return originalGetDisplayMedia(constraints)
    }
  }

  getAppName() {
    const ua = navigator.userAgent
    if (ua.includes('Chrome') && !ua.includes('Edg')) return 'Google Chrome'
    if (ua.includes('Firefox')) return 'Mozilla Firefox'
    if (ua.includes('Safari') && !ua.includes('Chrome')) return 'Apple Safari'
    if (ua.includes('Edg')) return 'Microsoft Edge'
    if (ua.includes('Opera') || ua.includes('OPR')) return 'Opera'
    return 'Unknown Browser'
  }

  checkThresholds(type, duration) {
    let warnings = []

    if (this.switchCount > this.maxSwitchCount) {
      warnings.push(`切屏次数已达 ${this.switchCount} 次，超过最大限制 ${this.maxSwitchCount} 次`)
    }

    if (duration > this.maxSingleSwitchDuration) {
      warnings.push(`单次切屏时长 ${duration} 秒，超过最大限制 ${this.maxSingleSwitchDuration} 秒`)
    }

    if (this.totalSwitchDuration > this.maxTotalSwitchDuration) {
      warnings.push(`累计切屏时长 ${this.totalSwitchDuration} 秒，超过最大限制 ${this.maxTotalSwitchDuration} 秒`)
    }

    if (warnings.length > 0) {
      this.triggerWarning(type, warnings.join('；'))
    }
  }

  async triggerWarning(type, message) {
    this.warningCount++

    try {
      await incrementWarningCount(this.recordId)
    } catch (e) {
      console.warn('Failed to increment warning count:', e)
    }

    if (this.onWarning) {
      this.onWarning({
        type,
        typeLabel: SwitchTypeLabel[type],
        message,
        count: this.warningCount,
        switchCount: this.switchCount,
        totalSwitchDuration: this.totalSwitchDuration,
        screenshotCount: this.screenshotCount,
        screenRecordCount: this.screenRecordCount
      })
    }

    if (this.autoSubmitOnExceed && this.warningCount >= 3) {
      if (this.onAutoSubmit) {
        this.onAutoSubmit()
      }
    }
  }

  getStats() {
    return {
      switchCount: this.switchCount,
      totalSwitchDuration: this.totalSwitchDuration,
      screenshotCount: this.screenshotCount,
      screenRecordCount: this.screenRecordCount,
      warningCount: this.warningCount,
      isMonitoring: this.isMonitoring,
      maxSwitchCount: this.maxSwitchCount,
      maxSingleSwitchDuration: this.maxSingleSwitchDuration,
      maxTotalSwitchDuration: this.maxTotalSwitchDuration
    }
  }

  updateRecordId(recordId) {
    this.recordId = recordId
    this.init()
  }
}

export default ScreenDetection
