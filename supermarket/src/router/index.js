import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

// 前端路由配置：
// - /login、/register：公开页面（未登录可访问）
// - / 下面的页面：需要登录（requiresAuth=true），并复用 MainLayout 作为整体布局
const routes = [
  { path: '/login', name: 'login', component: () => import('../views/LoginView.vue') },
  { path: '/register', name: 'register', component: () => import('../views/RegisterView.vue') },
  {
    path: '/',
    //用MainLayout.vue来渲染
    component: () => import('../layouts/MainLayout.vue'),
    //需要登录,meta表示给路由记录添加附加信息
    //在下面的前置守卫beforeEach中即可使用requiresAuth变量
    meta: { requiresAuth: true },
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'dashboard', component: () => import('../views/DashboardView.vue') },
      { path: 'products', name: 'products', component: () => import('../views/ProductView.vue') },
      { path: 'categories', name: 'categories', component: () => import('../views/CategoryView.vue') },
      { path: 'inventory', name: 'inventory', component: () => import('../views/InventoryView.vue') },
      { path: 'inventory-logs', name: 'inventory-logs', component: () => import('../views/InventoryLogView.vue') },
      { path: 'members', name: 'members', component: () => import('../views/MemberView.vue') },
      { path: 'member-balance-logs', name: 'member-balance-logs', component: () => import('../views/MemberBalanceLogView.vue') },
      { path: 'permission-assign', name: 'permission-assign', component: () => import('../views/PermissionAssignView.vue'), meta: { adminOnly: true } },
      { path: 'purchases', name: 'purchases', component: () => import('../views/PurchaseView.vue') },
      { path: 'suppliers', name: 'suppliers', component: () => import('../views/SupplierView.vue') },
      { path: 'sales', name: 'sales', component: () => import('../views/SalesView.vue') },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

//async代表一个异步函数，与await搭配使用
//to表示目标路由，from表示来源路由
router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (to.path === '/login' || to.path === '/register') {
    // 已登录用户没必要再进登录/注册页，直接去首页
    if (auth.token) return '/'
    return true
  }
  // 需要登录的页面：没有 token 就踢回登录页
  // 如果目标路由的meta中的requiresAuth为true的话（也就是需要登录），检查目前是否已登录，检查auth.token
  if (to.meta.requiresAuth && !auth.token) return '/login'
  if (auth.token && !auth.profileLoaded) {
    try {
      // 刷新页面时 pinia 状态会丢失：用 token 拉取一次个人权限信息，恢复前端“我是谁/我能做什么”
      await auth.fetchProfile()
    } catch {
      auth.logout()
      return '/login'
    }
  }
  if (to.meta.adminOnly && auth.role !== 'ADMIN') return '/dashboard'
  return true
})

export default router
