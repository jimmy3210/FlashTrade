<template>
  <div class="page">
    <div class="toolbar">
      <h2>用户-账户绑定管理</h2>
      <el-button :loading="pageLoading" @click="loadPage">刷新</el-button>
    </div>

    <el-alert
      title="管理端仅允许切换到 SANDBOX 账户。LIVE 账户需通过受控流程管理；前端限制不能替代后端授权与环境保护。"
      type="warning"
      show-icon
      :closable="false"
      class="notice"
    />

    <el-alert
      v-if="pageError"
      title="绑定管理数据加载失败"
      :description="pageError"
      type="error"
      show-icon
      :closable="false"
      class="notice"
    />

    <div class="section">
      <label for="user-select">选择用户：</label>
      <el-select
        id="user-select"
        v-model="selectedUserId"
        placeholder="请选择用户"
        filterable
        clearable
        :loading="pageLoading"
        :disabled="pageLoading || bindingId !== null || !!pageError"
        @change="loadUserBinding"
      >
        <el-option
          v-for="user in users"
          :key="user.userId"
          :label="`${user.username} (ID: ${user.userId})`"
          :value="user.userId"
        />
      </el-select>
    </div>

    <template v-if="selectedUserId && !pageError">
      <el-skeleton v-if="bindingLoading" :rows="3" animated class="section" />
      <el-alert
        v-else-if="bindingError"
        title="当前绑定状态加载失败"
        :description="bindingError"
        type="error"
        show-icon
        :closable="false"
        class="notice"
      >
        <el-button type="primary" plain @click="loadUserBinding">重试</el-button>
      </el-alert>

      <div v-else-if="currentBinding" class="section">
        <h3>当前绑定的账户</h3>
        <div class="binding-info">
          <p><strong>用户：</strong>{{ currentBinding.username || selectedUserName }}</p>
          <p><strong>券商：</strong>{{ currentBinding.brokerName }} ({{ currentBinding.brokerCode }})</p>
          <p><strong>账户号：</strong>{{ maskAccount(currentBinding.accountNo) }}</p>
          <p><strong>账户类型：</strong>{{ currentBinding.accountType }}</p>
          <p><strong>API 环境：</strong>
            <el-tag :type="currentBinding.apiEnvironment === 'SANDBOX' ? 'success' : 'danger'">
              {{ currentBinding.apiEnvironment }}
            </el-tag>
          </p>
          <p><strong>状态：</strong>{{ currentBinding.status }}</p>
        </div>
      </div>

      <div v-if="!bindingLoading && !bindingError" class="section">
        <h3>切换为其他 SANDBOX 账户</h3>
        <el-table
          v-if="availableAccounts.length > 0"
          v-loading="bindingId !== null"
          :data="availableAccounts"
          row-key="id"
          stripe
        >
          <el-table-column label="操作" width="100">
            <template #default="{ row }: { row: BrokerAccount }">
              <el-button
                v-if="row.id !== currentBinding?.id"
                type="primary"
                link
                :loading="bindingId === row.id"
                :disabled="bindingId !== null"
                @click="bindAccount(row)"
              >
                切换
              </el-button>
              <el-tag v-else type="success">当前</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="brokerName" label="券商" min-width="140" />
          <el-table-column label="账户号" min-width="140">
            <template #default="{ row }: { row: BrokerAccount }">
              {{ maskAccount(row.accountNo) }}
            </template>
          </el-table-column>
          <el-table-column prop="accountType" label="类型" width="110" />
          <el-table-column prop="status" label="状态" width="110" />
          <el-table-column prop="apiEnvironment" label="API 环境" width="120">
            <template #default="{ row }: { row: BrokerAccount }">
              <el-tag type="success">{{ row.apiEnvironment }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="没有可绑定的 SANDBOX 账户" />
      </div>

      <el-alert
        v-if="bindingState === 'unbound' && !bindingLoading && !bindingError"
        title="该用户尚未绑定账户"
        description="选择一个可用的 SANDBOX 账户进行绑定。"
        type="info"
        show-icon
        :closable="false"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

interface UserOption {
  userId: number
  username: string
}

interface BrokerAccount {
  id: number
  userId: number
  username: string | null
  brokerName: string | null
  brokerCode: string | null
  accountNo: string
  accountType: string
  status: string
  apiEnvironment: string
  currentBindingUsername: string | null
}

type BindingState = 'idle' | 'loading' | 'bound' | 'unbound' | 'error'

const selectedUserId = ref<number | null>(null)
const users = ref<UserOption[]>([])
const allAccounts = ref<BrokerAccount[]>([])
const currentBinding = ref<BrokerAccount | null>(null)
const pageLoading = ref(false)
const pageError = ref('')
const bindingLoading = ref(false)
const bindingError = ref('')
const bindingState = ref<BindingState>('idle')
const bindingId = ref<number | null>(null)
let bindingRequestId = 0

const selectedUserName = computed(
  () => users.value.find(user => user.userId === selectedUserId.value)?.username || ''
)
const availableAccounts = computed(() =>
  allAccounts.value.filter(account =>
    account.status === 'ACTIVE'
    && account.apiEnvironment === 'SANDBOX'
    && (!account.currentBindingUsername || account.currentBindingUsername === selectedUserName.value)
  )
)

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

const isNotFound = (error: unknown) => {
  if (typeof error !== 'object' || error === null || !('error' in error)) return false
  const body = error.error
  return typeof body === 'object'
    && body !== null
    && 'code' in body
    && (body.code === 'RESOURCE_NOT_FOUND' || body.code === 'ACCOUNT_NOT_FOUND')
}

const maskAccount = (accountNo: string) =>
  accountNo.length <= 4 ? '••••' : `${'•'.repeat(Math.min(accountNo.length - 4, 8))}${accountNo.slice(-4)}`

const loadPage = async () => {
  pageLoading.value = true
  pageError.value = ''
  try {
    const [userResult, accountResult] = await Promise.all([
      request.get<UserOption[]>('/users'),
      request.get<BrokerAccount[]>('/admin/broker-accounts')
    ])
    if (!Array.isArray(userResult) || !Array.isArray(accountResult)) {
      throw new Error('服务器返回的数据格式无效')
    }
    users.value = userResult
    allAccounts.value = accountResult
    if (selectedUserId.value !== null) await loadUserBinding()
  } catch (error) {
    pageError.value = errorMessage(error)
  } finally {
    pageLoading.value = false
  }
}

const loadUserBinding = async () => {
  const requestId = ++bindingRequestId
  if (selectedUserId.value === null) {
    currentBinding.value = null
    bindingError.value = ''
    bindingState.value = 'idle'
    bindingLoading.value = false
    return
  }

  bindingLoading.value = true
  bindingError.value = ''
  bindingState.value = 'loading'
  try {
    const userId = selectedUserId.value
    const result = await request.get<BrokerAccount>(
      `/admin/broker-accounts/user/${userId}/current`
    )
    if (requestId !== bindingRequestId || selectedUserId.value !== userId) return
    currentBinding.value = result
    bindingState.value = 'bound'
  } catch (error) {
    if (requestId !== bindingRequestId) return
    currentBinding.value = null
    if (isNotFound(error)) {
      bindingState.value = 'unbound'
    } else {
      bindingState.value = 'error'
      bindingError.value = errorMessage(error)
    }
  } finally {
    if (requestId === bindingRequestId) bindingLoading.value = false
  }
}

const bindAccount = async (account: BrokerAccount) => {
  if (selectedUserId.value === null || bindingId.value !== null) return
  const userId = selectedUserId.value
  try {
    await ElMessageBox.confirm(
      `确认将用户“${selectedUserName.value}”绑定到账户 ${maskAccount(account.accountNo)}？`,
      '切换 SANDBOX 账户',
      {
        type: 'warning',
        confirmButtonText: '确认切换',
        cancelButtonText: '取消'
      }
    )
  } catch {
    return
  }

  bindingId.value = account.id
  try {
    await request.post<void>(`/admin/broker-accounts/user/${userId}/bind/${account.id}`)
    await loadUserBinding()
    ElMessage.success('账户绑定已更新')
  } catch (error) {
    await loadUserBinding()
    ElMessage.error(`绑定请求未确认，请核对当前绑定状态：${errorMessage(error)}`)
  } finally {
    bindingId.value = null
  }
}

onMounted(loadPage)
</script>

<style scoped>
.page {
  max-width: 1000px;
  margin: 0 auto;
  padding: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.toolbar h2 {
  margin: 0;
  font-size: 22px;
}

.section {
  margin-bottom: 16px;
  padding: 16px;
  border: 1px solid #e6e6e6;
  border-radius: 6px;
  background: #fff;
}

.section h3 {
  margin: 0 0 12px;
  font-size: 16px;
}

.notice {
  margin-bottom: 16px;
}

.binding-info {
  padding: 12px;
  border-radius: 4px;
  background: #f7f8fb;
  line-height: 1.8;
}

.binding-info p {
  margin: 6px 0;
  font-size: 14px;
}
</style>
