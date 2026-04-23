<template>
  <!-- gutter表示每个el-col之间的间距
   el-row为行容器,el-col为列容器,el-card为卡片容器 -->
  <el-row :gutter="16">
    <!-- 顶部 4 个卡片：展示今日的关键运营指标 
     span表示每个卡片占据的列宽，element固定一行为24列 
     在template中ref会自动解包，不用加.value，在script中需要加.value -->
    <el-col :span="6"><el-card>今日销售额：{{ overview.todaySalesAmount || 0 }}</el-card></el-col>
    <el-col :span="6"><el-card>销售订单数：{{ overview.todaySalesOrders || 0 }}</el-card></el-col>
    <el-col :span="6"><el-card>今日采购额：{{ overview.todayPurchaseAmount || 0 }}</el-card></el-col>
    <el-col :span="6"><el-card>低库存数：{{ overview.lowStockCount || 0 }}</el-card></el-col>
  </el-row>
  <!-- margin-top表示卡片与上方元素的间距 -->
  <el-card style="margin-top: 16px">
    <!-- 将库存预警放到卡片的header区域,下方body区域放表格内容 -->
    <template #header>库存预警</template>
    <el-table :data="warnings">
      <!-- width为表格列宽，不填则根据剩余空间自动调整 -->
      <el-table-column prop="productId" label="商品ID" width="100" />
      <el-table-column prop="productName" label="商品名称" />
      <el-table-column prop="quantity" label="当前库存" width="120" />
      <el-table-column prop="warningQty" label="预警值" width="120" />
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import request from '../utils/request'

const overview = ref({})
const warnings = ref([])

//onMounted生命周期钩子函数：组件挂载完成后执行一次
onMounted(async () => {
  // 读取后端看板接口：今日概览 + 库存预警列表
  overview.value = await request.get('/dashboard/overview/today')
  //params参数会被转换成查询字符串，例如?limit=20，后端可以通过@RequestParam获取，表示一次查询20条
  warnings.value = await request.get('/dashboard/inventory-warnings', { params: { limit: 20 } })
})
</script>
