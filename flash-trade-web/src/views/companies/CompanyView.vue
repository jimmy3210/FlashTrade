<template>
  <div class="company-view-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="card-header">
          <h2>公司详情</h2>
          <div class="actions">
            <el-button @click="goBack">返回列表</el-button>
            <el-button type="primary" :disabled="!company" @click="editCompany">编辑公司</el-button>
          </div>
        </div>
      </template>

      <div v-if="loadError" class="load-error">
        <el-alert
          title="公司详情加载失败"
          :description="loadError"
          type="error"
          show-icon
          :closable="false"
        />
        <el-button type="primary" plain @click="loadCompany">重试</el-button>
      </div>

      <el-descriptions v-else-if="company" :column="2" border>
        <el-descriptions-item label="公司 ID">{{ company.id }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="company.isActive ? 'success' : 'info'">
            {{ company.isActive ? '已启用' : '已停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="公司名称">{{ company.name }}</el-descriptions-item>
        <el-descriptions-item label="分组数">{{ company.groupCount ?? 0 }}</el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">
          {{ formatDate(company.createdAt) }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'

interface Company {
  id: number
  name: string
  isActive: boolean
  groupCount: number
  createdAt: string
}

const route = useRoute()
const router = useRouter()
const company = ref<Company | null>(null)
const loading = ref(false)
const loadError = ref('')
let activeRequest = 0

const errorMessage = (error: unknown) => {
  if (error instanceof Error && error.message) return error.message
  if (typeof error === 'object' && error !== null && 'message' in error) {
    const message = error.message
    if (typeof message === 'string' && message) return message
  }
  return '请检查管理端 API 服务后重试'
}

const loadCompany = async () => {
  const requestId = ++activeRequest
  const id = Number(route.params.id)
  company.value = null
  loadError.value = ''

  if (!Number.isSafeInteger(id) || id <= 0) {
    loadError.value = '公司 ID 无效'
    loading.value = false
    return
  }

  loading.value = true
  try {
    const result = await request.get<Company>(`/api/v1/admin/companies/${id}`)
    if (!result || typeof result.id !== 'number' || typeof result.name !== 'string') {
      throw new Error('服务器返回的公司详情格式无效')
    }
    if (requestId === activeRequest) company.value = result
  } catch (error: unknown) {
    if (requestId === activeRequest) {
      loadError.value = errorMessage(error)
    }
  } finally {
    if (requestId === activeRequest) loading.value = false
  }
}

const formatDate = (value: string) => {
  if (!value) return '—'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString()
}

const goBack = () => router.push({ name: 'CompanyList' })
const editCompany = () => {
  if (company.value) {
    router.push({ name: 'CompanyEdit', params: { id: company.value.id } })
  }
}

watch(() => route.params.id, loadCompany, { immediate: true })
</script>

<style scoped>
.company-view-page {
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

.load-error :deep(.el-alert) {
  flex: 1;
}

@media (max-width: 640px) {
  .card-header {
    align-items: flex-start;
    flex-direction: column;
  }

  .company-view-page :deep(.el-descriptions__body .el-descriptions__table) {
    table-layout: auto;
  }

  .company-view-page :deep(.el-descriptions__cell) {
    overflow-wrap: anywhere;
  }
}
</style>
