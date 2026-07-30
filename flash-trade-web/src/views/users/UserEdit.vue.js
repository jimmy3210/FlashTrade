import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import request from '@/utils/request';
const router = useRouter();
const route = useRoute();
const form = ref({
    username: '',
    accountUserId: '',
    brokerName: '',
    password: ''
});
const loadUser = async () => {
    const id = route.params.id;
    const data = await request.get(`/users/${id}`);
    form.value.username = data.username;
    form.value.accountUserId = data.accountUserId;
    form.value.brokerName = data.brokerName;
};
const save = async () => {
    const id = route.params.id;
    await request.put(`/users/${id}`, form.value);
    router.push({ name: 'UserList' });
};
const cancel = () => router.push({ name: 'UserList' });
onMounted(loadUser);
const __VLS_ctx = {
    ...{},
    ...{},
};
let __VLS_components;
let __VLS_intrinsics;
let __VLS_directives;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "form-page" },
});
/** @type {__VLS_StyleScopedClasses['form-page']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.form, __VLS_intrinsics.form)({
    ...{ onSubmit: (__VLS_ctx.save) },
});
__VLS_asFunctionalElement1(__VLS_intrinsics.input)({
    placeholder: "用户名",
    required: true,
});
(__VLS_ctx.form.username);
__VLS_asFunctionalElement1(__VLS_intrinsics.input)({
    placeholder: "账户名",
    required: true,
});
(__VLS_ctx.form.accountUserId);
__VLS_asFunctionalElement1(__VLS_intrinsics.input)({
    placeholder: "所属券商",
    required: true,
});
(__VLS_ctx.form.brokerName);
__VLS_asFunctionalElement1(__VLS_intrinsics.input)({
    placeholder: "密码（留空不修改）",
});
(__VLS_ctx.form.password);
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    type: "submit",
});
__VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
    ...{ onClick: (__VLS_ctx.cancel) },
    type: "button",
});
// @ts-ignore
[save, form, form, form, form, cancel,];
const __VLS_export = (await import('vue')).defineComponent({});
export default {};
//# sourceMappingURL=UserEdit.vue.js.map