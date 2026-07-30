import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import request from '@/utils/request';
const router = useRouter();
const users = ref([]);
const load = async () => {
    const res = await request.get("/users");
    const list = res;
    users.value = list.map(u => ({ ...u, deleteFlag: Number(u.deleteFlag) }));
};
const goCreate = () => router.push({ name: 'UserCreate' });
const goEdit = (u) => router.push({ name: 'UserEdit', params: { id: u.userId } });
const goView = (u) => router.push({ name: 'UserView', params: { id: u.userId } });
const toggle = async (u) => {
    const nextFlag = u.deleteFlag === 1 ? 0 : 1;
    if (!confirm(u.deleteFlag === 0 ? `确认停用用户 ${u.username}?` : `确认启用用户 ${u.username}?`))
        return;
    const oldFlag = u.deleteFlag;
    try {
        await request.post("/users/status", { userId: u.userId, deleteFlag: nextFlag });
        u.deleteFlag = nextFlag;
    }
    catch {
        u.deleteFlag = oldFlag;
        alert("更新失败");
    }
};
const changePwd = async (u) => {
    const pwd = prompt("请输入新密码");
    if (pwd)
        await request.post(`/users/${u.userId}/password`, { password: pwd });
};
onMounted(load);
const __VLS_ctx = {
    ...{},
    ...{},
};
let __VLS_components;
let __VLS_intrinsics;
let __VLS_directives;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "page" },
});
/** @type {__VLS_StyleScopedClasses['page']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ onClick: (__VLS_ctx.goCreate) },
    ...{ class: "primary" },
});
/** @type {__VLS_StyleScopedClasses['primary']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.table, __VLS_intrinsics.table)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.thead, __VLS_intrinsics.thead)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.tr, __VLS_intrinsics.tr)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({
    ...{ class: "ops-col" },
});
/** @type {__VLS_StyleScopedClasses['ops-col']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.tbody, __VLS_intrinsics.tbody)({});
for (const [u] of __VLS_vFor((__VLS_ctx.users))) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.tr, __VLS_intrinsics.tr)({
        key: (u.userId),
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({
        ...{ class: "ops" },
    });
    /** @type {__VLS_StyleScopedClasses['ops']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.i, __VLS_intrinsics.i)({
        ...{ onClick: (...[$event]) => {
                __VLS_ctx.goView(u);
                // @ts-ignore
                [goCreate, users, goView,];
            } },
        ...{ class: "icon view" },
        title: "预览",
    });
    /** @type {__VLS_StyleScopedClasses['icon']} */ ;
    /** @type {__VLS_StyleScopedClasses['view']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.i, __VLS_intrinsics.i)({
        ...{ onClick: (...[$event]) => {
                __VLS_ctx.goEdit(u);
                // @ts-ignore
                [goEdit,];
            } },
        ...{ class: "icon edit" },
        title: "编辑",
    });
    /** @type {__VLS_StyleScopedClasses['icon']} */ ;
    /** @type {__VLS_StyleScopedClasses['edit']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.i, __VLS_intrinsics.i)({
        ...{ onClick: (...[$event]) => {
                __VLS_ctx.changePwd(u);
                // @ts-ignore
                [changePwd,];
            } },
        ...{ class: "icon pwd" },
        title: "改密码",
    });
    /** @type {__VLS_StyleScopedClasses['icon']} */ ;
    /** @type {__VLS_StyleScopedClasses['pwd']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.i, __VLS_intrinsics.i)({
        ...{ onClick: (...[$event]) => {
                __VLS_ctx.toggle(u);
                // @ts-ignore
                [toggle,];
            } },
        ...{ class: "icon toggle" },
        ...{ class: (u.deleteFlag === 0 ? 'pause' : 'play') },
        title: (u.deleteFlag === 0 ? '停用' : '启用'),
    });
    /** @type {__VLS_StyleScopedClasses['icon']} */ ;
    /** @type {__VLS_StyleScopedClasses['toggle']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
    (u.username);
    __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
    (u.accountUserId);
    __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
    (u.brokerName);
    __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
        ...{ class: (u.deleteFlag === 0 ? 'on' : 'off') },
    });
    (u.deleteFlag === 0 ? '已启用' : '已停用');
    // @ts-ignore
    [];
}
// @ts-ignore
[];
const __VLS_export = (await import('vue')).defineComponent({});
export default {};
//# sourceMappingURL=UserList.vue.js.map