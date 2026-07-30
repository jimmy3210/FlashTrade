package com.tradex.flashtrade.fix.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.SessionID;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * FIX セッション単位のレート制限
 *
 * 1 秒あたりのメッセージ数を制限することで DoS 攻撃を防ぐ。
 * スライディングウィンドウ方式（1 秒単位）を採用。
 */
public class FixRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(FixRateLimiter.class);

    private final int maxMessagesPerSecond;

    /** セッション → カウンター のマップ */
    private final ConcurrentHashMap<String, RateCounter> counters = new ConcurrentHashMap<>();

    public FixRateLimiter(int maxMessagesPerSecond) {
        this.maxMessagesPerSecond = maxMessagesPerSecond;
    }

    /**
     * メッセージ送信前にレート制限チェックを実行する
     *
     * @param sessionId セッション ID
     * @throws RateLimitExceededException レート制限超過の場合
     */
    public void checkAndIncrement(SessionID sessionId) {
        String key = sessionId.toString();
        RateCounter counter = counters.computeIfAbsent(key, k -> new RateCounter());

        if (!counter.tryIncrement(maxMessagesPerSecond)) {
            log.warn("レート制限超過: session={}, limit={}/sec", sessionId, maxMessagesPerSecond);
            throw new RateLimitExceededException(
                    "FIX セッション " + sessionId + " のレート制限超過 (" + maxMessagesPerSecond + "/sec)"
            );
        }
    }

    /**
     * セッションのカウンターをリセット（セッション切断時に使用）
     *
     * @param sessionId セッション ID
     */
    public void resetSession(SessionID sessionId) {
        counters.remove(sessionId.toString());
    }

    // -------------------------------------------------------------------------
    // 内部クラス
    // -------------------------------------------------------------------------

    /**
     * セッション単位のレートカウンター（1 秒ウィンドウ）
     */
    private static class RateCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis();

        synchronized boolean tryIncrement(int limit) {
            long now = System.currentTimeMillis();

            // 1 秒経過したらカウンターリセット
            if (now - windowStart >= 1000) {
                count.set(0);
                windowStart = now;
            }

            int current = count.incrementAndGet();
            return current <= limit;
        }
    }

    /**
     * レート制限超過例外
     */
    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String message) {
            super(message);
        }
    }
}
