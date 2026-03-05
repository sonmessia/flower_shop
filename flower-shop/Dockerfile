# Development stage
FROM maven:3.9-eclipse-temurin-25-alpine AS development

WORKDIR /app

# Pre-download dependencies using pom.xml for faster startup
# Source code is bind-mounted from host via docker-compose for live editing
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Expose port (matches docker-compose port mapping 8081:8080)
EXPOSE 8080

# Run the application in development mode
# SPRING_PROFILES_ACTIVE is set via docker-compose environment, so no -D flag needed
CMD ["mvn", "spring-boot:run"]

# Build stage
FROM maven:3.9-eclipse-temurin-25-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Production stage
FROM eclipse-temurin:25-jre-alpine AS production
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Create images directory for file storage
RUN mkdir -p /app/images/products /app/images/blogs && \
    chmod -R 755 /app/images

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
