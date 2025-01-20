# Demo---RedisCache-

# MySQL and Redis Caching Application

This project implements a robust system that performs CRUD operations on a MySQL database while leveraging Redis as a caching layer to improve performance. The application supports parallel threading for high throughput and low latency.

---

## Features

1. **Database Operations**:
   - Secure database connection using SSL/TLS.
   - CRUD operations on a relational database (MySQL).
   - Concurrent query handling with thread safety.

2. **Redis Caching**:
   - Key-value data storage and retrieval.
   - Caches results of frequently used database queries.
   - Cache invalidation and updates when data changes.

3. **Parallel Threading**:
   - Implements multithreading to handle concurrent tasks.
   - Safeguards against race conditions, deadlocks, and resource contention.

4. **Error Handling & Resilience**:
   - Graceful handling of connection failures (MySQL, Redis).
   - Retry mechanisms and fallback strategies.

5. **Performance Metrics**:
   - Measures thread execution times and cache hit/miss rates.
   - Optimized for high throughput and low latency.

---

## Requirements

- **Database**:
  - MySQL Server.
  - Table: `users` with columns: `id`, `name`, `email`.
- **Caching**:
  - Redis Server.
- **Backend Framework**:
  - Spring Boot (Java).

---

## Setup Instructions

### Prerequisites

1. Install **MySQL** and **Redis**.
2. Install **Java 18** or later.
3. Install **Maven** for dependency management.

### Configuration

#### MySQL

Update `application.properties` file with your MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mydb?useSSL=true&requireSSL=true
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

#### Redis

Update `application.properties` file with your Redis configuration:

```properties
spring.redis.host=localhost
spring.redis.port=6379
```

### Build and Run

1. Clone the repository.
   ```bash
   git clone <repository-url>
   cd <repository-folder>
   ```

2. Build the project.
   ```bash
   mvn clean install
   ```

3. Run the application.
   ```bash
   mvn spring-boot:run
   ```

---

## API Endpoints

### User APIs

| Method | Endpoint            | Description                   |
|--------|---------------------|-------------------------------|
| GET    | `/api/users/{id}`   | Fetch user by ID (cached).    |
| POST   | `/api/users`        | Create a new user.            |
| PUT    | `/api/users/{id}`   | Update an existing user.      |
| DELETE | `/api/users/{id}`   | Delete a user.                |

### Performance Testing API

- **Endpoint**: `/performance-test`
  - Measures sequential vs. parallel database queries and logs results.

---

## Deliverables

1. **Code Implementation**:
   - Handles MySQL and Redis integration.
   - Supports parallel processing.

2. **Documentation**:
   - Setup instructions and architecture overview.

3. **Test Cases**:
   - Unit and integration tests for database and Redis.

4. **Performance Report**:
   - Demonstrates sequential vs. parallel execution which can be seen in logs 



## Author

**Your Name**  
Feel free to reach out at [your-email@example.com](mailto:your-email@example.com) for queries or contributions.
