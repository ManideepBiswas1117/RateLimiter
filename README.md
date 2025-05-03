
```markdown
# 🛠️ Spring Boot Rate Limiter with Redis

This project implements a Rate Limiting system using Spring Boot and Redis. It supports both **Sliding Window** and **Token Bucket** algorithms, and allows dynamic per-user configuration via a REST API.

## 🚀 Features

- 🌐 REST APIs to configure rate limits per user
- ⏳ Supports:
  - Sliding Window Algorithm
  - Token Bucket Algorithm
- 💾 Redis for fast storage and retrieval
- 🐳 Dockerized for easy deployment
- 📊 Logs request statistics

## 📦 Technologies Used

- Java 17
- Spring Boot
- Redis
- Docker
- Maven

## 📁 Project Structure

```

spring-boot-rate-limiter/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com.example.ratelimiter/
│       │       ├── controller/
│       │       ├── service/
│       │       ├── model/
│       │       └── config/
│       └── resources/
│           └── application.yml
│
├── Dockerfile
├── docker-compose.yml (if used)
├── .gitignore
├── README.md
└── pom.xml

````

## ⚙️ Getting Started

### Prerequisites

- Java 17+
- Maven
- Docker
- Redis (running locally or via Docker)

### Running Locally (Without Docker)

```bash
# Start Redis (if not already running)
docker run -d -p 6379:6379 redis

# Build and run the app
mvn clean install
mvn spring-boot:run
````

### Running with Docker

1. Build the Docker image:

```bash
docker build -t spring-boot-rate-limiter .
```

2. Run the container:

```bash
docker run -p 8080:8080 spring-boot-rate-limiter
```

3. App will be available at: `http://localhost:8080`

---

## 🔌 REST API Endpoints


## 📈 How it Works

* **Sliding Window**: Tracks timestamps of recent requests and checks if the number exceeds the limit in the given window.
* **Token Bucket**: Adds tokens over time and allows requests if tokens are available.

Both strategies are implemented using Redis for distributed and fast-access rate tracking.

---

## 🧪 Run Tests

```bash
mvn test
```

---

## 🐳 Docker Compose (Optional)

If you want to run both Redis and the Spring Boot app together, you can use a `docker-compose.yml`:

```yaml
version: '3'
services:
  redis:
    image: redis
    ports:
      - "6379:6379"

  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - redis
```

Run with:

```bash
docker-compose up --build
```

Testing:

Postman Collection added

## 👨‍💻 Author

Manideep Biswas – [manideepbiswas@gmail.com](mailto:manideepbiswas@gmail.com)


