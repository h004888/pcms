FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -B dependency:go-offline -pl config-server -am -q 2>/dev/null || true
RUN mvn -B -pl config-server -am clean package -DskipTests

FROM eclipse-temurin:21.0.11_10-jre-alpine
WORKDIR /app
COPY --from=build /app/config-server/target/*.jar app.jar
EXPOSE 8888
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
