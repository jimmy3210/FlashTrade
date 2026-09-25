import { createRouter, createWebHistory } from "vue-router";
import Login from "@/views/Login.vue";
import Dashboard from "@/views/Dashboard.vue";
//import UserManage from "@/views/UserManage.vue";
import PositionManage from "@/views/PositionManage.vue";
import BrokerConfig from '@/views/BrokerConfig.vue'
import UserList from '@/views/users/UserList.vue'
import UserCreate from '@/views/users/UserCreate.vue'
import UserEdit from '@/views/users/UserEdit.vue'
import UserView from '@/views/users/UserView.vue'
import BrokerAccountList from '@/views/broker-accounts/BrokerAccountList.vue'
import BrokerAccountCreate from '@/views/broker-accounts/BrokerAccountCreate.vue'
import BrokerAccountEdit from '@/views/broker-accounts/BrokerAccountEdit.vue'
import BrokerAccountView from '@/views/broker-accounts/BrokerAccountView.vue'
import UserBrokerBinding from '@/views/broker-accounts/UserBrokerBinding.vue'
import OrderManagement from '@/views/OrderManagement.vue'
import AuditLogDashboard from '@/views/AuditLogDashboard.vue'
import CompanyList from '@/views/companies/CompanyList.vue'
import CompanyCreate from '@/views/companies/CompanyCreate.vue'
import CompanyEdit from '@/views/companies/CompanyEdit.vue'
import CompanyView from '@/views/companies/CompanyView.vue'

const routes = [
    {
        path: "/login",
        name: "Login",
        component: Login,
        meta: { title: "风控管理 - 登录" }
    },
    {
        path: "/dashboard",
        name: "Dashboard",
        component: Dashboard,
        redirect: "/dashboard/positions",
        meta: { title: "风控管理", requiresAuth: true },
        children: [
            {
                path: "positions",
                name: "PositionManage",
                component: PositionManage,
                meta: { title: "在仓管理", requiresAuth: true }
            },
            {
                path: "brokers",
                name: "BrokerConfig",
                component: BrokerConfig,
                meta: { title: "券商配置", requiresAuth: true }
            },
            {
                path: 'companies',
                name: 'CompanyList',
                component: CompanyList,
                meta: { title: '公司管理', requiresAuth: true }
            },
            {
                path: 'companies/create',
                name: 'CompanyCreate',
                component: CompanyCreate,
                meta: { title: '新建公司', requiresAuth: true }
            },
            {
                path: 'companies/:id/edit',
                name: 'CompanyEdit',
                component: CompanyEdit,
                meta: { title: '编辑公司', requiresAuth: true }
            },
            {
                path: 'companies/:id/view',
                name: 'CompanyView',
                component: CompanyView,
                meta: { title: '查看公司', requiresAuth: true }
            },
            {
                path: 'users',
                name: 'UserList',
                component: UserList,
                meta: { title: '用户管理', requiresAuth: true }
            },
            {
                path: 'users/create',
                name: 'UserCreate',
                component: UserCreate,
                meta: { title: '新建用户', requiresAuth: true }
            },
            {
                path: 'users/:id/edit',
                name: 'UserEdit',
                component: UserEdit,
                meta: { title: '编辑用户', requiresAuth: true }
            },
            {
                path: 'users/:id/view',
                name: 'UserView',
                component: UserView,
                meta: { title: '预览用户', requiresAuth: true }
            },
            {
                path: 'broker-accounts',
                name: 'BrokerAccountList',
                component: BrokerAccountList,
                meta: { title: '券商账户管理', requiresAuth: true }
            },
            {
                path: 'broker-accounts/create',
                name: 'BrokerAccountCreate',
                component: BrokerAccountCreate,
                meta: { title: '新建券商账户', requiresAuth: true }
            },
            {
                path: 'broker-accounts/:id/edit',
                name: 'BrokerAccountEdit',
                component: BrokerAccountEdit,
                meta: { title: '编辑券商账户', requiresAuth: true }
            },
            {
                path: 'broker-accounts/:id/view',
                name: 'BrokerAccountView',
                component: BrokerAccountView,
                meta: { title: '查看券商账户', requiresAuth: true }
            },
            {
                path: 'user-broker-binding',
                name: 'UserBrokerBinding',
                component: UserBrokerBinding,
                meta: { title: '用户账户绑定', requiresAuth: true }
            },
            {
                path: 'orders',
                name: 'OrderManagement',
                component: OrderManagement,
                meta: { title: '订单管理', requiresAuth: true }
            },
            {
                path: 'audit-logs',
                name: 'AuditLogDashboard',
                component: AuditLogDashboard,
                meta: { title: '审计日志监控', requiresAuth: true }
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
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')

    if (to.meta.requiresAuth && !token) {
        // 没登录，跳回 login
        next('/login')
    } else {
        next() // 正常访问
    }
})

// 动态修改浏览器 title
router.afterEach((to) => {
    document.title = (to.meta.title as string) || "风控管理";
});

export default router;
