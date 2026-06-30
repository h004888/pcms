FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -B dependency:go-offline -pl supplier-service -am -q 2>/dev/null || true
RUN mvn -B -pl supplier-service -am clean package -DskipTests

FROM eclipse-temurin:21.0.11_10-jre-alpine
WORKDIR /app
COPY --from=build /app/supplier-service/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
