# Stage 1: Build WasmJs web application
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy gradle wrapper and configuration for layer caching
COPY gradle /app/gradle
COPY gradlew /app/gradlew
COPY gradle.properties /app/gradle.properties
COPY settings.gradle.kts /app/settings.gradle.kts
COPY build.gradle.kts /app/build.gradle.kts
COPY build-logic /app/build-logic

RUN chmod +x gradlew

# Warm up gradle dependencies
RUN ./gradlew :webApp:dependencies --no-daemon || true

# Copy full source code
COPY . /app

# Build production WasmJs web distribution
RUN ./gradlew :webApp:wasmJsBrowserDistribution --no-daemon

# Stage 2: Serve static files with Nginx
FROM nginx:alpine

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy WasmJs distribution from builder stage
COPY --from=builder /app/webApp/build/dist/wasmJs/productionExecutable /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
