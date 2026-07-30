package com.tradex.flashtrade.fix.routing;

import com.tradex.flashtrade.fix.adapter.Fix42MessageAdapter;
import com.tradex.flashtrade.fix.adapter.Fix44MessageAdapter;
import com.tradex.flashtrade.fix.adapter.Fix50MessageAdapter;
import com.tradex.flashtrade.fix.adapter.FixMessageAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.SessionID;

import java.util.Map;

/**
 * FIX バージョンルーター
 *
 * SessionID の BeginString を元に対応するメッセージアダプターを選択する。
 * FIX 5.x は FIXT.1.1 で転送されるため、BeginString="FIXT.1.1" の場合は
 * Fix50MessageAdapter を使用する。
 */
public class FixVersionRouter {

    private static final Logger log = LoggerFactory.getLogger(FixVersionRouter.class);

    /** BeginString → アダプター のマッピング */
    private final Map<String, FixMessageAdapter> adapterMap;

    public FixVersionRouter(
            Fix42MessageAdapter fix42Adapter,
            Fix44MessageAdapter fix44Adapter,
            Fix50MessageAdapter fix50Adapter
    ) {
        this.adapterMap = Map.of(
                "FIX.4.2", fix42Adapter,
                "FIX.4.4", fix44Adapter,
                "FIXT.1.1", fix50Adapter,   // FIX 5.x の Transport バージョン
                "FIX.5.0SP2", fix50Adapter  // 直接指定された場合も考慮
        );
    }

    /**
     * SessionID からアダプターを解決する
     *
     * @param sessionId QuickFIX/J の SessionID
     * @return 対応する FixMessageAdapter
     * @throws UnsupportedOperationException 未対応バージョンの場合
     */
    public FixMessageAdapter resolve(SessionID sessionId) {
        String beginString = sessionId.getBeginString();
        FixMessageAdapter adapter = adapterMap.get(beginString);

        if (adapter == null) {
            log.error("未対応の FIX バージョン: beginString={}, sessionId={}", beginString, sessionId);
            throw new UnsupportedOperationException(
                    "未対応の FIX バージョン: " + beginString + "。対応バージョン: FIX.4.2, FIX.4.4, FIXT.1.1"
            );
        }

        log.debug("FIX バージョンルーティング: {} → {}", beginString, adapter.getSupportedVersion());
        return adapter;
    }

    /**
     * 指定 BeginString が対応済みかチェック
     *
     * @param beginString FIX BeginString
     * @return true: 対応済み
     */
    public boolean isSupported(String beginString) {
        return adapterMap.containsKey(beginString);
    }
}
