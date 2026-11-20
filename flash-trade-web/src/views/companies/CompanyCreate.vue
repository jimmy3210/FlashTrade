<template>
  <div class="company-form-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>新建公司</h2>
        </div>
      </template>

      <el-form
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
          <el-checkbox v-model="form.isActive" :disabled="saving">创建后立即启用</el-checkbox>
        </el-form-item>
        <div v-if="submitError" class="submit-error" role="alert">{{ submitError }}</div>
        <div class="actions">
          <el-button :disabled="saving" @click="goBack">取消</el-button>
          <el-button type="primary" :loading="saving" @click="submit">创建公司</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import request from '@/utils/request'

interface CompanyForm {
  name: string
  isActive: boolean
}

const router = useRouter()
const formRef = ref<FormInstance>()
const saving = ref(false)
const submitError = ref('')
const form = reactive<CompanyForm>({
  name: '',
  isActive: true
})

const rules: FormRules<CompanyForm> = {
  name: [{
    validator: (_rule, value: string, callback) => {
      if (!value?.trim()) callback(new Error('公司名称不能为空'))
      else callback()
    },
    trigger: ['blur', 'change']
  }]
}

const errorMessage = (error: unknown) => {
  if (error instanceof Error && error.message) return error.message
  if (typeof error === 'object' && error !== null && 'message' in error) {
    const message = error.message
    if (typeof message === 'string' && message) return message
  }
  return '创建公司失败，请稍后重试'
}

const submit = async () => {
  if (saving.value || !formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  saving.value = true
  submitError.value = ''
  try {
    await request.post('/api/v1/admin/companies', {
      name: form.name.trim(),
      isActive: form.isActive
    })
    ElMessage.success('公司已创建')
    await router.push({ name: 'CompanyList' })
  } catch (error: unknown) {
    submitError.value = errorMessage(error)
  } finally {
    saving.value = false
  }
}

const goBack = () => router.push({ name: 'CompanyList' })
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
