# Paso 1: Compilación con Maven y Java 25
FROM maven:3.9-eclipse-temurin-25-noble AS build


WORKDIR /app


COPY pom.xml .
RUN mvn dependency:go-offline -q


COPY src ./src
RUN mvn clean package -q -DskipTests


# Paso 2: Imagen de ejecución con JRE Java 25
FROM eclipse-temurin:25-jre-noble


WORKDIR /app


# SOLUCIÓN: Copia cualquier JAR generado en target hacia app.jar
COPY --from=build /app/target/*.jar app.jar


EXPOSE 8083


ENTRYPOINT ["java", "-jar", "app.jar"]

