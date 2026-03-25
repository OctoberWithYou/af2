<template>
  <router-view />
</template>

<script setup lang="ts">
// 根组件，仅作为路由容器
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial,
    'Noto Sans', sans-serif;
  background: linear-gradient(135deg, #667eea, #764ba2);
  min-height: 100vh;
  padding: 20px;
}

#app {
  max-width: 1400px;
  margin: 0 auto;
}
</style>
