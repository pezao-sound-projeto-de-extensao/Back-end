# ============================================================
# Stage 1: compilar Spring Boot como GraalVM Native Image
# ============================================================
FROM ghcr.io/graalvm/native-image-community:21 AS build

WORKDIR /app

# A imagem do GraalVM não necessariamente possui Maven
RUN microdnf install -y maven \
    && microdnf clean all

# Copia primeiro o pom para aproveitar o cache das dependências
COPY pom.xml .

RUN mvn dependency:go-offline -B

# Copia o código da aplicação
COPY src ./src

# Compila o executável nativo
RUN mvn clean native:compile \
    -Pnative \
    -DskipTests \
    -B

# ============================================================
# Stage 2: imagem mínima de execução
# ============================================================
FROM gcr.io/distroless/base-debian12:nonroot

WORKDIR /app

COPY --from=build /app/target/app /app/app

ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 8080

ENTRYPOINT ["/app/app"]