<template>
  <div class="page">
    <div class="toolbar">
      <h2>券商 API 模式与 URL 配置</h2>
    </div>

    <table>
      <thead>
        <tr>
          <th>券商</th>
          <th>模式</th>
          <th>Mock</th>
          <th>API Base URL</th>
          <th>更新时间</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="config in configs" :key="config.brokerName">
          <td>{{ config.brokerName }}</td>
          <td>
            <select v-model="editable[config.brokerName].mode">
              <option value="PRODUCTION">Production</option>
              <option value="SANDBOX">Sandbox</option>
              <option value="MOCK">Mock</option>
            </select>
          </td>
          <td>
            <input type="checkbox" v-model="editable[config.brokerName].mockEnabled" />
          </td>
          <td>
            <input
              type="text"
              v-model="editable[config.brokerName].baseUrl"
              placeholder="留空使用默认 URL"
            />
          </td>
          <td>{{ config.updatedAt || '-' }}</td>
          <td>
            <button class="primary" @click="save(config.brokerName)">保存</button>
          </td>
        </tr>
        <tr v-if="configs.length === 0">
          <td colspan="6" class="empty">暂无配置数据</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted } from 'vue'
import request from '@/utils/request'

interface BrokerConfig {
  brokerName: string
  mode: string
  baseUrl: string | null
  mockEnabled: boolean
  updatedAt: string | null
}

const configs = ref<BrokerConfig[]>([])
const editable = reactive<Record<string, Partial<BrokerConfig>>>({})

const loadConfigs = async () => {
  const list = await request.get<BrokerConfig[]>('/admin/brokers/configs')
  configs.value = list || []
  configs.value.forEach(config => {
    editable[config.brokerName] = {
      mode: config.mode || 'PRODUCTION',
      baseUrl: config.baseUrl || '',
      mockEnabled: config.mockEnabled
    }
  })
}

const save = async (brokerName: string) => {
  const current = editable[brokerName]
  if (!current) return
  await request.put<BrokerConfig>(`/admin/brokers/configs/${brokerName}`, {
    mode: current.mode,
    baseUrl: current.baseUrl,
    mockEnabled: current.mockEnabled
  })
  await loadConfigs()
  alert(`券商 ${brokerName} 配置已保存`)
}

onMounted(loadConfigs)
</script>

<style scoped>
.page {
  padding: 16px;
}

.toolbar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
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

input[type="text"] {
  width: 100%;
  padding: 6px 8px;
  border: 1px solid #d3d6dd;
  border-radius: 4px;
}

button.primary {
  padding: 6px 12px;
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
</style>
