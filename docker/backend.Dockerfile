FROM docker.io/library/eclipse-temurin:11-jre-alpine
ENV TZ=Asia/Shanghai
ENV LANG=C.UTF-8
ENV LANGUAGE=C.UTF-8
ENV LC_ALL=C.UTF-8
WORKDIR /app

RUN apk update --no-cache && \
    apk add --no-cache tzdata curl ca-certificates && \
    ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    rm -rf /var/cache/apk/* /tmp/*

COPY backend/target/exam-system-1.0.0.jar app.jar

EXPOSE 6080

HEALTHCHECK --interval=30s --timeout=10s --retries=5 --start-period=90s \
  CMD curl -fsS http://localhost:6080/api/auth/public-key >/dev/null 2>&1 || wget --quiet --tries=1 --spider http://localhost:6080/api/auth/public-key >/dev/null 2>&1 || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar --server.port=6080"]
