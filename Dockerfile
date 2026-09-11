FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

ARG SPRING_PROFILE=prod

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -P${SPRING_PROFILE} -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/app.jar app.jar

ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILE}

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]