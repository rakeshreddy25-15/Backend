# Use official Java 21 JDK image
FROM eclipse-temurin:21-jdk

# Set working directory inside the container
WORKDIR /app

# Copy Maven wrapper and pom.xml first (for caching dependencies)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (this step is cached)
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# Copy the rest of the project files
COPY src src

# Build the project (skip tests for faster build)
RUN ./mvnw clean package -DskipTests

# Expose the port your Spring Boot app uses
EXPOSE 5000

# Run the JAR file
ENTRYPOINT ["java", "-jar", "target/server-0.0.1-SNAPSHOT.jar"]
