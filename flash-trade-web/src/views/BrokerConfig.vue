<template>
  <div class="broker-config-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>券商运行配置</h2>
          <el-button :loading="loading" :disabled="loading" @click="loadConfigs">刷新</el-button>
        </div>
      </template>

      <el-alert
        class="readonly-notice"
        :title="modeNoticeTitle"
        description="运行模式由后端部署允许集合与有效配置决定；页面只展示服务端返回的模式，不提供模式切换，也不回显 API 地址或凭证。"
        :type="modeNoticeType"
        show-icon
        :closable="false"
      />

      <div v-if="loadError" class="load-error">
        <el-alert
          title="券商配置加载失败"
          :description="loadError"
          type="error"
          show-icon
          :closable="false"
        />
        <el-button type="primary" plain @click="loadConfigs">重试</el-button>
      </div>

      <el-table
        v-else
        v-loading="loading"
        :data="configs"
        row-key="brokerName"
        stripe
        empty-text="暂无券商配置"
      >
        <el-table-column prop="brokerName" label="券商" min-width="130" />
        <el-table-column label="运行模式" min-width="130">
          <template #default="{ row }: { row: BrokerConfig }">
            <el-tag :type="modeTagType(row)">
              {{ modeLabel(row.mode) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Mock 状态" width="130">
          <template #default="{ row }: { row: BrokerConfig }">
            {{ row.mockEnabled ? '已启用' : '未启用' }}
          </template>
        </el-table-column>
        <el-table-column label="券商 API 请求" min-width="220">
          <template #default="{ row }: { row: BrokerConfig }">
            {{ brokerRequestDescription(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="配置更新时间" min-width="180">
          <template #default="{ row }: { row: BrokerConfig }">
            {{ row.updatedAt || '—' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import request from '@/utils/request'

interface BrokerConfig {
  brokerName: string
  mode: string
  mockEnabled: boolean
  updatedAt: string | null
}

const configs = ref<BrokerConfig[]>([])
const loading = ref(false)
const loadError = ref('')
const hasLoaded = ref(false)

const modeNoticeTitle = computed(() => {
  if (loadError.value) return '无法确认券商当前运行模式'
  if (loading.value) return hasLoaded.value ? '正在刷新券商有效模式' : '正在读取券商有效模式'
  if (!hasLoaded.value || configs.value.length === 0) return '暂无可确认的券商运行模式'
  if (configs.value.some(config => !isKnownMode(config.mode))) {
    return '收到未知券商模式，无法确认请求行为'
  }
  if (configs.value.some(config => (config.mode === 'MOCK') !== config.mockEnabled)) {
    return '券商模式与 Mock 标志不一致，请检查服务端配置'
  }
  if (configs.value.every(config => config.mode === 'MOCK' && config.mockEnabled)) {
    return '当前所有券商均使用 Mock，不会调用外部 API。'
  }
  return '存在非 Mock 模式；实际外部请求仍由服务端部署策略控制'
})

const modeNoticeType = computed<'success' | 'warning' | 'error'>(() => {
  if (loadError.value) return 'error'
  if (loading.value || !hasLoaded.value || configs.value.length === 0) return 'warning'
  if (configs.value.some(config => !isKnownMode(config.mode))) return 'error'
  if (configs.value.some(config => (config.mode === 'MOCK') !== config.mockEnabled)) return 'error'
  return configs.value.every(config => config.mode === 'MOCK' && config.mockEnabled)
    ? 'success'
    : 'warning'
})

const isKnownMode = (mode: string) => mode === 'MOCK' || mode === 'SANDBOX' || mode === 'PRODUCTION'

const isBrokerConfig = (value: unknown): value is BrokerConfig => {
  if (typeof value !== 'object' || value === null) return false
  return 'brokerName' in value
    && typeof value.brokerName === 'string'
    && 'mode' in value
    && typeof value.mode === 'string'
    && 'mockEnabled' in value
    && typeof value.mockEnabled === 'boolean'
    && 'updatedAt' in value
    && (typeof value.updatedAt === 'string' || value.updatedAt === null)
}

const brokerRequestDescription = (config: BrokerConfig) => {
  if (!isKnownMode(config.mode)) return '未知模式，无法确认请求行为'
  if ((config.mode === 'MOCK') !== config.mockEnabled) return '模式与 Mock 标志不一致'
  if (config.mode === 'MOCK') return '本地/隔离 Mock，不发起外部请求'
  return '非 Mock；请求行为受服务端部署策略控制'
}

const modeLabel = (mode: string) => {
  if (isKnownMode(mode)) return mode
  return '未知模式'
}

const modeTagType = (config: BrokerConfig) => {
  if (!isKnownMode(config.mode)) return 'danger'
  if ((config.mode === 'MOCK') !== config.mockEnabled) return 'danger'
  return config.mode === 'MOCK' ? 'success' : 'warning'
}

const loadConfigs = async () => {
  loading.value = true
  loadError.value = ''
  try {
    const result = await request.get<unknown>('/admin/brokers/configs')
    if (!Array.isArray(result) || !result.every(isBrokerConfig)) {
      throw new Error('服务器返回的券商配置格式无效')
    }
    configs.value = result
    hasLoaded.value = true
  } catch (error: unknown) {
    loadError.value = error instanceof Error && error.message
      ? error.message
      : '请检查连接和管理权限后重试'
  } finally {
    loading.value = false
  }
}

onMounted(loadConfigs)
</script>

<style scoped>
.broker-config-page {
  min-width: 0;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.card-header h2 {
  margin: 0;
  font-size: 18px;
}

.readonly-notice,
.load-error {
  margin-bottom: 16px;
}

.load-error {
  display: flex;
  align-items: center;
  gap: 12px;
}

.load-error :deep(.el-alert) {
  flex: 1;
}
</style>
