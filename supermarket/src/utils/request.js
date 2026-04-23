import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

// 统一的 HTTP 请求封装：
// - baseURL: 后端接口统一以 /api 开头
// - request 拦截器：在请求头里自动带上登录 Token
// - response 拦截器：统一处理后端的 ApiResponse 结构与错误提示
//axios是可以发送http，get post请求的库
const request = axios.create({
  baseURL: '/api',
  //表示请求超时的毫秒数
  timeout: 10000,
})

//interceptors.request为定义一个请求拦截器
//config为当前请求的配置对象，axios会把当前请求参数打包成config传进来
request.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) {
    // 后端使用 Bearer Token（JWT）鉴权
    //将当前请求的登录用户token带给后端
    config.headers.Authorization = `Bearer ${auth.token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const payload = response.data
    // 后端统一返回 ApiResponse：
    // - code === 0: 成功，data 为真实业务数据
    // - code !== 0: 失败，message 为错误原因（例如：用户名或密码错误）
    if (payload && payload.code === 0) return payload.data
    //如果payload存在则用.message,否则用默认错误信息'request failed'
    //创建一个错误，让try，catch能捕获到
    return Promise.reject(new Error(payload ?.message || 'request failed'))
  },
  (error) => {
    const auth = useAuthStore()
    //读取http状态码
    const status = error?.response?.status
    //获取错误信息
    const message = error?.response?.data?.message || error.message || 'request failed'
    if (status === 401) {
      // 401 一般表示 Token 失效/未登录：清理本地登录态并提示重新登录
      auth.logout()
      ElMessage.error('登录已失效，请重新登录')
    } else {
      // 其它错误：直接展示后端返回的 message（如果有）
      ElMessage.error(message)
    }
    // 关键点：这里抛出带 message 的 Error，页面层就不会只看到 “status code 400”
    return Promise.reject(new Error(message))
  },
)

export default request
