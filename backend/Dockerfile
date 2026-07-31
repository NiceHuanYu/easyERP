# ==================== 构建阶段 ====================
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 先复制 pom.xml 利用 Docker 层缓存下载依赖
COPY pom.xml .
RUN mvn dependency:resolve -B -q

# 复制源码并打包
COPY src ./src
RUN mvn package -DskipTests -q

# ==================== 运行阶段 ====================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 安装 curl 用于 healthcheck
RUN apk add --no-cache curl

# 创建非 root 用户
RUN addgroup -S app && adduser -S app -G app
USER app

# JVM 参数:
# - 堆内存初始 256m / 最大 512m（可被环境变量 JAVA_OPTS 覆盖）
# - OOM 时自动 dump 堆文件
# - 开启容器感知（cgroup 内存限制）
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java \
  -XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75 \
  -XX:InitialRAMPercentage=50 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/tmp/heapdump.hprof \
  ${JAVA_OPTS} \
  -jar app.jar"]
