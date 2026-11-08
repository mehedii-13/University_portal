# University Management Web Portal

[![Java CI with Maven](https://github.com/mehedii-13/University_portal/actions/workflows/maven.yml/badge.svg)](https://github.com/mehedii-13/University_portal/actions/workflows/maven.yml)
[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot 4.1.1](https://img.shields.io/badge/Spring%20Boot-4.1.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Build-Maven-blue.svg)](https://maven.apache.org/)

A full-stack University Management web application built with **Spring Boot 4**, **Spring Security**, **JWT (JSON Web Tokens)**, **Spring Data JPA**, and a lightweight, clean frontend.

---

## 📁 Project Directory Structure

```text
University_portal/
├── .github/
│   └── workflows/
│       └── maven.yml          # GitHub Actions automated CI/CD pipeline
├── docs/                      # Architectural documentation & guides
│   ├── auth.md                # JWT authentication mechanism
│   ├── routes.md              # REST API endpoint specifications
│   ├── dataFetch.md           # Data fetching and client-server flow
│   ├── description.md         # Project requirement and specifications
│   ├── docker.md              # Containerization instructions
│   └── explanation.md         # Architecture and design rationale
├── legacy-gradle/             # Archived Gradle build files (post-Maven migration)
│   ├── build.gradle.bak
│   └── settings.gradle.bak
├── src/
│   ├── main/
│   │   ├── java/com/example/universityweb/
│   │   │   ├── config/        # SecurityConfig, JWT Filter, DataInitializer
│   │   │   ├── controller/    # REST API endpoints (Auth, Admin, Teacher, Student)
│   │   │   ├── dto/           # Data Transfer Objects (AuthRequest, UserDto, etc.)
│   │   │   ├── model/         # JPA Entities (User, Role enum)
│   │   │   ├── repository/    # Spring Data JPA Repository interfaces
│   │   │   └── service/       # Business logic (UserService, CustomUserDetailsService)
│   │   └── resources/
│   │       ├── application.yml # Database and application settings
│   │       └── static/        # Clean Web UI (HTML, CSS, Vanilla JS)
│   │           ├── index.html       # Login portal
│   │           ├── dashboard.html   # Role-based dashboard
│   │           ├── css/style.css    # Clean, minimalist styling
│   │           └── js/              # Modular frontend scripts (auth, api, dashboard)
│   └── test/
│       ├── java/com/example/universityweb/
│       │   ├── config/        # JwtService unit tests
│       │   ├── service/       # 6 isolated business logic unit tests
│       │   └── integration/   # 3 end-to-end integration test suites
│       └── resources/         # Test configurations & Mockito extensions
├── compose.yaml               # Docker Compose configuration (PostgreSQL)
├── Dockerfile                 # Multi-stage Docker build configuration
├── mvnw & mvnw.cmd            # Maven Wrappers for Linux / macOS / Windows
└── pom.xml                    # Maven build configuration & dependencies
```

---

## 👥 Seeded Demonstration Accounts

The application automatically seeds demonstration accounts on startup (`DataInitializer.java`):

| Role | Username | Password | Full Name | Email |
| :--- | :--- | :--- | :--- | :--- |
| **ADMIN** | `admin` | `admin123` | Arthur Pendelton | `arthur.pendelton@university.edu` |
| **ADMIN** | `registrar` | `reg123` | Eleanor Vance | `eleanor.vance@university.edu` |
| **TEACHER** | `prof.miller` | `miller123` | Prof. Robert Miller | `robert.miller@university.edu` |
| **TEACHER** | `dr.zhao` | `zhao123` | Dr. Lin Zhao | `lin.zhao@university.edu` |
| **TEACHER** | `prof.patel` | `patel123` | Prof. Priya Patel | `priya.patel@university.edu` |
| **TEACHER** | `dr.ross` | `ross123` | Dr. David Ross | `david.ross@university.edu` |
| **STUDENT** | `alice.w` | `alice123` | Alice Walker | `alice.walker@university.edu` |
| **STUDENT** | `david.k` | `david123` | David Kim | `david.kim@university.edu` |
| **STUDENT** | `clara.b` | `clara123` | Clara Bennett | `clara.bennett@university.edu` |
| **STUDENT** | `liam.o` | `liam123` | Liam O'Connor | `liam.oconnor@university.edu` |
| **STUDENT** | `maya.s` | `maya123` | Maya Sharma | `maya.sharma@university.edu` |

---

## 🚀 Getting Started

### Prerequisites
- **Java 21** or later
- **Maven 3.9+** (or use the included `./mvnw` / `mvnw.cmd`)

### Running Locally

1. **Start the application:**
   ```bash
   mvn spring-boot:run
   ```
   *Or using Maven Wrapper on Windows:*
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

2. **Access the portal:**
   Open your browser and navigate to:
   [http://localhost:8082](http://localhost:8082)

---

## 🧪 Testing Suite

The project includes unit and integration tests covering security, role-based authorization, and business logic:

### Run All Tests
```bash
mvn test
```

### Run Unit Tests Only
```bash
mvn test -Dtest=UserServiceTest
```
- `testCreateUser_Success`: Verifies user persistence, password hashing, and DTO conversion.
- `testCreateUser_DuplicateUsername_ThrowsException`: Rejects duplicate usernames.
- `testCreateUser_DuplicateEmail_ThrowsException`: Rejects duplicate emails.
- `testGetUserById_Success`: Retrieves user by ID.
- `testGetUserById_NotFound_ThrowsException`: Handles non-existent user lookup.
- `testDeleteUser_NotFound_ThrowsException`: Handles non-existent user deletion.

### Run Integration Tests Only
```bash
mvn test -Dtest=AuthIntegrationTest,AdminControllerIntegrationTest,TeacherStudentIntegrationTest
```
- **Auth Flow (`AuthIntegrationTest`)**: Verifies login authentication, JWT token issuance, and 401 Unauthorized handling.
- **Admin Endpoints (`AdminControllerIntegrationTest`)**: Verifies admin user management and verifies 403 Forbidden enforcement on non-admin roles.
- **Teacher & Student Endpoints (`TeacherStudentIntegrationTest`)**: Verifies faculty roster access, student self-management, and privilege containment.

---

## ⚙️ CI/CD Pipeline

Continuous Integration is configured via **GitHub Actions** in [`.github/workflows/maven.yml`](.github/workflows/maven.yml).
On every `push` and `pull_request` to `main`, the runner automatically builds the project and executes the full test suite.
