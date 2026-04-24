<template>
  <el-card>
    <template #header>
      <div class="toolbar">
        <!-- 顶部工具栏：搜索 + 新增按钮 -->
        <div class="search-row">
          <el-input v-model="keyword" placeholder="名称/条码" style="width: 240px" />
          <el-select v-model="categoryId" placeholder="全部分类" clearable style="width: 180px">
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :value="item.id"
              :label="item.name"
            />
          </el-select>
        </div>
        <div>
          <el-button @click="onSearch">查询</el-button>
          <el-button type="primary" @click="openCreate" v-if="can('product:create')">新增商品</el-button>
        </div>
      </div>
    </template>
    <el-table :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="名称" />
      <el-table-column label="分类" width="140">
        <template #default="{ row }">{{ categoryName(row.categoryId) }}</template>
      </el-table-column>
      <el-table-column prop="barcode" label="条码" />
      <el-table-column prop="salePrice" label="售价" width="100" />
      <el-table-column prop="stockQty" label="库存" width="80" />
      <el-table-column prop="status" label="状态" width="100" >
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <!-- 根据权限决定是否显示编辑/上下架/删除操作 -->
          <el-button link @click="edit(row)" v-if="can('product:update')">编辑</el-button>
          <el-button link @click="toggle(row)" v-if="can('product:status:update')">{{ row.status === 1 ? '下架' : '上架' }}</el-button>
          <el-button link type="danger" @click="remove(row.id)" v-if="can('product:delete')">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="load"
        @current-change="load"
      />
    </div>
  </el-card>

  <!-- 新增/编辑商品弹窗 -->
  <el-dialog v-model="visible" :title="form.id ? '编辑商品' : '新增商品'" width="520px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="条码"><el-input v-model="form.barcode" /></el-form-item>
      <el-form-item label="商品分类">
        <el-select v-model="form.categoryId" style="width: 100%" placeholder="请选择商品分类">
          <el-option
            v-for="item in categoryOptions"
            :key="item.id"
            :value="item.id"
            :label="item.status === 1 ? item.name : `${item.name}（已停用）`"
            :disabled="item.status !== 1 && item.id !== form.categoryId"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="进价"><el-input-number v-model="form.purchasePrice" :min="0.01" :precision="2" /></el-form-item>
      <el-form-item label="售价"><el-input-number v-model="form.salePrice" :min="0.01" :precision="2" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <!-- type="primary"表示主按钮样式 -->
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
const categoryId = ref(null)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const categoryOptions = ref([])
const visible = ref(false)
const form = reactive({ id: null, name: '', barcode: '', categoryId: 1, purchasePrice: 1, salePrice: 1 })
const { can } = usePermission()

// 加载商品列表（支持关键字搜索）
const load = async () => {
  const page = await request.get('/products', {
    params: { keyword: keyword.value, categoryId: categoryId.value, pageNum: pageNum.value, pageSize: pageSize.value },
  })
  list.value = page.records || []
  total.value = page.total || 0
}
const onSearch = () => {
  pageNum.value = 1
  load()
}
const loadCategories = async () => {
  const categories = await request.get('/products/categories')
  categoryOptions.value = categories || []
  if (!form.categoryId && categoryOptions.value.length > 0) {
    form.categoryId = categoryOptions.value.find((item) => item.status === 1)?.id || categoryOptions.value[0].id
  }
}
const categoryName = (categoryId) => categoryOptions.value.find((item) => item.id === categoryId)?.name || `#${categoryId}`
//Object.assign()方法用于将所有可枚举的属性值从一个或多个源对象复制到目标对象。它将返回目标对象。
//visible.value = true表示显示弹窗，false表示隐藏弹窗
const openCreate = () => {
  Object.assign(form, {
    id: null,
    name: '',
    barcode: '',
    categoryId: categoryOptions.value.find((item) => item.status === 1)?.id || categoryOptions.value[0]?.id || null,
    purchasePrice: 1,
    salePrice: 1,
  })
  visible.value = true
}
const edit = (row) => { Object.assign(form, row); visible.value = true }

const save = async () => {
  if (!form.categoryId) return ElMessage.warning('请选择商品分类')
  // 组装请求数据：只提交后端需要的字段
  const payload = { name: form.name, barcode: form.barcode, categoryId: form.categoryId, purchasePrice: form.purchasePrice, salePrice: form.salePrice }
  if (form.id) await request.put(`/products/${form.id}`, payload)
  else await request.post('/products', payload)
  ElMessage.success('保存成功')
  visible.value = false
  pageNum.value = 1
  load()
}
const toggle = async (row) => {
  await ElMessageBox.confirm(`确认${row.status === 1 ? '下架' : '上架'}该商品吗？`, '提示', { type: 'warning' })
  await request.put(`/products/${row.id}/status`, { status: row.status === 1 ? 0 : 1 })
  ElMessage.success('操作成功')
  load()
}
const remove = async (id) => {
  await ElMessageBox.confirm('确认删除该商品吗？', '提示', { type: 'warning' })
  await request.delete(`/products/${id}`)
  ElMessage.success('删除成功')
  pageNum.value = 1
  load()
}

onMounted(async () => {
  await loadCategories()
  await load()
})
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; }
.search-row { display: flex; gap: 8px; }
.pagination-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>
