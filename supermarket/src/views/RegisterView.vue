<template>
  <div class="register-page">
    <el-card class="register-card">
      <h2>注册</h2>
      <!-- 注册表单：用户名 + 密码 + 确认密码 -->
      <!-- @submit.prevent表示提交表单时不刷新页面，而是执行onSubmit方法 -->
      <el-form :model="form" @submit.prevent="onSubmit">
        <el-form-item label="用户名">
          <!-- autocomplete是html输入框的自动填充提示属性，告诉浏览器这个字段是什么类型的数据 -->
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password autocomplete="new-password"/>
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="form.confirmPassword" type="password" show-password autocomplete="new-password" />
        </el-form-item>
        <!--loading为按钮添加加载状态,为true时显示加载动画,不可重复点击,false恢复正常-->
        <el-button type="primary" :loading="loading" @click="onSubmit" style="width: 100%">注册</el-button>

        <div class="actions">
          <el-button link @click="router.push('/login')">已有账号？去登录</el-button>
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
const form = reactive({ username: '', password: '', confirmPassword: '' })

const onSubmit = async () => {
  // 基础校验：不能为空、两次密码必须一致
  if (!form.username || !form.password) return ElMessage.warning('请输入用户名和密码')
  if (form.password !== form.confirmPassword) return ElMessage.warning('两次输入的密码不一致')

  loading.value = true
  try {
    // 注册成功后不会自动登录，而是引导用户去登录
    await auth.register({ username: form.username, password: form.password })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {
    // Error toast is handled centrally in `utils/request.js`.
    // finally无论try中代码是否抛出错误，都会执行finally中的代码。这里用于在请求结束后关闭加载状态。
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page { height: 100vh; display: flex; justify-content: center; align-items: center; background: #f3f6fb; }
.register-card { width: 380px; }
.actions { margin-top: 8px; display: flex; justify-content: flex-end; }
</style>

