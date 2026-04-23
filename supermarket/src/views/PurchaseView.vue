<template>
  <el-card>
    <template #header>
      <div class="toolbar">
        <!-- 采购单列表筛选：按状态 + 单号过滤 -->
        <div style="display:flex;gap:8px">
          <el-select v-model="status" clearable placeholder="状态" style="width: 140px">
            <el-option :value="0" label="待入库" />
            <el-option :value="1" label="已入库" />
            <el-option :value="2" label="已取消" />
          </el-select>
          <el-input v-model="orderNo" placeholder="采购单号" style="width: 220px" />
          <el-button @click="loadOrders">查询</el-button>
        </div>
        <el-button type="primary" @click="visible=true" v-if="can('purchase:order:create')">新建采购单</el-button>
      </div>
    </template>
    <el-table :data="orders">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="orderNo" label="采购单号" />
      <el-table-column prop="supplierName" label="供应商" />
      <el-table-column prop="statusText" label="状态" width="120" />
      <el-table-column prop="totalAmount" label="总额" width="120" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link @click="stockIn(row.id)" v-if="row.status===0 && can('purchase:order:stock-in')">入库</el-button>
          <el-button link type="danger" @click="cancel(row.id)" v-if="row.status===0 && can('purchase:order:cancel')">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="visible" title="新建采购单" width="560px">
    <el-form :model="form" label-width="100px">
      <!-- 这里为简化演示：每次新建采购单只包含一个商品，真实业务可以扩展成可添加多行 -->
      <el-form-item label="供应商">
        <el-select v-model="form.supplierId" placeholder="请选择供应商" style="width: 100%">
          <el-option v-for="item in supplierOptions" :key="item.id" :value="item.id" :label="item.name" />
        </el-select>
      </el-form-item>
      <el-form-item label="商品ID"><el-input-number v-model="form.productId" :min="1" /></el-form-item>
      <el-form-item label="数量"><el-input-number v-model="form.quantity" :min="1" /></el-form-item>
      <el-form-item label="单价"><el-input-number v-model="form.unitPrice" :min="0.01" :precision="2" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible=false">取消</el-button>
      <el-button type="primary" @click="create">创建</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { usePermission } from '../composables/usePermission'

const orders = ref([])
const status = ref(null)
const orderNo = ref('')
const visible = ref(false)
const supplierOptions = ref([])
const form = reactive({ supplierId: null, productId: 1, quantity: 1, unitPrice: 1 })
const { can } = usePermission()

// 加载采购单列表
const loadOrders = async () => {
  orders.value = await request.get('/purchases/orders', { params: { status: status.value, orderNo: orderNo.value } })
}

// 加载供应商选项（仅展示启用状态）
const loadSuppliers = async () => {
  const suppliers = await request.get('/purchases/suppliers')
  supplierOptions.value = (suppliers || []).filter((item) => item.status === 1)
  if (!form.supplierId && supplierOptions.value.length > 0) {
    form.supplierId = supplierOptions.value[0].id
  }
}

const create = async () => {
  if (!form.supplierId) {
    ElMessage.warning('请选择供应商')
    return
  }
  // 创建采购单：只传一个 items 元素（简化版）
  await request.post('/purchases/orders', {
    supplierId: form.supplierId,
    items: [{ productId: form.productId, quantity: form.quantity, unitPrice: form.unitPrice }],
  })
  visible.value = false
  ElMessage.success('创建成功')
  loadOrders()
}
const stockIn = async (id) => {
  await ElMessageBox.confirm('确认执行采购入库吗？', '提示', { type: 'warning' })
  await request.put(`/purchases/orders/${id}/stock-in`)
  ElMessage.success('入库成功')
  loadOrders()
}
const cancel = async (id) => {
  await ElMessageBox.confirm('确认取消该采购单吗？', '提示', { type: 'warning' })
  await request.put(`/purchases/orders/${id}/cancel`)
  ElMessage.success('已取消')
  loadOrders()
}
onMounted(async () => {
  await Promise.all([loadOrders(), loadSuppliers()])
})
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; }
</style>
