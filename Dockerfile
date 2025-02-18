# Use OpenJDK image as base
FROM openjdk:21-slim

# Install Maven to build the application
RUN apt-get update && apt-get install -y maven

# Set working directory inside the container
WORKDIR /app

# Copy the source code into the container
COPY . .

# Build the .jar file using Maven (this should create the target directory)
RUN mvn clean package -DskipTests

# Ensure that the .jar file exists in target directory
RUN ls -alh target/

# Copy the .jar file from the target folder into the container
COPY target/*.jar app.jar

# Expose the application's port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app.jar"]
