<template>
  <div class="broker-account-list-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>券商账户管理</h2>
          <div class="actions">
            <el-button :loading="loading" @click="loadAccounts">刷新</el-button>
            <el-button type="primary" @click="goCreate">新建券商账户</el-button>
          </div>
        </div>
      </template>

      <el-alert
        class="environment-notice"
        title="当前仅使用本地或隔离 Mock。新建账户默认 SANDBOX；LIVE 账户不能通过此列表触发交易。"
        type="info"
        show-icon
        :closable="false"
      />

      <el-form :inline="true" class="filters" @submit.prevent>
        <el-form-item label="搜索">
          <el-input
            v-model="keyword"
            clearable
            placeholder="用户名、券商或账户 ID"
            class="search-input"
          />
        </el-form-item>
        <el-form-item label="API 环境">
          <el-select v-model="environmentFilter" class="filter-select">
            <el-option label="全部环境" value="all" />
            <el-option label="SANDBOX" value="SANDBOX" />
            <el-option label="LIVE" value="LIVE" />
          </el-select>
        </el-form-item>
        <el-form-item label="账户状态">
          <el-select v-model="statusFilter" class="filter-select">
            <el-option label="全部状态" value="all" />
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="暂停" value="SUSPENDED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="绑定状态">
          <el-select v-model="bindingFilter" class="filter-select">
            <el-option label="全部绑定状态" value="all" />
            <el-option label="已绑定" value="bound" />
            <el-option label="未绑定" value="unbound" />
          </el-select>
        </el-form-item>
      </el-form>

      <div v-if="loadError" class="load-error">
        <el-alert
          title="券商账户加载失败"
          :description="loadError"
          type="error"
          show-icon
          :closable="false"
        />
        <el-button type="primary" plain @click="loadAccounts">重试</el-button>
      </div>

      <el-table
        v-else
        v-loading="loading"
        :data="pageAccounts"
        row-key="id"
        stripe
        :empty-text="emptyText"
      >
        <el-table-column prop="id" label="账户 ID" width="95" />
        <el-table-column prop="username" label="用户名" min-width="130" show-overflow-tooltip />
        <el-table-column prop="brokerName" label="券商" min-width="130" />
        <el-table-column label="账户号" min-width="140">
          <template #default="{ row }: { row: BrokerAccount }">
            {{ maskAccount(row.accountNo) }}
          </template>
        </el-table-column>
        <el-table-column prop="accountType" label="类型" width="100" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }: { row: BrokerAccount }">
            <el-tag :type="accountStatusType(row.status)">{{ accountStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="API 环境" width="120">
          <template #default="{ row }: { row: BrokerAccount }">
            <el-tag :type="row.apiEnvironment === 'SANDBOX' ? 'success' : 'warning'">
              {{ row.apiEnvironment || '未知' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="currentBindingUsername" label="当前绑定用户" min-width="140">
          <template #default="{ row }: { row: BrokerAccount }">
            {{ row.currentBindingUsername || '未绑定' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="210" fixed="right">
          <template #default="{ row }: { row: BrokerAccount }">
            <el-button link type="primary" @click="goView(row)">查看</el-button>
            <el-button link type="primary" @click="goEdit(row)">编辑</el-button>
            <el-button
              link
              type="danger"
              :disabled="row.status === 'CLOSED' || isUpdating(row.id)"
              :loading="isUpdating(row.id)"
              @click="deactivate(row)"
            >
              停用
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loadError" class="pagination">
        <span class="result-count">显示 {{ filteredAccounts.length }} / {{ accounts.length }} 个账户</span>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="filteredAccounts.length"
          layout="sizes, prev, pager, next"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

interface BrokerAccount {
  id: number
  userId: number
  username: string | null
  brokerCode: string | null
  brokerName: string | null
  accountNo: string
  accountType: string
  status: string
  apiEnvironment: string
  currentBindingUsername: string | null
}

const router = useRouter()
const accounts = ref<BrokerAccount[]>([])
const loading = ref(false)
const loadError = ref('')
const updatingIds = ref(new Set<number>())
const keyword = ref('')
const environmentFilter = ref('all')
const statusFilter = ref('all')
const bindingFilter = ref('all')
const currentPage = ref(1)
const pageSize = ref(10)

const filteredAccounts = computed(() => {
  const search = keyword.value.trim().toLocaleLowerCase()
  return accounts.value.filter(account => {
    const matchesSearch = !search
      || String(account.id).includes(search)
      || (account.username ?? '').toLocaleLowerCase().includes(search)
      || (account.brokerName ?? '').toLocaleLowerCase().includes(search)
      || (account.brokerCode ?? '').toLocaleLowerCase().includes(search)
    const matchesEnvironment = environmentFilter.value === 'all'
      || account.apiEnvironment === environmentFilter.value
    const matchesStatus = statusFilter.value === 'all'
      || account.status === statusFilter.value
    const matchesBinding = bindingFilter.value === 'all'
      || (bindingFilter.value === 'bound'
        ? Boolean(account.currentBindingUsername)
        : !account.currentBindingUsername)
    return matchesSearch && matchesEnvironment && matchesStatus && matchesBinding
  })
})

const pageAccounts = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredAccounts.value.slice(start, start + pageSize.value)
})

const emptyText = computed(() => accounts.value.length === 0
  ? '暂无券商账户'
  : '没有符合筛选条件的券商账户')

watch([keyword, environmentFilter, statusFilter, bindingFilter, pageSize], () => {
  currentPage.value = 1
})

watch(filteredAccounts, () => {
  const lastPage = Math.max(1, Math.ceil(filteredAccounts.value.length / pageSize.value))
  if (currentPage.value > lastPage) currentPage.value = lastPage
})

const errorMessage = (error: unknown, fallback: string) => {
  if (error instanceof Error && error.message) return error.message
  if (typeof error === 'object' && error !== null && 'message' in error) {
    const message = error.message
    if (typeof message === 'string' && message) return message
  }
  return fallback
}

const maskAccount = (accountNo: string) =>
  accountNo.length <= 4 ? '••••' : `${'•'.repeat(Math.min(accountNo.length - 4, 8))}${accountNo.slice(-4)}`

const accountStatusType = (status: string) => ({
  ACTIVE: 'success',
  SUSPENDED: 'warning',
  CLOSED: 'info'
}[status] ?? 'info')

const accountStatusLabel = (status: string) => ({
  ACTIVE: '活跃',
  SUSPENDED: '暂停',
  CLOSED: '已关闭'
}[status] ?? status)

const isUpdating = (id: number) => updatingIds.value.has(id)

const setUpdating = (id: number, updating: boolean) => {
  const ids = new Set(updatingIds.value)
  if (updating) ids.add(id)
  else ids.delete(id)
  updatingIds.value = ids
}

const loadAccounts = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const result = await request.get<BrokerAccount[]>('/admin/broker-accounts')
    if (!Array.isArray(result)) throw new Error('服务器返回的账户列表格式无效')
    accounts.value = result
  } catch (error: unknown) {
    loadError.value = errorMessage(error, '请检查管理端 API 服务后重试')
  } finally {
    loading.value = false
  }
}

const goCreate = () => router.push({ name: 'BrokerAccountCreate' })
const goEdit = (account: BrokerAccount) => router.push({ name: 'BrokerAccountEdit', params: { id: account.id } })
const goView = (account: BrokerAccount) => router.push({ name: 'BrokerAccountView', params: { id: account.id } })

const deactivate = async (account: BrokerAccount) => {
  try {
    await ElMessageBox.confirm(
      `确认停用用户“${account.username || account.userId}”的 ${account.brokerName || ''} 账户 ${maskAccount(account.accountNo)}？`,
      '停用券商账户',
      {
        type: 'warning',
        confirmButtonText: '确认停用',
        cancelButtonText: '取消'
      }
    )
  } catch {
    return
  }

  setUpdating(account.id, true)
  try {
    await request.delete<void>(`/admin/broker-accounts/${account.id}`)
    ElMessage.success('券商账户已停用')
    await loadAccounts()
  } catch (error: unknown) {
    await loadAccounts()
    ElMessage.warning(`停用请求结果未确认，请核对账户状态后再执行后续操作：${errorMessage(error, '请刷新列表确认')}`)
  } finally {
    setUpdating(account.id, false)
  }
}

onMounted(loadAccounts)
</script>

<style scoped>
.broker-account-list-page {
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

.environment-notice {
  margin-bottom: 16px;
}

.filters {
  margin: 20px 0 4px;
}

.search-input {
  width: 240px;
}

.filter-select {
  width: 160px;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-top: 20px;
}

.result-count {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.load-error {
  display: flex;
  align-items: center;
  gap: 12px;
}

.load-error :deep(.el-alert) {
  flex: 1;
}

@media (max-width: 640px) {
  .card-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .search-input,
  .filter-select {
    width: 100%;
  }

  .pagination {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
