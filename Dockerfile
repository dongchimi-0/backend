FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

COPY . .

# gradlew에 실행 권한 부여
RUN chmod +x gradlew

FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]