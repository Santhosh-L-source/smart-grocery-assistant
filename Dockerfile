# =========================================================
# Stage 1: Build the Spring Boot application using Maven
# =========================================================
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production JAR
COPY src ./src
RUN mvn clean package -DskipTests

# =========================================================
# Stage 2: Create lean production runtime image
# =========================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Add unprivileged user and create data directory
RUN addgroup -S appgroup && adduser -S appuser -G appgroup \
    && mkdir -p /app/data && chown -R appuser:appgroup /app/data
USER appuser

# Copy built JAR from Stage 1
COPY --from=build /app/target/smart-grocery-assistant-1.0.0.jar app.jar

# Configuration for 24/7 Render cloud deployment
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=cloud
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
