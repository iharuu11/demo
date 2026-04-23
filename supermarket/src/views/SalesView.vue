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
          <el-form-item label="商品ID"><el-input-number v-model="form.productId" :min="1" style="width:150px"/></el-form-item>
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
      <el-table-column prop="totalAmount" label="金额" width="120" />
      <el-table-column prop="payType" label="支付" width="100" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button link type="danger" @click="refund(row.id)" v-if="can('sales:refund')">退款</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { usePermission } from '../composables/usePermission'

const overview = ref({})
const orders = ref([])
const form = reactive({ payType: 'CASH', productId: 1, quantity: 1 })
const { can } = usePermission()

const load = async () => {
  // 加载今日销售概览 + 销售订单列表
  overview.value = await request.get('/sales/stats/overview/today')
  orders.value = await request.get('/sales/orders')
}
const create = async () => {
  // 创建一笔销售订单：此处为简化演示，只开一行商品
  await request.post('/sales/orders', { payType: form.payType, items: [{ productId: form.productId, quantity: form.quantity }] })
  ElMessage.success('开单成功')
  load()
}
const refund = async (id) => {
  await ElMessageBox.confirm('确认退款该销售单吗？', '提示', { type: 'warning' })
  await request.put(`/sales/orders/${id}/refund`, { reason: '前端退款' })
  ElMessage.success('退款成功')
  load()
}

onMounted(load)
</script>

<style scoped>
.stat-item { margin-bottom:10px }
</style>