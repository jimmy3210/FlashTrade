import { computed } from "vue";
import { useRouter, useRoute } from "vue-router";
const router = useRouter();
const route = useRoute();
// 页面标题
const pageTitle = computed(() => route.meta.title || "风控管理");
// Logout
const logout = () => {
    localStorage.removeItem("token");
    router.push("/login");
};
const __VLS_ctx = {
    ...{},
    ...{},
};
let __VLS_components;
let __VLS_intrinsics;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['sidebar']} */ ;
/** @type {__VLS_StyleScopedClasses['logout-btn']} */ ;
/** @type {__VLS_StyleScopedClasses['nav-list']} */ ;
/** @type {__VLS_StyleScopedClasses['nav-link']} */ ;
/** @type {__VLS_StyleScopedClasses['nav-link']} */ ;
/** @type {__VLS_StyleScopedClasses['main-header']} */ ;
/** @type {__VLS_StyleScopedClasses['table']} */ ;
/** @type {__VLS_StyleScopedClasses['table']} */ ;
/** @type {__VLS_StyleScopedClasses['table']} */ ;
/** @type {__VLS_StyleScopedClasses['table']} */ ;
/** @type {__VLS_StyleScopedClasses['btn']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "dashboard-container" },
});
/** @type {__VLS_StyleScopedClasses['dashboard-container']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.aside, __VLS_intrinsics.aside)({
    ...{ class: "sidebar" },
});
/** @type {__VLS_StyleScopedClasses['sidebar']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "sidebar-header" },
});
/** @type {__VLS_StyleScopedClasses['sidebar-header']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)({
    ...{ class: "logo" },
});
/** @type {__VLS_StyleScopedClasses['logo']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ onClick: (__VLS_ctx.logout) },
    ...{ class: "logout-btn" },
});
/** @type {__VLS_StyleScopedClasses['logout-btn']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.ul, __VLS_intrinsics.ul)({
    ...{ class: "nav-list" },
});
/** @type {__VLS_StyleScopedClasses['nav-list']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.li, __VLS_intrinsics.li)({});
let __VLS_0;
/** @ts-ignore @type {typeof __VLS_components.routerLink | typeof __VLS_components.RouterLink | typeof __VLS_components.routerLink | typeof __VLS_components.RouterLink} */
routerLink;
// @ts-ignore
const __VLS_1 = __VLS_asFunctionalComponent1(__VLS_0, new __VLS_0({
    to: "positions",
    ...{ class: "nav-link" },
}));
const __VLS_2 = __VLS_1({
    to: "positions",
    ...{ class: "nav-link" },
}, ...__VLS_functionalComponentArgsRest(__VLS_1));
/** @type {__VLS_StyleScopedClasses['nav-link']} */ ;
const { default: __VLS_5 } = __VLS_3.slots;
// @ts-ignore
[logout,];
var __VLS_3;
__VLS_asFunctionalElement1(__VLS_intrinsics.li, __VLS_intrinsics.li)({});
let __VLS_6;
/** @ts-ignore @type {typeof __VLS_components.routerLink | typeof __VLS_components.RouterLink | typeof __VLS_components.routerLink | typeof __VLS_components.RouterLink} */
routerLink;
// @ts-ignore
const __VLS_7 = __VLS_asFunctionalComponent1(__VLS_6, new __VLS_6({
    to: "users",
    ...{ class: "nav-link" },
}));
const __VLS_8 = __VLS_7({
    to: "users",
    ...{ class: "nav-link" },
}, ...__VLS_functionalComponentArgsRest(__VLS_7));
/** @type {__VLS_StyleScopedClasses['nav-link']} */ ;
const { default: __VLS_11 } = __VLS_9.slots;
// @ts-ignore
[];
var __VLS_9;
__VLS_asFunctionalElement1(__VLS_intrinsics.li, __VLS_intrinsics.li)({});
let __VLS_12;
/** @ts-ignore @type {typeof __VLS_components.routerLink | typeof __VLS_components.RouterLink | typeof __VLS_components.routerLink | typeof __VLS_components.RouterLink} */
routerLink;
// @ts-ignore
const __VLS_13 = __VLS_asFunctionalComponent1(__VLS_12, new __VLS_12({
    to: "brokers",
    ...{ class: "nav-link" },
}));
const __VLS_14 = __VLS_13({
    to: "brokers",
    ...{ class: "nav-link" },
}, ...__VLS_functionalComponentArgsRest(__VLS_13));
/** @type {__VLS_StyleScopedClasses['nav-link']} */ ;
const { default: __VLS_17 } = __VLS_15.slots;
// @ts-ignore
[];
var __VLS_15;
__VLS_asFunctionalElement1(__VLS_intrinsics.li, __VLS_intrinsics.li)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
    ...{ class: "nav-link disabled-text" },
});
/** @type {__VLS_StyleScopedClasses['nav-link']} */ ;
/** @type {__VLS_StyleScopedClasses['disabled-text']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.main, __VLS_intrinsics.main)({
    ...{ class: "main-content" },
});
/** @type {__VLS_StyleScopedClasses['main-content']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.header, __VLS_intrinsics.header)({
    ...{ class: "main-header" },
});
/** @type {__VLS_StyleScopedClasses['main-header']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.h1, __VLS_intrinsics.h1)({});
(__VLS_ctx.pageTitle);
__VLS_asFunctionalElement1(__VLS_intrinsics.section, __VLS_intrinsics.section)({
    ...{ class: "content" },
});
/** @type {__VLS_StyleScopedClasses['content']} */ ;
let __VLS_18;
/** @ts-ignore @type {typeof __VLS_components.routerView | typeof __VLS_components.RouterView} */
routerView;
// @ts-ignore
const __VLS_19 = __VLS_asFunctionalComponent1(__VLS_18, new __VLS_18({}));
const __VLS_20 = __VLS_19({}, ...__VLS_functionalComponentArgsRest(__VLS_19));
// @ts-ignore
[pageTitle,];
const __VLS_export = (await import('vue')).defineComponent({});
export default {};
//# sourceMappingURL=Dashboard.vue.js.map