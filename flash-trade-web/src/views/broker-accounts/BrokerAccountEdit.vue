<template>
  <div class="form-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>编辑券商账户</h2>
          <el-button @click="cancel">返回</el-button>
        </div>
      </template>

      <el-skeleton v-if="loading" :rows="7" animated />
      <el-alert
        v-else-if="loadError"
        :title="loadError"
        type="error"
        show-icon
        :closable="false"
      >
        <el-button type="primary" plain @click="loadData">重试</el-button>
      </el-alert>
      <template v-else>
        <el-alert
          v-if="form.apiEnvironment === 'LIVE'"
          title="LIVE 账户不能通过管理端编辑。请使用受控的后端运维流程；前端限制不能替代服务端保护。"
          type="error"
          show-icon
          :closable="false"
          class="environment-notice"
        />
        <el-alert
          v-else
          title="账户环境保持不变；该页面不会将账户切换到 LIVE。"
          type="info"
          show-icon
          :closable="false"
          class="environment-notice"
        />

        <el-form :model="form" label-width="150px">
          <el-form-item label="所属用户">
            <el-select v-model="form.userId" placeholder="请选择用户" filterable :disabled="isLive">
              <el-option
                v-for="user in users"
                :key="user.userId"
                :label="`${user.username} (ID: ${user.userId})`"
                :value="user.userId"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="券商名称">
            <el-input v-model="form.brokerName" maxlength="64" :disabled="isLive" />
          </el-form-item>
          <el-form-item label="账户号">
            <el-input :model-value="maskedAccountNo" readonly />
          </el-form-item>
          <el-form-item label="账户类型">
            <el-select v-model="form.accountType" :disabled="isLive">
              <el-option label="现金账户" value="CASH" />
              <el-option label="保证金账户" value="MARGIN" />
            </el-select>
          </el-form-item>
          <el-form-item label="账户状态">
            <el-select v-model="form.status" :disabled="isLive">
              <el-option label="启用" value="ACTIVE" />
              <el-option label="停用" value="SUSPENDED" />
              <el-option label="关闭" value="CLOSED" />
            </el-select>
          </el-form-item>
          <el-form-item label="开发者账号 ID">
            <el-input-number
              v-model="form.devAccountId"
              :min="1"
              :precision="0"
              controls-position="right"
              :disabled="isLive"
            />
          </el-form-item>
          <el-form-item label="API 环境">
            <el-tag :type="isLive ? 'danger' : 'success'">{{ form.apiEnvironment }}</el-tag>
          </el-form-item>
          <el-form-item>
            <el-button @click="cancel">返回</el-button>
            <el-button
              type="primary"
              :loading="saving"
              :disabled="isLive"
              @click="save"
            >
              保存
            </el-button>
          </el-form-item>
        </el-form>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

interface UserOption {
  userId: number
  username: string
}

interface AccountResponse {
  userId: number
  brokerName: string
  accountNo: string
  accountType: string
  status: string
  devAccountId: number | null
  apiEnvironment: string
}

const route = useRoute()
const router = useRouter()
const users = ref<UserOption[]>([])
const loading = ref(true)
const loadError = ref('')
const saving = ref(false)
const form = ref({
  userId: null as number | null,
  brokerName: '',
  accountNo: '',
  accountType: 'CASH',
  status: 'ACTIVE',
  devAccountId: null as number | null,
  apiEnvironment: 'SANDBOX'
})

const isLive = computed(() => form.value.apiEnvironment === 'LIVE')
const maskedAccountNo = computed(() => {
  const accountNo = form.value.accountNo
  return accountNo.length <= 4 ? '••••' : `${'•'.repeat(Math.min(accountNo.length - 4, 8))}${accountNo.slice(-4)}`
})

const loadData = async () => {
  const id = Number(route.params.id)
  if (!Number.isInteger(id) || id <= 0) {
    loadError.value = '账户 ID 无效'
    loading.value = false
    return
  }

  loading.value = true
  loadError.value = ''
  try {
    const [userResult, account] = await Promise.all([
      request.get<UserOption[]>('/users'),
      request.get<AccountResponse>(`/admin/broker-accounts/${id}`)
    ])
    if (!Array.isArray(userResult)) throw new Error('用户列表格式无效')
    users.value = userResult
    form.value = {
      userId: account.userId,
      brokerName: account.brokerName,
      accountNo: account.accountNo,
      accountType: account.accountType || 'CASH',
      status: account.status || 'ACTIVE',
      devAccountId: account.devAccountId,
      apiEnvironment: account.apiEnvironment || 'SANDBOX'
    }
  } catch (error) {
    loadError.value = `加载账户信息失败：${String(error)}`
  } finally {
    loading.value = false
  }
}

const save = async () => {
  if (saving.value || isLive.value) return
  const id = Number(route.params.id)
  const brokerName = form.value.brokerName.trim()
  if (!Number.isInteger(id) || id <= 0 || !form.value.userId || !brokerName) {
    ElMessage.warning('请检查账户和用户信息')
    return
  }

  saving.value = true
  try {
    await request.put(`/admin/broker-accounts/${id}`, {
      ...form.value,
      brokerName,
      apiEnvironment: form.value.apiEnvironment
    })
    ElMessage.success('券商账户已更新')
    await router.push({ name: 'BrokerAccountList' })
  } catch (error) {
    ElMessage.warning(`更新请求结果未确认，请返回列表刷新核对：${String(error)}`)
  } finally {
    saving.value = false
  }
}

const cancel = () => router.push({ name: 'BrokerAccountList' })

onMounted(loadData)
</script>

<style scoped>
.form-page {
  max-width: 760px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
}

.environment-notice {
  margin-bottom: 18px;
}
</style>
