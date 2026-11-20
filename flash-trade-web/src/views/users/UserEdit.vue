<template>
  <div class="user-edit-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>编辑用户</h2>
        </div>
      </template>

      <div v-if="loadError" class="load-error">
        <el-alert
          title="用户信息加载失败"
          :description="loadError"
          type="error"
          show-icon
          :closable="false"
        />
        <el-button type="primary" plain :disabled="loading" @click="loadUser">重试</el-button>
      </div>

      <el-form
        v-else-if="loaded"
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="user-form"
        @submit.prevent="save"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="form.username"
            autocomplete="username"
            placeholder="请输入用户名"
            maxlength="50"
            :disabled="saving"
          />
        </el-form-item>
        <el-form-item label="默认券商账户" prop="accountUserId">
          <el-input
            v-model="form.accountUserId"
            type="password"
            show-password
            placeholder="请输入券商账户名"
            :disabled="saving"
          />
        </el-form-item>
        <el-form-item label="券商" prop="brokerName">
          <el-input
            v-model="form.brokerName"
            placeholder="请输入后端支持的券商代码或名称"
            :disabled="saving"
          />
        </el-form-item>
        <el-form-item label="所属公司" prop="companyId">
          <el-select
            v-model="form.companyId"
            filterable
            clearable
            placeholder="请选择所属公司"
            :loading="companiesLoading"
            :disabled="saving || companiesLoading || !!companiesLoadError"
          >
            <el-option
              v-for="company in selectableCompanies"
              :key="company.id"
              :label="company.name + (company.isActive ? '' : '（已停用）')"
              :value="company.id"
              :disabled="!company.isActive"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="用户角色">
          <el-input :model-value="roleLabel" disabled />
        </el-form-item>
        <el-alert
          title="密码请通过用户列表中的“重置密码”操作修改。角色和启用状态不会在此表单中变更；更换公司会立即改变该用户的数据管理范围。"
          type="info"
          :closable="false"
          show-icon
        />
        <div v-if="companiesLoadError" class="company-load-error">
          <el-alert
            title="公司列表加载失败"
            :description="companiesLoadError"
            type="error"
            :closable="false"
            show-icon
          />
          <el-button type="primary" plain :disabled="companiesLoading" @click="loadCompanies">
            重试
          </el-button>
        </div>
        <el-alert
          v-else-if="!companiesLoading && activeCompanies.length === 0"
          class="company-load-error"
          title="当前没有可分配的启用公司"
          type="warning"
          :closable="false"
          show-icon
        />
        <div v-if="submitError" class="submit-error" role="alert">{{ submitError }}</div>
        <div class="actions">
          <el-button :disabled="saving" @click="cancel">取消</el-button>
          <el-button
            type="primary"
            :loading="saving"
            :disabled="companiesLoading || !!companiesLoadError || activeCompanies.length === 0"
            @click="save"
          >
            保存修改
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import request from '@/utils/request'
import { loadAdminCompanies, type AdminCompanyOption } from '@/utils/adminCompanies'

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

interface UserForm {
  username: string
  accountUserId: string
  brokerName: string
  companyId: number | null
}

const router = useRouter()
const route = useRoute()
const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const loaded = ref(false)
const loadError = ref('')
const submitError = ref('')
const companies = ref<AdminCompanyOption[]>([])
const companiesLoading = ref(false)
const companiesLoadError = ref('')
const currentUser = ref<UserAdmin | null>(null)
const form = reactive<UserForm>({
  username: '',
  accountUserId: '',
  brokerName: '',
  companyId: null
})
const activeCompanies = computed(() => companies.value.filter(company => company.isActive))
const selectableCompanies = computed(() => companies.value.filter(company =>
  company.isActive || company.id === form.companyId
))
let activeRequest = 0

const rules: FormRules<UserForm> = {
  username: [{ required: true, whitespace: true, message: '请输入用户名', trigger: ['blur', 'change'] }],
  accountUserId: [{ required: true, whitespace: true, message: '请输入默认券商账户', trigger: ['blur', 'change'] }],
  brokerName: [{ required: true, whitespace: true, message: '请输入券商代码或名称', trigger: ['blur', 'change'] }],
  companyId: [{
    validator: (_rule, value: number | null, callback) => {
      if (!Number.isSafeInteger(value) || (value ?? 0) <= 0) {
        callback(new Error('请选择所属公司'))
      } else if (!activeCompanies.value.some(company => company.id === value)) {
        callback(new Error('请选择有效的启用公司'))
      } else {
        callback()
      }
    },
    trigger: ['blur', 'change']
  }]
}

const roleLabel = computed(() => {
  const role = currentUser.value?.role ?? ''
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
})

const errorMessage = (error: unknown, fallback: string) => {
  if (error instanceof Error && error.message) return error.message
  if (typeof error === 'object' && error !== null && 'message' in error) {
    const message = error.message
    if (typeof message === 'string' && message) return message
  }
  return fallback
}

const loadCompanies = async () => {
  companiesLoading.value = true
  companiesLoadError.value = ''
  try {
    companies.value = await loadAdminCompanies()
  } catch (error: unknown) {
    companies.value = []
    companiesLoadError.value = errorMessage(error, '请检查管理端 API 服务后重试')
  } finally {
    companiesLoading.value = false
  }
}

const loadUser = async () => {
  const requestId = ++activeRequest
  const id = Number(route.params.id)
  loaded.value = false
  currentUser.value = null
  loadError.value = ''
  submitError.value = ''

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
      throw new Error('服务器返回的用户信息格式无效')
    }
    if (requestId === activeRequest) {
      currentUser.value = result
      form.username = result.username
      form.accountUserId = result.accountUserId ?? ''
      form.brokerName = result.brokerName ?? ''
      form.companyId = result.companyId ?? null
      loaded.value = true
    }
  } catch (error: unknown) {
    if (requestId === activeRequest) {
      loadError.value = errorMessage(error, '请检查管理端 API 服务后重试')
    }
  } finally {
    if (requestId === activeRequest) loading.value = false
  }
}

const save = async () => {
  if (saving.value || companiesLoading.value || companiesLoadError.value
      || !loaded.value || !currentUser.value || !formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  saving.value = true
  submitError.value = ''
  try {
    await request.put(`/users/${currentUser.value.userId}`, {
      username: form.username.trim(),
      accountUserId: form.accountUserId.trim(),
      brokerName: form.brokerName.trim(),
      companyId: form.companyId
    })
    ElMessage.success('用户信息已更新')
    await router.push({ name: 'UserList' })
  } catch (error: unknown) {
    submitError.value = errorMessage(error, '更新用户失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

const cancel = () => router.push({ name: 'UserList' })

watch(() => route.params.id, loadUser, { immediate: true })
onMounted(loadCompanies)
</script>

<style scoped>
.user-edit-page {
  max-width: 720px;
  min-width: 0;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
}

.user-form {
  max-width: 520px;
}

.load-error {
  display: flex;
  align-items: center;
  gap: 12px;
}

.load-error :deep(.el-alert) {
  flex: 1;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.submit-error {
  margin-top: 16px;
  color: var(--el-color-danger);
}

.company-load-error {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
}

.company-load-error :deep(.el-alert) {
  flex: 1;
}
</style>
