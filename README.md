# URL Shortener — Spring Boot

A full-featured URL shortening service built with Spring Boot 4.0.1, featuring JWT-based authentication, click analytics, and a Dockerized deployment setup.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.0.1 |
| Language | Java 23 |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| Persistence | Spring Data JPA + PostgreSQL |
| Build Tool | Maven (via Maven Wrapper) |
| Utilities | Lombok |
| Containerization | Docker |

---

## Project Structure

```
url-shortener-backend/
├── src/main/java/com/url/shortener/
│   ├── controller/
│   │   ├── AuthController.java          # Registration & login endpoints
│   │   ├── RedirectController.java      # Short URL redirect handler
│   │   └── UrlMappingController.java    # URL CRUD & analytics endpoints
│   ├── dtos/
│   │   ├── ClickEventDTO.java
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   └── UrlMappingDTO.java
│   ├── models/
│   │   ├── ClickEvent.java              # Tracks each click on a short URL
│   │   ├── UrlMapping.java              # Maps short code → original URL
│   │   └── User.java
│   ├── repository/
│   │   ├── ClickEventRepository.java
│   │   ├── UrlMappingRepository.java
│   │   └── UserRepository.java
│   ├── security/
│   │   ├── jwt/
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── JwtAuthenticationResponse.java
│   │   │   └── JwtUtils.java
│   │   ├── WebConfig.java
│   │   └── WebSecurityConfig.java
│   ├── service/
│   │   ├── UrlMappingService.java
│   │   ├── UserDetailsImpl.java
│   │   ├── UserDetailsServiceImpl.java
│   │   └── UserService.java
│   └── UrlShortenerSbApplication.java
├── src/main/resources/
│   └── application.properties
├── Dockerfile
└── pom.xml
```

---

## Features

- **User Authentication** — Register and login with JWT tokens
- **URL Shortening** — Generate short codes for any long URL
- **Redirects** — Short URLs redirect to the original destination
- **Click Analytics** — Every click is recorded as a `ClickEvent`
- **Secured Endpoints** — Protected routes require a valid JWT

---

## Getting Started

### Prerequisites

- Java 23+
- Maven 3.9+ (or use the included `mvnw` wrapper)
- PostgreSQL instance (local or remote)

### 1. Configure Environment Variables

Create a `.env` file in the project root:

```env
spring.datasource.url=jdbc:-----
username=------
password=----------
database_dialect=org.hibernate.dialect.PostgreSQLDialect
jwt_secret=---------------------
jwt_expiration=----------
frontend_url=http://--------.com
```


### 2. Run the Application

```bash
# Using the Maven wrapper
./mvnw spring-boot:run

# Or build and run the JAR
./mvnw clean package
java -jar target/url-shortener-sb-0.0.1-SNAPSHOT.jar
```

The server starts on `http://localhost:8080` by default.

---

## Running with Docker

### Build the Image

```bash
docker build -t url-shortener-sb .
```

### Run the Container

```bash
docker run -p 8080:8080 --env-file .env url-shortener-sb
```

---

## API Endpoints

### Auth

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| POST | `/api/auth/register` | Register a new user | No |
| POST | `/api/auth/login` | Login and receive a JWT | No |

### URL Management

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| POST | `/api/urls` | Create a short URL | Yes |
| GET | `/api/urls` | List all URLs for the user | Yes |
| DELETE | `/api/urls/{id}` | Delete a URL mapping | Yes |
| GET | `/api/urls/{id}/stats` | Get click analytics | Yes |

### Redirect

| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| GET | `/{shortCode}` | Redirect to the original URL | No |

> **Note:** Include the JWT as a Bearer token in the `Authorization` header for protected routes:
> `Authorization: Bearer <your_token>`

---

## Running Tests

```bash
./mvnw test
```

---

## Dependencies

| Dependency | Purpose |
|---|---|
| `spring-boot-starter-web` | REST API layer |
| `spring-boot-starter-data-jpa` | ORM & database access |
| `spring-boot-starter-security` | Authentication & authorization |
| `postgresql` | PostgreSQL JDBC driver |
| `jjwt-api / jjwt-impl / jjwt-jackson` | JWT generation & validation |
| `lombok` | Boilerplate reduction |
| `spring-boot-starter-test` | Unit & integration testing |
| `spring-security-test` | Security-layer testing |
