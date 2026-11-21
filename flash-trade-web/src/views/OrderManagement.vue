<template>
  <div class="order-management">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="title">📋 订单管理</span>
          <div class="header-right">
            <el-button type="primary" @click="refreshOrders">🔄 刷新</el-button>
            <el-button :disabled="loading || !!loadError || orders.length === 0" @click="exportOrders">
              📥 导出当前页
            </el-button>
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
                <el-option label="高风险标记" value="BLOCKED" />
                <el-option label="管理员已处置" value="CANCELLED_BY_ADMIN" />
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
                value-format="YYYY-MM-DD"
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
                value-format="YYYY-MM-DD"
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
      <el-alert
        title="撤单暂不可用：现有后端接口只更新本地订单状态，未确认券商侧撤单结果。"
        description="为避免本地显示已取消而券商订单仍有效，当前管理端不发送撤单请求。"
        type="warning"
        show-icon
        :closable="false"
        class="order-safety-notice"
      />
      <el-alert
        v-if="loadError"
        title="订单列表加载失败"
        :description="loadError"
        type="error"
        show-icon
        :closable="false"
      >
        <el-button type="primary" plain @click="loadOrders">重试</el-button>
      </el-alert>
      <el-table
        v-else
        :data="orders"
        stripe
        style="width: 100%; margin-top: 20px"
        :loading="loading"
        @row-click="viewOrderDetail"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="id" label="订单ID" width="80" sortable="custom" />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column prop="symbol" label="股票代码" width="80" sortable="custom" />
        <el-table-column prop="side" label="方向" width="60">
          <template #default="{ row }">
            <el-tag :type="row.side === 'BUY' ? 'success' : 'danger'">
              {{ row.side === 'BUY' ? '📈 买' : '📉 卖' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="数量" width="80" sortable="custom" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tooltip
              v-if="row.status === 'CANCELLED'"
              content="表示管理系统中的订单状态已更新，不代表券商侧已确认撤单。"
              placement="top"
            >
              <el-tag :type="getStatusColor(row.status)">
                {{ getStatusLabel(row.status) }}
              </el-tag>
            </el-tooltip>
            <el-tag v-else :type="getStatusColor(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="riskFlag" label="风险标记" width="100">
          <template #default="{ row }">
            <el-tooltip
              v-if="row.riskFlag === 'BLOCKED'"
              content="仅表示订单被管理员标记为高风险，不代表系统已封禁账户或阻止交易。"
              placement="top"
            >
              <el-tag type="danger">{{ getRiskFlagLabel(row.riskFlag) }}</el-tag>
            </el-tooltip>
            <el-tag v-else :type="getRiskFlagColor(row.riskFlag)">
              {{ getRiskFlagLabel(row.riskFlag) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" sortable="custom" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click.stop="viewOrderDetail(row)">
              👁️ 查看
            </el-button>
            <el-button link type="warning" size="small" @click.stop="markOrder(row)">
              📝 标记
            </el-button>
            <el-tooltip content="后端尚未实现券商侧撤单确认" placement="top">
              <el-button link type="info" size="small" disabled @click.stop>撤单暂不可用</el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-if="!loadError"
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
            <el-option label="高风险标记" value="BLOCKED" />
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
          <el-button :disabled="saving" @click="markDialogVisible = false">❌ 取消</el-button>
          <el-button type="warning" :loading="saving" @click="confirmMark">✅ 确认</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="订单详情" width="min(760px, 94vw)">
      <el-skeleton v-if="detailLoading" :rows="6" animated />
      <el-alert
        v-else-if="detailError"
        :title="detailError"
        type="error"
        show-icon
        :closable="false"
      >
        <el-button
          type="primary"
          plain
          :disabled="!selectedOrder"
          @click="selectedOrder && viewOrderDetail(selectedOrder)"
        >
          重试
        </el-button>
      </el-alert>
      <el-descriptions v-else-if="orderDetail" :column="2" border>
        <el-descriptions-item label="订单 ID">{{ orderDetail.id }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ orderDetail.username }} ({{ orderDetail.userId }})</el-descriptions-item>
        <el-descriptions-item label="券商">{{ orderDetail.brokerName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="账户">{{ maskAccountNo(orderDetail.accountNo) }}</el-descriptions-item>
        <el-descriptions-item label="股票代码">{{ orderDetail.symbol }}</el-descriptions-item>
        <el-descriptions-item label="方向 / 类型">{{ orderDetail.side }} / {{ orderDetail.orderType }}</el-descriptions-item>
        <el-descriptions-item label="委托数量">{{ orderDetail.quantity }}</el-descriptions-item>
        <el-descriptions-item label="已成交数量">{{ orderDetail.filledQuantity ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="限价">{{ orderDetail.limitPrice ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="平均成交价">{{ orderDetail.averagePrice ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="订单总价值">{{ orderDetail.totalValue ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="佣金">{{ orderDetail.commission ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusLabel(orderDetail.status) }}</el-descriptions-item>
        <el-descriptions-item label="风险标记">{{ getRiskFlagLabel(orderDetail.riskFlag) }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ orderDetail.createdAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ orderDetail.updatedAt || '-' }}</el-descriptions-item>
        <el-descriptions-item label="管理员备注" :span="2">{{ orderDetail.adminNote || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-empty v-else description="订单详情暂不可用" />
      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

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

interface OrderDetail extends Order {
  userId: number
  brokerName?: string
  accountNo?: string
  orderType: string
  limitPrice?: number
  filledQuantity?: number
  averagePrice?: number
  totalValue?: number
  commission?: number
  updatedAt?: string
}

interface PageResponse<T> {
  content: T[]
  totalElements: number
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
const sortBy = ref('created_at')
const sortOrder = ref<'asc' | 'desc'>('desc')
const total = ref(0)
const loadError = ref('')
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailError = ref('')
const orderDetail = ref<OrderDetail | null>(null)
const selectedOrder = ref<Order | null>(null)
const saving = ref(false)
let detailRequestId = 0

const filters = ref<Filters>({})
const markDialogVisible = ref(false)
const currentOrder = ref<Order | null>(null)

const markForm = ref({
  riskFlag: 'NORMAL',
  adminNote: ''
})
let ordersRequestId = 0

// 加载订单列表
const loadOrders = async () => {
  const requestId = ++ordersRequestId
  loading.value = true
  loadError.value = ''
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      userId: filters.value.userId || undefined,
      symbol: filters.value.symbol?.trim() || undefined,
      status: filters.value.status || undefined,
      riskFlag: filters.value.riskFlag || undefined,
      side: filters.value.side || undefined,
      startDate: filters.value.startDate || undefined,
      endDate: filters.value.endDate || undefined,
      sortBy: sortBy.value,
      sortOrder: sortOrder.value
    }
    const response = await request.get<PageResponse<Order>>('/admin/orders', { params })
    if (!response || !Array.isArray(response.content)
        || typeof response.totalElements !== 'number'
        || !Number.isFinite(response.totalElements)
        || response.totalElements < 0) {
      throw new Error('服务器返回的订单分页数据格式无效')
    }
    if (requestId === ordersRequestId) {
      orders.value = response.content
      total.value = response.totalElements
    }
  } catch (error) {
    if (requestId === ordersRequestId) {
      loadError.value = `加载订单失败：${formatRequestError(error)}`
    }
  } finally {
    if (requestId === ordersRequestId) loading.value = false
  }
}

// 搜索订单
const searchOrders = () => {
  const userId = filters.value.userId
  if (userId !== undefined && userId !== null && (!Number.isSafeInteger(userId) || userId <= 0)) {
    ElMessage.warning('用户 ID 必须是正整数')
    return
  }
  if (filters.value.startDate && filters.value.endDate
      && filters.value.startDate > filters.value.endDate) {
    ElMessage.warning('开始日期不能晚于结束日期')
    return
  }
  currentPage.value = 1
  loadOrders()
}

// 刷新订单
const refreshOrders = () => {
  loadOrders()
}

const handleSortChange = (sort: { prop?: string | null; order: 'ascending' | 'descending' | null }) => {
  const sortFields: Record<string, string> = {
    id: 'id',
    symbol: 'symbol',
    quantity: 'quantity',
    createdAt: 'created_at'
  }
  const sortField = sort.prop ? sortFields[sort.prop] : undefined
  if (!sort.order || !sortField) {
    sortBy.value = 'created_at'
    sortOrder.value = 'desc'
  } else {
    sortBy.value = sortField
    sortOrder.value = sort.order === 'ascending' ? 'asc' : 'desc'
  }
  currentPage.value = 1
  loadOrders()
}

// 重置过滤器
const resetFilters = () => {
  filters.value = {}
  sortBy.value = 'created_at'
  sortOrder.value = 'desc'
  currentPage.value = 1
  loadOrders()
}

// 查看订单详情
const viewOrderDetail = async (row: Order) => {
  detailDialogVisible.value = true
  selectedOrder.value = row
  const requestId = ++detailRequestId
  detailLoading.value = true
  detailError.value = ''
  orderDetail.value = null
  try {
    const result = await request.get<OrderDetail>(`/admin/orders/${row.id}`)
    if (requestId === detailRequestId) orderDetail.value = result
  } catch (error) {
    if (requestId === detailRequestId) detailError.value = `加载订单详情失败：${formatRequestError(error)}`
  } finally {
    if (requestId === detailRequestId) detailLoading.value = false
  }
}

const formatRequestError = (error: unknown): string => {
  if (error instanceof Error && error.message) return error.message
  if (typeof error === 'object' && error !== null) {
    const parts: string[] = []
    if ('message' in error && typeof error.message === 'string' && error.message) {
      parts.push(error.message)
    }
    if ('code' in error && typeof error.code === 'string' && error.code) {
      parts.push(`错误码：${error.code}`)
    }
    if ('traceId' in error && typeof error.traceId === 'string' && error.traceId) {
      parts.push(`追踪 ID：${error.traceId}`)
    }
    if (parts.length > 0) return parts.join('；')
  }
  return typeof error === 'string' && error ? error : '请求失败，请稍后重试'
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
  if (saving.value) return

  const validRiskFlags = ['NORMAL', 'SUSPICIOUS', 'BLOCKED']
  if (!validRiskFlags.includes(markForm.value.riskFlag)) {
    ElMessage.warning('请选择有效的风险标记')
    return
  }
  if (markForm.value.adminNote.length > 500) {
    ElMessage.warning('管理员备注不能超过 500 个字符')
    return
  }

  saving.value = true
  try {
    await request.put(`/admin/orders/${currentOrder.value.id}/mark`, {
      ...markForm.value,
      adminNote: markForm.value.adminNote.trim() || null
    })
    ElMessage.success('订单风险标记已更新')
    markDialogVisible.value = false
    loadOrders()
  } catch {
    await loadOrders()
    markDialogVisible.value = false
    ElMessage.warning('标记请求结果未确认，请核对订单状态后再执行后续操作。')
  } finally {
    saving.value = false
  }
}

const exportOrders = () => {
  if (orders.value.length === 0) {
    ElMessage.info('当前页没有可导出的订单')
    return
  }
  const headers = ['订单ID', '用户', '股票代码', '方向', '数量', '状态', '风险标记', '创建时间']
  const rows = orders.value.map(order => [
    order.id, order.username, order.symbol, order.side, order.quantity,
    order.status, order.riskFlag, order.createdAt
  ])
  const csvValue = (value: unknown) => {
    const text = String(value ?? '')
    const safeText = /^[\s]*[=+\-@]/.test(text) ? `'${text}` : text
    return `"${safeText.replace(/"/g, '""')}"`
  }
  const csv = [headers, ...rows].map(row => row.map(csvValue).join(',')).join('\r\n')
  const url = URL.createObjectURL(new Blob(['\uFEFF', csv], { type: 'text/csv;charset=utf-8' }))
  const link = document.createElement('a')
  link.href = url
  link.download = `orders-page-${currentPage.value}.csv`
  link.click()
  link.remove()
  window.setTimeout(() => URL.revokeObjectURL(url), 1000)
}

const maskAccountNo = (accountNo?: string) => {
  if (!accountNo) return '-'
  return accountNo.length <= 4 ? '••••' : `${'•'.repeat(accountNo.length - 4)}${accountNo.slice(-4)}`
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
    CANCELLED: '本地状态已取消',
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
    BLOCKED: '高风险标记',
    CANCELLED_BY_ADMIN: '管理员已处置'
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

.order-safety-notice {
  margin-bottom: 16px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
