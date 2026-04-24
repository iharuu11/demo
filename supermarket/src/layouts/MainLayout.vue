<!-- 模板/结构 -->
<template>
  <el-container class="layout-root">
    <el-aside width="220px" class="sider">
      <div class="brand">超商综合管理系统</div>
      <!-- 左侧菜单：根据当前用户权限控制哪些菜单可见 
      把当前路由路径作为默认激活项，所以当前页面对应的菜单会高亮
      使用 router 模式让菜单项点击后自动导航到对应路由 -->
      <el-menu :default-active="$route.path" router class="menu">
        <el-menu-item index="/dashboard">运营看板</el-menu-item>
        <el-menu-item index="/products" v-if="can('product:create') || can('product:update') || can('product:delete')">商品管理</el-menu-item>
        <el-menu-item index="/inventory" v-if="can('inventory:adjust') || can('inventory:warning:update')">库存管理</el-menu-item>
        <el-menu-item index="/members">会员管理</el-menu-item>
        <el-menu-item index="/member-balance-logs">会员余额流水</el-menu-item>
        <el-menu-item index="/purchases" v-if="can('purchase:order:create') || can('purchase:order:stock-in')">采购管理</el-menu-item>
        <el-menu-item index="/suppliers" v-if="can('purchase:supplier:create') || can('purchase:supplier:update') || can('purchase:supplier:status:update')">供应商管理</el-menu-item>
        <el-menu-item index="/sales" v-if="can('sales:create') || can('sales:refund')">销售管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <!-- 左侧占位，使用户与退出按钮在视觉上靠右 -->
        <div />
        <div class="header-right">
          <!-- 右上角显示当前登录用户名和角色，并提供退出登录按钮 -->
          <span>{{ auth.username }} ({{ auth.role }})</span>
          <!-- link类型按钮，更轻量，不是实心按钮，danger表示危险色，通常为红色 -->
          <el-button link type="danger" @click="onLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<!-- 行为/逻辑 -->
<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { usePermission } from '../composables/usePermission'

const router = useRouter()
const auth = useAuthStore()
const { can } = usePermission()

const onLogout = () => {
  // 退出登录：清理本地登录态并跳回登录页
  auth.logout()
  router.push('/login')
}
</script>

<!-- 样式 -->
<style scoped>
.layout-root { height: 100vh; }
.sider { border-right: 1px solid #eee; background: #fff; }
.brand { height: 56px; display: flex; align-items: center; justify-content: center; font-weight: 700; }
.menu { border-right: none; }
/*justify-content: space-between使两个div一个在左侧,一个在右侧,在空间上均匀分布,
border-bottom表示在底部添加一个分隔线*/
.header { border-bottom: 1px solid #eee; display: flex; align-items: center; justify-content: space-between; }
/*gap用于设置子元素之间的间距,这里设置用户名与退出按钮之间的距离。*/
.header-right { display: flex; align-items: center; gap: 12px; }
.main { background: #f6f8fb; }
</style>
