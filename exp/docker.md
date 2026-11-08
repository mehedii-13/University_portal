# Dockerfile Explanation

This file is used to build a Docker image for the Spring Boot application.

Dockerfile is a text file that tells Docker how to create an image step by step.

In this project, the Dockerfile uses a two-stage build:

1. Build the application inside a JDK container
2. Run the application inside a smaller JRE container

This is a good practice because it reduces the final image size and keeps the runtime lighter.

---

## Stage 1: Build

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS builder
```

This line tells Docker to start from a base image.

- `eclipse-temurin` = OpenJDK distribution
- `21-jdk-alpine` = Java 21 JDK version running on Alpine Linux
- `AS builder` = gives this stage a name so it can be referenced later

This stage is used only to compile the project.

---

```dockerfile
WORKDIR /app
```

This sets the working directory inside the container to `/app`.

All following commands will run from this directory.

---

```dockerfile
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
```

These lines copy the Maven wrapper and `pom.xml` build descriptor into the container.

---

```dockerfile
RUN chmod +x mvnw
```

This makes the Maven wrapper script executable inside the container.

---

```dockerfile
RUN ./mvnw dependency:go-offline -B || true
```

This downloads project dependencies before compiling the source code.

- `./mvnw` = run the project Maven wrapper
- `dependency:go-offline` = download dependencies for offline use
- `-B` = batch mode (non-interactive)
- `|| true` = continue even if some plugin dependency warning occurs

---

```dockerfile
COPY src src
```

This copies the application source code into the container.

---

```dockerfile
RUN ./mvnw clean package -DskipTests -B
```

This compiles the application and packages it into an executable JAR.

- `clean package` = compile classes, package JAR
- `-DskipTests` = skip running tests during container image build
- `-B` = non-interactive batch mode

This outputs the runnable Spring Boot JAR into `/app/target/`.

---

## Stage 2: Run

```dockerfile
FROM eclipse-temurin:21-jre-alpine
```

This starts the second stage using a smaller Java runtime image.

- `21-jre-alpine` = Java 21 Runtime Environment on Alpine Linux
- This is lighter than a full JDK image

This stage is for running the app only, not building it.

---

```dockerfile
WORKDIR /app
```

Sets the application directory inside the runtime container.

---

```dockerfile
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
```

This creates a non-root user and group.

- `appgroup` = group for the app
- `appuser` = unprivileged user

This improves security because the app does not run as root.

---

```dockerfile
COPY --from=builder /app/target/*.jar app.jar
```

This copies the built JAR file from the previous build stage into the final image.

The `--from=builder` part means Docker takes the artifact from the `builder` stage (`/app/target/*.jar`).

So the runtime image contains only the packaged application, not the whole build toolchain.

---

```dockerfile
USER appuser
```

This switches the container to run as the non-root user `appuser`.

This is a security best practice.

---

```dockerfile
EXPOSE 8082
```

This declares that the application listens on port `8082`.

This helps people know which port the app is expected to use.

---

```dockerfile
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8082/index.html || exit 1
```

This tells Docker to regularly check whether the app is healthy.

- `--interval=30s` = check every 30 seconds
- `--timeout=10s` = wait at most 10 seconds for a response
- `--start-period=30s` = give the app time to start before health checks begin
- `--retries=3` = mark unhealthy after 3 failed checks

The command:

```bash
wget --no-verbose --tries=1 --spider http://localhost:8082/index.html
```

tries to access the app homepage and confirms whether the app is responding.

If the page is not reachable, Docker marks the container unhealthy.

---

```dockerfile
ENTRYPOINT ["java", "-jar", "app.jar"]
```

This is the command that runs when the container starts.

It simply starts the Java application with:

```bash
java -jar app.jar
```

This launches the Spring Boot application inside the container.

---

## Summary

This Dockerfile builds a Java 21 Spring Boot app in a multi-stage process:

- first stage builds the project and generates the JAR
- second stage runs only the compiled app in a small runtime container
- app runs as a non-root user
- health check verifies the app is available on port `8082`

This setup is efficient, secure, and suitable for deployment using Docker Compose.
