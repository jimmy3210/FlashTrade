<template>
  <main class="status-page">
    <el-card class="status-card">
      <el-result
        :icon="isUnavailable ? 'warning' : 'error'"
        :title="isUnavailable ? '暂时无法验证管理权限' : '当前账号没有管理端权限'"
        :sub-title="isUnavailable
          ? '无法确认当前会话的权限。请检查服务连接后重试；不会因此放行管理页面。'
          : '登录成功不代表拥有管理权限。请使用已获授权的管理账号，或联系平台管理员。'"
      >
        <template #extra>
          <div class="status-actions">
            <el-button v-if="isUnavailable" type="primary" :loading="retrying" @click="retry">
              重试验证
            </el-button>
            <el-button v-else type="primary" @click="signOut">返回登录</el-button>
          </div>
        </template>
      </el-result>
    </el-card>
  </main>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { revokeCurrentSession } from '@/utils/session'

const route = useRoute()
const router = useRouter()
const isUnavailable = computed(() => route.name === 'SessionCheck')
const retrying = ref(false)

const retry = async () => {
  if (retrying.value) return

  const redirect = route.query.redirect
  const resolved = typeof redirect === 'string'
      && redirect.startsWith('/')
      && !redirect.startsWith('//')
      && !redirect.includes('\\')
    ? router.resolve(redirect)
    : null
  const target = resolved?.matched.some(record => record.meta.requiresAuth)
    ? resolved.fullPath
    : '/dashboard/overview'

  retrying.value = true
  try {
    await router.replace(target)
  } finally {
    retrying.value = false
  }
}

const signOut = async () => {
  try {
    await revokeCurrentSession()
  } catch {
    ElMessage.warning('本地登录状态已清除，但服务端撤销未确认。')
  }
  await router.replace({ name: 'Login' })
}
</script>

<style scoped>
.status-page {
  display: grid;
  min-height: 100vh;
  place-items: center;
  padding: 24px;
  background: #f5f6fa;
}

.status-card {
  width: min(100%, 560px);
}
</style>
