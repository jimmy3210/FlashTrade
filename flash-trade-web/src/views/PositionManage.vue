<template>
  <div class="positions-container">
    <div class="toolbar">
      <h2>持仓管理</h2>
      <button type="button" :disabled="loading" @click="fetchPositions">
        {{ loading ? '加载中...' : '刷新' }}
      </button>
    </div>
    <div v-if="loading" class="loading">加载中...</div>
    <div v-else-if="loadError" class="error-state" role="alert">
      <span>{{ loadError }}</span>
      <button type="button" @click="fetchPositions">重试</button>
    </div>

    <div v-else-if="Object.keys(groupedPositions).length === 0">
      <div class="empty-state">暂无仓位数据</div>
    </div>

    <div v-else>
      <div
        v-for="(positions, userId) in groupedPositions"
        :key="userId"
        class="user-block"
      >
        <div class="user-header">
          <h3 class="user-title">
            <button
              type="button"
              class="collapse-button"
              :aria-expanded="!collapsedUsers[userId]"
              :aria-label="`${collapsedUsers[userId] ? '展开' : '收起'}用户 ${userId} 的持仓`"
              @click="toggleCollapse(userId)"
            >
              <span
                class="collapse-icon"
                :class="{ collapsed: collapsedUsers[userId] }"
                aria-hidden="true"
              >
                ▶
              </span>
              用户 {{ userId }}
            </button>
          </h3>

          <div class="user-actions">
            <span class="summary" :class="profitClass(userSummary[userId]?.unrealized || 0)">
              浮盈：{{ formatNumber(userSummary[userId]?.unrealized || 0) }}
            </span>
            <span class="summary" :class="profitClass(userSummary[userId]?.marketValue || 0)">
              市值：{{ formatNumber(userSummary[userId]?.marketValue || 0) }}
            </span>
          </div>
        </div>

        <div class="table-wrapper" v-show="!collapsedUsers[userId]">
          <table class="position-table" aria-label="用户持仓明细">
            <thead>
              <tr>
                <th>券商</th>
                <th>账户</th>
                <th>股票</th>
                <th>方向</th>
                <th>数量</th>
                <th>均价</th>
                <th>现价</th>
                <th>市值</th>
                <th>浮动盈亏</th>
                <th>更新时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="positions.length === 0">
                <td colspan="10" class="empty-row">—</td>
              </tr>
              <tr v-for="pos in positions" :key="userId + '-' + pos.accountId + '-' + pos.symbol">
                <td>{{ pos.brokerName }}</td>
                <td>{{ maskAccount(pos.accountId) }}</td>
                <td>{{ pos.symbol }}</td>
                <td>{{ pos.direction }}</td>
                <td>{{ pos.quantity }}</td>
                <td>{{ pos.averageCost.toFixed(2) }}</td>
                <td>{{ pos.currentPrice.toFixed(2) }}</td>
                <td>{{ pos.marketValue.toFixed(2) }}</td>
                <td :class="profitClass(pos.unrealizedPnl)">{{ pos.unrealizedPnl.toFixed(2) }}</td>
                <td>{{ pos.updatedAt }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '@/utils/request'

interface Position {
  accountId: string
  brokerName: string
  symbol: string
  quantity: number
  direction: string
  averageCost: number
  currentPrice: number
  marketValue: number
  unrealizedPnl: number
  updatedAt: string
}

interface PositionApiRecord {
  account_id?: string | number | null
  broker_name?: string | null
  symbol?: string | null
  quantity?: number | string | null
  average_cost?: number | string | null
  current_price?: number | string | null
  market_value?: number | string | null
  unrealized_pnl?: number | string | null
  updated_at?: string | null
}

const groupedPositions = ref<Record<string, Position[]>>({})
const collapsedUsers = reactive<Record<string, boolean>>({})
const loading = ref(true)
const loadError = ref('')

const userSummary = reactive<Record<string, { unrealized: number; marketValue: number }>>({})
let activeRequestId = 0

const profitClass = (value: number) => (value > 0 ? 'profit' : value < 0 ? 'loss' : '')
const formatNumber = (value: number) => Number.isFinite(value) ? value.toFixed(2) : '—'
const maskAccount = (accountId: string) =>
  accountId.length <= 4 ? '••••' : `${'•'.repeat(Math.min(accountId.length - 4, 8))}${accountId.slice(-4)}`
const numericValue = (value: number | string | null | undefined) => {
  const parsed = Number(value ?? 0)
  return Number.isFinite(parsed) ? parsed : 0
}

const errorMessage = (error: unknown) => {
  if (typeof error === 'object' && error !== null && 'error' in error) {
    const body = error.error
    if (typeof body === 'object' && body !== null && 'message' in body && typeof body.message === 'string') {
      return body.message
    }
  }
  if (error instanceof Error && error.message) return error.message
  return String(error)
}

const buildSummary = (userId: string, list: Position[]) => {
  userSummary[userId] = list.reduce(
    (acc, position) => {
      acc.unrealized += position.unrealizedPnl
      acc.marketValue += position.marketValue
      return acc
    },
    { unrealized: 0, marketValue: 0 }
  )
}

const normalizePosition = (row: PositionApiRecord): Position => {
  const quantity = numericValue(row.quantity)
  return {
    accountId: String(row.account_id ?? ''),
    brokerName: String(row.broker_name ?? ''),
    symbol: String(row.symbol ?? ''),
    quantity,
    direction: quantity >= 0 ? '多头' : '空头',
    averageCost: numericValue(row.average_cost),
    currentPrice: numericValue(row.current_price),
    marketValue: numericValue(row.market_value),
    unrealizedPnl: numericValue(row.unrealized_pnl),
    updatedAt: row.updated_at ? String(row.updated_at) : ''
  }
}

const fetchPositions = async () => {
  const requestId = ++activeRequestId
  loading.value = true
  loadError.value = ''
  try {
    const res = await request.get<Record<string, PositionApiRecord[]>>('/positions')
    if (!res || typeof res !== 'object' || Array.isArray(res)) {
      throw new Error('服务器返回的持仓数据格式无效')
    }
    const nextGrouped: Record<string, Position[]> = {}
    Object.entries(res).forEach(([userId, rows]) => {
      if (!Array.isArray(rows)) throw new Error(`用户 ${userId} 的持仓列表格式无效`)
      nextGrouped[userId] = rows.map(normalizePosition)
      if (!(userId in collapsedUsers)) collapsedUsers[userId] = false
    })
    if (requestId === activeRequestId) {
      Object.keys(userSummary).forEach(userId => {
        if (!(userId in nextGrouped)) delete userSummary[userId]
      })
      Object.entries(nextGrouped).forEach(([userId, positions]) => buildSummary(userId, positions))
      groupedPositions.value = nextGrouped
    }
  } catch (error) {
    if (requestId === activeRequestId) loadError.value = `加载仓位失败：${errorMessage(error)}`
  } finally {
    if (requestId === activeRequestId) loading.value = false
  }
}

const toggleCollapse = (userId: string) => {
  collapsedUsers[userId] = !collapsedUsers[userId]
}

onMounted(fetchPositions)
</script>

<style scoped>
.positions-container {
  padding: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.toolbar h2 {
  margin: 0;
}

.toolbar button,
.error-state button {
  padding: 8px 14px;
  border: 1px solid #cbd5e1;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
}

.toolbar button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.error-state {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 14px;
  color: #b91c1c;
  background: #fef2f2;
  border: 1px solid #fecaca;
}

.user-block {
  margin-bottom: 20px;
  border: 1px solid #ddd;
  background: #fff;
}

.user-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 14px;
  background: #2c3e50;
  color: #fff;
}

.user-title {
  margin: 0;
}

.collapse-button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 0;
  border: 0;
  color: inherit;
  background: transparent;
  font: inherit;
  cursor: pointer;
}

.collapse-button:focus-visible {
  outline: 2px solid #42b883;
  outline-offset: 3px;
}

.collapse-icon {
  transition: transform 0.2s ease;
}

.collapse-icon:not(.collapsed) {
  transform: rotate(90deg);
}

.user-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.position-table {
  width: 100%;
  border-collapse: collapse;
}

.position-table th,
.position-table td {
  border: 1px solid #ddd;
  padding: 8px;
  text-align: center;
}

.position-table th {
  background: #f5f5f5;
}

.summary-row {
  font-weight: bold;
  background: #fafafa;
}

.table-wrapper {
  max-height: 320px;
  overflow: auto;
}

.empty-row {
  height: 60px;
  color: #bbb;
}

.profit {
  color: #e53935;
  font-weight: bold;
}

.loss {
  color: #43a047;
  font-weight: bold;
}

.loading {
  padding: 40px;
  text-align: center;
  color: #999;
}
</style>
