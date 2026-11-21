<template>
  <div class="company-form-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>编辑公司</h2>
        </div>
      </template>

      <div v-if="loadError" class="load-error">
        <el-alert
          title="公司信息加载失败"
          :description="loadError"
          type="error"
          show-icon
          :closable="false"
        />
        <el-button type="primary" plain :disabled="loading" @click="loadCompany">重试</el-button>
      </div>

      <el-form
        v-else-if="loaded"
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="company-form"
        @submit.prevent="submit"
      >
        <el-form-item label="公司名称" prop="name">
          <el-input
            v-model="form.name"
            autocomplete="organization"
            placeholder="请输入公司名称"
            :disabled="saving"
            @keyup.enter="submit"
          />
        </el-form-item>
        <el-form-item prop="isActive">
          <el-checkbox v-model="form.isActive" :disabled="saving">启用公司</el-checkbox>
        </el-form-item>
        <div v-if="submitError" class="submit-error" role="alert">{{ submitError }}</div>
        <div class="actions">
          <el-button :disabled="saving" @click="goBack">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submit">保存修改</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import request from '@/utils/request'

interface Company {
  id: number
  name: string
  isActive: boolean
}

interface CompanyForm {
  name: string
  isActive: boolean
}

const route = useRoute()
const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const loaded = ref(false)
const loadError = ref('')
const submitError = ref('')
const form = reactive<CompanyForm>({
  name: '',
  isActive: true
})
let activeRequest = 0

const rules: FormRules<CompanyForm> = {
  name: [{
    validator: (_rule, value: string, callback) => {
      if (!value?.trim()) callback(new Error('公司名称不能为空'))
      else callback()
    },
    trigger: ['blur', 'change']
  }]
}

const errorMessage = (error: unknown, fallback: string) => {
  if (error instanceof Error && error.message) return error.message
  if (typeof error === 'object' && error !== null && 'message' in error) {
    const message = error.message
    if (typeof message === 'string' && message) return message
  }
  return fallback
}

const loadCompany = async () => {
  const requestId = ++activeRequest
  const id = Number(route.params.id)
  loaded.value = false
  loadError.value = ''
  submitError.value = ''

  if (!Number.isSafeInteger(id) || id <= 0) {
    loadError.value = '公司 ID 无效'
    loading.value = false
    return
  }

  loading.value = true
  try {
    const company = await request.get<Company>(`/api/v1/admin/companies/${id}`)
    if (!company || company.id !== id || typeof company.name !== 'string') {
      throw new Error('服务器返回的公司信息格式无效')
    }
    if (requestId === activeRequest) {
      form.name = company.name
      form.isActive = Boolean(company.isActive)
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

const submit = async () => {
  if (saving.value || !loaded.value || !formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  saving.value = true
  submitError.value = ''
  try {
    await request.put(`/api/v1/admin/companies/${route.params.id}`, {
      name: form.name.trim(),
      isActive: form.isActive
    })
    ElMessage.success('公司信息已更新')
    await router.push({ name: 'CompanyList' })
  } catch (error: unknown) {
    submitError.value = errorMessage(error, '更新公司失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

const goBack = () => router.push({ name: 'CompanyList' })

watch(() => route.params.id, loadCompany, { immediate: true })
</script>

<style scoped>
.company-form-page {
  max-width: 720px;
  min-width: 0;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
}

.company-form {
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
}

.submit-error {
  margin-bottom: 16px;
  color: var(--el-color-danger);
}
</style>
