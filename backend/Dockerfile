# ==========================================
# Stage 1: Build the Quarkus Backend
# ==========================================
FROM eclipse-temurin:11-jdk-focal AS builder

WORKDIR /code

# Copy Gradle wrapper & configuration
COPY gradlew gradlew.bat settings.gradle build.gradle gradle.properties ./
COPY gradle/ gradle/

# Fix CRLF line endings on Windows and grant execute permissions
RUN sed -i 's/\r$//' ./gradlew && chmod +x ./gradlew

# Pre-fetch dependencies
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src/ src/

# Build Quarkus fast-jar without running unit tests during image build
RUN ./gradlew build -Dquarkus.package.type=fast-jar -x test --no-daemon

# ==========================================
# Stage 2: Runtime Image
# ==========================================
FROM eclipse-temurin:11-jre-focal

WORKDIR /deployments

# Copy Quarkus fast-jar artifacts from builder
COPY --from=builder /code/build/quarkus-app/lib/ /deployments/lib/
COPY --from=builder /code/build/quarkus-app/*.jar /deployments/
COPY --from=builder /code/build/quarkus-app/app/ /deployments/app/
COPY --from=builder /code/build/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080

ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTIONS -jar /deployments/quarkus-run.jar"]

