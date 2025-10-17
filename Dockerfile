# Use an official Java runtime as a parent image
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Give mvnw permission to execute
RUN chmod +x mvnw

# Build the project and skip tests
RUN ./mvnw clean package -DskipTests

# Expose the port defined in application.properties
EXPOSE 5000

# Run the jar
ENTRYPOINT ["java", "-jar", "target/server-0.0.1-SNAPSHOT.jar"]
