<template>
  <div class="page">
    <button class="primary" @click="goCreate">➕ 新建用户</button>
    <table>
      <thead>
        <tr>
          <th class="ops-col"></th>
          <th>用户名</th>
          <th>账户名</th>
          <th>所属券商</th>
          <th>状态</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="u in users" :key="u.userId">
          <td class="ops">
            <i class="icon view" @click="goView(u)" title="预览"></i>
            <i class="icon edit" @click="goEdit(u)" title="编辑"></i>
            <i class="icon pwd" @click="changePwd(u)" title="改密码"></i>
            <i
              class="icon toggle"
              :class="u.deleteFlag === 0 ? 'pause' : 'play'"
              @click="toggle(u)"
              :title="u.deleteFlag === 0 ? '停用' : '启用'"
            ></i>
          </td>
          <td>{{ u.username }}</td>
          <td>{{ u.accountUserId }}</td>
          <td>{{ u.brokerName }}</td>
          <td>
            <span :class="u.deleteFlag === 0 ? 'on' : 'off'">
              {{ u.deleteFlag === 0 ? '已启用' : '已停用' }}
            </span>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '@/utils/request'

interface UserForm {
  username: string
  accountUserId: string
  brokerName: string
  deleteFlag: string
}

const router = useRouter()
const users = ref<any[]>([])

const load = async () => {
  const res = await request.get("/users")
  const list = res as UserForm[]
  users.value = list.map(u => ({ ...u, deleteFlag: Number(u.deleteFlag) }))
}

const goCreate = () => router.push({ name: 'UserCreate' })
const goEdit = (u: any) => router.push({ name: 'UserEdit', params: { id: u.userId } })
const goView = (u: any) => router.push({ name: 'UserView', params: { id: u.userId } })

const toggle = async (u: any) => {
  const nextFlag = u.deleteFlag === 1 ? 0 : 1
  if (!confirm(u.deleteFlag === 0 ? `确认停用用户 ${u.username}?` : `确认启用用户 ${u.username}?`)) return
  const oldFlag = u.deleteFlag
  try {
    await request.post("/users/status", { userId: u.userId, deleteFlag: nextFlag })
    u.deleteFlag = nextFlag
  } catch {
    u.deleteFlag = oldFlag
    alert("更新失败")
  }
}

const changePwd = async (u: any) => {
  const pwd = prompt("请输入新密码")
  if (pwd) await request.post(`/users/${u.userId}/password`, { password: pwd })
}

onMounted(load)
</script>

<style scoped>
/* ...和之前一览页类似，按钮和图标 CSS ... */
</style>
