<template>
  <div class="page">
    <div class="toolbar">
      <h2>用户-账户绑定管理</h2>
    </div>

    <div class="section">
      <label>选择用户：</label>
      <select v-model.number="selectedUserId" @change="loadUserBinding" class="select">
        <option :value="null">-- 请选择用户 --</option>
        <option v-for="user in users" :key="user.id" :value="user.id">
          {{ user.username }} (ID: {{ user.id }})
        </option>
      </select>
    </div>

    <div v-if="selectedUserId && currentBinding" class="section">
      <h3>当前绑定的账户</h3>
      <div class="binding-info">
        <p><strong>用户：</strong> {{ currentBinding.username }}</p>
        <p><strong>券商：</strong> {{ currentBinding.brokerName }} ({{ currentBinding.brokerCode }})</p>
        <p><strong>账户号：</strong> {{ currentBinding.accountNo }}</p>
        <p><strong>账户类型：</strong> {{ currentBinding.accountType }}</p>
        <p><strong>API 环境：</strong> 
          <span class="badge" :class="currentBinding.apiEnvironment">
            {{ currentBinding.apiEnvironment }}
          </span>
        </p>
        <p><strong>状态：</strong> {{ currentBinding.status }}</p>
      </div>
    </div>

    <div v-if="selectedUserId" class="section">
      <h3>切换为其他账户</h3>
      <table v-if="availableAccounts.length > 0">
        <thead>
          <tr>
            <th>操作</th>
            <th>券商</th>
            <th>账户号</th>
            <th>类型</th>
            <th>状态</th>
            <th>API 环境</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="account in availableAccounts" :key="account.id">
            <td>
              <button 
                v-if="account.id !== currentBinding?.id"
                class="btn-bind"
                @click="bindAccount(account)"
              >
                切换
              </button>
              <span v-else class="current">当前</span>
            </td>
            <td>{{ account.brokerName }}</td>
            <td>{{ account.accountNo }}</td>
            <td>{{ account.accountType }}</td>
            <td>{{ account.status }}</td>
            <td>
              <span class="badge" :class="account.apiEnvironment">
                {{ account.apiEnvironment }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="empty">该用户无可用的券商账户</p>
    </div>

    <div v-if="selectedUserId && !currentBinding" class="section warning">
      <p>该用户未绑定任何券商账户，请选择一个账户进行绑定。</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import request from '@/utils/request'

const selectedUserId = ref<number | null>(null)
const users = ref<any[]>([])
const allAccounts = ref<any[]>([])
const currentBinding = ref<any>(null)

const availableAccounts = computed(() => {
  // 只显示状态为 ACTIVE 的账户
  return allAccounts.value.filter(a => a.status === 'ACTIVE')
})

const loadUsers = async () => {
  users.value = await request.get('/admin/users')
}

const loadAllAccounts = async () => {
  allAccounts.value = await request.get('/admin/broker-accounts')
}

const loadUserBinding = async () => {
  if (!selectedUserId.value) {
    currentBinding.value = null
    return
  }

  try {
    currentBinding.value = await request.get(`/admin/broker-accounts/user/${selectedUserId.value}/current`)
  } catch (error) {
    // 用户未绑定账户
    currentBinding.value = null
  }
}

const bindAccount = async (account: any) => {
  if (!selectedUserId.value) return
  
  if (!confirm(`确认将用户 ${users.value.find(u => u.id === selectedUserId.value)?.username} 绑定到账户 ${account.accountNo} ?`)) {
    return
  }

  try {
    await request.post(
      `/admin/broker-accounts/user/${selectedUserId.value}/bind/${account.id}`,
      {}
    )
    await loadUserBinding()
    alert('账户绑定成功')
  } catch (error) {
    alert('账户绑定失败：' + (error as any).message)
  }
}

onMounted(async () => {
  await loadUsers()
  await loadAllAccounts()
})
</script>

<style scoped>
.page {
  padding: 16px;
  max-width: 1000px;
}

.toolbar {
  margin-bottom: 20px;
}

.toolbar h2 {
  margin: 0;
  font-size: 24px;
}

.section {
  background: #fff;
  padding: 16px;
  margin-bottom: 16px;
  border-radius: 6px;
  border: 1px solid #e6e6e6;
}

.section h3 {
  margin-top: 0;
  margin-bottom: 12px;
  font-size: 16px;
  color: #333;
}

.select {
  padding: 8px 12px;
  border: 1px solid #d0d7e3;
  border-radius: 4px;
  font-size: 14px;
  min-width: 300px;
}

.binding-info {
  background: #f7f8fb;
  padding: 12px;
  border-radius: 4px;
  line-height: 1.8;
}

.binding-info p {
  margin: 6px 0;
  font-size: 14px;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 10px 12px;
  border: 1px solid #e6e6e6;
  text-align: left;
}

th {
  background: #f7f8fb;
  font-weight: 600;
  font-size: 13px;
}

td {
  font-size: 13px;
}

.btn-bind {
  padding: 6px 12px;
  border: none;
  border-radius: 4px;
  background: #42b883;
  color: #fff;
  cursor: pointer;
  font-size: 12px;
  transition: background 0.2s;
}

.btn-bind:hover {
  background: #369b6f;
}

.current {
  color: #42b883;
  font-weight: 600;
  font-size: 12px;
}

.badge {
  display: inline-block;
  padding: 3px 8px;
  border-radius: 3px;
  font-size: 12px;
  font-weight: 500;
}

.badge.LIVE {
  background: #e8f5e9;
  color: #2e7d32;
}

.badge.SANDBOX {
  background: #fff3e0;
  color: #e65100;
}

.empty {
  text-align: center;
  color: #888;
  padding: 20px;
  font-size: 14px;
}

.warning {
  background: #fff9e6;
  border: 1px solid #ffd933;
  color: #b87803;
}
</style>
