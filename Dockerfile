FROM openjdk:21-slim
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar" , "/app.jar"]