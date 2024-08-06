FROM maven:3.8.6-eclipse-temurin-17 as builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=builder /app/target/translation-service-0.0.1-SNAPSHOT.jar app.jar
RUN chmod +x app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
