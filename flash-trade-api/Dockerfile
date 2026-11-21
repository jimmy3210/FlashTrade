# =============================================================
# FlashTradeAPI — マルチステージ Dockerfile
# ステージ1: Maven ビルド
# ステージ2: 軽量 Alpine JRE ランタイム（非 root ユーザー）
# =============================================================

# ---- ステージ 1: ビルド ----
FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu AS builder

RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
WORKDIR /build

# 依存関係キャッシュのために pom.xml を先にコピー
COPY pom.xml .
RUN mvn dependency:go-offline -q

# ソースコピー & ビルド（テストはスキップ）
COPY src ./src
# Run Maven in non-quiet, batch mode and show errors to make build failures visible in Docker logs
RUN mvn -e -B package -DskipTests

# ---- ステージ 2: ランタイム ----
FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu

# セキュリティ: 非 root ユーザーでアプリを実行
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

WORKDIR /app

COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chmod 0555 /usr/local/bin/docker-entrypoint.sh

# ビルド成果物をコピー
COPY --from=builder /build/target/FlashTradeAPI-*.jar app.jar

# 所有者変更
RUN chown appuser:appgroup app.jar

USER appuser

# HTTP 8080 / WebSocket 共通
EXPOSE 8080

# JVM 最適化オプション（コンテナフレンドリー）
ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]
CMD ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
