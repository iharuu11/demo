<template>
  <el-tabs v-model="activeTab">
    <el-tab-pane label="角色权限分配" name="role-permission">
      <el-card>
        <template #header>
          <div class="toolbar">
            <span>权限分配</span>
            <div class="toolbar-actions">
              <el-button @click="openCreateRole">新增角色</el-button>
              <el-button type="primary" :disabled="!selectedRoleId || saving" @click="save">保存权限</el-button>
            </div>
          </div>
        </template>

        <el-form label-width="100px">
          <el-form-item label="选择角色">
            <el-select v-model="selectedRoleId" placeholder="请选择角色" style="width: 260px" @change="loadRolePermissions">
              <el-option v-for="role in roleOptions" :key="role.id" :value="role.id" :label="`${role.name} (${role.code})`" />
            </el-select>
          </el-form-item>
        </el-form>

        <el-table :data="permissionList" v-loading="loading">
          <el-table-column width="70">
            <template #default="{ row }">
              <el-checkbox :model-value="selectedPermissionIds.includes(row.id)" @change="togglePermission(row.id, $event)" />
            </template>
          </el-table-column>
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="code" label="权限编码" min-width="220" />
          <el-table-column prop="name" label="权限名称" min-width="180" />
          <el-table-column prop="type" label="类型" width="100" />
        </el-table>
      </el-card>
    </el-tab-pane>

    <el-tab-pane label="用户角色分配" name="user-role">
      <el-card>
        <template #header>
          <div class="toolbar">
            <span>用户与角色分配</span>
          </div>
        </template>

        <el-table :data="userList" v-loading="userLoading">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="username" label="用户名" min-width="180" />
          <el-table-column prop="role" label="当前角色" width="140" />
          <el-table-column label="分配角色" width="280">
            <template #default="{ row }">
              <el-select v-model="userRoleMap[row.id]" placeholder="请选择角色" style="width: 220px">
                <el-option v-for="role in roleOptions" :key="role.id" :value="role.id" :label="`${role.name} (${role.code})`" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button type="primary" link @click="saveUserRole(row)" :disabled="!userRoleMap[row.id]">保存</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="userPageNum"
            v-model:page-size="userPageSize"
            :page-sizes="[10, 20, 50, 100]"
            :total="userTotal"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadUsers"
            @current-change="loadUsers"
          />
        </div>
      </el-card>
    </el-tab-pane>
  </el-tabs>

  <el-dialog v-model="createRoleVisible" title="新增角色" width="480px">
    <el-form :model="createRoleForm" label-width="90px">
      <el-form-item label="角色编码">
        <el-input v-model="createRoleForm.code" placeholder="例如：STORE_MANAGER" />
      </el-form-item>
      <el-form-item label="角色名称">
        <el-input v-model="createRoleForm.name" placeholder="例如：店长" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createRoleVisible = false">取消</el-button>
      <el-button type="primary" @click="createRole">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const activeTab = ref('role-permission')
const roleOptions = ref([])
const permissionList = ref([])
const selectedRoleId = ref(null)
const selectedPermissionIds = ref([])
const loading = ref(false)
const saving = ref(false)
const createRoleVisible = ref(false)
const createRoleForm = reactive({ code: '', name: '' })

const userList = ref([])
const userLoading = ref(false)
const userPageNum = ref(1)
const userPageSize = ref(10)
const userTotal = ref(0)
const userRoleMap = ref({})

const loadBase = async () => {
  loading.value = true
  try {
    const [roles, permissions] = await Promise.all([
      request.get('/rbac/roles'),
      request.get('/rbac/permissions'),
    ])
    roleOptions.value = roles || []
    permissionList.value = permissions || []
    if (!selectedRoleId.value && roleOptions.value.length > 0) {
      selectedRoleId.value = roleOptions.value[0].id
      await loadRolePermissions()
    }
    await loadUsers()
  } finally {
    loading.value = false
  }
}

const openCreateRole = () => {
  createRoleForm.code = ''
  createRoleForm.name = ''
  createRoleVisible.value = true
}

const createRole = async () => {
  if (!createRoleForm.code.trim() || !createRoleForm.name.trim()) {
    ElMessage.warning('请填写角色编码和角色名称')
    return
  }
  await request.post('/rbac/roles', { code: createRoleForm.code.trim(), name: createRoleForm.name.trim() })
  ElMessage.success('角色创建成功')
  createRoleVisible.value = false
  await loadBase()
}

const loadRolePermissions = async () => {
  if (!selectedRoleId.value) return
  selectedPermissionIds.value = await request.get(`/rbac/roles/${selectedRoleId.value}/permissions`)
}

const togglePermission = (permissionId, checked) => {
  const next = new Set(selectedPermissionIds.value)
  if (checked) next.add(permissionId)
  else next.delete(permissionId)
  selectedPermissionIds.value = Array.from(next)
}

const save = async () => {
  if (!selectedRoleId.value) {
    ElMessage.warning('请先选择角色')
    return
  }
  saving.value = true
  try {
    await request.put(`/rbac/roles/${selectedRoleId.value}/permissions`, {
      permissionIds: selectedPermissionIds.value,
    })
    ElMessage.success('权限分配已保存')
    await loadRolePermissions()
  } finally {
    saving.value = false
  }
}

const loadUsers = async () => {
  userLoading.value = true
  try {
    const page = await request.get('/users', { params: { pageNum: userPageNum.value, pageSize: userPageSize.value } })
    userList.value = page.records || []
    userTotal.value = page.total || 0

    const roleIdByCode = Object.fromEntries(roleOptions.value.map((role) => [role.code, role.id]))
    const map = {}
    for (const user of userList.value) {
      map[user.id] = roleIdByCode[user.role] || null
    }
    userRoleMap.value = map
  } finally {
    userLoading.value = false
  }
}

const saveUserRole = async (row) => {
  const roleId = userRoleMap.value[row.id]
  if (!roleId) {
    ElMessage.warning('请选择角色')
    return
  }
  await request.post(`/rbac/users/${row.id}/role`, { roleId })
  ElMessage.success('用户角色已更新')
  await loadUsers()
}

onMounted(loadBase)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; }
.toolbar-actions { display: flex; gap: 8px; }
.pagination-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>
