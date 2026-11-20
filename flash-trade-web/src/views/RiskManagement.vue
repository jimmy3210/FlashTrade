<template>
  <div class="risk-management-page">
    <el-card v-loading="accountsLoading">
      <template #header>
        <div class="card-header">
          <h2>账户风控</h2>
          <el-button :loading="accountsLoading" @click="loadAccounts">刷新</el-button>
        </div>
      </template>

      <div v-if="accountsError" class="accounts-error">
        <el-alert
          title="账户列表加载失败"
          :description="accountsError"
          type="error"
          show-icon
          :closable="false"
        />
        <el-button type="primary" plain @click="loadAccounts">重试</el-button>
      </div>

      <el-form v-else label-position="top" class="risk-form">
        <el-form-item label="券商账户">
          <el-select
            v-model="selectedAccountId"
            filterable
            placeholder="选择账户"
            :loading="accountsLoading"
            :disabled="accountsLoading || accounts.length === 0"
            @change="loadStatus"
          >
            <el-option
              v-for="account in accounts"
              :key="account.id"
              :label="accountLabel(account)"
              :value="account.accountNo"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <template v-if="selectedAccount && !accountsError">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户">{{ selectedAccount.username || '—' }}</el-descriptions-item>
          <el-descriptions-item label="券商">{{ selectedAccount.brokerName || selectedAccount.brokerCode || '—' }}</el-descriptions-item>
          <el-descriptions-item label="账户">{{ maskAccount(selectedAccount.accountNo) }}</el-descriptions-item>
          <el-descriptions-item label="API 环境">
            <el-tag :type="selectedAccount.apiEnvironment === 'SANDBOX' ? 'success' : 'warning'">
              {{ selectedAccount.apiEnvironment || '未知' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="风控状态">
            <span v-if="statusLoading">读取中…</span>
            <el-tag v-else-if="riskStatus" :type="statusTagType(riskStatus)">
              {{ statusLabel(riskStatus) }}
            </el-tag>
            <span v-else>—</span>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="statusError" class="status-error">
          <el-alert
            title="风控状态读取失败"
            :description="statusError"
            type="error"
            show-icon
            :closable="false"
          />
          <el-button type="primary" plain @click="loadStatus">重试</el-button>
        </div>

        <el-alert
          class="environment-warning"
          title="风控状态变更暂不可用：服务端尚未实现管理员账户范围授权。"
          description="仅限制前端账户选择或 SANDBOX 环境不能代替服务端授权；当前仅提供状态查询。"
          type="warning"
          show-icon
          :closable="false"
        />

        <div class="actions">
          <el-button
            type="warning"
            disabled
          >
            限制交易
          </el-button>
          <el-button
            type="danger"
            disabled
          >
            关停账户
          </el-button>
          <el-button
            type="success"
            disabled
          >
            恢复正常
          </el-button>
        </div>

        <el-alert
          class="liquidation-notice"
          title="强制清仓暂不可用：服务端当前拒绝执行。"
          description="需先补齐账户范围授权、环境保护、执行审计和券商侧结果核验，再开放此操作。"
          type="info"
          show-icon
          :closable="false"
        />
      </template>

      <el-empty
        v-else-if="!accountsLoading && !accountsError && accounts.length === 0"
        description="没有可管理的券商账户"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import request from '@/utils/request'

interface BrokerAccount {
  id: number
  userId: number
  username: string | null
  brokerName: string | null
  brokerCode: string | null
  accountNo: string
  status: string
  apiEnvironment: string
}

interface RiskStatusResponse {
  accountId: string
  riskStatus: string
}

const accounts = ref<BrokerAccount[]>([])
const selectedAccountId = ref('')
const riskStatus = ref('')
const accountsLoading = ref(false)
const statusLoading = ref(false)
const accountsError = ref('')
const statusError = ref('')
let statusRequestId = 0
let accountsRequestId = 0

const selectedAccount = computed(
  () => accounts.value.find(account => account.accountNo === selectedAccountId.value) ?? null
)

const errorMessage = (error: unknown, fallback: string) => {
  if (error instanceof Error && error.message) return error.message
  if (typeof error === 'object' && error !== null && 'message' in error) {
    const message = error.message
    if (typeof message === 'string' && message) return message
  }
  return fallback
}

const accountLabel = (account: BrokerAccount) =>
  `${account.username || `用户 ${account.userId}`} · ${account.brokerName || account.brokerCode || '券商'} · ${maskAccount(account.accountNo)} · ${account.apiEnvironment || '未知环境'}`

const maskAccount = (accountNo: string) =>
  accountNo.length <= 4 ? '••••' : `${'•'.repeat(Math.min(accountNo.length - 4, 8))}${accountNo.slice(-4)}`

const statusLabel = (status: string) => ({
  NORMAL: '正常',
  DISABLED: '已关停',
  LIMITED: '限制交易',
  LIQUIDATED: '清仓结果未核实'
}[status] ?? status)

const statusTagType = (status: string) => ({
  NORMAL: 'success',
  DISABLED: 'danger',
  LIMITED: 'warning',
  LIQUIDATED: 'warning'
}[status] ?? 'info')

const loadAccounts = async () => {
  const requestId = ++accountsRequestId
  accountsLoading.value = true
  accountsError.value = ''
  try {
    const result = await request.get<BrokerAccount[]>('/admin/broker-accounts')
    if (!Array.isArray(result)) throw new Error('服务器返回的账户列表格式无效')
    if (result.some(account => !account
        || !Number.isSafeInteger(account.id)
        || !Number.isSafeInteger(account.userId)
        || typeof account.accountNo !== 'string'
        || !account.accountNo
        || typeof account.status !== 'string'
        || typeof account.apiEnvironment !== 'string')) {
      throw new Error('服务器返回的账户记录格式无效')
    }

    if (requestId === accountsRequestId) {
      accounts.value = result.filter(account => account.status === 'ACTIVE')
      if (!accounts.value.some(account => account.accountNo === selectedAccountId.value)) {
        selectedAccountId.value = accounts.value[0]?.accountNo ?? ''
        riskStatus.value = ''
        if (selectedAccountId.value) await loadStatus()
      }
    }
  } catch (error: unknown) {
    if (requestId === accountsRequestId) {
      accountsError.value = errorMessage(error, '请检查管理端 API 服务后重试')
    }
  } finally {
    if (requestId === accountsRequestId) accountsLoading.value = false
  }
}

const loadStatus = async () => {
  const accountId = selectedAccountId.value
  const requestId = ++statusRequestId
  riskStatus.value = ''
  statusError.value = ''
  if (!accountId) {
    statusLoading.value = false
    return
  }

  statusLoading.value = true
  try {
    const result = await request.get<RiskStatusResponse>('/admin/risk/status', {
      params: { accountId }
    })
    if (!result || result.accountId !== accountId || typeof result.riskStatus !== 'string') {
      throw new Error('服务器返回的风控状态格式无效')
    }
    if (requestId === statusRequestId) riskStatus.value = result.riskStatus
  } catch (error: unknown) {
    if (requestId === statusRequestId) {
      statusError.value = errorMessage(error, '请检查管理端 API 服务后重试')
    }
  } finally {
    if (requestId === statusRequestId) statusLoading.value = false
  }
}

onMounted(loadAccounts)
</script>

<style scoped>
.risk-management-page {
  min-width: 0;
}

.card-header,
.actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
}

.risk-form {
  max-width: 480px;
  margin: 20px 0;
}

.risk-form :deep(.el-select) {
  width: 100%;
}

.actions {
  justify-content: flex-start;
  flex-wrap: wrap;
  margin: 20px 0;
}

.accounts-error,
.status-error,
.environment-warning,
.liquidation-notice {
  margin-top: 16px;
}

.accounts-error,
.status-error {
  display: flex;
  align-items: center;
  gap: 12px;
}

.accounts-error :deep(.el-alert),
.status-error :deep(.el-alert) {
  flex: 1;
}

.liquidation-notice {
  margin-top: 24px;
}

@media (max-width: 640px) {
  .card-header {
    align-items: flex-start;
  }

  .risk-management-page :deep(.el-descriptions__body .el-descriptions__table) {
    table-layout: auto;
  }

  .risk-management-page :deep(.el-descriptions__cell) {
    overflow-wrap: anywhere;
  }
}
</style>
