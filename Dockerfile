# Use Java 21 (or 17 if needed)
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Build the app
RUN ./gradlew build

# Expose port (Render will override anyway)
EXPOSE 8082

# Run the app
CMD ["java", "-jar", "build/libs/eduScale-0.0.1-SNAPSHOT.jar"]