<template>
  <div class="audit-dashboard">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="title">🔍 审计日志监控</span>
          <div class="header-right">
            <el-button type="primary" :loading="refreshing" @click="refreshAllTabs">🔄 刷新全部</el-button>
          </div>
        </div>
      </template>

      <!-- Tab 切换 -->
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <!-- 操作日志 Tab -->
        <el-tab-pane label="📝 操作日志" name="operations">
          <el-form :model="operationFilters" label-width="100px" class="filter-form">
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="操作员ID">
                  <el-input
                    v-model.number="operationFilters.operatorId"
                    placeholder="输入操作员ID"
                    clearable
                    type="number"
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="操作类型">
                  <el-input v-model="operationFilters.operation" placeholder="输入操作类型" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="目标类型">
                  <el-input v-model="operationFilters.targetType" placeholder="输入目标类型" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="目标ID">
                  <el-input v-model.number="operationFilters.targetId" placeholder="输入目标ID" clearable type="number" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="结果状态码">
                  <el-input v-model.number="operationFilters.httpStatus" placeholder="输入 HTTP 状态码" clearable type="number" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="操作时间">
                  <el-date-picker
                    v-model="operationFilters.timeRange"
                    type="datetimerange"
                    value-format="YYYY-MM-DDTHH:mm:ss"
                    range-separator="至"
                    start-placeholder="开始时间"
                    end-placeholder="结束时间"
                    class="time-range"
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item>
                  <el-button type="primary" @click="searchOperationLogs">🔍 查询</el-button>
                  <el-button @click="resetOperationFilters">↻ 重置</el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>

          <el-table
            :data="operationLogs"
            stripe
            style="width: 100%"
            :loading="operationLoading"
          >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="operatorUsername" label="操作员" width="100" />
            <el-table-column prop="operation" label="操作类型" width="120" />
            <el-table-column prop="targetType" label="目标类型" width="100" />
            <el-table-column prop="targetId" label="目标ID" width="100" />
            <el-table-column prop="changeSummary" label="变更摘要" show-overflow-tooltip />
            <el-table-column prop="requestIp" label="IP地址" width="130" />
            <el-table-column prop="createdAt" label="操作时间" width="170" />
          </el-table>

          <el-pagination
            v-model:current-page="operationPage"
            v-model:page-size="operationPageSize"
            :page-sizes="[10, 20, 50]"
            :total="operationTotal"
            layout="total, sizes, prev, pager, next"
            style="margin-top: 20px; text-align: right"
            @change="loadOperationLogs"
          />
        </el-tab-pane>

        <!-- 登录日志 Tab -->
        <el-tab-pane label="🔐 登录日志" name="logins">
          <el-form :model="loginFilters" label-width="100px" class="filter-form">
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="用户名">
                  <el-input
                    v-model="loginFilters.username"
                    placeholder="输入用户名"
                    clearable
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="登录状态">
                  <el-select
                    v-model="loginFilters.loginStatus"
                    placeholder="选择状态"
                    clearable
                  >
                    <el-option label="全部" value="" />
                    <el-option label="成功" value="SUCCESS" />
                    <el-option label="失败" value="FAILED" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="请求 IP">
                  <el-input
                    v-model="loginFilters.requestIp"
                    placeholder="输入完整 IP"
                    clearable
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="登录时间">
                  <el-date-picker
                    v-model="loginFilters.timeRange"
                    type="datetimerange"
                    value-format="YYYY-MM-DDTHH:mm:ss"
                    range-separator="至"
                    start-placeholder="开始时间"
                    end-placeholder="结束时间"
                    class="time-range"
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item>
                  <el-button type="primary" @click="searchLoginLogs">🔍 查询</el-button>
                  <el-button @click="resetLoginFilters">↻ 重置</el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>

          <el-table
            :data="loginLogs"
            stripe
            style="width: 100%"
            :loading="loginLoading"
          >
            <el-table-column prop="username" label="用户名" width="120" />
            <el-table-column prop="loginStatus" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.loginStatus === 'SUCCESS' ? 'success' : 'danger'">
                  {{ row.loginStatus === 'SUCCESS' ? '✅ 成功' : '❌ 失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="failedReason" label="失败原因" show-overflow-tooltip />
            <el-table-column prop="requestIp" label="IP地址" width="130" />
            <el-table-column prop="loginMethod" label="登录方式" width="100" />
            <el-table-column prop="loginTime" label="登录时间" width="170" />
          </el-table>

          <el-pagination
            v-model:current-page="loginPage"
            v-model:page-size="loginPageSize"
            :page-sizes="[10, 20, 50]"
            :total="loginTotal"
            layout="total, sizes, prev, pager, next"
            style="margin-top: 20px; text-align: right"
            @change="loadLoginLogs"
          />
        </el-tab-pane>

        <!-- API 调用日志 Tab -->
        <el-tab-pane label="📊 API调用日志" name="api-calls">
          <el-form :model="apiCallFilters" label-width="100px" class="filter-form">
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="6">
                <el-form-item label="用户ID">
                  <el-input
                    v-model.number="apiCallFilters.userId"
                    placeholder="输入用户ID"
                    clearable
                    type="number"
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="API 端点">
                  <el-input v-model="apiCallFilters.endpoint" placeholder="输入完整 API 端点" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="响应状态码">
                  <el-input v-model.number="apiCallFilters.responseCode" placeholder="输入 HTTP 状态码" clearable type="number" />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <el-form-item label="仅错误调用">
                  <el-checkbox v-model="apiCallFilters.errorFlag">是</el-checkbox>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <el-form-item label="仅关键操作">
                  <el-checkbox v-model="apiCallFilters.isCritical">是</el-checkbox>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="调用时间">
                  <el-date-picker
                    v-model="apiCallFilters.timeRange"
                    type="datetimerange"
                    value-format="YYYY-MM-DDTHH:mm:ss"
                    range-separator="至"
                    start-placeholder="开始时间"
                    end-placeholder="结束时间"
                    class="time-range"
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="6">
                <el-form-item>
                  <el-button type="primary" @click="searchApiCallLogs">🔍 查询</el-button>
                  <el-button @click="resetApiCallFilters">↻ 重置</el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>

          <el-table
            :data="apiCallLogs"
            stripe
            style="width: 100%"
            :loading="apiCallLoading"
          >
            <el-table-column prop="username" label="用户" width="100" />
            <el-table-column prop="httpMethod" label="方法" width="80" />
            <el-table-column prop="endpoint" label="端点" show-overflow-tooltip />
            <el-table-column prop="responseCode" label="状态码" width="80">
              <template #default="{ row }">
                <el-tag :type="row.responseCode < 400 ? 'success' : 'danger'">
                  {{ row.responseCode }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="responseTimeMs" label="响应时间(ms)" width="120" />
            <el-table-column prop="isCritical" label="关键" width="60">
              <template #default="{ row }">
                <el-tag v-if="row.isCritical" type="warning">⭐ 是</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="时间" width="170" />
          </el-table>

          <el-pagination
            v-model:current-page="apiCallPage"
            v-model:page-size="apiCallPageSize"
            :page-sizes="[10, 20, 50]"
            :total="apiCallTotal"
            layout="total, sizes, prev, pager, next"
            style="margin-top: 20px; text-align: right"
            @change="loadApiCallLogs"
          />
        </el-tab-pane>

        <!-- 系统事件日志 Tab -->
        <el-tab-pane label="🚨 系统事件" name="events">
          <el-form :model="eventFilters" label-width="100px" class="filter-form">
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="严重级别">
                  <el-select
                    v-model="eventFilters.severity"
                    placeholder="选择级别"
                    clearable
                  >
                    <el-option label="全部" value="" />
                    <el-option label="🔵 低" value="LOW" />
                    <el-option label="🟡 中" value="MEDIUM" />
                    <el-option label="🟠 高" value="HIGH" />
                    <el-option label="🔴 严重" value="CRITICAL" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="仅未解决">
                  <el-checkbox
                    v-model="eventFilters.unresolved"
                  >是</el-checkbox>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="事件类型">
                  <el-input v-model="eventFilters.eventType" placeholder="输入事件类型" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="组件">
                  <el-input v-model="eventFilters.component" placeholder="输入组件名称" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="发生时间">
                  <el-date-picker
                    v-model="eventFilters.timeRange"
                    type="datetimerange"
                    value-format="YYYY-MM-DDTHH:mm:ss"
                    range-separator="至"
                    start-placeholder="开始时间"
                    end-placeholder="结束时间"
                    class="time-range"
                  />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item>
                  <el-button type="primary" @click="searchSystemEventLogs">🔍 查询</el-button>
                  <el-button @click="resetEventFilters">↻ 重置</el-button>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>

          <el-table
            :data="systemEventLogs"
            stripe
            style="width: 100%"
            :loading="eventLoading"
          >
            <el-table-column prop="id" label="事件 ID" width="90" />
            <el-table-column prop="eventType" label="事件类型" width="150" show-overflow-tooltip />
            <el-table-column prop="severity" label="级别" width="100">
              <template #default="{ row }">
                <el-tag :type="getSeverityType(row.severity)">
                  {{ getSeverityLabel(row.severity) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="component" label="组件" width="120" />
            <el-table-column prop="message" label="消息" show-overflow-tooltip />
            <el-table-column prop="relatedResource" label="相关资源" width="150" />
            <el-table-column prop="resolved" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.resolved ? 'success' : 'warning'">
                  {{ row.resolved ? '✅ 已解决' : '⏳ 待解决' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createdAt" label="时间" width="170" />
            <el-table-column label="详情" width="90" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="viewSystemEvent(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            v-model:current-page="eventPage"
            v-model:page-size="eventPageSize"
            :page-sizes="[10, 20, 50]"
            :total="eventTotal"
            layout="total, sizes, prev, pager, next"
            style="margin-top: 20px; text-align: right"
            @change="loadSystemEventLogs"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog
      v-model="eventDetailVisible"
      title="系统事件详情"
      width="min(680px, calc(100vw - 32px))"
    >
      <el-descriptions v-if="selectedEvent" :column="1" border>
        <el-descriptions-item label="事件 ID">{{ selectedEvent.id }}</el-descriptions-item>
        <el-descriptions-item label="事件类型">{{ selectedEvent.eventType || '—' }}</el-descriptions-item>
        <el-descriptions-item label="严重级别">
          <el-tag :type="getSeverityType(selectedEvent.severity)">
            {{ getSeverityLabel(selectedEvent.severity) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="组件">{{ selectedEvent.component || '—' }}</el-descriptions-item>
        <el-descriptions-item label="事件消息">
          <div class="event-message">{{ selectedEvent.message }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="相关资源">{{ selectedEvent.relatedResource || '—' }}</el-descriptions-item>
        <el-descriptions-item label="关联用户 ID">{{ selectedEvent.relatedUserId ?? '—' }}</el-descriptions-item>
        <el-descriptions-item label="关联订单 ID">{{ selectedEvent.relatedOrderId ?? '—' }}</el-descriptions-item>
        <el-descriptions-item label="发生时间">{{ selectedEvent.createdAt || '—' }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">
          {{ selectedEvent.resolved ? '已解决' : '待解决' }}
        </el-descriptions-item>
        <el-descriptions-item label="解决时间">{{ selectedEvent.resolvedAt || '—' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="eventDetailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const activeTab = ref('operations')
const refreshing = ref(false)

interface OperationLog {
  id: number
  operatorUsername: string
  operation: string
  targetType?: string
  targetId?: number | string
  changeSummary?: string
  requestIp?: string
  createdAt?: string
}

interface LoginLog {
  username: string
  loginStatus: string
  failedReason?: string
  requestIp?: string
  loginMethod?: string
  loginTime?: string
}

interface ApiCallLog {
  username?: string
  httpMethod: string
  endpoint: string
  responseCode: number
  responseTimeMs?: number
  isCritical: boolean
  createdAt?: string
}

interface SystemEventLog {
  id: number
  eventType?: string
  severity: string
  component?: string
  message: string
  relatedUserId?: number
  relatedOrderId?: number
  relatedResource?: string
  resolved: boolean
  createdAt?: string
  resolvedAt?: string
}

// 操作日志
const operationLogs = ref<OperationLog[]>([])
const operationLoading = ref(false)
const operationPage = ref(1)
const operationPageSize = ref(20)
const operationTotal = ref(0)
const operationFilters = ref({
  operatorId: undefined as number | undefined,
  operation: '',
  targetType: '',
  targetId: undefined as number | undefined,
  httpStatus: undefined as number | undefined,
  timeRange: [] as string[]
})

// 登录日志
const loginLogs = ref<LoginLog[]>([])
const loginLoading = ref(false)
const loginPage = ref(1)
const loginPageSize = ref(20)
const loginTotal = ref(0)
const loginFilters = ref({ username: '', loginStatus: '', requestIp: '', timeRange: [] as string[] })

// API 调用日志
const apiCallLogs = ref<ApiCallLog[]>([])
const apiCallLoading = ref(false)
const apiCallPage = ref(1)
const apiCallPageSize = ref(20)
const apiCallTotal = ref(0)
const apiCallFilters = ref<{
  userId?: number
  endpoint: string
  responseCode?: number
  isCritical: boolean
  errorFlag: boolean
  timeRange: string[]
}>({
  endpoint: '',
  isCritical: false,
  errorFlag: false,
  timeRange: []
})

// 系统事件日志
const systemEventLogs = ref<SystemEventLog[]>([])
const eventLoading = ref(false)
const eventPage = ref(1)
const eventPageSize = ref(20)
const eventTotal = ref(0)
const eventDetailVisible = ref(false)
const selectedEvent = ref<SystemEventLog | null>(null)
const eventFilters = ref({
  severity: '',
  unresolved: false,
  eventType: '',
  component: '',
  timeRange: [] as string[]
})

interface PageResponse<T> {
  content: T[]
  totalElements: number
}

const errorMessage = (error: unknown) => {
  if (typeof error === 'object' && error !== null && 'error' in error) {
    const body = error.error
    if (typeof body === 'object' && body !== null && 'message' in body && typeof body.message === 'string') {
      return body.message
    }
  }
  if (error instanceof Error && error.message) return error.message
  return String(error)
}

const timeRangeParams = (range?: string[] | null) => ({
  startTime: range?.[0] || undefined,
  endTime: range?.[1] || undefined
})

// 加载操作日志
const loadOperationLogs = async () => {
  operationLoading.value = true
  try {
    const params = {
      page: operationPage.value,
      pageSize: operationPageSize.value,
      operatorId: operationFilters.value.operatorId || undefined,
      operation: operationFilters.value.operation.trim() || undefined,
      targetType: operationFilters.value.targetType.trim() || undefined,
      targetId: operationFilters.value.targetId || undefined,
      httpStatus: operationFilters.value.httpStatus || undefined,
      ...timeRangeParams(operationFilters.value.timeRange)
    }
    const response = await request.get<PageResponse<OperationLog>>('/admin/audit/operations', { params })
    operationLogs.value = response.content || []
    operationTotal.value = response.totalElements || 0
    return true
  } catch (error) {
    ElMessage.error(`加载操作日志失败：${errorMessage(error)}`)
    return false
  } finally {
    operationLoading.value = false
  }
}

// 加载登录日志
const loadLoginLogs = async () => {
  loginLoading.value = true
  try {
    const params = {
      page: loginPage.value,
      pageSize: loginPageSize.value,
      username: loginFilters.value.username.trim() || undefined,
      loginStatus: loginFilters.value.loginStatus || undefined,
      requestIp: loginFilters.value.requestIp.trim() || undefined,
      ...timeRangeParams(loginFilters.value.timeRange)
    }
    const response = await request.get<PageResponse<LoginLog>>('/admin/audit/logins', { params })
    loginLogs.value = response.content || []
    loginTotal.value = response.totalElements || 0
    return true
  } catch (error) {
    ElMessage.error(`加载登录日志失败：${errorMessage(error)}`)
    return false
  } finally {
    loginLoading.value = false
  }
}

// 加载 API 调用日志
const loadApiCallLogs = async () => {
  apiCallLoading.value = true
  try {
    const params = {
      page: apiCallPage.value,
      pageSize: apiCallPageSize.value,
      userId: apiCallFilters.value.userId || undefined,
      endpoint: apiCallFilters.value.endpoint.trim() || undefined,
      responseCode: apiCallFilters.value.responseCode || undefined,
      isCritical: apiCallFilters.value.isCritical || undefined,
      errorFlag: apiCallFilters.value.errorFlag || undefined,
      ...timeRangeParams(apiCallFilters.value.timeRange)
    }
    const response = await request.get<PageResponse<ApiCallLog>>('/admin/audit/api-calls', { params })
    apiCallLogs.value = response.content || []
    apiCallTotal.value = response.totalElements || 0
    return true
  } catch (error) {
    ElMessage.error(`加载 API 日志失败：${errorMessage(error)}`)
    return false
  } finally {
    apiCallLoading.value = false
  }
}

// 加载系统事件日志
const loadSystemEventLogs = async () => {
  eventLoading.value = true
  try {
    const params = {
      page: eventPage.value,
      pageSize: eventPageSize.value,
      severity: eventFilters.value.severity || undefined,
      unresolved: eventFilters.value.unresolved || undefined,
      eventType: eventFilters.value.eventType.trim() || undefined,
      component: eventFilters.value.component.trim() || undefined,
      ...timeRangeParams(eventFilters.value.timeRange)
    }
    const response = await request.get<PageResponse<SystemEventLog>>('/admin/audit/events', { params })
    systemEventLogs.value = response.content || []
    eventTotal.value = response.totalElements || 0
    return true
  } catch (error) {
    ElMessage.error(`加载系统事件日志失败：${errorMessage(error)}`)
    return false
  } finally {
    eventLoading.value = false
  }
}

// Tab 切换时加载数据
const onTabChange = (tab: string) => {
  switch (tab) {
    case 'operations':
      loadOperationLogs()
      break
    case 'logins':
      loadLoginLogs()
      break
    case 'api-calls':
      loadApiCallLogs()
      break
    case 'events':
      loadSystemEventLogs()
      break
  }
}

// 重置过滤器
const searchOperationLogs = () => {
  operationPage.value = 1
  loadOperationLogs()
}

const searchLoginLogs = () => {
  loginPage.value = 1
  loadLoginLogs()
}

const searchApiCallLogs = () => {
  apiCallPage.value = 1
  loadApiCallLogs()
}

const searchSystemEventLogs = () => {
  eventPage.value = 1
  loadSystemEventLogs()
}

const resetOperationFilters = () => {
  operationFilters.value = {
    operatorId: undefined,
    operation: '',
    targetType: '',
    targetId: undefined,
    httpStatus: undefined,
    timeRange: []
  }
  operationPage.value = 1
  loadOperationLogs()
}

const resetLoginFilters = () => {
  loginFilters.value = { username: '', loginStatus: '', requestIp: '', timeRange: [] }
  loginPage.value = 1
  loadLoginLogs()
}

const resetEventFilters = () => {
  eventFilters.value = {
    severity: '',
    unresolved: false,
    eventType: '',
    component: '',
    timeRange: []
  }
  eventPage.value = 1
  loadSystemEventLogs()
}

const resetApiCallFilters = () => {
  apiCallFilters.value = {
    userId: undefined,
    endpoint: '',
    responseCode: undefined,
    isCritical: false,
    errorFlag: false,
    timeRange: []
  }
  apiCallPage.value = 1
  loadApiCallLogs()
}

const viewSystemEvent = (event: SystemEventLog) => {
  selectedEvent.value = event
  eventDetailVisible.value = true
}

// 刷新所有 Tab
const refreshAllTabs = async () => {
  if (refreshing.value) return
  refreshing.value = true
  try {
    const results = await Promise.all([
      loadOperationLogs(),
      loadLoginLogs(),
      loadApiCallLogs(),
      loadSystemEventLogs()
    ])
    if (results.every(Boolean)) ElMessage.success('全部审计日志已刷新')
  } finally {
    refreshing.value = false
  }
}

// 获取严重级别类型
const getSeverityType = (severity: string): string => {
  const types: Record<string, string> = {
    LOW: 'info',
    MEDIUM: 'warning',
    HIGH: 'warning',
    CRITICAL: 'danger'
  }
  return types[severity] || 'info'
}

// 获取严重级别标签
const getSeverityLabel = (severity: string): string => {
  const labels: Record<string, string> = {
    LOW: '🔵 低',
    MEDIUM: '🟡 中',
    HIGH: '🟠 高',
    CRITICAL: '🔴 严重'
  }
  return labels[severity] || severity
}

// 初始加载
loadOperationLogs()
</script>

<style scoped>
.audit-dashboard {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: bold;
}

.header-right {
  display: flex;
  gap: 10px;
}

.filter-form {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.time-range {
  width: 100%;
}

.event-message {
  max-height: 240px;
  overflow: auto;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}
</style>
