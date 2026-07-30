<template>
  <div class="form-page" v-if="loaded">
    <h2>编辑券商账户</h2>
    <form @submit.prevent="save">
      <label>所属用户</label>
      <select v-model.number="form.userId" required>
        <option :value="null" disabled>请选择用户</option>
        <option v-for="user in users" :key="user.userId" :value="user.userId">
          {{ user.username }} (ID: {{ user.userId }})
        </option>
      </select>

      <label>券商名称</label>
      <input v-model="form.brokerName" placeholder="schwab / etrade / tastyworks" required />

      <label>账户号</label>
      <input v-model="form.accountNo" placeholder="账户号" required />

      <label>账户类型</label>
      <select v-model="form.accountType">
        <option value="CASH">CASH</option>
        <option value="MARGIN">MARGIN</option>
      </select>

      <label>账户状态</label>
      <select v-model="form.status">
        <option value="ACTIVE">ACTIVE</option>
        <option value="SUSPENDED">SUSPENDED</option>
        <option value="CLOSED">CLOSED</option>
      </select>

      <label>开发者账号 ID（可选）</label>
      <input v-model="form.devAccountId" type="number" placeholder="dev account id" />

      <label>API 环境</label>
      <select v-model="form.apiEnvironment">
        <option value="LIVE">LIVE</option>
        <option value="SANDBOX">SANDBOX</option>
      </select>

      <div class="actions">
        <button class="primary" type="submit">保存</button>
        <button type="button" @click="cancel">取消</button>
      </div>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const users = ref<any[]>([])
const loaded = ref(false)

const form = ref({
  userId: null as number | null,
  brokerName: '',
  accountNo: '',
  accountType: 'CASH',
  status: 'ACTIVE',
  devAccountId: null as number | null,
  apiEnvironment: 'LIVE'
})

const loadUsers = async () => {
  users.value = await request.get<any[]>('/users')
}

const loadAccount = async () => {
  const id = route.params.id
  const account = await request.get<any>(`/admin/broker-accounts/${id}`)
  form.value.userId = account.userId
  form.value.brokerName = account.brokerName
  form.value.accountNo = account.accountNo
  form.value.accountType = account.accountType || 'CASH'
  form.value.status = account.status || 'ACTIVE'
  form.value.devAccountId = account.devAccountId ?? null
  form.value.apiEnvironment = account.apiEnvironment || 'LIVE'
  loaded.value = true
}

const save = async () => {
  const id = route.params.id
  const payload = {
    userId: form.value.userId,
    brokerName: form.value.brokerName,
    accountNo: form.value.accountNo,
    accountType: form.value.accountType,
    status: form.value.status,
    devAccountId: form.value.devAccountId ? Number(form.value.devAccountId) : null,
    apiEnvironment: form.value.apiEnvironment
  }
  await request.put(`/admin/broker-accounts/${id}`, payload)
  router.push({ name: 'BrokerAccountList' })
}

const cancel = () => router.push({ name: 'BrokerAccountList' })

onMounted(async () => {
  await Promise.all([loadUsers(), loadAccount()])
})
</script>

<style scoped>
.form-page {
  padding: 16px;
  max-width: 640px;
}

form {
  display: grid;
  gap: 16px;
}

label {
  font-weight: 600;
}

input, select {
  width: 100%;
  padding: 10px;
  border: 1px solid #d3d6dd;
  border-radius: 6px;
}

.actions {
  display: flex;
  gap: 12px;
}

button.primary {
  padding: 10px 16px;
  border: none;
  border-radius: 6px;
  background: #42b883;
  color: #fff;
  cursor: pointer;
}

button.primary:hover {
  background: #369b6f;
}
</style>
