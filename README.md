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

