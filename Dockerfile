# Use an official Java runtime as a parent image
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy the project files
COPY . .

# Build the project (you can also skip tests)
RUN ./mvnw clean package -DskipTests

# Expose Render's assigned port
EXPOSE 8080

# Run the jar
CMD ["java", "-jar", "target/*.jar"]
