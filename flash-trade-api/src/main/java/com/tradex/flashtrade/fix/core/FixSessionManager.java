package com.tradex.flashtrade.fix.core;

import com.tradex.flashtrade.fix.model.FixSessionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.SessionID;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FIX セッション状態管理
 *
 * 全アクティブセッションの状態（FixSessionStatus）を管理し、
 * セッション ID からの状態参照・更新を提供する。
 */
public class FixSessionManager {

    private static final Logger log = LoggerFactory.getLogger(FixSessionManager.class);

    /** SessionID → 状態 のマップ */
    private final ConcurrentHashMap<SessionID, FixSessionStatus> sessionStatusMap = new ConcurrentHashMap<>();

    /**
     * セッションを登録する（onCreate コールバックで呼び出す）
     *
     * @param sessionId セッション ID
     */
    public void registerSession(SessionID sessionId) {
        sessionStatusMap.put(sessionId, FixSessionStatus.DISCONNECTED);
        log.info("FIX セッション登録: {}", sessionId);
    }

    /**
     * セッション状態を更新する
     *
     * @param sessionId セッション ID
     * @param status    新しい状態
     */
    public void updateStatus(SessionID sessionId, FixSessionStatus status) {
        FixSessionStatus previous = sessionStatusMap.put(sessionId, status);
        log.info("FIX セッション状態変更: {} {} → {}", sessionId, previous, status);
    }

    /**
     * セッション状態を取得する
     *
     * @param sessionId セッション ID
     * @return 状態、未登録の場合は DISCONNECTED
     */
    public FixSessionStatus getStatus(SessionID sessionId) {
        return sessionStatusMap.getOrDefault(sessionId, FixSessionStatus.DISCONNECTED);
    }

    /**
     * 全セッションの状態マップを返す（読み取り専用）
     *
     * @return SessionID → FixSessionStatus のマップ
     */
    public Map<SessionID, FixSessionStatus> getAllStatuses() {
        return Collections.unmodifiableMap(sessionStatusMap);
    }

    /**
     * セッションが LOGGED_ON 状態かチェック
     *
     * @param sessionId セッション ID
     * @return true: LOGGED_ON
     */
    public boolean isLoggedOn(SessionID sessionId) {
        return FixSessionStatus.LOGGED_ON.equals(sessionStatusMap.get(sessionId));
    }

    /**
     * セッション登録解除（セッション破棄時）
     *
     * @param sessionId セッション ID
     */
    public void unregisterSession(SessionID sessionId) {
        sessionStatusMap.remove(sessionId);
        log.info("FIX セッション登録解除: {}", sessionId);
    }

    /**
     * 登録済みセッション ID セットを返す
     *
     * @return SessionID の Set（読み取り専用）
     */
    public Set<SessionID> getRegisteredSessionIds() {
        return Collections.unmodifiableSet(sessionStatusMap.keySet());
    }

    /**
     * LOGGED_ON セッション数を返す
     *
     * @return 接続中セッション数
     */
    public long getLoggedOnCount() {
        return sessionStatusMap.values().stream()
                .filter(FixSessionStatus.LOGGED_ON::equals)
                .count();
    }
}
