<template>
  <el-card>
    <template #header>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="手机号/姓名" style="width: 240px" />
        <div>
          <el-button @click="load">查询</el-button>
          <el-button type="primary" @click="openCreate">会员注册</el-button>
        </div>
      </div>
    </template>

    <el-table :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column label="性别" width="90">
        <template #default="{ row }">{{ genderText(row.gender) }}</template>
      </el-table-column>
      <el-table-column prop="level" label="等级" width="90" />
      <el-table-column prop="points" label="积分" width="90" />
      <el-table-column prop="balance" label="余额" width="110" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link @click="edit(row)">维护信息</el-button>
          <el-button link @click="toggleStatus(row)">{{ row.status === 1 ? '停用' : '启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="createVisible" title="会员注册" width="520px">
    <el-form :model="createForm" label-width="90px">
      <el-form-item label="手机号"><el-input v-model="createForm.phone" /></el-form-item>
      <el-form-item label="密码"><el-input v-model="createForm.password" type="password" show-password /></el-form-item>
      <el-form-item label="姓名"><el-input v-model="createForm.name" /></el-form-item>
      <el-form-item label="性别">
        <el-select v-model="createForm.gender" style="width: 100%">
          <el-option :value="0" label="未知" />
          <el-option :value="1" label="男" />
          <el-option :value="2" label="女" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="createVisible = false">取消</el-button>
      <el-button type="primary" @click="createMember">注册</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="editVisible" title="会员信息维护" width="520px">
    <el-form :model="editForm" label-width="90px">
      <el-form-item label="姓名"><el-input v-model="editForm.name" /></el-form-item>
      <el-form-item label="性别">
        <el-select v-model="editForm.gender" style="width: 100%">
          <el-option :value="0" label="未知" />
          <el-option :value="1" label="男" />
          <el-option :value="2" label="女" />
        </el-select>
      </el-form-item>
      <el-form-item label="余额"><el-input-number v-model="editForm.balance" :min="0" :precision="2" /></el-form-item>
      <el-form-item label="等级"><el-input-number v-model="editForm.level" :min="1" /></el-form-item>
      <el-form-item label="积分"><el-input-number v-model="editForm.points" :min="0" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="editVisible = false">取消</el-button>
      <el-button type="primary" @click="updateMember">保存</el-button>
    </template>
  </el-dialog>

</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const keyword = ref('')
const list = ref([])

const createVisible = ref(false)
const editVisible = ref(false)

const createForm = reactive({ phone: '', password: '', name: '', gender: 0 })
const editForm = reactive({ id: null, name: '', gender: 0, balance: 0, level: 1, points: 0 })

const genderText = (gender) => {
  const map = { 0: '未知', 1: '男', 2: '女' }
  return map[gender] || '未知'
}

const load = async () => {
  list.value = await request.get('/members', { params: { keyword: keyword.value, pageNum: 1, pageSize: 100 } })
}

const openCreate = () => {
  Object.assign(createForm, { phone: '', password: '', name: '', gender: 0 })
  createVisible.value = true
}

const createMember = async () => {
  await request.post('/members/register', createForm)
  ElMessage.success('会员注册成功')
  createVisible.value = false
  load()
}

const edit = (row) => {
  Object.assign(editForm, {
    id: row.id,
    name: row.name,
    gender: row.gender ?? 0,
    balance: Number(row.balance || 0),
    level: row.level || 1,
    points: row.points || 0,
  })
  editVisible.value = true
}

const updateMember = async () => {
  await request.put(`/members/${editForm.id}`, {
    name: editForm.name,
    gender: editForm.gender,
    balance: editForm.balance,
    level: editForm.level,
    points: editForm.points,
  })
  ElMessage.success('会员信息已更新')
  editVisible.value = false
  load()
}

const toggleStatus = async (row) => {
  const nextStatus = row.status === 1 ? 0 : 1
  await ElMessageBox.confirm(`确认${nextStatus === 1 ? '启用' : '停用'}该会员吗？`, '提示', { type: 'warning' })
  await request.put(`/members/${row.id}/status`, null, { params: { status: nextStatus } })
  ElMessage.success('状态更新成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; }
</style>
