# Multi-stage Docker build for Koubaibu application
# Note: Development environment not ready - this file is prepared for production deployment

# ===== Build Stage =====
# FROM eclipse-temurin:21-jdk AS build
# WORKDIR /app
# 
# # Copy Maven wrapper and pom.xml
# COPY .mvn/ .mvn/
# COPY mvnw pom.xml ./
# 
# # Download dependencies (cached layer)
# RUN ./mvnw dependency:go-offline -B
# 
# # Copy source code
# COPY src ./src
# 
# # Build with production profile
# RUN ./mvnw package -Pproduction -DskipTests -B

# ===== Production Stage =====
# FROM eclipse-temurin:21-jre-alpine AS production
# WORKDIR /app
# 
# # Create non-root user for security
# RUN addgroup -S koubaibu && adduser -S koubaibu -G koubaibu
# 
# # Copy built artifact
# COPY --from=build /app/target/*.jar app.jar
# 
# # Change ownership
# RUN chown -R koubaibu:koubaibu /app
# USER koubaibu
# 
# # Expose port
# EXPOSE 8080
# 
# # Health check
# HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
#     CMD wget -q --spider http://localhost:8080/actuator/health || exit 1
# 
# # Run application
# ENTRYPOINT ["java", "-jar", "app.jar"]

# ===== Placeholder for development =====
# This Dockerfile is prepared for production deployment.
# Uncomment the above sections when ready to deploy.
# 
# Build command (when ready):
#   docker build -t koubaibu:latest .
# 
# Run command (when ready):
#   docker run -p 8080:8080 \
#     -e SPRING_PROFILES_ACTIVE=prod \
#     -e DATABASE_URL=jdbc:postgresql://host:5432/koubaibu \
#     -e DATABASE_USERNAME=koubaibu \
#     -e DATABASE_PASSWORD=secret \
#     -e SLACK_WEBHOOK_URL=https://hooks.slack.com/services/xxx \
#     koubaibu:latest
