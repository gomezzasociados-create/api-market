# ==========================================
# GOMEZ SYSTEMS - DOCKERFILE PRODUCCION (EASYPANEL)
# ==========================================

# Etapa 1: Compilación con Maven y Java 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Copiar descriptor de dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Etapa 2: Entorno de ejecución ligero Alpine
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Crear carpeta de base de datos
RUN mkdir -p /app/basedatos

# Copiar el archivo ejecutable compilado
COPY --from=builder /app/target/*.jar app.jar

# Copiar catálogo y base de datos física H2
COPY basedatos ./basedatos

# Variables de entorno por defecto
ENV PORT=8085
EXPOSE 8085
EXPOSE 80

# Comando de arranque
ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
