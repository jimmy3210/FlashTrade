package com.tradex.flashtrade.fix.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.ArrayList;
import java.util.List;

/**
 * FIX Gateway 設定プロパティ
 * application.yml の flashtrade.fix.* にマッピング
 */
@ConfigurationProperties(prefix = "flashtrade.fix")
public record FixGatewayProperties(

        /** FIX Gateway 機能の有効/無効 */
        @DefaultValue("false")
        boolean enabled,

        /** FIX ログ出力ディレクトリ */
        @DefaultValue("logs/fix")
        String logDirectory,

        /** FIX ファイルストアパス（SeqNum 永続化） */
        @DefaultValue("data/fix")
        String fileStorePath,

        /** セッション設定リスト */
        @DefaultValue
        List<SessionConfig> sessions,

        /** レート制限設定 */
        @DefaultValue
        RateLimitConfig rateLimit,

        /** テストサーバー設定 */
        @DefaultValue
        TestServerConfig testServer

) {
    public FixGatewayProperties {
        if (sessions == null) sessions = new ArrayList<>();
        if (rateLimit == null) rateLimit = new RateLimitConfig(30);
        if (testServer == null) testServer = new TestServerConfig(false, 9876, "FIX.4.2");
    }

    /**
     * 個別セッション設定
     *
     * @param key         セッション識別キー（例: "test-local", "broker-a-uat"）
     * @param configFile  QuickFIX/J 設定ファイルパス（classpath 相対）
     * @param enabled     このセッションを自動起動するか
     * @param description 説明
     */
    public record SessionConfig(
            String key,
            String configFile,
            @DefaultValue("false") boolean enabled,
            @DefaultValue("") String description
    ) {}

    /**
     * レート制限設定
     *
     * @param messagesPerSecond セッションあたりの最大メッセージ数/秒
     */
    public record RateLimitConfig(
            @DefaultValue("30") int messagesPerSecond
    ) {}

    /**
     * テスト Acceptor サーバー設定
     *
     * @param enabled    テストサーバーを起動するか
     * @param port       テストサーバーのポート番号
     * @param fixVersion テストサーバーの FIX バージョン
     */
    public record TestServerConfig(
            @DefaultValue("false") boolean enabled,
            @DefaultValue("9876") int port,
            @DefaultValue("FIX.4.2") String fixVersion
    ) {}
}
