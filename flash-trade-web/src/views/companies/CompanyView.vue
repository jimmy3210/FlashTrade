<template>
  <div class="page">
    <div class="card">
      <h3>公司详情</h3>
      <div class="info-grid">
        <div class="row"><span class="label">ID</span><span>{{ company.id }}</span></div>
        <div class="row"><span class="label">公司名称</span><span>{{ company.name }}</span></div>
        <div class="row"><span class="label">状态</span><span :class="company.isActive ? 'on' : 'off'">{{ company.isActive ? '已启用' : '已停用' }}</span></div>
        <div class="row"><span class="label">分组数</span><span>{{ company.groupCount ?? 0 }}</span></div>
        <div class="row"><span class="label">创建时间</span><span>{{ formatDate(company.createdAt) }}</span></div>
      </div>
      <div class="actions">
        <button class="secondary" @click="goBack">返回列表</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const company = ref<any>({})

const load = async () => {
  company.value = await request.get(`/api/v1/admin/companies/${route.params.id}`)
}

const formatDate = (value: string | undefined) => {
  if (!value) return '-'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString()
}

const goBack = () => router.push({ name: 'CompanyList' })

onMounted(load)
</script>

<style scoped>
.page { padding: 16px; }
.card {
  max-width: 700px;
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 8px 20px rgba(0,0,0,0.05);
}

h3 { margin-top: 0; }
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
  margin-top: 16px;
}
.row {
  display: flex;
  justify-content: space-between;
  padding: 10px 12px;
  border-bottom: 1px solid #f0f0f0;
}
.label { font-weight: 600; color: #555; }
.on { color: #2ecc71; font-weight: 600; }
.off { color: #e74c3c; font-weight: 600; }
.actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
