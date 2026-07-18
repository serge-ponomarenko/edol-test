FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests

# =======================
# Runtime
# =======================

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        curl && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java","-jar","app.jar"]