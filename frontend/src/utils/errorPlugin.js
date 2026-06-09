import errorHandler from './errorHandler'
import logger from './logger'

export default {
  install(app) {
    app.config.errorHandler = (error, vm, info) => {
      errorHandler.handleGlobalError(error, vm, info)
    }

    app.config.warnHandler = (msg, vm, trace) => {
      logger.warn('Vue警告', {
        type: 'VUE_WARNING',
        message: msg,
        component: vm?.$options?.name,
        trace
      })
    }

    app.config.globalProperties.$errorHandler = errorHandler
    app.config.globalProperties.$logger = logger

    app.provide('errorHandler', errorHandler)
    app.provide('logger', logger)

    setupWindowErrorHandlers()
  }
}

function setupWindowErrorHandlers() {
  window.addEventListener('error', (event) => {
    if (event.target !== window) {
      errorHandler.handleResourceError(event)
      return
    }
    errorHandler.handleScriptError(
      event.message,
      event.filename,
      event.lineno,
      event.colno,
      event.error
    )
  }, true)

  window.addEventListener('unhandledrejection', (event) => {
    errorHandler.handleUnhandledRejection(event)
  })
}
