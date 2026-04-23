<template>
  <el-row :gutter="16">
    <el-col :span="12">
      <el-card>
        <template #header>销售开单</template>
        <!-- 左侧：一个极简版“收银台”，支持按商品ID+数量快速开单 -->
        <el-form :model="form" label-width="90px">
          <el-form-item label="支付方式">
            <el-select v-model="form.payType" style="width: 150px">
              <el-option label="CASH" value="CASH" />
              <el-option label="WECHAT" value="WECHAT" />
              <el-option label="ALIPAY" value="ALIPAY" />
            </el-select>
          </el-form-item>
          <el-form-item label="商品">
            <el-select v-model="form.productId" placeholder="请选择商品" style="width: 260px" filterable>
              <el-option
                v-for="item in productOptions"
                :key="item.id"
                :value="item.id"
                :label="`${item.name}（${item.barcode || '无条码'}）`"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="数量"><el-input-number v-model="form.quantity" :min="1" style="width:150px"/></el-form-item>
          <!-- disabled属性用于控制按钮是否可点击 -->
          <el-button type="primary" @click="create" :disabled="!can('sales:create')" style="margin-left:100px">提交订单</el-button>
        </el-form>
      </el-card>
    </el-col>
    <el-col :span="12">
      <el-card>
        <template #header>今日销售概览</template>
        <div class="stat-item">销售额：{{ overview.totalSalesAmount || 0 }}</div>
        <div class="stat-item">订单数：{{ overview.totalOrders || 0 }}</div>
        <div class="stat-item">客单价：{{ overview.averageOrderAmount || 0 }}</div>
      </el-card>
    </el-col>
  </el-row>

  <el-card style="margin-top: 16px">
    <template #header>销售订单</template>
    <el-table :data="orders">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="orderNo" label="单号" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="row.status === 3 ? 'danger' : 'success'">
            {{ orderStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button link @click="openDetail(row.id)">查看明细</el-button>
          <el-button link type="danger" @click="refund(row.id)" v-if="can('sales:refund') && row.status !== 3">退款</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="detailVisible" title="销售订单明细" width="760px">
    <div v-loading="detailLoading" class="receipt-wrap">
      <div class="receipt-title">销售小票</div>
      <div class="receipt-meta">
        <div>单号：{{ detail.orderNo || '-' }}</div>
        <div>支付方式：{{ payTypeText(detail.payType) }}</div>
        <div>收银员：{{ detail.cashierName || '-' }}</div>
        <div>开单时间：{{ detail.createdAt || '-' }}</div>
      </div>

      <el-table :data="detail.items || []" size="small" style="margin-top: 12px">
        <el-table-column prop="productName" label="商品" min-width="220" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column label="单价" width="120">
          <template #default="{ row }">￥{{ formatAmount(row.unitPrice) }}</template>
        </el-table-column>
        <el-table-column label="小计" width="120">
          <template #default="{ row }">￥{{ formatAmount(row.amount) }}</template>
        </el-table-column>
      </el-table>

      <div class="receipt-total">
        <div>合计：￥{{ formatAmount(detail.totalAmount) }}</div>
        <div>实付：￥{{ formatAmount(detail.paidAmount) }}</div>
      </div>
    </div>
    <template #footer>
      <el-button @click="detailVisible = false">关闭</el-button>
      <el-button type="primary" @click="printReceipt" :disabled="detailLoading">打印小票</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { usePermission } from '../composables/usePermission'

const overview = ref({})
const orders = ref([])
const productOptions = ref([])
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref({ items: [] })
const form = reactive({ payType: 'CASH', productId: null, quantity: 1 })
const { can } = usePermission()

const loadProducts = async () => {
  const products = await request.get('/products', { params: { pageNum: 1, pageSize: 100 } })
  productOptions.value = (products || []).filter((item) => item.status === 1)
  if (!form.productId && productOptions.value.length > 0) {
    form.productId = productOptions.value[0].id
  }
}

const load = async () => {
  // 加载今日销售概览 + 销售订单列表
  overview.value = await request.get('/sales/stats/overview/today')
  orders.value = await request.get('/sales/orders')
}
const create = async () => {
  if (!form.productId) {
    ElMessage.warning('请选择商品')
    return
  }
  // 创建一笔销售订单：此处为简化演示，只开一行商品
  await request.post('/sales/orders', { payType: form.payType, items: [{ productId: form.productId, quantity: form.quantity }] })
  ElMessage.success('开单成功')
  load()
}

const payTypeText = (payType) => {
  const map = { CASH: '现金', WECHAT: '微信', ALIPAY: '支付宝' }
  return map[payType] || payType || '-'
}

const orderStatusText = (status) => {
  return status === 3 ? '已退款' : '交易完成'
}

const formatAmount = (amount) => {
  const value = Number(amount)
  return Number.isFinite(value) ? value.toFixed(2) : '0.00'
}

const openDetail = async (id) => {
  detailVisible.value = true
  detailLoading.value = true
  try {
    detail.value = await request.get(`/sales/orders/${id}`)
  } finally {
    detailLoading.value = false
  }
}

const printReceipt = () => {
  if (!detail.value?.orderNo) {
    ElMessage.warning('请先加载订单明细')
    return
  }
  const rows = (detail.value.items || []).map((item) => `
      <tr>
        <td>${item.productName || '-'}</td>
        <td>${item.quantity ?? 0}</td>
        <td>￥${formatAmount(item.unitPrice)}</td>
        <td>￥${formatAmount(item.amount)}</td>
      </tr>
    `).join('')

  const printWindow = window.open('', '_blank', 'width=820,height=900')
  if (!printWindow) {
    ElMessage.error('浏览器拦截了打印窗口，请允许弹窗后重试')
    return
  }

  const html = `
    <!DOCTYPE html>
    <html>
      <head>
        <meta charset="UTF-8" />
        <title>销售小票-${detail.value.orderNo}</title>
        <style>
          body { font-family: Arial, "Microsoft YaHei", sans-serif; margin: 24px; color: #222; }
          .title { text-align: center; font-size: 22px; font-weight: 700; margin-bottom: 14px; }
          .meta { display: grid; grid-template-columns: 1fr 1fr; gap: 8px 18px; margin-bottom: 12px; }
          table { width: 100%; border-collapse: collapse; margin-top: 8px; }
          th, td { border: 1px solid #dcdfe6; padding: 8px; font-size: 14px; }
          th { background: #f5f7fa; }
          .right { text-align: right; }
          .total { margin-top: 14px; display: flex; justify-content: flex-end; gap: 20px; font-size: 16px; font-weight: 700; }
        </style>
      </head>
      <body>
        <div class="title">销售小票</div>
        <div class="meta">
          <div>单号：${detail.value.orderNo || '-'}</div>
          <div>支付方式：${payTypeText(detail.value.payType)}</div>
          <div>收银员：${detail.value.cashierName || '-'}</div>
          <div>开单时间：${detail.value.createdAt || '-'}</div>
        </div>
        <table>
          <thead>
            <tr>
              <th>商品</th>
              <th>数量</th>
              <th>单价</th>
              <th>小计</th>
            </tr>
          </thead>
          <tbody>${rows}</tbody>
        </table>
        <div class="total">
          <div>合计：￥${formatAmount(detail.value.totalAmount)}</div>
          <div>实付：￥${formatAmount(detail.value.paidAmount)}</div>
        </div>
      </body>
    </html>
  `

  printWindow.document.open()
  printWindow.document.write(html)
  printWindow.document.close()
  printWindow.focus()
  printWindow.print()
  printWindow.close()
}

const refund = async (id) => {
  await ElMessageBox.confirm('确认退款该销售单吗？', '提示', { type: 'warning' })
  await request.put(`/sales/orders/${id}/refund`, { reason: '前端退款' })
  ElMessage.success('退款成功')
  load()
}

onMounted(async () => {
  await Promise.all([load(), loadProducts()])
})
</script>

<style scoped>
.stat-item { margin-bottom:10px }
.receipt-wrap { padding: 4px 2px; }
.receipt-title { text-align: center; font-size: 18px; font-weight: 700; margin-bottom: 12px; }
.receipt-meta { display: grid; grid-template-columns: 1fr 1fr; gap: 8px 20px; color: #606266; }
.receipt-total { margin-top: 12px; display: flex; justify-content: flex-end; gap: 20px; font-size: 15px; font-weight: 600; }
</style>