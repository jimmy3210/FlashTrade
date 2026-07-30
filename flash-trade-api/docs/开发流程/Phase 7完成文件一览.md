**Phase 7 完了サマリー**

| ファイル | 内容 |
|---|---|
| pom.xml | Spring Cloud BOM `2025.0.0` + `spring-cloud-starter-config` 追加 |
| resources/application.yml | `spring.config.import` + `/actuator/refresh` 公開 |
| pom.xml | 独立 Config Server（`spring-cloud-config-server`） |
| config-server/.../ConfigServerApplication.java | `@EnableConfigServer` |
| Dockerfile | マルチステージビルド（非 root 実行） |
| flashtradeapi.yml | 全環境共通設定 |
| flashtradeapi-dev.yml | 開発環境専用（mock=true, DEBUG） |
| flashtradeapi-prod.yml | 本番環境（プレースホルダーのみ） |
| .env.example | 非機密環境変数テンプレート |
| README.md | Docker Secret 作成ガイド |
| docker-compose.yml | config-server サービス + `secrets:` 宣言 + アプリテンプレート |
| .gitignore | `.env`, `secrets/*.txt`, `*.key` を除外 |
| security/JwtTokenProvider.java | `@RefreshScope` 追加 |
| broker/schwab/config/SchwabProperties.java | `@RefreshScope` 追加 |
| config/SecurityConfig.java | `/actuator/refresh` を ADMIN のみ許可 |

**動的設定更新の使い方：**
```bash
# Config Server 連携後、設定変更を反映
curl -X POST -u admin:password http://localhost:8080/actuator/refresh
```

変更を行いました。