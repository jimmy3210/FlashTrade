<template>
  <div class="form-page">
    <h2>编辑用户</h2>
    <form @submit.prevent="save">
      <input v-model="form.username" placeholder="用户名" required />
      <input v-model="form.accountUserId" placeholder="账户名" required />
      <input v-model="form.brokerName" placeholder="所属券商" required />
      <input v-model="form.password" placeholder="密码（留空不修改）" />
      <button type="submit">保存</button>
      <button @click="cancel" type="button">取消</button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()

interface UserForm {
  username: string
  accountUserId: string
  brokerName: string
  password: string
}

const form = ref<UserForm>({
  username: '',
  accountUserId: '',
  brokerName: '',
  password: ''
})

const loadUser = async () => {
  const id = route.params.id
  const data = await request.get(`/users/${id}`)
  form.value.username = data.username
  form.value.accountUserId = data.accountUserId
  form.value.brokerName = data.brokerName
}

const save = async () => {
  const id = route.params.id
  await request.put(`/users/${id}`, form.value)
  router.push({ name: 'UserList' })
}

const cancel = () => router.push({ name: 'UserList' })

onMounted(loadUser)
</script>
