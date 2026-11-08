# Docker Compose Explanation

This file is the configuration for Docker Compose. It defines how multiple containers should run together for this project.

The project has two main services:

1. PostgreSQL database
2. Spring Boot application

---

## File: compose.yaml

```yaml
services:
```

This starts the section where all container services are defined.

In Docker Compose, each service is a separate container. Here, we define `postgres` and `app` as two services.

---

```yaml
  # PostgreSQL Database
```

This is just a comment to make the file easier to understand.

It tells developers that this section is for the database service.

---

```yaml
  postgres:
```

This creates a Docker service named `postgres`.

This container will run the PostgreSQL database that the Spring Boot application uses.

---

```yaml
    image: postgres:16-alpine
```

This tells Docker which image to use for this container.

- `postgres` = official PostgreSQL image
- `16-alpine` = PostgreSQL version 16 with a small Alpine-based Linux image

This keeps the database lightweight and fast.

---

```yaml
    container_name: university-db
```

This sets the name of the running Docker container.

Instead of a random generated name, the container will be called `university-db`.

---

```yaml
    environment:
```

This section provides environment variables to the container.

These variables are used when PostgreSQL starts and initializes the database.

---

```yaml
      POSTGRES_DB: university_db
```

This creates a database named `university_db` automatically when PostgreSQL starts.

The application will connect to this database.

---

```yaml
      POSTGRES_USER: university_user
```

This creates a database user named `university_user`.

This user is used by the Spring Boot app to access the database.

---

```yaml
      POSTGRES_PASSWORD: university_pass
```

This sets the password for the PostgreSQL user.

This password is used when the app connects to the database.

---

```yaml
    ports:
      - "5433:5432"
```

This maps ports between the host machine and the container.

- `5433` = port on your machine
- `5432` = port inside the PostgreSQL container

So if you access the database from your computer, you connect to port `5433`.

The application inside Docker uses the internal container port `5432`.

---

```yaml
    volumes:
      - postgres_data:/var/lib/postgresql/data
```

This creates a persistent storage volume for the database.

- `postgres_data` = named Docker volume
- `/var/lib/postgresql/data` = directory inside the database container where PostgreSQL stores its data

This ensures the database data remains saved even if the container is stopped or restarted.

---

```yaml
    networks:
      - university-network
```

This attaches the PostgreSQL container to a custom Docker network named `university-network`.

This allows the app container to find the database using the service name `postgres`.

---

```yaml
    healthcheck:
```

This tells Docker to check whether the database is ready before other services depend on it.

The app should only start when PostgreSQL is healthy.

---

```yaml
      test: ["CMD-SHELL", "pg_isready -U university_user -d university_db"]
```

This command checks whether PostgreSQL is accepting connections.

- `pg_isready` = PostgreSQL readiness check command
- `-U university_user` = check as the database user
- `-d university_db` = check the specific database

If the command succeeds, the database is considered healthy.

---

```yaml
      interval: 10s
```

Docker checks the health of the container every 10 seconds.

---

```yaml
      timeout: 5s
```

If the check takes longer than 5 seconds, it is considered failed or timed out.

---

```yaml
      retries: 5
```

Docker will retry the health check 5 times before marking the container unhealthy.

---

```yaml

  # Spring Boot Application
```

This comment marks the next section as the application container.

---

```yaml
  app:
```

This defines the app service, which runs the Spring Boot application.

---

```yaml
    build:
```

This tells Docker to build the application container using a Dockerfile instead of downloading a ready-made image.

---

```yaml
      context: .
```

This sets the Docker build context to the current project folder.

Docker will look for the Dockerfile in the project root.

---

```yaml
      dockerfile: Dockerfile
```

This tells Docker which Dockerfile to use to build the image.

The file is named `Dockerfile` in the project root.

---

```yaml
    container_name: university-app
```

This sets the name of the application container to `university-app`.

---

```yaml
    ports:
      - "8082:8082"
```

This maps the host port `8082` to the container's port `8082`.

This means the application can be accessed through the browser on the host machine at:

- `http://localhost:8082`

---

```yaml
    environment:
```

This section defines environment variables for the Spring Boot app.

The app uses these values to connect to the database and configure JPA behavior.

---

```yaml
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/university_db
```

This is the database connection URL.

- `jdbc:postgresql://` = PostgreSQL JDBC connection type
- `postgres` = host name of the database service inside Docker network
- `5432` = PostgreSQL internal port
- `university_db` = database name

This tells the app where to find the database.

---

```yaml
      SPRING_DATASOURCE_USERNAME: university_user
```

This sets the username used by the Spring Boot app to connect to PostgreSQL.

---

```yaml
      SPRING_DATASOURCE_PASSWORD: university_pass
```

This sets the password for the database connection.

---

```yaml
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
```

This configures Hibernate to automatically update the database schema.

- `update` means Hibernate will modify the schema as needed when the application starts
- This is useful during development, but not always recommended for production

---

```yaml
    depends_on:
      postgres:
        condition: service_healthy
```

This means the application will start only after the `postgres` container is healthy.

So the database is guaranteed to be ready before the app tries to connect to it.

---

```yaml
    networks:
      - university-network
```

This connects the app service to the same Docker network as PostgreSQL.

Because both containers are on the same network, the app can reach the database by the service name `postgres`.

---

```yaml
volumes:
  postgres_data:
```

This defines a Docker volume named `postgres_data`.

It is used by the PostgreSQL service to store database files persistently.

---

```yaml
networks:
  university-network:
    driver: bridge
```

This creates a custom Docker network called `university-network`.

- `driver: bridge` = the default Docker bridge network type

This lets containers communicate with each other by name using the internal Docker network.

---

## Summary

This Compose file sets up a simple full-stack development environment with:

- a PostgreSQL database container
- a Spring Boot application container
- a shared Docker network
- persistent storage for database data
- startup order control so the app waits for the database to be ready

This is useful for running the whole application locally with a single command and consistent configuration.
