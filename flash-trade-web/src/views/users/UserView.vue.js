import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request from '@/utils/request';
const router = useRouter();
const route = useRoute();
const form = ref(null);
onMounted(async () => {
    const id = route.params.id;
    form.value = await request.get(`/users/${id}`);
});
const back = () => router.push({ name: 'UserList' });
const edit = () => {
    const id = route.params.id;
    router.push({ name: 'UserEdit', params: { id } });
};
const __VLS_ctx = {
    ...{},
    ...{},
};
let __VLS_components;
let __VLS_intrinsics;
let __VLS_directives;
if (__VLS_ctx.form) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "view-page" },
    });
    /** @type {__VLS_StyleScopedClasses['view-page']} */ ;
    __VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({});
    (__VLS_ctx.form.username);
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({});
    (__VLS_ctx.form.accountUserId);
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({});
    (__VLS_ctx.form.brokerName);
    __VLS_asFunctionalElement1(__VLS_intrinsics.p, __VLS_intrinsics.p)({});
    (__VLS_ctx.form.deleteFlag === 0 ? '已启用' : '已停用');
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (__VLS_ctx.back) },
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (__VLS_ctx.edit) },
    });
}
// @ts-ignore
[form, form, form, form, form, back, edit,];
const __VLS_export = (await import('vue')).defineComponent({});
export default {};
//# sourceMappingURL=UserView.vue.js.map