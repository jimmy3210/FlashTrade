<template>
  <div class="page">
    <div class="toolbar">
      <div style="display:flex; gap:12px; align-items:center;">
        <button class="primary" @click="goCreate">➕ 新建公司</button>
        <input v-model="q" placeholder="按名称搜索" @keyup.enter="search" style="padding:6px 10px; border-radius:6px; border:1px solid #ddd;" />
        <button @click="search" class="btn">搜索</button>
      </div>
    </div>

    <table>
      <thead>
        <tr>
          <th class="ops-col"></th>
          <th>ID</th>
          <th>公司名称</th>
          <th>分组数</th>
          <th>状态</th>
          <th>创建时间</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="company in companies" :key="company.id">
          <td class="ops">
            <i class="icon view" @click="goView(company)" title="查看"></i>
            <i class="icon edit" @click="goEdit(company)" title="编辑"></i>
            <i
              class="icon toggle"
              :class="company.isActive ? 'pause' : 'play'"
              @click="toggle(company)"
              :title="company.isActive ? '停用公司' : '启用公司'"
            ></i>
            <i class="icon delete" @click="remove(company)" title="删除"></i>
          </td>
          <td>{{ company.id }}</td>
          <td>{{ company.name }}</td>
          <td>{{ company.groupCount ?? 0 }}</td>
          <td>
            <span :class="company.isActive ? 'on' : 'off'">
              {{ company.isActive ? '已启用' : '已停用' }}
            </span>
          </td>
          <td>{{ formatDate(company.createdAt) }}</td>
        </tr>
        <tr v-if="companies.length === 0">
          <td colspan="6" class="empty">暂无公司数据</td>
        </tr>
      </tbody>
    </table>

    <div class="pagination">
      <button class="btn" @click="prevPage" :disabled="page === 0">上一页</button>
      <span style="margin:0 12px">第 {{ page + 1 }} 页 / 共 {{ Math.max(1, Math.ceil(total / size)) }} 页（共 {{ total }} 条）</span>
      <button class="btn" @click="nextPage" :disabled="(page + 1) * size >= total">下一页</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const companies = ref<any[]>([])
const q = ref<string>('')
const page = ref<number>(0)
const size = ref<number>(20)
const total = ref<number>(0)

const load = async () => {
  const res = await request.get('/api/v1/admin/companies', { params: { page: page.value, size: size.value, q: q.value || undefined } })
  // res: { items: [], total, page, size }
  companies.value = (res?.items) || []
  total.value = res?.total || 0
}

const search = async () => {
  page.value = 0
  await load()
}

const goCreate = () => router.push({ name: 'CompanyCreate' })
const goEdit = (company: any) => router.push({ name: 'CompanyEdit', params: { id: company.id } })
const goView = (company: any) => router.push({ name: 'CompanyView', params: { id: company.id } })

const toggle = async (company: any) => {
  const nextState = !company.isActive
  if (!confirm(company.isActive ? `确认停用公司 ${company.name}?` : `确认启用公司 ${company.name}?`)) return

  try {
    await request.post(`/api/v1/admin/companies/${company.id}/status`, { isActive: nextState })
    company.isActive = nextState
  } catch (error) {
    alert('状态更新失败')
  }
}

const remove = async (company: any) => {
  if (!confirm(`确认删除公司 ${company.name}?`)) return

  try {
    await request.delete(`/api/v1/admin/companies/${company.id}`)
    await load()
  } catch (error) {
    alert('删除失败')
  }
}

const prevPage = async () => {
  if (page.value > 0) {
    page.value -= 1
    await load()
  }
}

const nextPage = async () => {
  const lastPage = Math.max(0, Math.ceil(total.value / size.value) - 1)
  if (page.value < lastPage) {
    page.value += 1
    await load()
  }
}

const formatDate = (value: string | undefined) => {
  if (!value) return '-'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString()
}

onMounted(load)
</script>

<style scoped>
.page {
  padding: 16px;
}

.toolbar {
  margin-bottom: 12px;
}

.ops-col { width: 110px; }
.ops {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.icon {
  width: 18px;
  height: 18px;
  cursor: pointer;
  opacity: 0.8;
  user-select: none;
}
.icon:hover { opacity: 1; transform: scale(1.14); }

.icon.view::before { content: '👁️'; }
.icon.edit::before { content: '✏️'; }
.icon.pause::before { content: '⏸️'; }
.icon.play::before { content: '▶️'; }
.icon.delete::before { content: '🗑️'; }

.on { color: #2ecc71; font-weight: 600; }
.off { color: #e74c3c; font-weight: 600; }

table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  background: #fff;
}

th, td {
  padding: 8px 10px;
  border-bottom: 1px solid #eee;
  white-space: nowrap;
}

th { background: #f8f9fa; text-align: left; }
.empty { text-align: center; color: #999; padding: 16px; }
</style>
