import { createRouter, createWebHistory } from "vue-router";
import request from '@/utils/request.ts'
import {
    clearAccessToken,
    getAccessToken,
    refreshWebSession
} from '@/utils/session'

const routes = [
    {
        path: "/login",
        name: "Login",
        component: () => import("@/views/Login.vue"),
        meta: { title: "风控管理 - 登录" }
    },
    {
        path: "/access-denied",
        name: "AccessDenied",
        component: () => import("@/views/AdminAccessStatus.vue"),
        meta: { title: "无管理权限" }
    },
    {
        path: "/session-check",
        name: "SessionCheck",
        component: () => import("@/views/AdminAccessStatus.vue"),
        meta: { title: "无法验证管理权限" }
    },
    {
        path: "/dashboard",
        name: "Dashboard",
        component: () => import("@/views/Dashboard.vue"),
        redirect: "/dashboard/overview",
        meta: { title: "风控管理", requiresAuth: true },
        children: [
            {
                path: "overview",
                name: "AdminOverview",
                component: () => import("@/views/AdminOverview.vue"),
                meta: { title: "运营概览", requiresAuth: true }
            },
            {
                path: "positions",
                name: "PositionManage",
                component: () => import("@/views/PositionManage.vue"),
                meta: { title: "在仓管理", requiresAuth: true }
            },
            {
                path: "brokers",
                name: "BrokerConfig",
                component: () => import("@/views/BrokerConfig.vue"),
                meta: { title: "券商配置", requiresAuth: true }
            },
            {
                path: "risk",
                name: "RiskManagement",
                component: () => import("@/views/RiskManagement.vue"),
                meta: { title: "账户风控", requiresAuth: true }
            },
            {
                path: 'companies',
                name: 'CompanyList',
                component: () => import("@/views/companies/CompanyList.vue"),
                meta: { title: '公司管理', requiresAuth: true }
            },
            {
                path: 'companies/create',
                name: 'CompanyCreate',
                component: () => import("@/views/companies/CompanyCreate.vue"),
                meta: { title: '新建公司', requiresAuth: true }
            },
            {
                path: 'companies/:id/edit',
                name: 'CompanyEdit',
                component: () => import("@/views/companies/CompanyEdit.vue"),
                meta: { title: '编辑公司', requiresAuth: true }
            },
            {
                path: 'companies/:id/view',
                name: 'CompanyView',
                component: () => import("@/views/companies/CompanyView.vue"),
                meta: { title: '查看公司', requiresAuth: true }
            },
            {
                path: 'users',
                name: 'UserList',
                component: () => import("@/views/users/UserList.vue"),
                meta: { title: '用户管理', requiresAuth: true }
            },
            {
                path: 'users/create',
                name: 'UserCreate',
                component: () => import("@/views/users/UserCreate.vue"),
                meta: { title: '新建用户', requiresAuth: true, requiresPlatformAdmin: true }
            },
            {
                path: 'users/:id/edit',
                name: 'UserEdit',
                component: () => import("@/views/users/UserEdit.vue"),
                meta: { title: '编辑用户', requiresAuth: true, requiresPlatformAdmin: true }
            },
            {
                path: 'users/:id/view',
                name: 'UserView',
                component: () => import("@/views/users/UserView.vue"),
                meta: { title: '预览用户', requiresAuth: true }
            },
            {
                path: 'broker-accounts',
                name: 'BrokerAccountList',
                component: () => import("@/views/broker-accounts/BrokerAccountList.vue"),
                meta: { title: '券商账户管理', requiresAuth: true }
            },
            {
                path: 'broker-accounts/create',
                name: 'BrokerAccountCreate',
                component: () => import("@/views/broker-accounts/BrokerAccountCreate.vue"),
                meta: { title: '新建券商账户', requiresAuth: true }
            },
            {
                path: 'broker-accounts/:id/edit',
                name: 'BrokerAccountEdit',
                component: () => import("@/views/broker-accounts/BrokerAccountEdit.vue"),
                meta: { title: '编辑券商账户', requiresAuth: true }
            },
            {
                path: 'broker-accounts/:id/view',
                name: 'BrokerAccountView',
                component: () => import("@/views/broker-accounts/BrokerAccountView.vue"),
                meta: { title: '查看券商账户', requiresAuth: true }
            },
            {
                path: 'user-broker-binding',
                name: 'UserBrokerBinding',
                component: () => import("@/views/broker-accounts/UserBrokerBinding.vue"),
                meta: { title: '用户账户绑定', requiresAuth: true }
            },
            {
                path: 'orders',
                name: 'OrderManagement',
                component: () => import("@/views/OrderManagement.vue"),
                meta: { title: '订单管理', requiresAuth: true }
            },
            {
                path: 'audit-logs',
                name: 'AuditLogDashboard',
                component: () => import("@/views/AuditLogDashboard.vue"),
                meta: { title: '审计日志监控', requiresAuth: true }
            },
            {
                path: 'scope-mappings',
                name: 'ScopeMapping',
                component: () => import("@/views/ScopeMapping.vue"),
                meta: { title: '公司 Scope 映射', requiresAuth: true, requiresPlatformAdmin: true }
            }
        ]
    },
    // catch-all 重定向到登录
    {
        path: "/:catchAll(.*)",
        redirect: "/login",
    },
];

const router = createRouter({
    history: createWebHistory("/quicktrade/admin/"),
    routes
});

// 全局路由守卫
router.beforeEach(async (to) => {
    if (to.meta.requiresAuth) {
        if (!getAccessToken()) {
            try {
                await refreshWebSession()
            } catch {
                clearAccessToken()
                return { name: 'Login', query: { redirect: to.fullPath } }
            }
        }

        try {
            const session = await request.get<{ roles: string[] }>('/api/v1/auth/me')
            if (!Array.isArray(session?.roles)) {
                return { name: 'SessionCheck', query: { redirect: to.fullPath } }
            }
            const isPlatformAdmin = session.roles.includes('ROLE_SUPER_ADMIN')
            if (to.meta.requiresPlatformAdmin === true && !isPlatformAdmin) {
                return { name: 'AccessDenied' }
            }
            if (!session.roles.some(role =>
                role === 'ROLE_ADMIN'
                || role === 'ROLE_COMPANY_ADMIN'
                || role === 'ROLE_SUPER_ADMIN')) {
                return { name: 'AccessDenied' }
            }
        } catch (error: unknown) {
            const status = typeof error === 'object' && error !== null && 'status' in error
                ? error.status
                : undefined
            const code = typeof error === 'object' && error !== null && 'code' in error
                ? error.code
                : undefined
            if (status === 401 || code === 'UNAUTHORIZED') {
                clearAccessToken()
                return { name: 'Login', query: { redirect: to.fullPath } }
            }
            return { name: 'SessionCheck', query: { redirect: to.fullPath } }
        }
    }

    return true
})

// 动态修改浏览器 title
router.afterEach((to) => {
    document.title = (to.meta.title as string) || "风控管理";
});

export default router;
