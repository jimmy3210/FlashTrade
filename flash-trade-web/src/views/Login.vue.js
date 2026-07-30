import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import request from "@/utils/request";
const userId = ref("");
const password = ref("");
const router = useRouter();
onMounted(() => {
    document.title = "风控管理";
});
const login = async () => {
    if (!userId.value || !password.value)
        return alert("请输入用户名和密码");
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
    }
    catch (err) {
        if (err.code === "ECONNABORTED") {
            alert("请求超时，请稍后再试。\n");
            return;
        }
        const message = err?.error?.message || err?.message || "无法连接服务器";
        alert(message);
    }
};
const __VLS_ctx = {
    ...{},
    ...{},
};
let __VLS_components;
let __VLS_intrinsics;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['input-group']} */ ;
/** @type {__VLS_StyleScopedClasses['input-group']} */ ;
/** @type {__VLS_StyleScopedClasses['login-btn']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "login-container" },
});
/** @type {__VLS_StyleScopedClasses['login-container']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "login-card" },
});
/** @type {__VLS_StyleScopedClasses['login-card']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.img)({
    ...{ class: "logo" },
    src: "@/assets/daiwa.png",
    alt: "QuickTrade Logo",
});
/** @type {__VLS_StyleScopedClasses['logo']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)({
    ...{ class: "title" },
});
/** @type {__VLS_StyleScopedClasses['title']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "input-group" },
});
/** @type {__VLS_StyleScopedClasses['input-group']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.input)({
    value: (__VLS_ctx.userId),
    type: "text",
    placeholder: "用户名",
});
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "input-group" },
});
/** @type {__VLS_StyleScopedClasses['input-group']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.input)({
    type: "password",
    placeholder: "密码",
});
(__VLS_ctx.password);
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ onClick: (__VLS_ctx.login) },
    ...{ class: "login-btn" },
});
/** @type {__VLS_StyleScopedClasses['login-btn']} */ ;
// @ts-ignore
[userId, password, login,];
const __VLS_export = (await import('vue')).defineComponent({});
export default {};
//# sourceMappingURL=Login.vue.js.map