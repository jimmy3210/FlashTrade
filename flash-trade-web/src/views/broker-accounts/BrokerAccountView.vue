<template>
  <div class="view-page" v-if="account">
    <h2>券商账户详情</h2>
    <p><strong>ID：</strong>{{ account.id }}</p>
    <p><strong>用户 ID：</strong>{{ account.userId }}</p>
    <p><strong>用户名：</strong>{{ account.username }}</p>
    <p><strong>券商：</strong>{{ account.brokerName }}</p>
    <p><strong>账户号：</strong>{{ account.accountNo }}</p>
    <p><strong>账户类型：</strong>{{ account.accountType }}</p>
    <p><strong>状态：</strong>{{ account.status }}</p>
    <p><strong>API 环境：</strong>{{ account.apiEnvironment }}</p>
    <p><strong>开发者账号 ID：</strong>{{ account.devAccountId || '-' }}</p>
    <button class="primary" @click="back">返回</button>
    <button class="secondary" @click="edit">编辑</button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()
const account = ref<any>(null)

const loadAccount = async () => {
  const id = route.params.id
  account.value = await request.get<any>(`/admin/broker-accounts/${id}`)
}

const back = () => router.push({ name: 'BrokerAccountList' })
const edit = () => {
  const id = route.params.id
  router.push({ name: 'BrokerAccountEdit', params: { id } })
}

onMounted(loadAccount)
</script>

<style scoped>
.view-page {
  padding: 16px;
  max-width: 640px;
}

p {
  margin: 8px 0;
}

button {
  margin-right: 12px;
  padding: 10px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

button.primary {
  background: #42b883;
  color: white;
}

button.secondary {
  background: #999;
  color: white;
}
</style>
