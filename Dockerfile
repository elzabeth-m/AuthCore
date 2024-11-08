FROM maven:3.9.9-eclipse-temurin-21 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-alpine
COPY --from=build /target/AuthCore-0.0.1-SNAPSHOT.jar AuthCore.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","AuthCore.jar"]