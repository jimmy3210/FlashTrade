<template>
  <div class="view-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>券商账户详情</h2>
          <el-button @click="back">返回</el-button>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="7" animated />
      <div v-else-if="loadError">
        <el-alert
          title="账户详情加载失败"
          :description="loadError"
          type="error"
          show-icon
          :closable="false"
        />
        <el-button type="primary" plain class="retry-button" @click="loadAccount">重试</el-button>
      </div>
      <template v-else-if="account">
        <el-alert
          v-if="account.apiEnvironment === 'LIVE'"
          title="LIVE 账户不能通过管理端进行编辑。"
          type="warning"
          show-icon
          :closable="false"
          class="environment-notice"
        />
        <el-descriptions :column="2" border>
          <el-descriptions-item label="账户 ID">{{ account.id }}</el-descriptions-item>
          <el-descriptions-item label="用户 ID">{{ account.userId }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ account.username || '—' }}</el-descriptions-item>
          <el-descriptions-item label="券商">{{ account.brokerName || account.brokerCode || '—' }}</el-descriptions-item>
          <el-descriptions-item label="账户号">{{ maskAccount(account.accountNo) }}</el-descriptions-item>
          <el-descriptions-item label="账户类型">{{ account.accountType }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ account.status }}</el-descriptions-item>
          <el-descriptions-item label="API 环境">
            <el-tag :type="account.apiEnvironment === 'SANDBOX' ? 'success' : 'warning'">
              {{ account.apiEnvironment }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="开发者账号 ID">{{ account.devAccountId ?? '—' }}</el-descriptions-item>
        </el-descriptions>
        <div class="actions">
          <el-button v-if="account.apiEnvironment === 'SANDBOX'" type="primary" @click="edit">编辑</el-button>
          <el-button @click="back">返回列表</el-button>
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'

interface BrokerAccount {
  id: number
  userId: number
  username: string | null
  brokerName: string | null
  brokerCode: string | null
  accountNo: string
  accountType: string
  status: string
  apiEnvironment: string
  devAccountId: number | null
}

const route = useRoute()
const router = useRouter()
const account = ref<BrokerAccount | null>(null)
const loading = ref(false)
const loadError = ref('')

const maskAccount = (accountNo: string) =>
  accountNo.length <= 4 ? '••••' : `${'•'.repeat(Math.min(accountNo.length - 4, 8))}${accountNo.slice(-4)}`

const loadAccount = async () => {
  const id = Number(route.params.id)
  if (!Number.isInteger(id) || id <= 0) {
    loadError.value = '账户 ID 无效'
    return
  }
  loading.value = true
  loadError.value = ''
  try {
    account.value = await request.get<BrokerAccount>(`/admin/broker-accounts/${id}`)
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : String(error)
  } finally {
    loading.value = false
  }
}

const back = () => router.push({ name: 'BrokerAccountList' })
const edit = () => {
  if (account.value?.apiEnvironment !== 'SANDBOX') return
  router.push({ name: 'BrokerAccountEdit', params: { id: account.value.id } })
}

onMounted(loadAccount)
</script>

<style scoped>
.view-page {
  max-width: 760px;
  margin: 0 auto;
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

.actions {
  justify-content: flex-start;
  margin-top: 20px;
}

.environment-notice,
.retry-button {
  margin-bottom: 16px;
}
</style>
