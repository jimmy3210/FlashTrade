# ===================================================
# FlashTrade Config Server — Dockerfile
# マルチステージビルドで最小イメージを作成
# ===================================================

# --- ビルドステージ ---
FROM amazoncorretto:21-alpine AS builder
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && \
    mvn -B package -DskipTests --no-transfer-progress

# --- 実行ステージ ---
FROM amazoncorretto:21-alpine
WORKDIR /app

# セキュリティ: 非 root ユーザーで実行
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=builder /build/target/flashtrade-config-server-*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
