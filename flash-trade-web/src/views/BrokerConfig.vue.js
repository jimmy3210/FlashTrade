import { reactive, ref, onMounted } from 'vue';
import request from '@/utils/request';
const configs = ref([]);
const editable = reactive({});
const loadConfigs = async () => {
    const list = await request.get('/admin/brokers/configs');
    configs.value = list || [];
    configs.value.forEach(config => {
        editable[config.brokerName] = {
            mode: config.mode || 'PRODUCTION',
            baseUrl: config.baseUrl || '',
            mockEnabled: config.mockEnabled
        };
    });
};
const save = async (brokerName) => {
    const current = editable[brokerName];
    if (!current)
        return;
    await request.put(`/admin/brokers/configs/${brokerName}`, {
        mode: current.mode,
        baseUrl: current.baseUrl,
        mockEnabled: current.mockEnabled
    });
    await loadConfigs();
    alert(`券商 ${brokerName} 配置已保存`);
};
onMounted(loadConfigs);
const __VLS_ctx = {
    ...{},
    ...{},
};
let __VLS_components;
let __VLS_intrinsics;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['primary']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "page" },
});
/** @type {__VLS_StyleScopedClasses['page']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "toolbar" },
});
/** @type {__VLS_StyleScopedClasses['toolbar']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.h2, __VLS_intrinsics.h2)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.table, __VLS_intrinsics.table)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.thead, __VLS_intrinsics.thead)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.tr, __VLS_intrinsics.tr)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({});
__VLS_asFunctionalElement1(__VLS_intrinsics.tbody, __VLS_intrinsics.tbody)({});
for (const [config] of __VLS_vFor((__VLS_ctx.configs))) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.tr, __VLS_intrinsics.tr)({
        key: (config.brokerName),
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
    (config.brokerName);
    __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.select, __VLS_intrinsics.select)({
        value: (__VLS_ctx.editable[config.brokerName].mode),
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.option, __VLS_intrinsics.option)({
        value: "PRODUCTION",
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.option, __VLS_intrinsics.option)({
        value: "SANDBOX",
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.option, __VLS_intrinsics.option)({
        value: "MOCK",
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.input)({
        type: "checkbox",
    });
    (__VLS_ctx.editable[config.brokerName].mockEnabled);
    __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.input)({
        type: "text",
        value: (__VLS_ctx.editable[config.brokerName].baseUrl),
        placeholder: "留空使用默认 URL",
    });
    __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
    (config.updatedAt || '-');
    __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
        ...{ onClick: (...[$event]) => {
                __VLS_ctx.save(config.brokerName);
                // @ts-ignore
                [configs, editable, editable, editable, save,];
            } },
        ...{ class: "primary" },
    });
    /** @type {__VLS_StyleScopedClasses['primary']} */ ;
    // @ts-ignore
    [];
}
if (__VLS_ctx.configs.length === 0) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.tr, __VLS_intrinsics.tr)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({
        colspan: "6",
        ...{ class: "empty" },
    });
    /** @type {__VLS_StyleScopedClasses['empty']} */ ;
}
// @ts-ignore
[configs,];
const __VLS_export = (await import('vue')).defineComponent({});
export default {};
//# sourceMappingURL=BrokerConfig.vue.js.map