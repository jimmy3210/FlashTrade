<template>
  <div class="user-list-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <div>
            <h2>用户管理</h2>
            <p class="subtitle">查询用户、管理账户访问状态并重置密码</p>
          </div>
          <div class="header-actions">
            <el-button :loading="loading" @click="loadUsers">刷新</el-button>
            <el-button v-if="canManageUsers" type="primary" @click="goCreate">新建用户</el-button>
          </div>
        </div>
      </template>

      <el-alert
        v-if="permissionLoadError"
        class="permission-warning"
        title="当前按只读方式显示"
        :description="permissionLoadError"
        type="warning"
        show-icon
        :closable="false"
      />

      <div v-if="loadError" class="load-error">
        <el-alert
          title="用户列表加载失败"
          :description="loadError"
          type="error"
          show-icon
          :closable="false"
        />
        <el-button type="primary" plain @click="loadUsers">重试</el-button>
      </div>

      <el-form v-else :inline="true" class="filters" @submit.prevent>
        <el-form-item label="搜索">
          <el-input
            v-model="filters.keyword"
            clearable
            placeholder="用户名或券商账户"
            class="search-input"
          />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="filters.role" clearable placeholder="全部角色" class="filter-select">
            <el-option label="超级管理员" value="ROLE_SUPER_ADMIN" />
            <el-option label="交易用户" value="ROLE_TRADER" />
            <el-option label="管理员" value="ROLE_ADMIN" />
            <el-option label="公司管理员" value="ROLE_COMPANY_ADMIN" />
            <el-option label="风控操作员" value="ROLE_RISK_OPERATOR" />
            <el-option label="审计员" value="ROLE_AUDITOR" />
            <el-option label="只读用户" value="ROLE_VIEWER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.status" placeholder="全部状态" class="filter-select">
            <el-option label="全部状态" value="all" />
            <el-option label="已启用" value="enabled" />
            <el-option label="已停用" value="disabled" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="resetFilters">清除筛选</el-button>
        </el-form-item>
      </el-form>

      <el-table
        v-if="!loadError"
        v-loading="loading"
        :data="pageUsers"
        row-key="userId"
        stripe
        empty-text="没有符合条件的用户"
      >
        <el-table-column prop="userId" label="用户 ID" width="100" />
        <el-table-column prop="username" label="用户名" min-width="160" show-overflow-tooltip />
        <el-table-column prop="companyName" label="所属公司" min-width="160" show-overflow-tooltip>
          <template #default="{ row }: { row: UserAdmin }">
            {{ row.companyName || '未分配' }}
          </template>
        </el-table-column>
        <el-table-column prop="accountUserId" label="券商账户" min-width="170">
          <template #default="{ row }: { row: UserAdmin }">
            {{ maskAccountId(row.accountUserId) }}
          </template>
        </el-table-column>
        <el-table-column prop="brokerName" label="券商" min-width="130">
          <template #default="{ row }: { row: UserAdmin }">
            {{ row.brokerName || '—' }}
          </template>
        </el-table-column>
        <el-table-column prop="role" label="角色" width="140">
          <template #default="{ row }: { row: UserAdmin }">
            {{ roleLabel(row.role) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }: { row: UserAdmin }">
            <el-tag :type="row.deleteFlag === 0 ? 'success' : 'info'">
              {{ row.deleteFlag === 0 ? '已启用' : '已停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="250" fixed="right">
          <template #default="{ row }: { row: UserAdmin }">
            <el-button link type="primary" @click="goView(row)">查看</el-button>
            <template v-if="canManageUsers">
              <el-button link type="primary" @click="goEdit(row)">编辑</el-button>
              <el-button link type="warning" @click="openPasswordDialog(row)">重置密码</el-button>
              <el-button
                link
                :type="row.deleteFlag === 0 ? 'danger' : 'success'"
                :loading="updatingUserIds.has(row.userId)"
                @click="toggleStatus(row)"
              >
                {{ row.deleteFlag === 0 ? '停用' : '启用' }}
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!loadError" class="pagination">
        <span class="result-count">共 {{ filteredUsers.length }} 位用户</span>
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="filteredUsers.length"
          layout="sizes, prev, pager, next"
          background
        />
      </div>
    </el-card>

    <el-dialog
      v-model="passwordDialogVisible"
      title="重置用户密码"
      width="440px"
      @closed="resetPasswordForm"
    >
      <p v-if="selectedUser" class="dialog-description">
        为用户 <strong>{{ selectedUser.username }}</strong> 设置新密码。密码不会再次显示。
      </p>
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-position="top">
        <el-form-item label="新密码" prop="password">
          <el-input
            v-model="passwordForm.password"
            type="password"
            show-password
            autocomplete="new-password"
            placeholder="请输入新密码"
            minlength="6"
            maxlength="100"
            @keyup.enter="submitPasswordReset"
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmation">
          <el-input
            v-model="passwordForm.confirmation"
            type="password"
            show-password
            autocomplete="new-password"
            placeholder="请再次输入新密码"
            maxlength="100"
            @keyup.enter="submitPasswordReset"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="resettingPassword" @click="submitPasswordReset">
          确认重置
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
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

interface PasswordForm {
  password: string
  confirmation: string
}

const router = useRouter()
const users = ref<UserAdmin[]>([])
const loading = ref(false)
const loadError = ref('')
const canManageUsers = ref(false)
const permissionLoadError = ref('')
const updatingUserIds = ref(new Set<number>())
const currentPage = ref(1)
const pageSize = ref(10)
const filters = reactive({
  keyword: '',
  role: '',
  status: 'all'
})
let activeListRequest = 0

const filteredUsers = computed(() => {
  const keyword = filters.keyword.trim().toLocaleLowerCase()
  return users.value.filter(user => {
    const matchesKeyword = !keyword
      || user.username.toLocaleLowerCase().includes(keyword)
      || (user.accountUserId ?? '').toLocaleLowerCase().includes(keyword)
    const matchesRole = !filters.role || user.role === filters.role
    const matchesStatus = filters.status === 'all'
      || (filters.status === 'enabled' ? user.deleteFlag === 0 : user.deleteFlag !== 0)
    return matchesKeyword && matchesRole && matchesStatus
  })
})

const pageUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredUsers.value.slice(start, start + pageSize.value)
})

watch([filteredUsers, pageSize], () => {
  const lastPage = Math.max(1, Math.ceil(filteredUsers.value.length / pageSize.value))
  if (currentPage.value > lastPage) currentPage.value = lastPage
})

watch(() => [filters.keyword, filters.role, filters.status], () => {
  currentPage.value = 1
})

const passwordDialogVisible = ref(false)
const resettingPassword = ref(false)
const selectedUser = ref<UserAdmin | null>(null)
const passwordFormRef = ref<FormInstance>()
const passwordForm = reactive<PasswordForm>({
  password: '',
  confirmation: ''
})

const validateConfirmation = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!value) {
    callback(new Error('请确认新密码'))
  } else if (value !== passwordForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

watch(() => passwordForm.password, () => {
  if (passwordForm.confirmation) {
    void passwordFormRef.value?.validateField('confirmation').catch(() => undefined)
  }
})

const validatePassword = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!value) callback(new Error('请输入新密码'))
  else if (value.length < 6 || value.length > 100) callback(new Error('密码长度须在 6~100 字符之间'))
  else callback()
}

const passwordRules: FormRules<PasswordForm> = {
  password: [{ validator: validatePassword, trigger: ['blur', 'change'] }],
  confirmation: [{ validator: validateConfirmation, trigger: ['blur', 'change'] }]
}

const errorMessage = (error: unknown, fallback: string) => {
  if (error instanceof Error && error.message) return error.message
  if (typeof error === 'object' && error !== null && 'message' in error) {
    const message = error.message
    if (typeof message === 'string' && message) return message
  }
  return fallback
}

const loadPermissions = async () => {
  permissionLoadError.value = ''
  try {
    canManageUsers.value = await loadIsPlatformAdmin()
  } catch {
    canManageUsers.value = false
    permissionLoadError.value = '无法验证平台管理权限；用户列表仍可查看，但写操作已隐藏。'
  }
}

const loadUsers = async () => {
  const requestId = ++activeListRequest
  loading.value = true
  loadError.value = ''
  try {
    const result = await request.get<UserAdmin[]>('/users')
    if (!Array.isArray(result)) {
      throw new Error('服务器返回的用户列表格式无效')
    }
    const normalizedUsers = result.map(user => {
      if (!user || !Number.isSafeInteger(user.userId) || user.userId <= 0
          || typeof user.username !== 'string'
          || typeof user.role !== 'string'
          || (user.deleteFlag !== 0 && user.deleteFlag !== 1)
          || (user.accountUserId !== null && typeof user.accountUserId !== 'string')
          || (user.brokerName !== null && typeof user.brokerName !== 'string')
          || (user.companyId != null && (!Number.isSafeInteger(user.companyId) || user.companyId <= 0))
          || (user.companyName != null && typeof user.companyName !== 'string')) {
        throw new Error('服务器返回的用户记录格式无效')
      }
      return user
    })
    if (requestId === activeListRequest) {
      users.value = normalizedUsers
      currentPage.value = 1
    }
  } catch (error: unknown) {
    if (requestId === activeListRequest) {
      loadError.value = errorMessage(error, '请检查管理端 API 服务后重试')
    }
  } finally {
    if (requestId === activeListRequest) loading.value = false
  }
}

const resetFilters = () => {
  filters.keyword = ''
  filters.role = ''
  filters.status = 'all'
  currentPage.value = 1
}

const roleLabel = (role: string) => ({
  ROLE_SUPER_ADMIN: '超级管理员',
  ROLE_ADMIN: '管理员',
  ROLE_COMPANY_ADMIN: '公司管理员',
  ROLE_RISK_OPERATOR: '风控操作员',
  ROLE_AUDITOR: '审计员',
  ROLE_TRADER: '交易用户',
  ROLE_VIEWER: '只读用户'
}[role] ?? role)

const maskAccountId = (accountId: string | null) => {
  if (!accountId) return '未绑定'
  if (accountId.length <= 4) return '••••'
  return `${'•'.repeat(Math.min(accountId.length - 4, 8))}${accountId.slice(-4)}`
}

const goCreate = () => router.push({ name: 'UserCreate' })
const goEdit = (user: UserAdmin) => router.push({ name: 'UserEdit', params: { id: user.userId } })
const goView = (user: UserAdmin) => router.push({ name: 'UserView', params: { id: user.userId } })

const toggleStatus = async (user: UserAdmin) => {
  const nextFlag = user.deleteFlag === 0 ? 1 : 0
  const action = nextFlag === 1 ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(
      `确认${action}用户“${user.username}”吗？${nextFlag === 1 ? '停用后，该用户将无法继续使用受保护功能。' : ''}`,
      `${action}用户`,
      { type: nextFlag === 1 ? 'warning' : 'info', confirmButtonText: `确认${action}`, cancelButtonText: '取消' }
    )
  } catch {
    return
  }

  updatingUserIds.value = new Set(updatingUserIds.value).add(user.userId)
  try {
    await request.post('/users/status', { userId: user.userId, deleteFlag: nextFlag })
    user.deleteFlag = nextFlag
    ElMessage.success(`用户已${action}`)
  } catch (error: unknown) {
    await loadUsers()
    ElMessage.warning(`${action}用户请求结果未确认，已刷新列表，请核对当前状态后再执行后续操作：${errorMessage(error, '请刷新列表确认')}`)
  } finally {
    const updatedIds = new Set(updatingUserIds.value)
    updatedIds.delete(user.userId)
    updatingUserIds.value = updatedIds
  }
}

const openPasswordDialog = (user: UserAdmin) => {
  selectedUser.value = user
  passwordDialogVisible.value = true
}

const resetPasswordForm = () => {
  selectedUser.value = null
  passwordForm.password = ''
  passwordForm.confirmation = ''
  passwordFormRef.value?.clearValidate()
}

const submitPasswordReset = async () => {
  if (!selectedUser.value || !passwordFormRef.value) return
  try {
    await passwordFormRef.value.validate()
  } catch {
    return
  }

  resettingPassword.value = true
  try {
    await request.post(`/users/${selectedUser.value.userId}/password`, {
      password: passwordForm.password
    })
    ElMessage.success(`已重置用户“${selectedUser.value.username}”的密码`)
    passwordDialogVisible.value = false
  } catch (error: unknown) {
    passwordDialogVisible.value = false
    ElMessage.warning(`密码重置请求结果未确认。不要盲目重复提交；请按账户恢复流程核实后再操作：${errorMessage(error, '请联系系统管理员核实')}`)
  } finally {
    resettingPassword.value = false
  }
}

onMounted(() => {
  void loadUsers()
  void loadPermissions()
})
</script>

<style scoped>
.user-list-page {
  padding: 20px;
}

.card-header,
.header-actions,
.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

h2 {
  margin: 0;
  font-size: 20px;
}

.subtitle,
.dialog-description,
.result-count {
  color: var(--el-text-color-secondary);
}

.subtitle {
  margin: 6px 0 0;
  font-size: 13px;
}

.filters {
  margin: 20px 0 4px;
}

.load-error {
  display: flex;
  align-items: center;
  gap: 12px;
}

.permission-warning {
  margin-bottom: 16px;
}

.search-input {
  width: 240px;
}

.filter-select {
  width: 150px;
}

.pagination {
  justify-content: flex-end;
  margin-top: 20px;
}

.result-count {
  margin-right: auto;
  font-size: 13px;
}

.dialog-description {
  margin: 0 0 20px;
}

@media (max-width: 700px) {
  .user-list-page {
    padding: 12px;
  }

  .card-header {
    align-items: flex-start;
  }

  .header-actions {
    flex-direction: column-reverse;
    align-items: stretch;
    gap: 8px;
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
