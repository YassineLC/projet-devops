# Build multi-stage (contexte = racine du dépôt, pour accéder à proto/)
# buildx : le jar est indépendant de l'architecture, il est compilé une seule fois en natif
FROM --platform=$BUILDPLATFORM eclipse-temurin:21-jdk AS build
WORKDIR /build
COPY proto/ proto/
COPY catalogue-service/gradlew catalogue-service/settings.gradle catalogue-service/build.gradle catalogue-service/
COPY catalogue-service/gradle/ catalogue-service/gradle/
WORKDIR /build/catalogue-service
# Checkout Windows en CRLF : normaliser le script avant exécution Linux
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN --mount=type=cache,target=/root/.gradle ./gradlew dependencies --no-daemon -q > /dev/null
COPY catalogue-service/src/ src/
RUN --mount=type=cache,target=/root/.gradle ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre
RUN groupadd --system --gid 10001 app && useradd --system --uid 10001 --gid app app
WORKDIR /app
COPY --from=build /build/catalogue-service/build/libs/catalogue-service-0.0.1-SNAPSHOT.jar app.jar
USER 10001
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
EXPOSE 8080 9090
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
