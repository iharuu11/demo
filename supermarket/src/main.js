import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import App from './App.vue'
import router from './router'

// 创建 Vue 应用的入口文件：
// - 挂载全局状态管理 Pinia
// - 注册路由
// - 注册 Element Plus UI 组件库
const app = createApp(App)
//存储数据
app.use(createPinia())
app.use(router)
app.use(ElementPlus)
app.mount('#app')
