<template>
  <div class="form-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>旧数据公司 Scope 映射</h2>
          <el-button :loading="loading" @click="loadData">刷新</el-button>
        </div>
      </template>

      <el-alert
        title="映射前拒绝授权"
        description="未归属有效启用公司的旧用户及旧账户不会获得公司 Scope；映射需平台管理员确认。"
        type="warning"
        show-icon
        :closable="false"
      />

      <el-skeleton v-if="loading" :rows="8" animated />
      <el-alert v-else-if="loadError" :title="loadError" type="error" show-icon :closable="false" />
      <template v-else>
        <h3>未映射用户（{{ status.unmappedUsers.length }}）</h3>
        <el-table :data="status.unmappedUsers" border stripe empty-text="没有未映射用户">
          <el-table-column prop="userId" label="用户 ID" width="100" />
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="role" label="角色" width="180" />
          <el-table-column label="目标公司" min-width="220">
            <template #default="{ row }">
              <el-select v-model="userCompanies[row.userId]" placeholder="选择有效公司" filterable>
                <el-option
                  v-for="company in activeCompanies"
                  :key="company.id"
                  :label="company.name"
                  :value="company.id"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                :loading="mappingUserId === row.userId"
                :disabled="!userCompanies[row.userId]"
                @click="mapUser(row.userId)"
              >映射</el-button>
            </template>
          </el-table-column>
        </el-table>

        <h3>未映射券商账户（{{ status.unmappedAccounts.length }}）</h3>
        <el-table :data="status.unmappedAccounts" border stripe empty-text="没有未映射账户">
          <el-table-column prop="accountId" label="账户 ID" width="100" />
          <el-table-column prop="brokerCode" label="券商" width="140" />
          <el-table-column prop="accountNo" label="账户号" width="140" />
          <el-table-column prop="ownerUsername" label="当前所有者">
            <template #default="{ row }">{{ row.ownerUsername || '未设置' }}</template>
          </el-table-column>
          <el-table-column label="目标用户" min-width="220">
            <template #default="{ row }">
              <el-select v-model="accountUsers[row.accountId]" placeholder="选择已映射公司用户" filterable>
                <el-option
                  v-for="user in mappedUsers"
                  :key="user.userId"
                  :label="`${user.username} (公司: ${user.companyName || '未提供'})`"
                  :value="user.userId"
                />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                :loading="mappingAccountId === row.accountId"
                :disabled="!accountUsers[row.accountId]"
                @click="mapAccount(row.accountId)"
              >映射</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { loadAdminCompanies, type AdminCompanyOption } from '@/utils/adminCompanies'

const errorMessage = (error: unknown, fallback: string) => {
  if (typeof error === 'object' && error !== null) {
    if ('message' in error && typeof error.message === 'string' && error.message) {
      return error.message
    }
  }
  return fallback
}

interface UnmappedUser {
  userId: number
  username: string
  role: string
}

interface UnmappedAccount {
  accountId: number
  ownerUserId: number | null
  ownerUsername: string | null
  brokerCode: string | null
  accountNo: string | null
}

interface MappingStatus {
  unmappedUsers: UnmappedUser[]
  unmappedAccounts: UnmappedAccount[]
}

interface UserOption {
  userId: number
  username: string
  companyId: number | null
  companyName: string | null
}

const loading = ref(true)
const loadError = ref('')
const mappingUserId = ref<number | null>(null)
const mappingAccountId = ref<number | null>(null)
const status = ref<MappingStatus>({ unmappedUsers: [], unmappedAccounts: [] })
const activeCompanies = ref<AdminCompanyOption[]>([])
const mappedUsers = ref<UserOption[]>([])
const userCompanies = reactive<Record<number, number>>({})
const accountUsers = reactive<Record<number, number>>({})

const loadData = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const [mappingStatus, companies, users] = await Promise.all([
      request.get<MappingStatus>('/admin/company-mappings'),
      loadAdminCompanies(),
      request.get<UserOption[]>('/users')
    ])
    status.value = mappingStatus
    activeCompanies.value = companies.filter(company => company.isActive)
    mappedUsers.value = users.filter(user => user.companyId != null)
  } catch (error: unknown) {
    loadError.value = errorMessage(error, '加载 Scope 映射状态失败')
  } finally {
    loading.value = false
  }
}

const mapUser = async (userId: number) => {
  const companyId = userCompanies[userId]
  if (!companyId) return
  mappingUserId.value = userId
  try {
    await request.post(`/admin/company-mappings/users/${userId}`, { companyId })
    ElMessage.success('用户公司 Scope 映射成功')
    await loadData()
  } catch (error: unknown) {
    ElMessage.error(errorMessage(error, '用户公司 Scope 映射失败'))
  } finally {
    mappingUserId.value = null
  }
}

const mapAccount = async (accountId: number) => {
  const userId = accountUsers[accountId]
  if (!userId) return
  mappingAccountId.value = accountId
  try {
    await request.post(`/admin/company-mappings/accounts/${accountId}`, { userId })
    ElMessage.success('券商账户所有者映射成功')
    await loadData()
  } catch (error: unknown) {
    ElMessage.error(errorMessage(error, '券商账户所有者映射失败'))
  } finally {
    mappingAccountId.value = null
  }
}

onMounted(loadData)
</script>
