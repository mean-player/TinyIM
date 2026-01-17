# Instant Messaging (IM) & RTC System Demo

This project is a robust Instant Messaging (IM) and Real-Time Communication (RTC) backend system built with **Spring Boot**. It supports one-on-one chat, group chat, and real-time audio/video calls using WebRTC.

## 🚀 Features

*   **User Management**: Registration, Login (JWT), Password Reset (Email verification).
*   **Instant Messaging**:
    *   One-on-one private messaging.
    *   Group messaging.
    *   Message persistence and history.
    *   Real-time delivery using WebSocket.
*   **Real-Time Communication (RTC)**:
    *   Audio/Video calls.
    *   WebRTC signaling server.
    *   TURN/STUN server integration (CoTurn).
*   **Friendship System**: Add/Remove friends, friend requests.
*   **Group Management**: Create/Delete groups, Member management.
*   **File Handling**:
    *   Avatar upload.
    *   File/Image sharing in chats.
    *   Integration with Object Storage (MinIO/S3).
*   **System Reliability**:
    *   **Rate Limiting**: Custom AOP-based rate limiting for API protection.
    *   **Async Processing**: RabbitMQ for decoupling high-volume operations.
    *   **Caching**: Redis/Redisson for caching and distributed locks.

## 🛠 Tech Stack

*   **Language**: Java 17
*   **Framework**: Spring Boot 3.4.4
*   **Database**: MySQL
*   **ORM**: MyBatis
*   **Caching & Locks**: Redis, Redisson
*   **Message Queue**: RabbitMQ
*   **Object Storage**: MinIO / AWS S3
*   **Communication**: WebSocket (Signaling & Chat)
*   **Security**: Spring Security, JWT
*   **API Documentation**: Knife4j (OpenAPI 3)

## 📋 Prerequisites

Before running the application, ensure you have the following services installed and running:

*   **Java Development Kit (JDK)** 17+
*   **MySQL** (Create a database named `im`)
*   **Redis**
*   **RabbitMQ**
*   **MinIO** (or an S3-compatible service)

## ⚙️ Configuration

The application is configured via `src/main/resources/application.properties`. You need to update the following settings to match your environment:

```properties
# Server
server.port=8090

# Database (MySQL)
spring.datasource.url=jdbc:mysql://localhost:3306/im?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=your_password

# Redis
spring.data.redis.host=localhost
spring.data.redis.password=your_redis_password

# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest

# MinIO (Object Storage)
minio.endpoint=http://localhost:9000
minio.accessKey=minioadmin
minio.secretKey=minioadmin
minio.bucket=myim

# CoTurn (For WebRTC)
coturn.host=your_turn_server_ip:3478
coturn.secretKey=your_turn_secret

# Mail (For notifications/reset password)
spring.mail.username=your_email@qq.com
spring.mail.password=your_email_auth_code
```

## 📦 Installation & Run

1.  **Clone the repository**
    ```bash
    git clone https://github.com/yourusername/im-demo.git
    cd im-demo
    ```

2.  **Build the project**
    ```bash
    ./mvnw clean package -DskipTests
    ```

3.  **Run the application**
    ```bash
    java -jar target/demo-0.0.1-SNAPSHOT.jar
    ```
    Or run directly using Maven:
    ```bash
    ./mvnw spring-boot:run
    ```

## 📖 API Documentation

Once the application is running, you can access the interactive API documentation at:

*   **Knife4j UI**: `http://localhost:8090/doc.html`
*   **Swagger UI**: `http://localhost:8090/swagger-ui/index.html`

## 📂 Project Structure

*   `com.example.demo.Controller`: API Endpoints.
*   `com.example.demo.Service`: Business Logic.
*   `com.example.demo.Repository` / `Mapper`: Database Access.
*   `com.example.demo.Entity`: Data Models.
*   `com.example.demo.Component`: Infrastructure components (WebSocket, JWT, MinIO, etc.).
*   `com.example.demo.Config`: Configuration classes (Security, Redis, RabbitMQ, etc.).
*   `com.example.demo.AOP`: Aspect-Oriented Programming (Rate Limiting).
