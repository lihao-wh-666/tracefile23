<template>
  <ErrorBoundary>
    <router-view />
  </ErrorBoundary>
</template>

<script setup>
import ErrorBoundary from './views/error/ErrorBoundary.vue'
</script>

<style>
body {
  margin: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
</style>
