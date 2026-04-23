<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2>登录</h2>
      <!-- 简单登录表单：只包含用户名 + 密码 -->
      <el-form :model="form" @submit.prevent="onSubmit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-button type="primary" :loading="loading" @click="onSubmit" style="width: 100%">登录</el-button>
        <div class="actions">
          <el-button link @click="router.push('/register')">没有账号？去注册</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
// 表单默认填入一个演示账号，方便本地体验；实际生产可改为空
const form = reactive({ username: '', password: '' })

const onSubmit = async () => {
  // 基础校验：不能为空
  if (!form.username || !form.password) return ElMessage.warning('请输入用户名和密码')
  loading.value = true
  try {
    // 调用 auth store 中的 login，成功后会保存 token + 用户信息
    await auth.login(form)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e) {
    // Error toast is handled centrally in `utils/request.js`.
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page { height: 100vh; display: flex; justify-content: center; align-items: center; background: #f3f6fb; }
.login-card { width: 380px; }
.actions { margin-top: 8px; display: flex; justify-content: flex-end; }
</style>
