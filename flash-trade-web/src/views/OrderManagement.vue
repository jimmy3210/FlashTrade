<template>
  <div class="order-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="title">📋 订单管理</span>
          <div class="header-right">
            <el-button type="primary" @click="refreshOrders">🔄 刷新</el-button>
            <el-button @click="exportOrders">📥 导出</el-button>
          </div>
        </div>
      </template>

      <!-- 搜索过滤 -->
      <el-form :model="filters" label-width="80px" class="filter-form">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="用户ID">
              <el-input v-model.number="filters.userId" placeholder="输入用户ID" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="股票代码">
              <el-input v-model="filters.symbol" placeholder="如AAPL" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="订单状态">
              <el-select v-model="filters.status" placeholder="选择状态" clearable>
                <el-option label="全部" value="" />
                <el-option label="待处理" value="PENDING" />
                <el-option label="已成交" value="FILLED" />
                <el-option label="部分成交" value="PARTIALLY_FILLED" />
                <el-option label="已取消" value="CANCELLED" />
                <el-option label="已拒绝" value="REJECTED" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="风险标记">
              <el-select v-model="filters.riskFlag" placeholder="选择风险标记" clearable>
                <el-option label="全部" value="" />
                <el-option label="正常" value="NORMAL" />
                <el-option label="可疑" value="SUSPICIOUS" />
                <el-option label="已封禁" value="BLOCKED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="买卖方向">
              <el-select v-model="filters.side" placeholder="选择方向" clearable>
                <el-option label="全部" value="" />
                <el-option label="买入" value="BUY" />
                <el-option label="卖出" value="SELL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="开始日期">
              <el-date-picker
                v-model="filters.startDate"
                type="date"
                placeholder="选择开始日期"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="结束日期">
              <el-date-picker
                v-model="filters.endDate"
                type="date"
                placeholder="选择结束日期"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item>
              <el-button type="primary" @click="searchOrders">🔍 查询</el-button>
              <el-button @click="resetFilters">↻ 重置</el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 订单列表 -->
      <el-table
        :data="orders"
        stripe
        :default-sort="{ prop: 'createdAt', order: 'descending' }"
        style="width: 100%; margin-top: 20px"
        :loading="loading"
        @row-click="viewOrderDetail"
      >
        <el-table-column prop="id" label="订单ID" width="80" sortable />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column prop="symbol" label="股票代码" width="80" sortable />
        <el-table-column prop="side" label="方向" width="60">
          <template #default="{ row }">
            <el-tag :type="row.side === 'BUY' ? 'success' : 'danger'">
              {{ row.side === 'BUY' ? '📈 买' : '📉 卖' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusColor(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="riskFlag" label="风险标记" width="100">
          <template #default="{ row }">
            <el-tag :type="getRiskFlagColor(row.riskFlag)">
              {{ getRiskFlagLabel(row.riskFlag) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" sortable />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click.stop="viewOrderDetail(row)">
              👁️ 查看
            </el-button>
            <el-button link type="warning" size="small" @click.stop="markOrder(row)">
              📝 标记
            </el-button>
            <el-button
              link
              type="danger"
              size="small"
              :disabled="!canCancelOrder(row.status)"
              @click.stop="cancelOrder(row)"
            >
              ❌ 取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; text-align: right"
        @change="loadOrders"
      />
    </el-card>

    <!-- 标记订单对话框 -->
    <el-dialog v-model="markDialogVisible" title="📝 标记订单" width="500px">
      <el-form :model="markForm" label-width="100px">
        <el-form-item label="风险标记" required>
          <el-select v-model="markForm.riskFlag" placeholder="选择风险标记">
            <el-option label="正常" value="NORMAL" />
            <el-option label="可疑" value="SUSPICIOUS" />
            <el-option label="已封禁" value="BLOCKED" />
            <el-option label="管理员取消" value="CANCELLED_BY_ADMIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="markForm.adminNote"
            type="textarea"
            rows="4"
            placeholder="输入管理员备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="markDialogVisible = false">❌ 取消</el-button>
          <el-button type="warning" @click="confirmMark">✅ 确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 取消订单对话框 -->
    <el-dialog v-model="cancelDialogVisible" title="⚠️  取消订单" width="500px">
      <el-form :model="cancelForm" label-width="100px">
        <el-form-item label="订单ID">
          <el-input v-model="cancelForm.orderId" disabled />
        </el-form-item>
        <el-form-item label="取消原因" required>
          <el-input
            v-model="cancelForm.reason"
            type="textarea"
            rows="4"
            placeholder="输入取消原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cancelDialogVisible = false">❌ 取消</el-button>
          <el-button type="danger" @click="confirmCancel">⚠️  确认取消</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { request } from '@/utils/request'

interface Order {
  id: number
  username: string
  symbol: string
  side: string
  quantity: number
  status: string
  riskFlag: string
  createdAt: string
  adminNote?: string
}

interface Filters {
  userId?: number
  symbol?: string
  status?: string
  riskFlag?: string
  side?: string
  startDate?: string
  endDate?: string
}

const orders = ref<Order[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const filters = ref<Filters>({})
const markDialogVisible = ref(false)
const cancelDialogVisible = ref(false)
const currentOrder = ref<Order | null>(null)

const markForm = ref({
  riskFlag: 'NORMAL',
  adminNote: ''
})

const cancelForm = ref({
  orderId: '',
  reason: ''
})

// 加载订单列表
const loadOrders = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      ...filters.value
    }
    const response = await request.get('/admin/orders', { params })
    orders.value = response.data.content || []
    total.value = response.data.totalElements || 0
  } catch (error) {
    ElMessage.error('加载订单失败: ' + error)
  } finally {
    loading.value = false
  }
}

// 搜索订单
const searchOrders = () => {
  currentPage.value = 1
  loadOrders()
}

// 刷新订单
const refreshOrders = () => {
  loadOrders()
}

// 重置过滤器
const resetFilters = () => {
  filters.value = {}
  currentPage.value = 1
  loadOrders()
}

// 查看订单详情
const viewOrderDetail = (row: Order) => {
  ElMessage.info('待实现: 查看订单 ' + row.id + ' 的详情')
  // 后续可跳转到订单详情页面
}

// 标记订单
const markOrder = (row: Order) => {
  currentOrder.value = row
  markForm.value = {
    riskFlag: row.riskFlag || 'NORMAL',
    adminNote: row.adminNote || ''
  }
  markDialogVisible.value = true
}

// 确认标记
const confirmMark = async () => {
  if (!currentOrder.value) return

  try {
    await request.put(`/admin/orders/${currentOrder.value.id}/mark`, markForm.value)
    ElMessage.success('✅ 订单已标记')
    markDialogVisible.value = false
    loadOrders()
  } catch (error) {
    ElMessage.error('标记失败: ' + error)
  }
}

// 检查是否可取消
const canCancelOrder = (status: string): boolean => {
  return !['FILLED', 'CANCELLED', 'REJECTED'].includes(status)
}

// 取消订单
const cancelOrder = (row: Order) => {
  currentOrder.value = row
  cancelForm.value = {
    orderId: String(row.id),
    reason: ''
  }
  cancelDialogVisible.value = true
}

// 确认取消
const confirmCancel = async () => {
  if (!currentOrder.value || !cancelForm.value.reason) {
    ElMessage.warning('请输入取消原因')
    return
  }

  try {
    await request.delete(`/admin/orders/${currentOrder.value.id}/cancel`, {
      data: { reason: cancelForm.value.reason }
    })
    ElMessage.success('✅ 订单已取消')
    cancelDialogVisible.value = false
    loadOrders()
  } catch (error) {
    ElMessage.error('取消失败: ' + error)
  }
}

// 导出订单
const exportOrders = () => {
  ElMessage.info('待实现: 导出订单数据为 CSV')
}

// 获取状态颜色
const getStatusColor = (status: string): string => {
  const colors: Record<string, string> = {
    PENDING: 'warning',
    FILLED: 'success',
    PARTIALLY_FILLED: 'info',
    CANCELLED: 'info',
    REJECTED: 'danger'
  }
  return colors[status] || 'info'
}

// 获取状态标签
const getStatusLabel = (status: string): string => {
  const labels: Record<string, string> = {
    PENDING: '待处理',
    FILLED: '已成交',
    PARTIALLY_FILLED: '部分成交',
    CANCELLED: '已取消',
    REJECTED: '已拒绝'
  }
  return labels[status] || status
}

// 获取风险标记颜色
const getRiskFlagColor = (flag: string): string => {
  const colors: Record<string, string> = {
    NORMAL: 'success',
    SUSPICIOUS: 'warning',
    BLOCKED: 'danger',
    CANCELLED_BY_ADMIN: 'info'
  }
  return colors[flag] || 'info'
}

// 获取风险标记标签
const getRiskFlagLabel = (flag: string): string => {
  const labels: Record<string, string> = {
    NORMAL: '正常',
    SUSPICIOUS: '可疑',
    BLOCKED: '已封禁',
    CANCELLED_BY_ADMIN: '已取消'
  }
  return labels[flag] || flag
}

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-management {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.header-right {
  display: flex;
  gap: 10px;
}

.filter-form {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
