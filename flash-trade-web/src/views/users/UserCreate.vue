<template>
  <div class="form-page">
    <h2>新建用户</h2>
    <form @submit.prevent="save">
      <input v-model="form.username" placeholder="用户名" required />
      <input v-model="form.accountUserId" placeholder="账户名" required />
      <input v-model="form.brokerName" placeholder="所属券商" required />
      <input v-model="form.password" placeholder="密码" required />
      <button type="submit">保存</button>
      <button @click="cancel" type="button">取消</button>
    </form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

const router = useRouter()

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

const save = async () => {
  await request.post('/users', form.value)
  router.push({ name: 'UserList' })
}

const cancel = () => router.push({ name: 'UserList' })
</script>
