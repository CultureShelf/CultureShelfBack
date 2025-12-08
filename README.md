## Running the Project with Docker

This project is set up to run as a containerized Java (Spring Boot) application using Docker and Docker Compose. The provided Dockerfile and `docker-compose.yml` handle building and running the application with the following specifics:

### Project-Specific Docker Requirements
- **Java Version:** Uses Eclipse Temurin 17 (JDK for build, JRE for runtime)
- **Build Tool:** Maven Wrapper (`mvnw`) is used for building the application inside the container
- **Ports:** The application exposes port **8080** (default Spring Boot port)

### Environment Variables
- No required environment variables are specified by default in the Dockerfile or compose file.
- If you need to provide environment variables, you can uncomment and use the `env_file: ./.env` line in `docker-compose.yml`.

### Build and Run Instructions
1. **Build and start the application:**
   ```sh
   docker compose up --build
   ```
   This will build the application using the multi-stage Dockerfile and start the container exposing port 8080.

2. **Access the application:**
   - The application will be available at [http://localhost:8080](http://localhost:8080) on your host machine.

### Special Configuration
- The Dockerfile creates a non-root user (`appuser`) for running the application, improving container security.
- JVM is configured with container-aware memory settings via `JAVA_OPTS`.
- If you need to add external services (e.g., PostgreSQL, Redis), sample configurations are provided in the `docker-compose.yml` as comments. Uncomment and configure as needed.

### Exposed Ports
- **java-app:** 8080 (host:container)

---

_If you add databases or other services, update the `docker-compose.yml` accordingly and expose the necessary ports._
