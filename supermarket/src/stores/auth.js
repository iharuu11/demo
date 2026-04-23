import { defineStore } from 'pinia'
import request from '../utils/request'

// 登录态（auth store）：
// - token/username/role/permissions：用于控制路由跳转、菜单显示、按钮权限等
// - localStorage：用于刷新页面后仍保留登录态（否则一刷新就变“未登录”）
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    username: localStorage.getItem('username') || '',
    role: localStorage.getItem('role') || '',
    // permissions为数组，JSON.parse将json字符串转为JavaScript对象
    permissions: JSON.parse(localStorage.getItem('permissions') || '[]'),
    //profileLoaded用于表示用户资料是否已加载
    profileLoaded: false,
  }),
  actions: {
    //payload为请求体的数据包，用这些数据post调用后端register服务
    async register(payload) {
      // 注册（不自动登录）：注册成功后让用户回到登录页进行登录
      await request.post('/auth/register', payload)
    },
    async login(payload) {
      // 登录成功后：
      // - 保存 token（后续请求会自动带到请求头里）
      // - 保存用户信息与权限（用于前端权限控制）
      const data = await request.post('/auth/login', payload)
      //this指当前pinia store实例，即更新前面state中的元素
      this.token = data.token
      this.username = data.username
      this.role = data.role
      this.permissions = data.permissions || []
      this.profileLoaded = true
      localStorage.setItem('token', this.token)
      localStorage.setItem('username', this.username)
      localStorage.setItem('role', this.role)
      localStorage.setItem('permissions', JSON.stringify(this.permissions))
    },
    async fetchProfile() {
      // 刷新页面后，pinia 的内存状态会丢失：
      // 但 localStorage 里还留着 token，所以这里用 token 去后端换回权限信息
      //http发送get请求，等待后端返回数据
      const data = await request.get('/auth/me/permissions')
      this.username = data.username
      this.role = data.role
      this.permissions = data.permissions || []
      this.profileLoaded = true
      localStorage.setItem('username', this.username)
      localStorage.setItem('role', this.role)
      localStorage.setItem('permissions', JSON.stringify(this.permissions))
    },
    hasPermission(code) {
      // 超级管理员默认拥有所有权限；其它用户按 permissions 列表判断
      return this.role === 'ADMIN' || this.permissions.includes(code)
    },
    logout() {
      // 退出登录：清空内存态 + 本地持久化数据

      //立即影响当前页面，当前权限立马失效
      this.token = ''
      this.username = ''
      this.role = ''
      this.permissions = []
      this.profileLoaded = false
      //清空本地数据，防止刷新后恢复登录状态
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('role')
      localStorage.removeItem('permissions')
    },
  },
})
