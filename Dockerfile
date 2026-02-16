FROM eclipse-temurin:22-jdk-jammy AS build
WORKDIR /home/gradle/src
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew build -x test --no-daemon

FROM eclipse-temurin:22-jre-jammy
EXPOSE 8888
COPY --from=build /home/gradle/src/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
