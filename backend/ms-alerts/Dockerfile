FROM eclipse-temurin:25-jdk AS build

RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y maven curl tar gzip ca-certificates \
	&& rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src ./src
RUN mvn clean package -q -DskipTests

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=build /app/target/ms-alerts-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
