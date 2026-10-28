FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

ARG SPRING_PROFILE=prod

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -P${SPRING_PROFILE} -DskipTests

FROM eclipse-temurin:21-jre
ARG SPRING_PROFILE=prod
WORKDIR /app

ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.33.1/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
ADD https://github.com/grafana/otel-profiling-java/releases/download/v2.1.4/pyroscope-otel-javaagent-extension.jar /otel/pyroscope-otel-javaagent-extension.jar

COPY --from=build /app/target/app.jar app.jar

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILE}

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]