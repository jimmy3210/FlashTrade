<template>
  <div class="page">
    <div class="toolbar">
      <h2>券商账户管理</h2>
      <button class="primary" @click="goCreate">➕ 新建券商账户</button>
    </div>

    <table>
      <thead>
        <tr>
          <th class="ops-col"></th>
          <th>ID</th>
          <th>用户名</th>
          <th>券商</th>
          <th>账户号</th>
          <th>类型</th>
          <th>状态</th>
          <th>API 环境</th>
          <th>开发者账号</th>
          <th>当前使用者</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="account in accounts" :key="account.id">
          <td class="ops">
            <i class="icon view" @click="goView(account)" title="预览"></i>
            <i class="icon edit" @click="goEdit(account)" title="编辑"></i>
            <i class="icon trash" @click="remove(account)" title="停用"></i>
          </td>
          <td>{{ account.id }}</td>
          <td>{{ account.username }}</td>
          <td>{{ account.brokerName }}</td>
          <td>{{ account.accountNo }}</td>
          <td>{{ account.accountType }}</td>
          <td>{{ account.status }}</td>
          <td>{{ account.apiEnvironment }}</td>
          <td>{{ account.devAccountId || '-' }}</td>
          <td class="binding-status">{{ account.currentBindingUser || '-' }}</td>
        </tr>
        <tr v-if="accounts.length === 0">
          <td colspan="10" class="empty">暂无券商账户</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const accounts = ref<any[]>([])

const loadAccounts = async () => {
  accounts.value = await request.get<any[]>('/admin/broker-accounts')
}

const goCreate = () => router.push({ name: 'BrokerAccountCreate' })
const goEdit = (account: any) => router.push({ name: 'BrokerAccountEdit', params: { id: account.id } })
const goView = (account: any) => router.push({ name: 'BrokerAccountView', params: { id: account.id } })

const remove = async (account: any) => {
  if (!confirm(`确认停用券商账户 ${account.accountNo} ?`)) return
  await request.delete<void>(`/admin/broker-accounts/${account.id}`)
  await loadAccounts()
}

onMounted(loadAccounts)
</script>

<style scoped>
.page {
  padding: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
}

th, td {
  padding: 10px 12px;
  border: 1px solid #e6e6e6;
  text-align: left;
}

th {
  background: #f7f8fb;
}

.ops {
  display: flex;
  gap: 6px;
}

.icon {
  width: 20px;
  height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 4px;
  background: #f0f0f0;
}

.icon:hover {
  background: #d2d7e2;
}

button.primary {
  padding: 8px 14px;
  border: none;
  border-radius: 6px;
  background: #42b883;
  color: #fff;
  cursor: pointer;
}

button.primary:hover {
  background: #369b6f;
}

.empty {
  text-align: center;
  color: #888;
  padding: 18px 0;
}

.binding-status {
  font-weight: 500;
  color: #42b883;
}
</style>
