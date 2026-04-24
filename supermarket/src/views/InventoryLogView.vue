<template>
  <el-card>
    <template #header>
      <div class="toolbar">
        <div style="display:flex;gap:8px">
          <el-input-number v-model="productId" :min="1" placeholder="商品ID" />
          <el-button @click="onSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
      </div>
    </template>

    <el-table :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="productId" label="商品ID" width="100" />
      <el-table-column prop="productName" label="商品名称" min-width="180" />
      <el-table-column prop="deltaQty" label="变动数量" width="100" />
      <el-table-column prop="afterQty" label="变动后库存" width="110" />
      <el-table-column label="业务类型" width="140">
        <template #default="{ row }">{{ bizTypeText(row.bizType) }}</template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" min-width="220" />
      <el-table-column prop="operatorName" label="操作人" width="120" />
      <el-table-column prop="createdAt" label="时间" width="180" />
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
</template>

<script setup>
import { onMounted, ref } from 'vue'
import request from '../utils/request'

const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const productId = ref(null)

const bizTypeText = (bizType) => {
  const map = {
    MANUAL: '手动调整',
    PURCHASE_STOCK_IN: '采购入库',
    SALES_OUT: '销售出库',
    SALES_REFUND_IN: '销售退货入库',
  }
  return map[bizType] || bizType || '-'
}

const load = async () => {
  const page = await request.get('/products/inventory/logs', {
    params: {
      productId: productId.value || undefined,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    },
  })
  list.value = page.records || []
  total.value = page.total || 0
}

const onSearch = () => {
  pageNum.value = 1
  load()
}

const resetSearch = () => {
  productId.value = null
  pageNum.value = 1
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; }
.pagination-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>
