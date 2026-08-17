# Build stage: full JDK, produces the jar.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

# Copy the wrapper and pom first so dependency downloads are cached and only
# re-run when pom.xml actually changes, not on every source edit.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src/ src/
COPY frontend/ frontend/

# Builds the React bundle too: frontend-maven-plugin fetches its own Node, so
# this image needs no Node installed.
RUN ./mvnw clean package -DskipTests -B

# Runtime stage: JRE only. Smaller, and ships no compiler to a public server.
FROM eclipse-temurin:21-jre
WORKDIR /app

# Never run as root. If the app is compromised, this limits what that buys.
RUN useradd --system --create-home --uid 1001 catchcompass \
    && mkdir -p /data/photos \
    && chown -R catchcompass:catchcompass /data

COPY --from=build /build/target/*.jar app.jar

USER catchcompass
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
