# Use Java 21
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

# 🔥 Add this line (IMPORTANT)
RUN chmod +x gradlew

# Build the app
RUN ./gradlew build

EXPOSE 8082

CMD ["java", "-jar", "build/libs/eduScale-0.0.1-SNAPSHOT.jar"]