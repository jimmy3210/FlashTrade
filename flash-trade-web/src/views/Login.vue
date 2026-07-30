<template>
  <div class="login-container">
    <div class="login-card">
      <!-- Logo -->
      <img class="logo" src="@/assets/daiwa.png" alt="QuickTrade Logo" />
      <h2 class="title">风控管理</h2>
      <div class="input-group">
        <input v-model="userId" type="text" placeholder="用户名" />
      </div>
      <div class="input-group">
        <input v-model="password" type="password" placeholder="密码" />
      </div>
      <button class="login-btn" @click="login">Login</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import request from "@/utils/request";
import MD5 from "crypto-js/md5";

const userId = ref("");
const password = ref("");
const router = useRouter();

onMounted(() => {
  document.title = "风控管理";
});

const login = async () => {
  if (!userId.value || !password.value) return alert("请输入用户名和密码");

  try {
    const data = await request.post("/api/v1/auth/login", {
      username: userId.value,
      password: password.value,
    });

    if (!data || typeof data !== "object" || !data.accessToken) {
      alert("登录失败：未返回访问令牌");
      return;
    }

    localStorage.setItem("token", data.accessToken);
    router.push("/dashboard");
  } catch (err:any) {
    if (err.code === "ECONNABORTED") {
      alert("请求超时，请稍后再试。\n");
      return;
    }

    const message = err?.error?.message || err?.message || "无法连接服务器";
    alert(message);
  }
};
</script>

<style scoped>
/* 容器全屏居中 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  min-width: 100vh;
  background: linear-gradient(135deg, #4f6ef7, #42b883);
}

/* 卡片样式 */
.login-card {
  width: 360px;
  padding: 40px;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  text-align: center;
}

/* Logo 样式 */
.logo {
  width: 100px;   /* 宽度，可根据需要调整 */
  height: 100px;  /* 高度，可根据需要调整 */
  margin-bottom: 20px;
  object-fit: contain; /* 保持图片比例，不拉伸 */
}

/* 标题 */
.title {
  margin-bottom: 30px;
  font-size: 24px;
  color: #333;
}

/* 输入框组 */
.input-group {
  margin-bottom: 20px;
}

.input-group input {
  width: 90%;
  padding: 12px 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  outline: none;
  transition: border-color 0.3s, box-shadow 0.3s;
}

.input-group input:focus {
  border-color: #42b883;
  box-shadow: 0 0 5px rgba(66, 184, 131, 0.5);
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  padding: 12px;
  background: #42b883;
  color: #fff;
  font-size: 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.3s;
}

.login-btn:hover {
  background: #369b6f;
}
</style>
