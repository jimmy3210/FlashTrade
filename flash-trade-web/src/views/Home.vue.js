import { useRouter } from 'vue-router';
const router = useRouter();
const logout = () => {
    localStorage.removeItem('token');
    router.push('/login');
};
const __VLS_ctx = {
    ...{},
    ...{},
};
let __VLS_components;
let __VLS_intrinsics;
let __VLS_directives;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.h1, __VLS_intrinsics.h1)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ onClick: (__VLS_ctx.logout) },
});
// @ts-ignore
[logout,];
const __VLS_export = (await import('vue')).defineComponent({});
export default {};
//# sourceMappingURL=Home.vue.js.map