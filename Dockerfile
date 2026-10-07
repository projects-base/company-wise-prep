# API + code runner for Render. The UI is deployed separately to Netlify.
#
# The runtime image must be a full JDK, not a JRE: the judge compiles learners' code with
# javax.tools (jdk.compiler) and starts a `java` process per test.

FROM eclipse-temurin:21-jdk AS build
WORKDIR /src
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -B dependency:go-offline
COPY src src
# The UI isn't bundled into the jar for the hosted split; tests run in CI/locally, not here.
RUN ./mvnw -q -B package -DskipTests && cp target/*.jar /src/app.jar

FROM eclipse-temurin:21-jdk
WORKDIR /app
RUN useradd --system --create-home --home-dir /home/app app
COPY --from=build /src/app.jar app.jar
COPY data data
RUN chown -R app:app /app
USER app

# Render injects PORT. Listen on all interfaces inside the container; the password login
# (AUTH_ENABLED=true) and the CORS allowlist protect the API.
ENV SERVER_ADDRESS=0.0.0.0 \
    DATA_DIR=/app/data \
    AUTH_ENABLED=true \
    JAVA_OPTS="-Xmx640m -XX:+UseSerialGC -XX:MaxMetaspaceSize=192m"

EXPOSE 8090
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
