# ─────────────────────────────────
# Stage 1: Build
# Uses full JDK + Maven to compile
# This layer is DISCARDED in final image
# ─────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml first — separate layer
# If only source code changes, Maven deps are NOT re-downloaded
# This is a critical pipeline speed optimisation
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Now copy source and build
COPY src ./src
RUN mvn package -DskipTests -q

# ─────────────────────────────────
# Stage 2: Runtime
# Minimal Alpine JRE only (~85MB vs ~500MB)
# No Maven, no JDK, no source code in final image
# Smaller image = smaller attack surface = faster pulls
# ─────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Security: never run as root in production containers
# If container is compromised, attacker gets low-privilege user
# Not root — limits blast radius
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy only the built JAR from Stage 1
COPY --from=builder /app/target/*.jar app.jar

# Switch to non-root user
USER appuser

EXPOSE 8080

# Use exec form (not shell form) — proper signal handling
# When Kubernetes sends SIGTERM to stop pod gracefully,
# exec form ensures the JVM receives it directly
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-jar", "app.jar"]
