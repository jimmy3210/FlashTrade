<template>
  <div class="admin-overview">
    <div class="overview-heading">
      <div>
        <h2>运营概览</h2>
        <p>管理数据摘要 · {{ lastRefreshedAt ? `本次请求完成于 ${lastRefreshedAt}` : '尚未刷新' }}</p>
      </div>
      <el-button type="primary" :loading="refreshing" @click="refreshOverview">
        刷新概览
      </el-button>
    </div>

    <div class="metrics-grid">
      <el-card v-for="item in metrics" :key="item.key" class="metric-card" shadow="hover">
        <div class="metric-title">{{ item.title }}</div>
        <div class="metric-value" aria-live="polite" :aria-busy="metricState[item.key].loading">
          <span v-if="metricState[item.key].loading">加载中…</span>
          <span v-else>{{ metricState[item.key].value ?? '—' }}</span>
        </div>
        <el-alert
          v-if="metricState[item.key].error"
          class="metric-error"
          :title="metricState[item.key].error"
          type="error"
          show-icon
          :closable="false"
        />
        <div class="metric-footer">
          <router-link :to="item.to">{{ item.action }}</router-link>
          <el-button
            v-if="metricState[item.key].error"
            link
            type="primary"
            :disabled="metricState[item.key].loading"
            @click="loadMetric(item.key, metricLoaders[item.key])"
          >
            重试
          </el-button>
        </div>
      </el-card>
    </div>

    <el-alert
      class="overview-note"
      title="数据说明"
      description="概览数据来自管理 API 的查询结果；刷新时间反映本次请求完成时间，不代表各业务数据均为实时行情或券商侧最终状态。"
      type="info"
      show-icon
      :closable="false"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import request from '@/utils/request'

type MetricKey = 'activeUsers' | 'activeAccounts' | 'positions' | 'pendingOrders'

interface MetricState {
  value: number | null
  loading: boolean
  error: string
}

interface AdminUser {
  deleteFlag: number
}

interface BrokerAccount {
  status: string
}

interface OrderPage {
  totalElements: number
}

const metrics: Array<{ key: MetricKey; title: string; to: string; action: string }> = [
  { key: 'activeUsers', title: '启用用户', to: '/dashboard/users', action: '查看用户' },
  { key: 'activeAccounts', title: '有效券商账户', to: '/dashboard/broker-accounts', action: '查看账户' },
  { key: 'positions', title: '当前持仓条目', to: '/dashboard/positions', action: '查看持仓' },
  { key: 'pendingOrders', title: '待处理订单', to: '/dashboard/orders', action: '查看订单' }
]

const metricState = reactive<Record<MetricKey, MetricState>>({
  activeUsers: { value: null, loading: false, error: '' },
  activeAccounts: { value: null, loading: false, error: '' },
  positions: { value: null, loading: false, error: '' },
  pendingOrders: { value: null, loading: false, error: '' }
})

const refreshing = ref(false)
const lastRefreshedAt = ref('')
const requestIds: Record<MetricKey, number> = {
  activeUsers: 0,
  activeAccounts: 0,
  positions: 0,
  pendingOrders: 0
}

const metricLoaders: Record<MetricKey, () => Promise<number>> = {
  activeUsers: async () => {
    const users = await request.get<AdminUser[]>('/users')
    if (!Array.isArray(users) || users.some(user => !user || !Number.isInteger(user.deleteFlag))) {
      throw new Error('服务器返回的用户列表格式无效')
    }
    return users.filter(user => user.deleteFlag === 0).length
  },
  activeAccounts: async () => {
    const accounts = await request.get<BrokerAccount[]>('/admin/broker-accounts')
    if (!Array.isArray(accounts) || accounts.some(account => !account || typeof account.status !== 'string')) {
      throw new Error('服务器返回的券商账户列表格式无效')
    }
    return accounts.filter(account => account.status === 'ACTIVE').length
  },
  positions: async () => {
    const grouped = await request.get<Record<string, unknown>>('/positions')
    if (!grouped || typeof grouped !== 'object' || Array.isArray(grouped)) {
      throw new Error('服务器返回的持仓数据格式无效')
    }
    return Object.values(grouped).reduce<number>((total, rows) => {
      if (!Array.isArray(rows)) throw new Error('服务器返回的持仓分组格式无效')
      return total + rows.length
    }, 0)
  },
  pendingOrders: async () => {
    const page = await request.get<OrderPage>('/admin/orders', {
      params: { page: 1, pageSize: 1, status: 'PENDING' }
    })
    if (!page || !Number.isSafeInteger(page.totalElements) || page.totalElements < 0) {
      throw new Error('服务器返回的订单统计格式无效')
    }
    return page.totalElements
  }
}

const errorMessage = (error: unknown) => {
  if (error instanceof Error && error.message) return error.message
  if (typeof error === 'object' && error !== null && 'message' in error) {
    const message = error.message
    if (typeof message === 'string' && message) return message
  }
  return '请检查管理 API 服务后重试'
}

const loadMetric = async (key: MetricKey, loader: () => Promise<number>) => {
  const requestId = ++requestIds[key]
  const state = metricState[key]
  state.value = null
  state.error = ''
  state.loading = true
  try {
    const value = await loader()
    if (requestId === requestIds[key]) state.value = value
  } catch (error: unknown) {
    if (requestId === requestIds[key]) state.error = errorMessage(error)
  } finally {
    if (requestId === requestIds[key]) state.loading = false
  }
}

const refreshOverview = async () => {
  refreshing.value = true
  try {
    await Promise.all(metrics.map(item => loadMetric(item.key, metricLoaders[item.key])))
    lastRefreshedAt.value = new Intl.DateTimeFormat(undefined, {
      dateStyle: 'medium',
      timeStyle: 'medium'
    }).format(new Date())
  } finally {
    refreshing.value = false
  }
}

onMounted(refreshOverview)
</script>

<style scoped>
.admin-overview {
  display: grid;
  gap: 20px;
}

.overview-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.overview-heading h2 {
  margin: 0;
  color: #1f2937;
  font-size: 22px;
}

.overview-heading p {
  margin: 8px 0 0;
  color: #64748b;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.metric-card {
  min-width: 0;
}

.metric-title {
  color: #475569;
  font-size: 14px;
}

.metric-value {
  min-height: 48px;
  margin: 12px 0;
  color: #0f172a;
  font-size: 32px;
  font-weight: 650;
  line-height: 1.5;
}

.metric-error {
  margin-bottom: 12px;
}

.metric-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 24px;
}

.metric-footer a {
  color: #2563eb;
  text-decoration: none;
}

.metric-footer a:hover {
  text-decoration: underline;
}

.overview-note {
  max-width: 100%;
}

@media (max-width: 1000px) {
  .metrics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 600px) {
  .overview-heading {
    align-items: stretch;
    flex-direction: column;
  }

  .metrics-grid {
    grid-template-columns: 1fr;
  }
}
</style>
