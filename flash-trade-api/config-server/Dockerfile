# ===================================================
# FlashTrade Config Server — Dockerfile
# マルチステージビルドで最小イメージを作成
# ===================================================

# --- ビルドステージ ---
FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu AS builder
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B package -DskipTests --no-transfer-progress

# --- 実行ステージ ---
FROM mcr.microsoft.com/openjdk/jdk:21-ubuntu
WORKDIR /app

# セキュリティ: 非 root ユーザーで実行
RUN groupadd -r appgroup && useradd -r -g appgroup appuser
COPY --from=builder /build/target/flashtrade-config-server-*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8888
ENTRYPOINT ["java", "-jar", "app.jar"]
