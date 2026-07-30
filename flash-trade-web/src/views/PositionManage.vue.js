import { ref, reactive, onMounted } from 'vue';
import request from '@/utils/request';
const groupedPositions = ref({});
const collapsedUsers = reactive({});
const closingUsers = reactive({});
const loading = ref(true);
const userSummary = reactive({});
const profitClass = (v) => (v > 0 ? 'profit' : v < 0 ? 'loss' : '');
const buildSummary = (userId, list) => {
    userSummary[userId] = list.reduce((acc, p) => {
        acc.unrealized += p.unrealizedPnl;
        acc.marketValue += p.marketValue;
        return acc;
    }, { unrealized: 0, marketValue: 0 });
};
const normalizePosition = (row) => ({
    accountId: String(row.account_id || ''),
    brokerName: String(row.broker_name || ''),
    symbol: String(row.symbol || ''),
    quantity: Number(row.quantity || 0),
    direction: Number(row.quantity || 0) >= 0 ? '多头' : '空头',
    averageCost: Number(row.average_cost || 0),
    currentPrice: Number(row.current_price || 0),
    marketValue: Number(row.market_value || 0),
    unrealizedPnl: Number(row.unrealized_pnl || 0),
    updatedAt: row.updated_at ? String(row.updated_at) : '',
});
const fetchPositions = async () => {
    loading.value = true;
    try {
        const res = await request.get('/positions');
        groupedPositions.value = {};
        Object.entries(res || {}).forEach(([userId, rows]) => {
            groupedPositions.value[userId] = rows.map(normalizePosition);
            if (!(userId in collapsedUsers))
                collapsedUsers[userId] = false;
            if (!(userId in closingUsers))
                closingUsers[userId] = false;
            buildSummary(userId, groupedPositions.value[userId]);
        });
    }
    finally {
        loading.value = false;
    }
};
const toggleCollapse = (userId) => {
    collapsedUsers[userId] = !collapsedUsers[userId];
};
const closeAllPositions = async (userId) => {
    if (!confirm(`确认平仓 ${userId} 所有仓位？`))
        return;
    closingUsers[userId] = true;
    try {
        await request.post('/positions/close_all', { userId });
        await fetchPositions();
    }
    finally {
        closingUsers[userId] = false;
    }
};
onMounted(fetchPositions);
const __VLS_ctx = {
    ...{},
    ...{},
};
let __VLS_components;
let __VLS_intrinsics;
let __VLS_directives;
/** @type {__VLS_StyleScopedClasses['collapse-icon']} */ ;
/** @type {__VLS_StyleScopedClasses['close-btn']} */ ;
/** @type {__VLS_StyleScopedClasses['position-table']} */ ;
/** @type {__VLS_StyleScopedClasses['position-table']} */ ;
/** @type {__VLS_StyleScopedClasses['position-table']} */ ;
__VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
    ...{ class: "positions-container" },
});
/** @type {__VLS_StyleScopedClasses['positions-container']} */ ;
if (__VLS_ctx.loading) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "loading" },
    });
    /** @type {__VLS_StyleScopedClasses['loading']} */ ;
}
else if (Object.keys(__VLS_ctx.groupedPositions).length === 0) {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({});
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
        ...{ class: "empty-state" },
    });
    /** @type {__VLS_StyleScopedClasses['empty-state']} */ ;
}
else {
    __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({});
    for (const [positions, userId] of __VLS_vFor((__VLS_ctx.groupedPositions))) {
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
            key: (userId),
            ...{ class: "user-block" },
        });
        /** @type {__VLS_StyleScopedClasses['user-block']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
            ...{ class: "user-header" },
        });
        /** @type {__VLS_StyleScopedClasses['user-header']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.h3, __VLS_intrinsics.h3)({
            ...{ onClick: (...[$event]) => {
                    if (!!(__VLS_ctx.loading))
                        return;
                    if (!!(Object.keys(__VLS_ctx.groupedPositions).length === 0))
                        return;
                    __VLS_ctx.toggleCollapse(userId);
                    // @ts-ignore
                    [loading, groupedPositions, groupedPositions, toggleCollapse,];
                } },
            ...{ class: "user-title" },
        });
        /** @type {__VLS_StyleScopedClasses['user-title']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
            ...{ class: "collapse-icon" },
            ...{ class: ({ collapsed: __VLS_ctx.collapsedUsers[userId] }) },
        });
        /** @type {__VLS_StyleScopedClasses['collapse-icon']} */ ;
        /** @type {__VLS_StyleScopedClasses['collapsed']} */ ;
        (userId);
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
            ...{ class: "user-actions" },
        });
        /** @type {__VLS_StyleScopedClasses['user-actions']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
            ...{ class: "summary" },
            ...{ class: (__VLS_ctx.profitClass(__VLS_ctx.userSummary[userId].unrealized)) },
        });
        /** @type {__VLS_StyleScopedClasses['summary']} */ ;
        (__VLS_ctx.userSummary[userId].unrealized.toFixed(2));
        __VLS_asFunctionalElement1(__VLS_intrinsics.span, __VLS_intrinsics.span)({
            ...{ class: "summary" },
            ...{ class: (__VLS_ctx.profitClass(__VLS_ctx.userSummary[userId].marketValue)) },
        });
        /** @type {__VLS_StyleScopedClasses['summary']} */ ;
        (__VLS_ctx.userSummary[userId].marketValue.toFixed(2));
        __VLS_asFunctionalElement1(__VLS_intrinsics.button, __VLS_intrinsics.button)({
            ...{ onClick: (...[$event]) => {
                    if (!!(__VLS_ctx.loading))
                        return;
                    if (!!(Object.keys(__VLS_ctx.groupedPositions).length === 0))
                        return;
                    __VLS_ctx.closeAllPositions(userId);
                    // @ts-ignore
                    [collapsedUsers, profitClass, profitClass, userSummary, userSummary, userSummary, userSummary, closeAllPositions,];
                } },
            ...{ class: "close-btn" },
            disabled: (positions.length === 0 || __VLS_ctx.closingUsers[userId]),
        });
        /** @type {__VLS_StyleScopedClasses['close-btn']} */ ;
        (__VLS_ctx.closingUsers[userId] ? '平仓中...' : '一键平仓');
        __VLS_asFunctionalElement1(__VLS_intrinsics.div, __VLS_intrinsics.div)({
            ...{ class: "table-wrapper" },
        });
        __VLS_asFunctionalDirective(__VLS_directives.vShow, {})(null, { ...__VLS_directiveBindingRestFields, value: (!__VLS_ctx.collapsedUsers[userId]) }, null, null);
        /** @type {__VLS_StyleScopedClasses['table-wrapper']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.table, __VLS_intrinsics.table)({
            ...{ class: "position-table" },
        });
        /** @type {__VLS_StyleScopedClasses['position-table']} */ ;
        __VLS_asFunctionalElement1(__VLS_intrinsics.thead, __VLS_intrinsics.thead)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.tr, __VLS_intrinsics.tr)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.th, __VLS_intrinsics.th)({});
        __VLS_asFunctionalElement1(__VLS_intrinsics.tbody, __VLS_intrinsics.tbody)({});
        if (positions.length === 0) {
            __VLS_asFunctionalElement1(__VLS_intrinsics.tr, __VLS_intrinsics.tr)({});
            __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({
                colspan: "10",
                ...{ class: "empty-row" },
            });
            /** @type {__VLS_StyleScopedClasses['empty-row']} */ ;
        }
        for (const [pos] of __VLS_vFor((positions))) {
            __VLS_asFunctionalElement1(__VLS_intrinsics.tr, __VLS_intrinsics.tr)({
                key: (userId + '-' + pos.accountId + '-' + pos.symbol),
            });
            __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
            (pos.brokerName);
            __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
            (pos.accountId);
            __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
            (pos.symbol);
            __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
            (pos.direction);
            __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
            (pos.quantity);
            __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
            (pos.averageCost.toFixed(2));
            __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
            (pos.currentPrice.toFixed(2));
            __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
            (pos.marketValue.toFixed(2));
            __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({
                ...{ class: (__VLS_ctx.profitClass(pos.unrealizedPnl)) },
            });
            (pos.unrealizedPnl.toFixed(2));
            __VLS_asFunctionalElement1(__VLS_intrinsics.td, __VLS_intrinsics.td)({});
            (pos.updatedAt);
            // @ts-ignore
            [collapsedUsers, profitClass, closingUsers, closingUsers,];
        }
        // @ts-ignore
        [];
    }
}
// @ts-ignore
[];
const __VLS_export = (await import('vue')).defineComponent({});
export default {};
//# sourceMappingURL=PositionManage.vue.js.map