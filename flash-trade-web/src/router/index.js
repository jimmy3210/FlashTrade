import { createRouter, createWebHistory } from "vue-router";
import Login from "@/views/Login.vue";
import Dashboard from "@/views/Dashboard.vue";
//import UserManage from "@/views/UserManage.vue";
import PositionManage from "@/views/PositionManage.vue";
import BrokerConfig from '@/views/BrokerConfig.vue';
import UserList from '@/views/users/UserList.vue';
import UserCreate from '@/views/users/UserCreate.vue';
import UserEdit from '@/views/users/UserEdit.vue';
import UserView from '@/views/users/UserView.vue';
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
    const token = localStorage.getItem('token');
    if (to.meta.requiresAuth && !token) {
        // 没登录，跳回 login
        next('/login');
    }
    else {
        next(); // 正常访问
    }
});
// 动态修改浏览器 title
router.afterEach((to) => {
    document.title = to.meta.title || "风控管理";
});
export default router;
//# sourceMappingURL=index.js.map