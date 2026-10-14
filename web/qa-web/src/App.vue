<script setup lang="ts">
import { onMounted } from 'vue';
import { RouterView } from 'vue-router';
import { message } from 'ant-design-vue';
import AppHeader from './components/AppHeader.vue';
import AppFooter from './components/AppFooter.vue';
import { store } from './store';

// 应用启动即拉取医生数据，保证各页面拿到的是同一份后端数据
onMounted(async () => {
  await store.loadDoctors();
  if (store.state.doctorsError) {
    message.error(`医生数据加载失败：${store.state.doctorsError}`);
  }
});
</script>

<template>
  <a-layout class="app-layout">
    <AppHeader />
    <a-layout-content class="app-content">
      <RouterView />
    </a-layout-content>
    <AppFooter />
  </a-layout>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
    'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
    sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

.app-layout {
  min-height: 100vh;
}

.app-content {
  background: #fff;
}
</style>
