# ── Stage 1: build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Cache de dependências
COPY app/pom.xml .
RUN mvn dependency:go-offline -q

# Compila o JAR
COPY app/src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: imagem final ─────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /build/target/*.jar app.jar

# Volume para o arquivo de histórico de migrations (persistência entre runs)
VOLUME ["/app/history"]

ENTRYPOINT ["java", "-jar", "app.jar"]
