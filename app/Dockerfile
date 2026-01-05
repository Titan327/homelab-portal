FROM maven:3.9-eclipse-temurin-21 AS dev

WORKDIR /app

COPY pom.xml .
COPY src ./src

CMD ["mvn", "spring-boot:run"]