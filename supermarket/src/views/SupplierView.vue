<template>
  <el-card>
    <template #header>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="供应商名称/联系人/电话" style="width: 280px" />
        <div>
          <el-button @click="load">查询</el-button>
          <el-button type="primary" @click="openCreate" v-if="can('purchase:supplier:create')">新增供应商</el-button>
        </div>
      </div>
    </template>

    <el-table :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="供应商名称" min-width="180" />
      <el-table-column prop="contactName" label="联系人" width="140" />
      <el-table-column prop="contactPhone" label="联系电话" width="160" />
      <el-table-column prop="address" label="地址" min-width="220" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link @click="edit(row)" v-if="can('purchase:supplier:update')">编辑</el-button>
          <el-button link @click="toggleStatus(row)" v-if="can('purchase:supplier:status:update')">
            {{ row.status === 1 ? '停用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="visible" :title="form.id ? '编辑供应商' : '新增供应商'" width="520px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="供应商名称" required><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="联系人"><el-input v-model="form.contactName" /></el-form-item>
      <el-form-item label="联系电话"><el-input v-model="form.contactPhone" /></el-form-item>
      <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { usePermission } from '../composables/usePermission'

const keyword = ref('')
const list = ref([])
const visible = ref(false)
const form = reactive({ id: null, name: '', contactName: '', contactPhone: '', address: '' })
const { can } = usePermission()

const load = async () => {
  list.value = await request.get('/purchases/suppliers/page', { params: { keyword: keyword.value, pageNum: 1, pageSize: 100 } })
}

const openCreate = () => {
  Object.assign(form, { id: null, name: '', contactName: '', contactPhone: '', address: '' })
  visible.value = true
}

const edit = (row) => {
  Object.assign(form, {
    id: row.id,
    name: row.name,
    contactName: row.contactName || '',
    contactPhone: row.contactPhone || '',
    address: row.address || '',
  })
  visible.value = true
}

const save = async () => {
  if (!form.name.trim()) {
    ElMessage.warning('请输入供应商名称')
    return
  }

  const payload = {
    name: form.name.trim(),
    contactName: form.contactName?.trim() || null,
    contactPhone: form.contactPhone?.trim() || null,
    address: form.address?.trim() || null,
  }
  if (form.id) await request.put(`/purchases/suppliers/${form.id}`, payload)
  else await request.post('/purchases/suppliers', payload)
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

const toggleStatus = async (row) => {
  const nextStatus = row.status === 1 ? 0 : 1
  await ElMessageBox.confirm(`确认${nextStatus === 1 ? '启用' : '停用'}该供应商吗？`, '提示', { type: 'warning' })
  await request.put(`/purchases/suppliers/${row.id}/status`, { status: nextStatus })
  ElMessage.success('状态更新成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; }
</style>
