# syntax=docker/dockerfile:1
# Stage 1: Build WasmJs web application
FROM eclipse-temurin:21-jdk AS builder

# Install libatomic1 required by Node.js v25+ (used by Kotlin Wasm tooling)
RUN apt-get update && apt-get install -y --no-install-recommends libatomic1 && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy source code
COPY . /app

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

# Build production WasmJs web distribution with Gradle cache mount
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :webApp:wasmJsBrowserDistribution --no-daemon --build-cache

# Stage 2: Serve static files with Nginx
FROM nginx:alpine

RUN apk add --no-cache gzip

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy WasmJs distribution from builder stage
COPY --from=builder /app/webApp/build/dist/wasmJs/productionExecutable /usr/share/nginx/html

# Clean up .map files and pre-compress WASM/JS/CSS/HTML assets with max gzip
RUN rm -f /usr/share/nginx/html/*.map && \
    find /usr/share/nginx/html -type f \( -name "*.wasm" -o -name "*.js" -o -name "*.json" -o -name "*.html" -o -name "*.css" \) \
    -exec gzip -9 -k {} +

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
