<template>
  <div class="user-view-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>用户详情</h2>
          <div class="actions">
            <el-button @click="back">返回列表</el-button>
            <el-button v-if="canManageUsers" type="primary" :disabled="!user" @click="edit">
              编辑用户
            </el-button>
          </div>
        </div>
      </template>

      <div v-if="loadError" class="load-error">
        <el-alert
          title="用户详情加载失败"
          :description="loadError"
          type="error"
          show-icon
          :closable="false"
        />
        <el-button type="primary" plain @click="loadUser">重试</el-button>
      </div>

      <el-alert
        v-if="permissionLoadError"
        class="permission-warning"
        title="当前按只读方式显示"
        :description="permissionLoadError"
        type="warning"
        show-icon
        :closable="false"
      />

      <el-descriptions v-if="user && !loadError" :column="2" border>
        <el-descriptions-item label="用户 ID">{{ user.userId }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="user.deleteFlag === 0 ? 'success' : 'info'">
            {{ user.deleteFlag === 0 ? '已启用' : '已停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="用户名">{{ user.username }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ roleLabel(user.role) }}</el-descriptions-item>
        <el-descriptions-item label="所属公司">
          {{ user.companyName || '未分配' }}
        </el-descriptions-item>
        <el-descriptions-item label="默认券商账户">
          {{ maskAccountId(user.accountUserId) }}
        </el-descriptions-item>
        <el-descriptions-item label="券商">
          {{ user.brokerName || '—' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import { loadIsPlatformAdmin } from '@/utils/adminPermissions'

interface UserAdmin {
  userId: number
  username: string
  accountUserId: string | null
  brokerName: string | null
  companyId: number | null
  companyName: string | null
  role: string
  deleteFlag: number
}

const router = useRouter()
const route = useRoute()
const user = ref<UserAdmin | null>(null)
const loading = ref(false)
const loadError = ref('')
const canManageUsers = ref(false)
const permissionLoadError = ref('')
let activeRequest = 0

const errorMessage = (error: unknown) => {
  if (error instanceof Error && error.message) return error.message
  if (typeof error === 'object' && error !== null && 'message' in error) {
    const message = error.message
    if (typeof message === 'string' && message) return message
  }
  return '请检查管理端 API 服务后重试'
}

const loadUser = async () => {
  const requestId = ++activeRequest
  const id = Number(route.params.id)
  user.value = null
  loadError.value = ''

  if (!Number.isSafeInteger(id) || id <= 0) {
    loadError.value = '用户 ID 无效'
    loading.value = false
    return
  }

  loading.value = true
  try {
    const result = await request.get<UserAdmin>(`/users/${id}`)
    if (!result
        || result.userId !== id
        || typeof result.username !== 'string'
        || (result.companyId != null
          && (!Number.isSafeInteger(result.companyId) || result.companyId <= 0))
        || (result.companyName != null && typeof result.companyName !== 'string')) {
      throw new Error('服务器返回的用户详情格式无效')
    }
    if (requestId === activeRequest) user.value = result
  } catch (error: unknown) {
    if (requestId === activeRequest) loadError.value = errorMessage(error)
  } finally {
    if (requestId === activeRequest) loading.value = false
  }
}

const roleLabel = (role: string) => {
  const labels: Record<string, string> = {
    ROLE_SUPER_ADMIN: '超级管理员',
    ROLE_ADMIN: '管理员',
    ROLE_COMPANY_ADMIN: '公司管理员',
    ROLE_RISK_OPERATOR: '风控操作员',
    ROLE_AUDITOR: '审计员',
    ROLE_TRADER: '交易用户',
    ROLE_VIEWER: '只读用户'
  }
  return labels[role] ?? (role || '—')
}

const maskAccountId = (accountId: string | null) => {
  if (!accountId) return '未绑定有效账户'
  if (accountId.length <= 4) return '••••'
  return `${'•'.repeat(Math.min(accountId.length - 4, 8))}${accountId.slice(-4)}`
}

const back = () => router.push({ name: 'UserList' })
const edit = () => {
  if (canManageUsers.value && user.value) {
    router.push({ name: 'UserEdit', params: { id: user.value.userId } })
  }
}

const loadPermissions = async () => {
  permissionLoadError.value = ''
  try {
    canManageUsers.value = await loadIsPlatformAdmin()
  } catch {
    canManageUsers.value = false
    permissionLoadError.value = '无法验证平台管理权限；此页面仅提供只读信息。'
  }
}

watch(() => route.params.id, loadUser, { immediate: true })
onMounted(loadPermissions)
</script>

<style scoped>
.user-view-page {
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

.load-error {
  display: flex;
  align-items: center;
  gap: 12px;
}

.permission-warning {
  margin-bottom: 16px;
}

.load-error :deep(.el-alert) {
  flex: 1;
}

@media (max-width: 640px) {
  .card-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .user-view-page :deep(.el-descriptions__body .el-descriptions__table) {
    table-layout: auto;
  }

  .user-view-page :deep(.el-descriptions__cell) {
    overflow-wrap: anywhere;
  }
}
</style>
