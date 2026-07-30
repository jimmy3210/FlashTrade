<template>
  <div class="positions-container">
    <div v-if="loading" class="loading">加载中...</div>

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
          <h3 class="user-title" @click="toggleCollapse(userId)">
            <span
              class="collapse-icon"
              :class="{ collapsed: collapsedUsers[userId] }"
            >
              ▶
            </span>
            用户 {{ userId }}
          </h3>

          <div class="user-actions">
            <span class="summary" :class="profitClass(userSummary[userId]?.unrealized || 0)">
              浮盈：{{ formatNumber(userSummary[userId]?.unrealized || 0) }}
            </span>
            <span class="summary" :class="profitClass(userSummary[userId]?.marketValue || 0)">
              市值：{{ formatNumber(userSummary[userId]?.marketValue || 0) }}
            </span>
            <button
              class="close-btn"
              :disabled="positions.length === 0 || closingUsers[userId]"
              @click="closeAllPositions(userId)"
            >
              {{ closingUsers[userId] ? '平仓中...' : '一键平仓' }}
            </button>
          </div>
        </div>

        <div class="table-wrapper" v-show="!collapsedUsers[userId]">
          <table class="position-table">
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
                <td>{{ pos.accountId }}</td>
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

const groupedPositions = ref<Record<string, Position[]>>({})
const collapsedUsers = reactive<Record<string, boolean>>({})
const closingUsers = reactive<Record<string, boolean>>({})
const loading = ref(true)

const userSummary = reactive<Record<string, { unrealized: number; marketValue: number }>>({})

const profitClass = (value: number) => (value > 0 ? 'profit' : value < 0 ? 'loss' : '')
const formatNumber = (value: number) => value.toFixed(2)

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

const normalizePosition = (row: any): Position => ({
  accountId: String(row.account_id || ''),
  brokerName: String(row.broker_name || ''),
  symbol: String(row.symbol || ''),
  quantity: Number(row.quantity || 0),
  direction: Number(row.quantity || 0) >= 0 ? '多头' : '空头',
  averageCost: Number(row.average_cost || 0),
  currentPrice: Number(row.current_price || 0),
  marketValue: Number(row.market_value || 0),
  unrealizedPnl: Number(row.unrealized_pnl || 0),
  updatedAt: row.updated_at ? String(row.updated_at) : '',
})

const fetchPositions = async () => {
  loading.value = true
  try {
    const res = await request.get('/positions')
    groupedPositions.value = {}

    Object.entries(res || {}).forEach(([userId, rows]) => {
      groupedPositions.value[userId] = (rows as any[]).map(normalizePosition)
      if (!(userId in collapsedUsers)) collapsedUsers[userId] = false
      if (!(userId in closingUsers)) closingUsers[userId] = false
      buildSummary(userId, groupedPositions.value[userId])
    })
  } finally {
    loading.value = false
  }
}

const toggleCollapse = (userId: string) => {
  collapsedUsers[userId] = !collapsedUsers[userId]
}

const closeAllPositions = async (userId: string) => {
  if (!confirm(`确认平仓 ${userId} 所有仓位？`)) return
  closingUsers[userId] = true
  try {
    await request.post('/positions/close_all', { userId })
    await fetchPositions()
  } finally {
    closingUsers[userId] = false
  }
}

onMounted(fetchPositions)
</script>

<style scoped>
.positions-container {
  padding: 16px;
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
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
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

.close-btn {
  background: #d32f2f;
  color: #fff;
  border: none;
  padding: 6px 14px;
  cursor: pointer;
}

/* 不可用但仍是红色 */
.close-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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
  overflow-y: auto;
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
