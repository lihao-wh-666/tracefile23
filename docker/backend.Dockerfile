FROM docker.io/library/maven:3.8.6-eclipse-temurin-11 AS build
WORKDIR /app

RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?><settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd"><mirrors><mirror><id>aliyunmaven</id><mirrorOf>*,!aliyun!</mirrorOf><name>阿里云公共仓库</name><url>https://maven.aliyun.com/repository/public</url></mirror></mirrors></settings>' > /root/.m2/settings.xml

COPY backend/pom.xml .
RUN mvn dependency:go-offline -B -q || true

COPY backend/src ./src
RUN mvn clean package -DskipTests -q

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

COPY --from=build /app/target/exam-system-1.0.0.jar app.jar

EXPOSE 6080

HEALTHCHECK --interval=30s --timeout=10s --retries=5 --start-period=90s \
  CMD curl -fsS http://localhost:6080/api/auth/public-key >/dev/null 2>&1 || wget --quiet --tries=1 --spider http://localhost:6080/api/auth/public-key >/dev/null 2>&1 || exit 1

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar --server.port=6080"]
