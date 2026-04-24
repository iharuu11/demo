<template>
  <el-card>
    <template #header>
      <div class="toolbar">
        <span>商品分类管理</span>
        <el-button type="primary" @click="openCreate" v-if="can('product:category:create')">新增分类</el-button>
      </div>
    </template>

    <el-table :data="list">
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="name" label="分类名称" min-width="220" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link @click="edit(row)" v-if="can('product:category:update')">编辑</el-button>
          <el-button link type="danger" @click="remove(row.id)" v-if="can('product:category:delete')">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="visible" :title="form.id ? '编辑分类' : '新增分类'" width="520px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="分类名称" required>
        <el-input v-model="form.name" />
      </el-form-item>
      <el-form-item label="状态" v-if="form.id">
        <el-select v-model="form.status" style="width: 100%">
          <el-option :value="1" label="启用" />
          <el-option :value="0" label="停用" />
        </el-select>
      </el-form-item>
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

const list = ref([])
const visible = ref(false)
const form = reactive({ id: null, name: '', status: 1 })
const { can } = usePermission()

const load = async () => {
  list.value = await request.get('/products/categories')
}

const openCreate = () => {
  Object.assign(form, { id: null, name: '', status: 1 })
  visible.value = true
}

const edit = (row) => {
  Object.assign(form, {
    id: row.id,
    name: row.name,
    status: row.status ?? 1,
  })
  visible.value = true
}

const save = async () => {
  if (!form.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }

  if (form.id) {
    await request.put(`/products/categories/${form.id}`, {
      name: form.name.trim(),
      status: form.status,
    })
  } else {
    await request.post('/products/categories', {
      name: form.name.trim(),
    })
  }
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该分类吗？', '提示', { type: 'warning' })
  await request.delete(`/products/categories/${id}`)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; }
</style>
