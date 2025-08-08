# Stage 1: Build the app using Maven and Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the app with Java 21
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Use Render's PORT environment variable
ENV PORT=8080
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
