<template>
  <el-card>
    <template #header>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="商品关键字" style="width: 240px" />
        <el-button @click="load">查询</el-button>
      </div>
    </template>
    <el-table :data="list">
      <el-table-column prop="productId" label="商品ID" width="100" />
      <el-table-column prop="productName" label="商品名称" />
      <el-table-column prop="quantity" label="库存数量" width="120" />
      <el-table-column prop="warningQty" label="预警值" width="120" />
      <el-table-column label="操作" width="220">
        <!-- default插槽：自定义单元格内容，row表示当前行数据 -->
        <template #default="{ row }">
          <!-- v-if用于控制按钮是否渲染 -->
          <el-button link @click="adjustPrompt(row)" v-if="can('inventory:adjust')">调整</el-button>
          <el-button link @click="setWarning(row)" v-if="can('inventory:warning:update')">改预警</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'
import { usePermission } from '../composables/usePermission'

const keyword = ref('')
const list = ref([])
const { can } = usePermission()
// 加载库存列表：
// - keyword：按商品关键字模糊搜索
// - 接口返回：商品 + 当前库存数量 + 预警值
const load = async () => { list.value = await request.get('/products/inventory', { params: { keyword: keyword.value } }) }

const adjustPrompt = async (row) => {
  // 手动调整库存（前端输入任意整数）：
  // - 正数：增加库存
  // - 负数：减少库存
  // 后端会校验减库存不能小于 0（库存不足会报错）
  const { value } = await ElMessageBox.prompt('输入调整数量（支持负数）', '调整库存', {
    inputPlaceholder: '例如：15 或 -3',
    //正则表达式校验规则，^表示开头，$表示结尾，-?表示可选的负号，\d+表示一个或多个数字
    inputPattern: /^-?\d+$/,
    inputErrorMessage: '请输入整数（支持负数）',
  })
  const deltaQty = Number(value)
  if (!Number.isInteger(deltaQty) || deltaQty === 0) return ElMessage.warning('调整数量必须是非 0 整数')

  // bizType/remark：用于后端记录库存流水（方便审计是谁在什么时候改了多少）
  await request.put(`/products/${row.productId}/inventory/adjust`, { deltaQty, bizType: 'MANUAL', remark: '前端调整' })
  ElMessage.success('调整成功')
  load()
}

const setWarning = async (row) => {
  // 设置库存预警值：当库存低于预警值时，系统可在看板/提醒处提示
  const { value } = await ElMessageBox.prompt('输入新的预警值', '修改预警', { inputValue: row.warningQty })
  await request.put(`/products/${row.productId}/inventory/warning-qty`, { warningQty: Number(value) })
  ElMessage.success('修改成功')
  load()
}

// 页面打开时自动加载一次
onMounted(load)
</script>

<style scoped>
.toolbar { display: flex; justify-content: space-between; }
</style>
