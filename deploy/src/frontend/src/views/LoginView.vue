<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h2 class="login-title">AI Forward 部署管理</h2>
      </template>

      <el-form :model="form" label-position="top" @submit.prevent="handleLogin">
        <el-alert
          v-if="error"
          type="error"
          :title="error"
          :closable="false"
          style="margin-bottom: 15px"
        />

        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>

        <el-button type="primary" :loading="loading" @click="handleLogin" style="width: 100%">
          登录
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { login } = useAuth()

const form = reactive({
  username: 'admin',
  password: 'admin',
})

const loading = ref(false)
const error = ref('')

const handleLogin = async () => {
  if (!form.username || !form.password) {
    error.value = '用户名和密码不能为空'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const result = await login(form.username, form.password)
    if (result.success) {
      // 检查是否需要强制修改密码
      if (result.mustChangePassword) {
        router.push({ name: 'PasswordChange', query: { forced: 'true' } })
      } else {
        router.push('/')
      }
    } else {
      error.value = result.message || '登录失败'
    }
  } catch {
    error.value = '登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  max-width: 400px;
  margin: 100px auto;
}

.login-card {
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.login-title {
  text-align: center;
  color: #333;
  margin: 0;
}
</style>
