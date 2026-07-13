FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# preload deps
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests

# -----------------------
# Runtime stage
# -----------------------
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/edol-test/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]