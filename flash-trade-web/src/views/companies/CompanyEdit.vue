<template>
  <div class="page">
    <div class="card">
      <h3>编辑公司</h3>
      <form @submit.prevent="submit">
        <div class="field-row">
          <label>公司名称</label>
          <input v-model="form.name" placeholder="请输入公司名称" />
        </div>

        <div class="field-row checkbox-row">
          <label>
            <input v-model="form.isActive" type="checkbox" />
            启用状态
          </label>
        </div>

        <div class="actions">
          <button type="button" class="secondary" @click="goBack">取消</button>
          <button type="submit" class="primary">保存</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

const form = reactive({
  name: '',
  isActive: true,
})

const load = async () => {
  const company = await request.get(`/api/v1/admin/companies/${route.params.id}`)
  form.name = company.name
  form.isActive = !!company.isActive
}

const submit = async () => {
  if (!form.name.trim()) {
    alert('公司名称不能为空')
    return
  }

  try {
    await request.put(`/api/v1/admin/companies/${route.params.id}`, {
      name: form.name.trim(),
      isActive: form.isActive,
    })
    router.push({ name: 'CompanyList' })
  } catch (error) {
    alert('更新失败')
  }
}

const goBack = () => router.push({ name: 'CompanyList' })

onMounted(load)
</script>

<style scoped>
.page { padding: 16px; }
.card {
  max-width: 640px;
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 8px 20px rgba(0,0,0,0.05);
}

h3 { margin-top: 0; }
.field-row {
  display: flex;
  flex-direction: column;
  margin-bottom: 16px;
}
.field-row label {
  margin-bottom: 8px;
  font-weight: 600;
}
input {
  padding: 10px 12px;
  border: 1px solid #dfe3e8;
  border-radius: 6px;
  font-size: 14px;
}
.checkbox-row label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}
.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 12px;
}
</style>
