import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import request from "@/utils/request";
const router = useRouter();
const users = ref([]);
/* ---------- 加载 ---------- */
const load = async () => {
    users.value = await request.get("/users") || [];
};
/* ---------- 跳转 ---------- */
const openCreate = () => {
    router.push("/users/create");
};
const edit = (u) => {
    router.push(`/users/edit/${u.userId}`);
};
/* ---------- 启用 / 停用 ---------- */
const toggle = async (u) => {
    const isDisabled = u.deleteFlag === 1;
    const nextFlag = isDisabled ? 0 : 1;
    const oldFlag = u.deleteFlag;
    const msg = isDisabled
        ? `确认启用用户 ${u.userId}？`
        : `确认停用用户 ${u.userId}？`;
    if (!confirm(msg))
        return;
    // 乐观更新
    u.deleteFlag = nextFlag;
    try {
        await request.post("/users/status", {
            userId: u.userId,
            deleteFlag: nextFlag
        });
    }
    catch (e) {
        u.deleteFlag = oldFlag;
        alert("状态更新失败，请重试");
    }
};
/* ---------- 改密码 ---------- */
const changePwd = async (u) => {
    const pwd = prompt(`请输入 ${u.userId} 的新密码`);
    if (!pwd)
        return;
    await request.post(`/users/${u.userId}/password`, {
        password: pwd
    });
};
onMounted(load);
const __VLS_ctx = {
    ...{},
    ...{},
};
let __VLS_components;
let __VLS_intrinsics;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['icon']} */ ;
/** @type {__VLS_StyleScopedClasses['icon']} */ ;
/** @type {__VLS_StyleScopedClasses['icon']} */ ;
/** @type {__VLS_StyleScopedClasses['icon']} */ ;
/** @type {__VLS_StyleScopedClasses['icon']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "page" },
});
/** @type {__VLS_StyleScopedClasses['page']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "toolbar" },
});
/** @type {__VLS_StyleScopedClasses['toolbar']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ onClick: (__VLS_ctx.openCreate) },
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
                __VLS_ctx.edit(u);
                // @ts-ignore
                [openCreate, users, edit,];
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
        title: (u.deleteFlag === 0 ? '停用账户' : '启用账户'),
    });
    /** @type {__VLS_StyleScopedClasses['icon']} */ ;
    /** @type {__VLS_StyleScopedClasses['toggle']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
    (u.userId);
    __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
    (u.accountUserId);
    __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
        ...{ class: (u.deleteFlag === 0 ? 'on' : 'off') },
    });
    (u.deleteFlag === 0 ? '● 启用' : '● 停用');
    // @ts-ignore
    [];
}
if (__VLS_ctx.users.length === 0) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.tr, __VLS_intrinsics.tr)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({
        colspan: "4",
        ...{ class: "empty" },
    });
    /** @type {__VLS_StyleScopedClasses['empty']} */ ;
}
// @ts-ignore
[users,];
const __VLS_export = (await import('vue')).defineComponent({});
export default {};
//# sourceMappingURL=UserManage.vue.js.map