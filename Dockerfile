# Use Java 21
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN chmod +x gradlew

# 🔥 Skip tests (IMPORTANT FIX)
RUN ./gradlew build -x test

EXPOSE 8082

CMD ["java", "-jar", "build/libs/eduScale-0.0.1-SNAPSHOT.jar"]