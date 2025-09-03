# 🌱 Spring Boot Server with Firebase Messaging & OAuth2

This project is a RESTful API built with **Spring Boot**. It demonstrates integration with **Firebase Cloud Messaging (FCM)** to send push notifications and **OAuth2 authentication** with Spring Security, along with standard backend features such as database integration, profiles, and Docker support.

---

## ✨ Features
- REST API endpoints (CRUD operations)  
- OAuth2 authentication with JWT validation  
- Firebase Cloud Messaging (FCM) integration for push notifications  
- Spring Data JPA with Hibernate for database access  
- Profiles for development, testing, and production  
- Configurable via `application.properties`  
- Docker-ready for containerized deployments  

---

## 🚀 API Capabilities

This server powers a **Task Flow application**, providing secured REST APIs for managing tasks, users, and notifications.

- 🔐 **Authentication & Authorization** – OAuth2 with JWT  
- ✅ **Task Management** – Create, update, delete, and retrieve tasks  
- 👤 **User Management** – Manage user accounts (CRUD)  
- 📲 **Notifications** – Send push notifications with Firebase Cloud Messaging (FCM)  

### 📘 API Documentation
This project includes **Swagger UI** for exploring and testing the APIs.

Once the server is running, open:  
👉 [http://localhost:9091/swagger-ui.html](http://localhost:9091/swagger-ui.html)  

Swagger provides:
- Interactive API testing  
- Schema definitions (request/response models)  
- Endpoint documentation (methods, parameters, auth requirements)  

---

## 📦 Requirements

### Tools
- Java 17+  
- Maven 3.8+  
- PostgreSQL/MySQL (or H2 for testing)  
- (Optional) Docker  

### Libraries
- Spring Boot Starter Web – REST APIs  
- Spring Boot Starter Data JPA – database integration  
- Spring Boot Starter Validation – input validation  
- Spring Security OAuth2 Resource Server – for OAuth2 authentication & JWT validation  
- Firebase Admin SDK – push notifications  
- Lombok – reduce boilerplate code  
- PostgreSQL Driver – database connection  

**OAuth2 Dependencies in `pom.xml`:**
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>

<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```
**Firebase Admin SDK Dependency in `pom.xml`:**
```xml
<dependency>
  <groupId>com.google.firebase</groupId>
  <artifactId>firebase-admin</artifactId>
  <version>9.2.0</version>
</dependency>
```

---

## ⚡ Installation
Clone the repository:
```bash
git clone https://github.com/username/project-name.git
cd project-name
```
Build with Maven:
```bash
mvn clean install
```
Run directly:
```bash
mvn spring-boot:run
```
Or run packaged JAR:
```bash
java -jar target/project-name-0.0.1-SNAPSHOT.jar
```
The server will start at: http://localhost:9091

---

## ⚙️ Configuration
All configuration are in ```bash src/main/resources/application.properties ```
Example:
```properties
server.port=9091

spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=myuser
spring.datasource.password=mypass
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

spring.security.oauth2.resourceserver.jwt.issuer-uri=https://your-auth-server.com/realms/myrealm

firebase.service-account-file=classpath:firebase-service-account.json
```

---

## 🐳 Docker Support
```dockerfile
FROM openjdk:22

WORKDIR /app

COPY target/taskmanagementsystem-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9090

ENTRYPOINT [ "java", "-jar", "app.jar" ]
```
Build Docker images: ```bash docker build -t spring-boot-server . ```
Run container: ```bash docker run -p 9091:9091 spring-boot-server ```

---

## 🧪 Testing
Run tests: ```bash mvn test```

---

## 📂 Project Structure
```bash
project-name/
├── src/main/java/...    # Source code
├── src/main/resources/  # Config files (application.properties, firebase key, etc.)
├── src/test/java/...    # Tests
├── pom.xml              # Maven configuration
├── Dockerfile           # Container configuration
└── README.md            # Documentation
```

---

## 🤝 Contributing
Contributions are welcome! To get started:

1. Fork this repository  
2. Create your feature branch (`git checkout -b feature/my-feature`)  
3. Commit your changes (`git commit -m 'Add new feature'`)  
4. Push to the branch (`git push origin feature/my-feature`)  
5. Open a Pull Request  

Please make sure to update tests as appropriate.

---

## 📜 License
This project is licensed under the MIT License – see the [LICENSE](LICENSE) file for details.
