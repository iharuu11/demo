<template>
  <el-card>
    <template #header>
      <div class="toolbar">
        <div style="display:flex;gap:8px">
          <el-input v-model="keyword" placeholder="手机号/姓名" style="width: 220px" />
          <el-select v-model="bizType" clearable placeholder="类型" style="width: 180px">
            <el-option label="手动充值" value="MANUAL_RECHARGE" />
            <el-option label="手动扣减" value="MANUAL_DEDUCT" />
            <el-option label="消费扣款" value="SALES_CONSUME" />
            <el-option label="退款返还" value="SALES_REFUND" />
          </el-select>
          <el-button @click="onSearch">查询</el-button>
        </div>
      </div>
    </template>

    <el-table :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="memberPhone" label="手机号" width="140" />
      <el-table-column prop="memberName" label="会员姓名" width="120" />
      <el-table-column label="变动金额" width="120">
        <template #default="{ row }">
          <span :style="{ color: Number(row.deltaAmount) >= 0 ? '#67c23a' : '#f56c6c' }">
            {{ Number(row.deltaAmount) >= 0 ? '+' : '' }}{{ row.deltaAmount }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="afterBalance" label="变动后余额" width="120" />
      <el-table-column label="类型" width="140">
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

const keyword = ref('')
const bizType = ref('')
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const bizTypeText = (bizType) => {
  const map = {
    MANUAL_RECHARGE: '手动充值',
    MANUAL_DEDUCT: '手动扣减',
    SALES_CONSUME: '消费扣款',
    SALES_REFUND: '退款返还',
  }
  return map[bizType] || bizType || '-'
}

const load = async () => {
  const page = await request.get('/members/balance-logs', {
    params: {
      keyword: keyword.value,
      bizType: bizType.value,
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

onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; }
.pagination-wrap { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>
