import { computed } from 'vue'
import { useAuthStore } from '../stores/auth'

export function usePermission() {
  const auth = useAuthStore()
  // 计算属性：当前是否为管理员（ADMIN 通常拥有所有权限）
  const isAdmin = computed(() => auth.role === 'ADMIN')
  // can(code)：判断当前用户是否拥有某个权限码（按钮/菜单显示控制都用它）
  const can = (code) => auth.hasPermission(code)
  return { can, isAdmin }
}
