# -----------------------------
# 1. Base Stage (Contains common dependencies)
# -----------------------------
FROM maven:3.9-eclipse-temurin-25-alpine AS base
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

# -----------------------------
# 2. Development Stage
# -----------------------------
FROM base AS development
# Source code is bind-mounted from the host via docker-compose
EXPOSE 8080
CMD ["mvn", "spring-boot:run"]

# -----------------------------
# 3. Build Stage
# -----------------------------
FROM base AS build
COPY src ./src
# Build the application (can configure maven to skip plain jar in pom.xml)
RUN mvn clean package -DskipTests

# -----------------------------
# 4. Production Stage
# -----------------------------
FROM eclipse-temurin:25-jre-alpine AS production
WORKDIR /app

# Install curl for healthcheck
RUN apk add --no-cache curl

# Create a non-root user and group to run the application (Security)
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the jar from the build stage (should use specific name or pattern to avoid plain jar)
# Example: COPY --from=build /app/target/*-SNAPSHOT.jar app.jar
COPY --from=build /app/target/*.jar app.jar

# Create directories and set permissions for the 'spring' user
RUN mkdir -p /app/images/products /app/images/blogs && \
  chown -R spring:spring /app/images && \
  chmod -R 755 /app/images

# Switch to using the non-root user
USER spring:spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
