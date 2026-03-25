<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <h3 class="login-title">修改密码</h3>
      </template>

      <el-alert
        v-if="isForced"
        type="warning"
        title="首次登录提示：为了您的账户安全，请修改默认密码。"
        :closable="false"
        style="margin-bottom: 15px"
      />

      <el-alert
        v-if="error"
        :type="errorType"
        :title="error"
        :closable="false"
        style="margin-bottom: 15px"
      />

      <el-form :model="form" label-position="top" @submit.prevent="handleSubmit">
        <el-form-item label="原密码">
          <el-input v-model="form.oldPassword" type="password" placeholder="请输入原密码" />
        </el-form-item>

        <el-form-item label="新密码">
          <el-input v-model="form.newPassword" type="password" placeholder="请输入新密码" />
        </el-form-item>

        <el-form-item label="确认密码">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入新密码" />
        </el-form-item>

        <el-form-item>
          <el-button
            v-if="!isForced"
            @click="handleSkip"
            style="margin-right: 10px"
          >
            稍后修改
          </el-button>
          <el-button type="primary" :loading="loading" @click="handleSubmit">
            保存
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '@/composables/useAuth'

const route = useRoute()
const router = useRouter()
const { changePassword } = useAuth()

const isForced = computed(() => route.query.forced === 'true')

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const loading = ref(false)
const error = ref('')
const errorType = ref<'error' | 'success'>('error')

const handleSubmit = async () => {
  error.value = ''

  if (form.newPassword.length < 6) {
    error.value = '密码长度至少 6 位'
    errorType.value = 'error'
    return
  }

  if (form.newPassword !== form.confirmPassword) {
    error.value = '两次输入的密码不一致'
    errorType.value = 'error'
    return
  }

  loading.value = true

  try {
    const result = await changePassword(form.oldPassword, form.newPassword)
    if (result.success) {
      error.value = '密码修改成功，即将跳转...'
      errorType.value = 'success'
      setTimeout(() => {
        router.push('/')
      }, 1000)
    } else {
      error.value = result.message || '修改失败'
      errorType.value = 'error'
    }
  } catch {
    error.value = '修改失败，请稍后重试'
    errorType.value = 'error'
  } finally {
    loading.value = false
  }
}

const handleSkip = () => {
  if (isForced.value) {
    alert('首次登录必须修改密码')
    return
  }
  router.push('/')
}
</script>

<style scoped>
.login-container {
  max-width: 500px;
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
