<template>
  <div class="page">
    <div class="toolbar">
      <button class="primary" @click="openCreate">➕ 新建用户</button>
    </div>

    <table>
      <thead>
        <tr>
          <th class="ops-col"></th>
          <th>用户名</th>
          <th>账户名</th>
          <th>状态</th>
        </tr>
      </thead>

      <tbody>
        <tr v-for="u in users" :key="u.userId">
          <!-- 操作列 -->
          <td class="ops">
            <i class="icon edit" @click="edit(u)" title="编辑"></i>
            <i class="icon pwd" @click="changePwd(u)" title="改密码"></i>
            <i
              class="icon toggle"
              :class="u.deleteFlag === 0 ? 'pause' : 'play'"
              @click="toggle(u)"
              :title="u.deleteFlag === 0 ? '停用账户' : '启用账户'"
            ></i>
          </td>

          <td>{{ u.userId }}</td>
          <td>{{ u.accountUserId }}</td>
          <td>
            <span :class="u.deleteFlag === 0 ? 'on' : 'off'">
              {{ u.deleteFlag === 0 ? '● 启用' : '● 停用' }}
            </span>
          </td>
        </tr>

        <tr v-if="users.length === 0">
          <td colspan="4" class="empty">暂无用户数据</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import request from "@/utils/request";

const router = useRouter();
const users = ref<any[]>([]);

/* ---------- 加载 ---------- */
const load = async () => {
  users.value = await request.get("/users") || [];
};

/* ---------- 跳转 ---------- */
const openCreate = () => {
  router.push("/users/create");
};

const edit = (u: any) => {
  router.push(`/users/edit/${u.userId}`);
};

/* ---------- 启用 / 停用 ---------- */
const toggle = async (u: any) => {
  const isDisabled = u.deleteFlag === 1;
  const nextFlag = isDisabled ? 0 : 1;
  const oldFlag = u.deleteFlag;

  const msg = isDisabled
    ? `确认启用用户 ${u.userId}？`
    : `确认停用用户 ${u.userId}？`;

  if (!confirm(msg)) return;

  // 乐观更新
  u.deleteFlag = nextFlag;

  try {
    await request.post("/users/status", {
      userId: u.userId,
      deleteFlag: nextFlag
    });
  } catch (e) {
    u.deleteFlag = oldFlag;
    alert("状态更新失败，请重试");
  }
};

/* ---------- 改密码 ---------- */
const changePwd = async (u: any) => {
  const pwd = prompt(`请输入 ${u.userId} 的新密码`);
  if (!pwd) return;

  await request.post(`/users/${u.userId}/password`, {
    password: pwd
  });
};

onMounted(load);
</script>

<style scoped>
.page {
  padding: 16px;
  min-width: 900px; /* 防止挤掉 sidebar */
}

.toolbar {
  margin-bottom: 12px;
}

/* ---------- 操作列 ---------- */
.ops-col {
  width: 90px;
}

.ops {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.icon {
  width: 18px;
  height: 18px;
  cursor: pointer;
  opacity: 0.8;
  user-select: none;
}

.icon:hover {
  opacity: 1;
  transform: scale(1.15);
}

/* 图标（纯 CSS，无依赖） */
.icon.edit::before { content: "✏️"; }
.icon.pwd::before { content: "🔒"; }
.icon.pause::before { content: "⏸️"; }
.icon.play::before { content: "▶️"; }

/* ---------- 状态 ---------- */
.on {
  color: #2ecc71;
  font-weight: 600;
}

.off {
  color: #e74c3c;
  font-weight: 600;
}

/* ---------- 表格 ---------- */
table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  background: #fff;
}

th, td {
  padding: 6px 10px;
  border-bottom: 1px solid #eee;
  white-space: nowrap;
}

th {
  background: #f8f9fa;
  text-align: left;
}

.empty {
  text-align: center;
  color: #999;
  padding: 16px;
}
</style>
