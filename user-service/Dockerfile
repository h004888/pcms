# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY ../pom.xml ../pom.xml
COPY ../*/pom.xml ../
RUN mvn -B -q -f ../pom.xml -pl user-service -am dependency:go-offline
COPY . .
RUN mvn -B -q -f ../pom.xml -pl user-service -am clean package -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21.0.11_10-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
