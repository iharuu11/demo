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
          <el-button @click="onSearch">查询</el-button>
        </div>
        <el-button type="primary" @click="visible=true" v-if="can('purchase:order:create')">新建采购单</el-button>
      </div>
    </template>
    <el-table :data="orders">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="orderNo" label="采购单号" />
      <el-table-column prop="supplierName" label="供应商" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="purchaseStatusTagType(row.status)">
            {{ purchaseStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="总额" width="120" />
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button link @click="openDetail(row.id)">查看明细</el-button>
          <el-button link @click="stockIn(row.id)" v-if="row.status===0 && can('purchase:order:stock-in')">入库</el-button>
          <el-button link type="danger" @click="cancel(row.id)" v-if="row.status===0 && can('purchase:order:cancel')">取消</el-button>
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
        @size-change="loadOrders"
        @current-change="loadOrders"
      />
    </div>
  </el-card>

  <el-dialog v-model="visible" title="新建采购单" width="700px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="供应商">
        <el-select v-model="form.supplierId" placeholder="请选择供应商" style="width: 100%" filterable>
          <el-option v-for="item in supplierOptions" :key="item.id" :value="item.id" :label="item.name" />
        </el-select>
      </el-form-item>
      <el-form-item label="商品明细">
        <div class="purchase-items-wrap">
          <div class="purchase-item-head">
            <span>商品</span>
            <span>数量</span>
            <span>单价</span>
            <span>操作</span>
          </div>
          <div class="purchase-item-row" v-for="(item, index) in form.items" :key="index">
            <el-select v-model="item.productId" placeholder="请选择商品" style="width: 280px" filterable>
              <el-option
                v-for="product in productOptions"
                :key="product.id"
                :value="product.id"
                :label="`${product.name}（${product.barcode || '无条码'}）`"
              />
            </el-select>
            <el-input-number v-model="item.quantity" :min="1" placeholder="数量" />
            <el-input-number v-model="item.unitPrice" :min="0.01" :precision="2" placeholder="单价" />
            <el-button link type="danger" @click="removeItem(index)" :disabled="form.items.length === 1">删除</el-button>
          </div>
          <el-button link type="primary" @click="addItem">+ 添加商品</el-button>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible=false">取消</el-button>
      <el-button type="primary" @click="create">创建</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="detailVisible" title="采购单明细" width="760px">
    <div v-loading="detailLoading" class="purchase-detail-wrap">
      <div class="purchase-detail-head">
        <div>采购单号：{{ detail.orderNo || '-' }}</div>
        <div>供应商：{{ detail.supplierName || '-' }}</div>
        <div>状态：{{ purchaseStatusText(detail.status) }}</div>
        <div>创建人：{{ detail.createdBy || '-' }}</div>
        <div>创建时间：{{ detail.createdAt || '-' }}</div>
        <div>入库时间：{{ detail.auditedAt || '-' }}</div>
      </div>

      <el-table :data="detail.items || []" size="small" style="margin-top: 12px">
        <el-table-column prop="productName" label="商品" min-width="220" />
        <el-table-column prop="quantity" label="数量" width="90" />
        <el-table-column label="单价" width="130">
          <template #default="{ row }">￥{{ formatAmount(row.unitPrice) }}</template>
        </el-table-column>
        <el-table-column label="小计" width="130">
          <template #default="{ row }">￥{{ formatAmount(row.amount) }}</template>
        </el-table-column>
      </el-table>

      <div class="purchase-detail-total">
        总金额：￥{{ formatAmount(detail.totalAmount) }}
      </div>
    </div>
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
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const visible = ref(false)
const supplierOptions = ref([])
const productOptions = ref([])
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref({ items: [] })
const form = reactive({ supplierId: null, items: [{ productId: null, quantity: 1, unitPrice: 1 }] })
const { can } = usePermission()

// 加载采购单列表
const loadOrders = async () => {
  const page = await request.get('/purchases/orders', {
    params: { status: status.value, orderNo: orderNo.value, pageNum: pageNum.value, pageSize: pageSize.value },
  })
  orders.value = page.records || []
  total.value = page.total || 0
}
const onSearch = () => {
  pageNum.value = 1
  loadOrders()
}

// 加载供应商选项（仅展示启用状态）
const loadSuppliers = async () => {
  const suppliers = await request.get('/purchases/suppliers')
  supplierOptions.value = (suppliers || []).filter((item) => item.status === 1)
  if (!form.supplierId && supplierOptions.value.length > 0) {
    form.supplierId = supplierOptions.value[0].id
  }
}

// 加载商品选项（仅展示上架状态）
const loadProducts = async () => {
  const page = await request.get('/products', { params: { pageNum: 1, pageSize: 100 } })
  const products = page.records || []
  productOptions.value = (products || []).filter((item) => item.status === 1)
  if (productOptions.value.length > 0) {
    for (const item of form.items) {
      if (!item.productId) item.productId = productOptions.value[0].id
    }
  }
}

const addItem = () => {
  form.items.push({ productId: productOptions.value[0]?.id || null, quantity: 1, unitPrice: 1 })
}

const removeItem = (index) => {
  if (form.items.length <= 1) return
  form.items.splice(index, 1)
}

const create = async () => {
  if (!form.supplierId) {
    ElMessage.warning('请选择供应商')
    return
  }
  if (!form.items.length) {
    ElMessage.warning('请至少添加一个商品')
    return
  }

  const invalidItem = form.items.find((item) => !item.productId || !item.quantity || item.quantity < 1 || !item.unitPrice || item.unitPrice <= 0)
  if (invalidItem) {
    ElMessage.warning('请检查商品、数量和单价，数量和单价必须大于0')
    return
  }

  const merged = new Map()
  for (const item of form.items) {
    const key = String(item.productId)
    const prev = merged.get(key) || { productId: item.productId, quantity: 0, unitPrice: Number(item.unitPrice) }
    prev.quantity += Number(item.quantity)
    prev.unitPrice = Number(item.unitPrice)
    merged.set(key, prev)
  }
  const items = Array.from(merged.values()).map((item) => ({
    productId: item.productId,
    quantity: item.quantity,
    unitPrice: item.unitPrice,
  }))

  await request.post('/purchases/orders', { supplierId: form.supplierId, items })

  visible.value = false
  ElMessage.success('创建成功')
  pageNum.value = 1
  form.supplierId = supplierOptions.value[0]?.id || null
  form.items = [{ productId: productOptions.value[0]?.id || null, quantity: 1, unitPrice: 1 }]
  loadOrders()
}

const purchaseStatusText = (statusCode) => {
  const map = { 0: '待入库', 1: '已入库', 2: '已取消' }
  return map[statusCode] || '-'
}

const purchaseStatusTagType = (statusCode) => {
  if (statusCode === 1) return 'success'
  if (statusCode === 2) return 'info'
  return 'warning'
}

const formatAmount = (amount) => {
  const value = Number(amount)
  return Number.isFinite(value) ? value.toFixed(2) : '0.00'
}

const openDetail = async (id) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    detail.value = await request.get(`/purchases/orders/${id}`)
  } finally {
    detailLoading.value = false
  }
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
  await Promise.all([loadOrders(), loadSuppliers(), loadProducts()])
})
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; }
.pagination-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }
.purchase-items-wrap { width: 100%; }
.purchase-item-head { display: grid; grid-template-columns: 280px 82px 100px 70px; gap: 10px; margin-bottom: 6px; color: #909399; font-size: 12px; }
.purchase-item-row { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.purchase-detail-wrap { padding: 4px 2px; }
.purchase-detail-head { display: grid; grid-template-columns: 1fr 1fr; gap: 8px 16px; color: #606266; }
.purchase-detail-total { margin-top: 12px; text-align: right; font-size: 15px; font-weight: 600; }
</style>
