# AuthCore API

AuthCore is an authentication service providing user registration, login, token management, and password reset functionalities.

## API Documentation

Access Swagger UI:  
🔗 **[AuthCore API Docs](https://authcore.onrender.com)**

## Endpoints

- **POST** `/api/register` - Register user  
- **POST** `/api/login` - Authenticate user  
- **POST** `/api/refresh-token` - Refresh token  
- **POST** `/api/forgotpassword` - Request password reset  
- **POST** `/api/reset` - Reset password  
- **GET** `/api/verify` - Verify email  

## Tech Stack

- **Spring Boot (Java 21)** | **Spring Security & JWT** | **PostgreSQL**  
- **Docker** | **Swagger (OpenAPI 3.0)** | **Render.com (Deployment)**  

## Run Locally

git clone https://github.com/elzabeth-m/AuthCore.git

cd AuthCore

mvn clean package

java -jar target/AuthCore.jar
