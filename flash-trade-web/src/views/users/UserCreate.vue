<template>
  <div class="user-create-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>新建用户</h2>
        </div>
      </template>

      <el-form
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
              v-for="company in activeCompanies"
              :key="company.id"
              :label="company.name"
              :value="company.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="初始密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            autocomplete="new-password"
            placeholder="请输入初始密码"
            minlength="6"
            maxlength="100"
            :disabled="saving"
            @keyup.enter="save"
          />
        </el-form-item>
        <el-alert
          title="新用户将以交易用户身份创建，并使用 SANDBOX 环境；密码不会在创建后再次显示。"
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
            创建用户
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import request from '@/utils/request'
import { loadAdminCompanies, type AdminCompanyOption } from '@/utils/adminCompanies'

interface UserForm {
  username: string
  accountUserId: string
  brokerName: string
  companyId: number | null
  password: string
}

const router = useRouter()
const formRef = ref<FormInstance>()
const saving = ref(false)
const submitError = ref('')
const companies = ref<AdminCompanyOption[]>([])
const companiesLoading = ref(false)
const companiesLoadError = ref('')
const form = reactive<UserForm>({
  username: '',
  accountUserId: '',
  brokerName: '',
  companyId: null,
  password: ''
})
const activeCompanies = computed(() => companies.value.filter(company => company.isActive))

const requiredRule = (label: string) => ({
  validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
    if (!value?.trim()) callback(new Error(`${label}不能为空`))
    else callback()
  },
  trigger: ['blur', 'change']
})

const passwordRule = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!value) callback(new Error('初始密码不能为空'))
  else if (value.length < 6 || value.length > 100) callback(new Error('密码长度须在 6~100 字符之间'))
  else callback()
}

const companyRule = (_rule: unknown, value: number | null, callback: (error?: Error) => void) => {
  if (!Number.isSafeInteger(value) || (value ?? 0) <= 0) callback(new Error('请选择所属公司'))
  else if (!activeCompanies.value.some(company => company.id === value)) {
    callback(new Error('请选择有效的启用公司'))
  } else callback()
}

const rules: FormRules<UserForm> = {
  username: [requiredRule('用户名')],
  accountUserId: [requiredRule('默认券商账户')],
  brokerName: [requiredRule('券商')],
  companyId: [{ validator: companyRule, trigger: ['blur', 'change'] }],
  password: [{ validator: passwordRule, trigger: ['blur', 'change'] }]
}

const loadCompanies = async () => {
  companiesLoading.value = true
  companiesLoadError.value = ''
  try {
    companies.value = await loadAdminCompanies()
  } catch (error: unknown) {
    companies.value = []
    companiesLoadError.value = errorMessage(error)
  } finally {
    companiesLoading.value = false
  }
}

const errorMessage = (error: unknown) => {
  if (error instanceof Error && error.message) return error.message
  if (typeof error === 'object' && error !== null && 'message' in error) {
    const message = error.message
    if (typeof message === 'string' && message) return message
  }
  return '创建用户失败，请检查信息后重试'
}

const save = async () => {
  if (saving.value || companiesLoading.value || companiesLoadError.value || !formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  saving.value = true
  submitError.value = ''
  try {
    await request.post('/users', {
      username: form.username.trim(),
      accountUserId: form.accountUserId.trim(),
      brokerName: form.brokerName.trim(),
      companyId: form.companyId,
      password: form.password
    })
    ElMessage.success('用户已创建')
    await router.push({ name: 'UserList' })
  } catch (error: unknown) {
    form.password = ''
    submitError.value = `${errorMessage(error)}。创建结果未确认，请先在用户列表核对是否已创建，再决定是否重试。`
  } finally {
    saving.value = false
  }
}

const cancel = () => router.push({ name: 'UserList' })

onMounted(loadCompanies)
</script>

<style scoped>
.user-create-page {
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
