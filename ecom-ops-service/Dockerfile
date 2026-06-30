FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -B dependency:go-offline -pl ecom-ops-service -am -q 2>/dev/null || true
RUN mvn -B -pl ecom-ops-service -am clean package -DskipTests

FROM eclipse-temurin:21.0.11_10-jre-alpine
WORKDIR /app
COPY --from=build /app/ecom-ops-service/target/*.jar app.jar
EXPOSE 8098
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
