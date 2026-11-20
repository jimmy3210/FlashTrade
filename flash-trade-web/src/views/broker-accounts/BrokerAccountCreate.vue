<template>
  <div class="form-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>新建券商账户</h2>
          <el-button @click="cancel">返回</el-button>
        </div>
      </template>

      <el-alert
        title="新账户固定使用 SANDBOX 环境。管理端前端限制不能替代后端环境校验。"
        type="warning"
        show-icon
        :closable="false"
        class="environment-notice"
      />

      <el-alert
        v-if="loadError"
        :title="loadError"
        type="error"
        show-icon
        :closable="false"
        class="environment-notice"
      >
        <el-button type="primary" plain @click="loadUsers">重试</el-button>
      </el-alert>

      <el-form
        v-else
        :model="form"
        label-width="150px"
        @submit.prevent="save"
      >
        <el-form-item label="所属用户" required>
          <el-select v-model="form.userId" placeholder="请选择用户" filterable>
            <el-option
              v-for="user in users"
              :key="user.userId"
              :label="`${user.username} (ID: ${user.userId})`"
              :value="user.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="券商名称" required>
          <el-input v-model="form.brokerName" maxlength="64" placeholder="券商标识" />
        </el-form-item>
        <el-form-item label="账户号" required>
          <el-input v-model="form.accountNo" maxlength="128" autocomplete="off" />
        </el-form-item>
        <el-form-item label="账户类型">
          <el-select v-model="form.accountType">
            <el-option label="现金账户" value="CASH" />
            <el-option label="保证金账户" value="MARGIN" />
          </el-select>
        </el-form-item>
        <el-form-item label="账户状态">
          <el-select v-model="form.status">
            <el-option label="启用" value="ACTIVE" />
            <el-option label="停用" value="SUSPENDED" />
            <el-option label="关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
        <el-form-item label="开发者账号 ID">
          <el-input-number v-model="form.devAccountId" :min="1" :precision="0" controls-position="right" />
          <span class="field-hint">可选；留空时不绑定开发者账号</span>
        </el-form-item>
        <el-form-item label="API 环境">
          <el-tag type="success">SANDBOX</el-tag>
        </el-form-item>
        <el-form-item>
          <el-button @click="cancel">取消</el-button>
          <el-button type="primary" :loading="saving" :disabled="!users.length" @click="save">
            保存
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

interface UserOption {
  userId: number
  username: string
}

const router = useRouter()
const users = ref<UserOption[]>([])
const loadError = ref('')
const saving = ref(false)
const form = ref({
  userId: null as number | null,
  brokerName: '',
  accountNo: '',
  accountType: 'CASH',
  status: 'ACTIVE',
  devAccountId: null as number | null
})

const loadUsers = async () => {
  loadError.value = ''
  try {
    const result = await request.get<UserOption[]>('/users')
    if (!Array.isArray(result)) throw new Error('用户列表格式无效')
    users.value = result
  } catch (error) {
    loadError.value = `加载用户列表失败：${String(error)}`
  }
}

const save = async () => {
  if (saving.value) return
  const brokerName = form.value.brokerName.trim()
  const accountNo = form.value.accountNo.trim()
  if (!form.value.userId || !brokerName || !accountNo) {
    ElMessage.warning('请完整填写用户、券商名称和账户号')
    return
  }

  saving.value = true
  try {
    await request.post('/admin/broker-accounts', {
      ...form.value,
      brokerName,
      accountNo,
      apiEnvironment: 'SANDBOX'
    })
    ElMessage.success('券商账户已创建')
    await router.push({ name: 'BrokerAccountList' })
  } catch (error) {
    ElMessage.warning(`创建请求结果未确认，请先刷新账户列表核对后再提交：${String(error)}`)
  } finally {
    saving.value = false
  }
}

const cancel = () => router.push({ name: 'BrokerAccountList' })

onMounted(loadUsers)
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

.field-hint {
  margin-left: 12px;
  color: #64748b;
  font-size: 13px;
}
</style>
