<template>
  <div>
    <slot v-if="!hasError" />
    <component :is="Error500Component" v-else :error-info="errorInfo" />
  </div>
</template>

<script setup>
import { ref, onErrorCaptured, defineAsyncComponent } from 'vue'
import logger from '../../utils/logger'

const Error500Component = defineAsyncComponent(() => import('./Error500.vue'))

const hasError = ref(false)
const errorInfo = ref(null)

onErrorCaptured((error, vm, info) => {
  hasError.value = true
  errorInfo.value = {
    message: error.message,
    detail: info,
    traceId: null
  }
  logger.fatal('组件错误被捕获', {
    type: 'COMPONENT_ERROR',
    error: error.message,
    stack: error.stack,
    component: vm?.$options?.name,
    info
  })
  return false
})
</script>
