# Use OpenJDK image as base
FROM openjdk:21-slim

# Install Maven to build the application
RUN apt-get update && apt-get install -y maven

# Set working directory inside the container
WORKDIR /app

# Copy the source code into the container
COPY . .

# Build the .jar file using Maven
RUN mvn clean package -DskipTests

# Copy the .jar file into the container
COPY target/*.jar app.jar

# Expose the application's port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app.jar"]
