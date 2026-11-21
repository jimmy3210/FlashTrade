<template>
  <div class="company-list-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>公司管理</h2>
          <div class="header-actions">
            <el-button :loading="loading" @click="loadCompanies">刷新</el-button>
            <el-button type="primary" @click="goCreate">新建公司</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" class="filters" @submit.prevent="search">
        <el-form-item label="公司名称">
          <el-input
            v-model="keyword"
            clearable
            placeholder="输入公司名称"
            @keyup.enter="search"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="search">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="loadError"
        class="load-error"
        title="公司列表加载失败"
        :description="loadError"
        type="error"
        show-icon
        :closable="false"
      />

      <el-table
        v-loading="loading"
        :data="companies"
        row-key="id"
        stripe
        empty-text="暂无公司数据"
      >
        <el-table-column prop="id" label="公司 ID" width="100" />
        <el-table-column prop="name" label="公司名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="groupCount" label="分组数" width="100" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }: { row: Company }">
            <el-tag :type="row.isActive ? 'success' : 'info'">
              {{ row.isActive ? '已启用' : '已停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="180">
          <template #default="{ row }: { row: Company }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="230" fixed="right">
          <template #default="{ row }: { row: Company }">
            <el-button link type="primary" :disabled="isUpdating(row.id)" @click="goView(row)">查看</el-button>
            <el-button link type="primary" :disabled="isUpdating(row.id)" @click="goEdit(row)">编辑</el-button>
            <el-button
              link
              :type="row.isActive ? 'warning' : 'success'"
              :loading="isUpdating(row.id)"
              @click="toggleStatus(row)"
            >
              {{ row.isActive ? '停用' : '启用' }}
            </el-button>
            <el-button
              link
              type="danger"
              :disabled="isUpdating(row.id)"
              @click="deleteCompany(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <span>共 {{ total }} 家公司</span>
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="sizes, prev, pager, next"
          background
          @current-change="loadCompanies"
          @size-change="changePageSize"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

interface Company {
  id: number
  name: string
  isActive: boolean
  groupCount: number
  createdAt: string
}

interface CompanyPage {
  items: Company[]
  total: number
  page: number
  size: number
}

const router = useRouter()
const companies = ref<Company[]>([])
const keyword = ref('')
const page = ref(1)
const size = ref(20)
const total = ref(0)
const loading = ref(false)
const loadError = ref('')
const updatingIds = ref(new Set<number>())
let activeListRequest = 0

const errorMessage = (error: unknown, fallback: string) => {
  if (error instanceof Error && error.message) return error.message
  if (typeof error === 'object' && error !== null && 'message' in error) {
    const message = error.message
    if (typeof message === 'string' && message) return message
  }
  return fallback
}

const loadCompanies = async () => {
  const requestId = ++activeListRequest
  loading.value = true
  loadError.value = ''
  try {
    const result = await request.get<CompanyPage>('/api/v1/admin/companies', {
      params: {
        page: page.value - 1,
        size: size.value,
        q: keyword.value.trim() || undefined
      }
    })
    if (!result || !Array.isArray(result.items) || typeof result.total !== 'number') {
      throw new Error('服务器返回的公司列表格式无效')
    }
    if (requestId === activeListRequest) {
      companies.value = result.items
      total.value = result.total
    }
  } catch (error: unknown) {
    if (requestId === activeListRequest) {
      loadError.value = errorMessage(error, '请检查管理端 API 服务后重试')
    }
  } finally {
    if (requestId === activeListRequest) loading.value = false
  }
}

const search = () => {
  page.value = 1
  void loadCompanies()
}

const changePageSize = () => {
  page.value = 1
  void loadCompanies()
}

const goCreate = () => router.push({ name: 'CompanyCreate' })
const goEdit = (company: Company) => router.push({ name: 'CompanyEdit', params: { id: company.id } })
const goView = (company: Company) => router.push({ name: 'CompanyView', params: { id: company.id } })
const isUpdating = (companyId: number) => updatingIds.value.has(companyId)

const setUpdating = (companyId: number, updating: boolean) => {
  const ids = new Set(updatingIds.value)
  if (updating) ids.add(companyId)
  else ids.delete(companyId)
  updatingIds.value = ids
}

const toggleStatus = async (company: Company) => {
  const nextState = !company.isActive
  const action = nextState ? '启用' : '停用'
  try {
    await ElMessageBox.confirm(
      `确认${action}公司“${company.name}”吗？`,
      `${action}公司`,
      {
        type: nextState ? 'info' : 'warning',
        confirmButtonText: `确认${action}`,
        cancelButtonText: '取消'
      }
    )
  } catch {
    return
  }

  setUpdating(company.id, true)
  try {
    await request.post(`/api/v1/admin/companies/${company.id}/status`, { isActive: nextState })
    company.isActive = nextState
    ElMessage.success(`公司已${action}`)
  } catch (error: unknown) {
    await loadCompanies()
    ElMessage.warning(`${action}请求结果未确认，请核对公司当前状态后再执行后续操作：${errorMessage(error, '请刷新列表确认')}`)
  } finally {
    setUpdating(company.id, false)
  }
}

const deleteCompany = async (company: Company) => {
  try {
    await ElMessageBox.confirm(
      `删除公司“${company.name}”将同时删除其所属分组，且无法撤销。`,
      '删除公司',
      {
        type: 'error',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消'
      }
    )
  } catch {
    return
  }

  setUpdating(company.id, true)
  try {
    await request.delete(`/api/v1/admin/companies/${company.id}`)
    ElMessage.success('公司已删除')
    if (companies.value.length === 1 && page.value > 1) page.value -= 1
    await loadCompanies()
  } catch (error: unknown) {
    await loadCompanies()
    ElMessage.warning(`删除请求结果未确认，已刷新列表，请先确认公司是否仍存在：${errorMessage(error, '请刷新列表确认')}`)
  } finally {
    setUpdating(company.id, false)
  }
}

const formatDate = (value: string) => {
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString()
}

onMounted(loadCompanies)
</script>

<style scoped>
.company-list-page {
  min-width: 0;
}

.card-header,
.header-actions,
.pagination {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
}

.filters {
  margin-bottom: 8px;
}

.load-error {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 20px;
  color: #606266;
}

@media (max-width: 640px) {
  .card-header,
  .pagination {
    align-items: flex-start;
    flex-direction: column;
  }

  .header-actions,
  .pagination :deep(.el-pagination) {
    width: 100%;
  }

  .pagination :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: flex-start;
  }
}
</style>
