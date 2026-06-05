FROM maven:3.8.6-eclipse-temurin-11 AS build
WORKDIR /app
COPY backend/pom.xml .
COPY backend/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:11-jre
VOLUME /tmp
WORKDIR /app
COPY --from=build /app/target/exam-system-1.0.0.jar app.jar
EXPOSE 6080
ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=6080"]
