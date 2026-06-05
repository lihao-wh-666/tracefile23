FROM maven:3.8.6-eclipse-temurin-11 AS build
WORKDIR /app
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?><settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd"><mirrors><mirror><id>aliyun</id><mirrorOf>central</mirrorOf><name>aliyun maven</name><url>https://maven.aliyun.com/repository/public</url></mirror></mirrors></settings>' > /root/.m2/settings.xml
COPY backend/pom.xml .
COPY backend/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:11-jre
VOLUME /tmp
WORKDIR /app
COPY --from=build /app/target/exam-system-1.0.0.jar app.jar
EXPOSE 6080
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=6080"]
