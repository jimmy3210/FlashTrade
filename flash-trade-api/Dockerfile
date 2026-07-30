# =============================================================
# FlashTradeAPI — マルチステージ Dockerfile
# ステージ1: Maven ビルド
# ステージ2: 軽量 Alpine JRE ランタイム（非 root ユーザー）
# =============================================================

# ---- ステージ 1: ビルド ----
FROM maven:3.9-amazoncorretto-21 AS builder

WORKDIR /build

# 依存関係キャッシュのために pom.xml を先にコピー
COPY pom.xml .
RUN mvn dependency:go-offline -q

# ソースコピー & ビルド（テストはスキップ）
COPY src ./src
# Run Maven in non-quiet, batch mode and show errors to make build failures visible in Docker logs
RUN mvn -e -B package -DskipTests

# ---- ステージ 2: ランタイム ----
FROM amazoncorretto:21-alpine

# セキュリティ: 非 root ユーザーでアプリを実行
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# ビルド成果物をコピー
COPY --from=builder /build/target/FlashTradeAPI-*.jar app.jar

# 所有者変更
RUN chown appuser:appgroup app.jar

USER appuser

# HTTP 8080 / WebSocket 共通
EXPOSE 8080

# JVM 最適化オプション（コンテナフレンドリー）
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
