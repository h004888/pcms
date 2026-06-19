FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw || true
COPY pcms-common/ pcms-common/
COPY pharmacist-workbench-service/ pharmacist-workbench-service/
RUN ./mvnw -pl pharmacist-workbench-service -am clean package -DskipTests || mvn -pl pharmacist-workbench-service -am clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/pharmacist-workbench-service/target/*.jar app.jar
EXPOSE 8095
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
