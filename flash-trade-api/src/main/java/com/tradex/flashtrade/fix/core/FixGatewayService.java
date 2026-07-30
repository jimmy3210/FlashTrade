package com.tradex.flashtrade.fix.core;

import com.tradex.flashtrade.fix.config.FixGatewayProperties;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FIX Gateway コアサービス
 *
 * FIX セッションの起動・停止・状態管理を担当する。
 * Spring Bean として管理され、REST API からも制御可能。
 *
 * <ul>
 *   <li>startAll() / stopAll(): 全 enabled セッションを一括制御</li>
 *   <li>start(key) / stop(key): 個別セッション制御</li>
 *   <li>@PreDestroy: アプリ終了時に全セッションを安全に停止</li>
 * </ul>
 */
public class FixGatewayService {

    private static final Logger log = LoggerFactory.getLogger(FixGatewayService.class);

    private final FixGatewayProperties properties;
    private final FixApplicationHandler applicationHandler;

    /** セッションキー → Initiator のマップ */
    private final ConcurrentHashMap<String, Initiator> initiators = new ConcurrentHashMap<>();

    public FixGatewayService(FixGatewayProperties properties, FixApplicationHandler applicationHandler) {
        this.properties = properties;
        this.applicationHandler = applicationHandler;
    }

    // -------------------------------------------------------------------------
    // 起動 / 停止
    // -------------------------------------------------------------------------

    /**
     * enabled=true の全セッションを起動する
     */
    public void startAll() {
        log.info("FIX Gateway: 全セッション起動開始");
        properties.sessions().stream()
                .filter(FixGatewayProperties.SessionConfig::enabled)
                .forEach(config -> {
                    try {
                        start(config.key());
                    } catch (Exception e) {
                        log.error("セッション起動失敗: key={}, error={}", config.key(), e.getMessage(), e);
                    }
                });
        log.info("FIX Gateway: 全セッション起動完了。起動数={}", initiators.size());
    }

    /**
     * 指定キーのセッションを起動する
     *
     * @param sessionKey sessions 設定の key
     */
    public void start(String sessionKey) {
        if (initiators.containsKey(sessionKey)) {
            log.warn("セッション既に起動中: key={}", sessionKey);
            return;
        }

        FixGatewayProperties.SessionConfig config = findSessionConfig(sessionKey)
                .orElseThrow(() -> new IllegalArgumentException("セッション設定が見つかりません: key=" + sessionKey));

        try {
            log.info("FIX セッション起動: key={}, config={}", sessionKey, config.configFile());

            SessionSettings settings = loadSettings(config.configFile());
            MessageStoreFactory storeFactory = new FileStoreFactory(settings);
            LogFactory logFactory = new FileLogFactory(settings);
            MessageFactory messageFactory = new DefaultMessageFactory();

            Initiator initiator = new SocketInitiator(
                    applicationHandler, storeFactory, settings, logFactory, messageFactory
            );
            initiator.start();
            initiators.put(sessionKey, initiator);

            log.info("FIX セッション起動完了: key={}", sessionKey);

        } catch (ConfigError | RuntimeError e) {
            log.error("FIX セッション起動エラー: key={}, error={}", sessionKey, e.getMessage(), e);
            throw new RuntimeException("FIX セッション起動失敗: " + sessionKey, e);
        }
    }

    /**
     * 全セッションを停止する
     */
    public void stopAll() {
        log.info("FIX Gateway: 全セッション停止開始");
        initiators.forEach((key, initiator) -> {
            try {
                stopInitiator(key, initiator);
            } catch (Exception e) {
                log.error("セッション停止エラー: key={}, error={}", key, e.getMessage(), e);
            }
        });
        initiators.clear();
        log.info("FIX Gateway: 全セッション停止完了");
    }

    /**
     * 指定キーのセッションを停止する
     *
     * @param sessionKey sessions 設定の key
     */
    public void stop(String sessionKey) {
        Initiator initiator = initiators.remove(sessionKey);
        if (initiator == null) {
            log.warn("セッションが起動していません: key={}", sessionKey);
            return;
        }
        stopInitiator(sessionKey, initiator);
    }

    // -------------------------------------------------------------------------
    // 状態確認
    // -------------------------------------------------------------------------

    /**
     * 全セッションの起動状態を返す
     *
     * @return セッションキー → 起動中(true)/停止(false) のマップ
     */
    public Map<String, Boolean> getAllRunningStatus() {
        return Map.copyOf(
                initiators.entrySet().stream()
                        .collect(java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                e -> e.getValue().isLoggedOn()
                        ))
        );
    }

    /**
     * 指定セッションが起動中かどうか返す
     *
     * @param sessionKey セッションキー
     * @return true: 起動中（少なくとも 1 つのセッションが LoggedOn）
     */
    public boolean isRunning(String sessionKey) {
        Initiator initiator = initiators.get(sessionKey);
        return initiator != null && initiator.isLoggedOn();
    }

    /**
     * 起動中のセッション一覧を返す
     *
     * @return 起動中セッションキーのリスト
     */
    public java.util.List<String> getRunningSessionKeys() {
        return initiators.keySet().stream().sorted().toList();
    }

    // -------------------------------------------------------------------------
    // アプリ終了時の安全停止
    // -------------------------------------------------------------------------

    @PreDestroy
    public void shutdown() {
        log.info("FIX Gateway: アプリケーション終了 → 全セッション安全停止");
        stopAll();
    }

    // -------------------------------------------------------------------------
    // プライベートヘルパー
    // -------------------------------------------------------------------------

    private void stopInitiator(String key, Initiator initiator) {
        try {
            log.info("FIX セッション停止: key={}", key);
            initiator.stop(true); // reset=true: シーケンス番号リセット
            log.info("FIX セッション停止完了: key={}", key);
        } catch (Exception e) {
            log.error("FIX セッション停止エラー: key={}, error={}", key, e.getMessage(), e);
        }
    }

    private Optional<FixGatewayProperties.SessionConfig> findSessionConfig(String key) {
        return properties.sessions().stream()
                .filter(c -> c.key().equals(key))
                .findFirst();
    }

    /**
     * classpath から QuickFIX/J 設定ファイルを読み込む
     *
     * @param configFilePath クラスパス相対パス（例: "fix/sessions/fix42-initiator.cfg"）
     * @return SessionSettings
     */
    private SessionSettings loadSettings(String configFilePath) throws ConfigError {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(configFilePath)) {
            if (is == null) {
                throw new ConfigError("FIX 設定ファイルが見つかりません: " + configFilePath);
            }
            return new SessionSettings(is);
        } catch (java.io.IOException e) {
            throw new ConfigError("FIX 設定ファイル読み込みエラー: " + configFilePath + ", " + e.getMessage());
        }
    }
}
