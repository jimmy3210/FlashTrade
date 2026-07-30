<template>
  <div class="view-page" v-if="form">
    <h2>用户信息</h2>
    <p>用户名：{{ form.username }}</p>
    <p>账户名：{{ form.accountUserId }}</p>
    <p>所属券商：{{ form.brokerName }}</p>
    <p>状态：{{ form.deleteFlag === 0 ? '已启用' : '已停用' }}</p>
    <button @click="back">返回</button>
    <button @click="edit">编辑</button>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const form = ref<any>(null)

onMounted(async () => {
  const id = route.params.id
  form.value = await request.get(`/users/${id}`)
})

const back = () => router.push({ name: 'UserList' })
const edit = () => {
  const id = route.params.id
  router.push({ name: 'UserEdit', params: { id } })
}
</script>
