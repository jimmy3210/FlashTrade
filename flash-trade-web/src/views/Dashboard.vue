<template>
  <div class="dashboard-container">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <h2 class="logo">风控管理</h2>
        <button class="logout-btn" :disabled="logoutLoading" @click="logout">
          {{ logoutLoading ? '正在退出…' : '退出' }}
        </button>
      </div>
      <nav aria-label="管理导航">
        <ul class="nav-list">
          <li>
            <router-link to="/dashboard/overview" class="nav-link">运营概览</router-link>
          </li>
          <li>
            <router-link to="/dashboard/positions" class="nav-link">在仓管理</router-link>
          </li>
          <li>
            <router-link to="/dashboard/orders" class="nav-link">订单管理</router-link>
          </li>
          <li>
            <router-link to="/dashboard/users" class="nav-link">用户管理</router-link>
          </li>
          <li>
            <router-link to="/dashboard/companies" class="nav-link">公司管理</router-link>
          </li>
          <li>
            <router-link to="/dashboard/broker-accounts" class="nav-link">券商账户</router-link>
          </li>
          <li>
            <router-link to="/dashboard/user-broker-binding" class="nav-link">账户绑定</router-link>
          </li>
          <li>
            <router-link to="/dashboard/brokers" class="nav-link">券商配置</router-link>
          </li>
          <li>
            <router-link to="/dashboard/risk" class="nav-link">账户风控</router-link>
          </li>
          <li>
            <router-link to="/dashboard/audit-logs" class="nav-link">审计日志</router-link>
          </li>
          <li>
            <router-link to="/dashboard/scope-mappings" class="nav-link">Scope 映射</router-link>
          </li>
        </ul>
      </nav>
    </aside>

    <!-- 主内容 -->
    <main class="main-content">
      <header class="main-header">
        <h1>{{ pageTitle }}</h1>
      </header>
      <section class="content">
        <router-view />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useRouter, useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { revokeCurrentSession } from "@/utils/session";

const router = useRouter();
const route = useRoute();
const logoutLoading = ref(false);

// 页面标题
const pageTitle = computed(() => route.meta.title || "风控管理");

// Logout
const logout = async () => {
  if (logoutLoading.value) return;
  logoutLoading.value = true;
  let revokeFailed = false;
  try {
    await revokeCurrentSession();
  } catch {
    revokeFailed = true;
  } finally {
    logoutLoading.value = false;
  }

  if (revokeFailed) {
    ElMessage.warning("本地登录状态已清除，但服务端撤销未确认；请确认网络正常后重新登录。");
  } else {
    ElMessage.success("已安全退出");
  }
  await router.replace("/login");
};
</script>

<style scoped>
/* 整体布局 */
.dashboard-container {
  display: flex;
  min-height: 100vh;
  font-family: "Segoe UI", Tahoma, Geneva, Verdana, sans-serif;
}

/* 侧边栏 */
.sidebar {
  width: 240px;
  background: #2c3e50;
  color: #fff;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}

.sidebar .logo {
  font-size: 24px;
  font-weight: bold;
}

.logout-btn {
  padding: 5px 10px;
  background-color: #e74c3c;
  border: none;
  border-radius: 4px;
  color: white;
  cursor: pointer;
  font-size: 12px;
  transition: background 0.3s;
}
.logout-btn:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}
.logout-btn:hover {
  background-color: #c0392b;
}

.nav-list {
  list-style: none;
  padding: 0;
}

.nav-list li {
  margin-bottom: 20px;
}

.nav-link {
  display: block;
  padding: 10px 15px;
  color: #fff;
  text-decoration: none;
  border-radius: 6px;
  transition: background 0.3s, color 0.3s;
}
.nav-link:hover {
  background: #34495e;
}
.nav-link.router-link-active {
  background: #42b883;
  color: #fff;
  font-weight: bold;
}

/* 主内容 */
.main-content {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  background: #f5f6fa;
  display: flex;
  flex-direction: column;
}

.main-header {
  background: #fff;
  padding: 20px 30px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.main-header h1 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

/* 内容区域 */
.content {
  flex: 1;
  padding: 30px;
}

/* 统一卡片样式 */
.card {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}

/* 表格统一样式 */
.table {
  width: 100%;
  border-collapse: collapse;
}

.table th, .table td {
  padding: 12px 15px;
  border-bottom: 1px solid #eee;
  text-align: left;
}

.table th {
  background: #f0f0f0;
  font-weight: 600;
}

.table tr:hover {
  background: #f9f9f9;
}

.btn {
  padding: 6px 12px;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  color: #fff;
  background-color: #42b883;
  transition: background 0.3s;
}
.btn:hover {
  background-color: #369b6f;
}

@media (max-width: 760px) {
  .dashboard-container {
    flex-direction: column;
  }

  .sidebar {
    width: auto;
    padding: 12px 16px;
  }

  .sidebar-header {
    margin-bottom: 12px;
  }

  .sidebar .logo {
    margin: 0;
    font-size: 20px;
  }

  .nav-list {
    display: flex;
    gap: 6px;
    margin: 0;
    overflow-x: auto;
    padding: 0 0 4px;
  }

  .nav-list li {
    flex: 0 0 auto;
    margin: 0;
  }

  .nav-link {
    padding: 8px 12px;
    white-space: nowrap;
  }

  .main-header {
    padding: 16px;
  }

  .content {
    padding: 16px;
  }
}
</style>
