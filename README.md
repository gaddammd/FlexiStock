
# FlexiStock - Inventory Management System

FlexiStock is a comprehensive inventory management system built with a **microservices architecture**. It provides flexible stock tracking, user management, and product inventory handling with role-based access control.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Running the Application](#running-the-application)
- [Services](#services)
- [Technology Stack](#technology-stack)
- [Key Features](#key-features)
- [API Documentation](#api-documentation)
- [Development Workflow](#development-workflow)
- [Troubleshooting](#troubleshooting)

## Overview

FlexiStock is an IT Capstone Project built for the University of Cincinnati MSIT program. It implements a modern microservices architecture with:

- **Frontend**: React-based user interface
- **Backend**: Two independent Spring Boot microservices
  - Inventory Service: Manages product inventory, stock levels, and receipts
  - Users Service: Handles user authentication, roles, and permissions

## Architecture

The application follows a **microservices architecture** with the following components:

```
┌─────────────────────────────────────────────────────┐
│                  FlexiStock UI                      │
│              (React Frontend)                       │
│            localhost:3000                           │
└──────────────────────────────────────────────────────┘
                       │
        ┌──────────────┴──────────────┐
        │                             │
        ↓                             ↓
┌───────────────────┐        ┌──────────────────┐
│ Inventory Service │        │   Users Service  │
│  Spring Boot      │        │   Spring Boot    │
│  Port: 8080       │        │   Port: 8081     │
│                   │        │                  │
│ - Products CRUD   │        │ - Authentication │
│ - Stock Tracking  │        │ - User Mgmt      │
│ - Receipt Upload  │        │ - Role-Based     │
│ - Attributes      │        │   Access Control │
└───────────────────┘        └──────────────────┘
        │                             │
        ├─────────────────────────────┤
        │                             │
        ↓                             ↓
   ┌─────────────┐            ┌──────────────┐
   │  H2 / SQL   │            │  PostgreSQL  │
   │  Database   │            │  Database    │
   └─────────────┘            └──────────────┘
```

## Project Structure

```
FlexiStock/
├── flexistock-ui/              # React Frontend Application
│   ├── public/                 # Static assets
│   ├── src/
│   │   ├── api/               # API integration module
│   │   ├── components/        # React components
│   │   │   ├── AuthShell.jsx
│   │   │   ├── ProductForm.jsx
│   │   │   ├── ProductDetail.jsx
│   │   │   ├── ReceiptUpload.jsx
│   │   │   └── Sidebar.jsx
│   │   ├── App.js
│   │   └── index.js
│   ├── package.json
│   └── README.md
│
├── flexistock.inventory/       # Inventory Microservice
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   └── test/
│   ├── pom.xml
│   └── README.md (optional)
│
├── flexistock.users/           # Users Microservice
│   ├── src/
│   │   ├── main/
│   │   │   └── java/
│   │   └── test/
│   ├── pom.xml
│   └── README.md (optional)
│
├── .gitignore
└── README.md                   # This file
```

## Prerequisites

### Required Software

- **Node.js** v14+ (for Frontend)
- **npm** v6+ (for Frontend)
- **Java 21** JDK (for Backend Services)
- **Maven** 3.6+ (for Backend Services)
- **PostgreSQL** 12+ (for Users Service production)
- **Git**

### Optional

- Docker & Docker Compose (for containerized deployment)
- Postman (for API testing)
- IDE: IntelliJ IDEA or VS Code

## Installation & Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd FlexiStock
```

### 2. Frontend Setup (React UI)

```bash
cd flexistock-ui
npm install
```

### 3. Backend Services Setup

#### Inventory Service

```bash
cd flexistock.inventory
mvn clean install
```

#### Users Service

```bash
cd flexistock.users
mvn clean install
```

### 4. Database Configuration

#### PostgreSQL (Users Service)

Create a PostgreSQL database for the Users service:

```sql
CREATE DATABASE flexistock_users;
```

Update `flexistock.users/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/flexistock_users
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

#### H2 (Inventory Service - Testing)

H2 is configured for testing. For production, configure SQL Server or PostgreSQL similarly.

## Running the Application

### Option 1: Run All Services (Terminal Approach)

Open 3 terminal windows:

**Terminal 1 - Users Service:**
```bash
cd flexistock.users
mvn spring-boot:run
```
Service runs on `http://localhost:8081`

**Terminal 2 - Inventory Service:**
```bash
cd flexistock.inventory
mvn spring-boot:run
```
Service runs on `http://localhost:8080`

**Terminal 3 - React UI:**
```bash
cd flexistock-ui
npm start
```
UI runs on `http://localhost:3000`

### Option 2: Build and Run with Maven & npm

```bash
# Build Backend Services
cd flexistock.users && mvn clean package && cd ..
cd flexistock.inventory && mvn clean package && cd ..

# Run JAR files (in separate terminals)
java -jar flexistock.users/target/flexistock.users-0.0.1-SNAPSHOT.jar
java -jar flexistock.inventory/target/flexistock.inventory-0.0.1-SNAPSHOT.jar

# Run React Frontend
cd flexistock-ui && npm start
```

## Services

### Inventory Service (Port 8080)

**Spring Boot Application**: Manages product inventory, stock levels, and receipts.

**Key Features:**
- Product CRUD operations
- Stock quantity adjustments
- Low stock threshold alerts
- Receipt upload and tracking
- Custom product attributes
- Multi-database support

**Technologies:**
- Spring Boot 3.4.1
- Spring Data JPA
- Spring Validation
- Spring Mail
- H2 Database (testing)
- Java 21

**Configuration File:** `src/main/resources/application.properties`

### Users Service (Port 8081)

**Spring Boot Application**: Manages user authentication, authorization, and role-based access control.

**Key Features:**
- User registration and login
- Token-based authentication
- Admin access requests
- Role management (User, Admin)
- User profile management

**Technologies:**
- Spring Boot 3.4.1
- Spring Data JPA
- Spring Web
- PostgreSQL (production)
- Lombok
- Java 21

**Configuration File:** `src/main/resources/application.properties`

## Technology Stack

### Frontend
- **React 18+** - UI Library
- **JavaScript ES6+** - Language
- **CSS3** - Styling
- **Fetch API** - HTTP Client

### Backend
- **Spring Boot 3.4.1** - Framework
- **Java 21** - Language
- **Spring Data JPA** - ORM
- **Spring Security** (if configured) - Authentication & Authorization
- **Spring Validation** - Data Validation
- **Lombok** - Boilerplate reduction

### Database
- **PostgreSQL** - Production database
- **H2** - In-memory database (testing)

### Build & Dependency Management
- **Maven** - Java build tool
- **npm** - Node.js package manager

## Key Features

✅ **Product Management**
- Create, read, update, delete products
- Manage product SKUs, categories, and locations
- Add custom attributes to products

✅ **Inventory Tracking**
- Real-time stock level management
- Low stock threshold alerts
- Stock adjustment history

✅ **Receipt Management**
- Upload and store purchase receipts
- Link receipts to products
- Receipt tracking and retrieval

✅ **User Management**
- Secure user authentication
- Role-based access control (User, Admin)
- Admin access request workflow
- User profile management

✅ **Database Flexibility**
- SQL mode for SQL-based databases
- Multi-database support configuration

## API Documentation

### Inventory Service Endpoints

Base URL: `http://localhost:8080/api`

**Products:**
- `GET /products` - Fetch all products
- `POST /products` - Create a product
- `GET /products/{id}` - Get product details
- `PUT /products/{id}` - Update product
- `DELETE /products/{id}` - Delete product
- `POST /products/{id}/quantity` - Adjust stock quantity

**Receipts:**
- `POST /receipts/upload` - Upload receipt
- `GET /receipts` - Fetch receipts

### Users Service Endpoints

Base URL: `http://localhost:8081/api`

**Authentication:**
- `POST /auth/signup` - Register new user
- `POST /auth/login` - Login user
- `POST /auth/validate` - Validate token

**User Management:**
- `GET /users` - Fetch all users (Admin)
- `GET /users/{id}` - Get user details
- `POST /users/admin-request` - Request admin access
- `PUT /users/{id}/role` - Update user role (Admin)

## Development Workflow

### Frontend Development

```bash
cd flexistock-ui

# Start development server with hot reload
npm start

# Run tests
npm test

# Build for production
npm run build
```

### Backend Development

```bash
cd flexistock.inventory  # or flexistock.users

# Run with Maven (auto-reload on code changes with spring-boot-devtools)
mvn spring-boot:run

# Run tests
mvn test

# Build JAR
mvn clean package
```

### Making API Calls

The frontend automatically proxies API calls to the backend services through `setupProxy.js`.

## Environment Configuration

### Frontend (.env)

Create a `.env` file in `flexistock-ui/` if needed:

```env
REACT_APP_API_URL=http://localhost:3000
```

### Backend

Configure `application.properties` in each service:

**Inventory Service** (`flexistock.inventory/src/main/resources/application.properties`):
```properties
server.port=8080
spring.application.name=flexistock-inventory
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
```

**Users Service** (`flexistock.users/src/main/resources/application.properties`):
```properties
server.port=8081
spring.application.name=flexistock-users
spring.datasource.url=jdbc:postgresql://localhost:5432/flexistock_users
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

## Troubleshooting

### Frontend Issues

**Port 3000 already in use:**
```bash
# macOS/Linux
lsof -i :3000
kill -9 <PID>

# Windows
netstat -ano | findstr :3000
taskkill /PID <PID> /F
```

**Dependencies not installing:**
```bash
rm package-lock.json node_modules/
npm install
```

### Backend Issues

**Port 8080/8081 already in use:**
```bash
# Change port in application.properties
server.port=8080

# Or kill existing process
# macOS/Linux
lsof -i :8080
kill -9 <PID>
```

**PostgreSQL Connection Issues:**
- Verify PostgreSQL is running
- Check connection string in `application.properties`
- Ensure database exists: `CREATE DATABASE flexistock_users;`

**Maven Build Failures:**
```bash
mvn clean
mvn install -U  # Update dependencies
```

### Database Issues

**Reset H2 Database:**
Delete `~/h2/` directory or change database name in `application.properties`

**Reset PostgreSQL:**
```sql
DROP DATABASE flexistock_users;
CREATE DATABASE flexistock_users;
```

## Contributing

1. Create a feature branch: `git checkout -b feature/your-feature`
2. Commit changes: `git commit -am 'Add feature'`
3. Push to branch: `git push origin feature/your-feature`
4. Submit pull request

## License

This project is part of the University of Cincinnati MSIT Capstone Program.

## Support

For issues or questions, please open an issue in the repository or contact the development team.

---

**Project**: FlexiStock Inventory Management System  
**Institution**: University of Cincinnati - Master of Science in Information Technology  
**Semester**: Spring 2026  
**Last Updated**: June 2026
