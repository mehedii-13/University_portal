# University Web - System Description

This project is a Spring Boot web application for managing users (Admin, Teacher, Student) with a JWT-based login system and role-based access control. The frontend is served from the static resources folder, and the backend exposes REST APIs under `/api`.

---

## 1. Project Structure and Purpose

### Main Java package
- `src/main/java/com/example/universityweb/`
  - Contains the application boot class, security configuration, models, repositories, services, controllers, and DTOs.

### Static frontend files
- `src/main/resources/static/`
  - Contains the login page, dashboard page, CSS, and JavaScript files used by the browser.

### Configuration files
- `application.yml` / `application.properties`
  - Contains app settings, DB configuration, and JWT secret values.

---

## 2. Application Startup

### File: `src/main/java/com/example/universityweb/UniversityWebApplication.java`
- This is the Spring Boot entry point.
- It starts the application using `SpringApplication.run(...)`.
- When the app starts, Spring Boot initializes the application context, loads the database, and enables security and controllers.

---

## 3. Role and User Model

### File: `src/main/java/com/example/universityweb/model/Role.java`
- Enum that defines the available roles:
  - `ADMIN`
  - `TEACHER`
  - `STUDENT`

### File: `src/main/java/com/example/universityweb/model/User.java`
- This is the main database entity.
- It represents each user in the system.
- It implements `UserDetails`, which is required by Spring Security.
- It stores:
  - `id`
  - `username`
  - `password`
  - `fullName`
  - `email`
  - `role`
  - `enabled`
  - timestamps
- `getAuthorities()` returns a Spring Security role like:
  - `ROLE_ADMIN`
  - `ROLE_TEACHER`
  - `ROLE_STUDENT`

This means the system can check permissions like:
- `hasRole('ADMIN')`
- `authenticated()`

---

## 4. Data Access Layer

### File: `src/main/java/com/example/universityweb/repository/UserRepository.java`
- This is the repository interface for database interaction.
- It extends `JpaRepository<User, Long>`.
- It provides methods such as:
  - `findByUsername(String username)`
  - `findByEmail(String email)`
  - `existsByUsername(String username)`
  - `existsByEmail(String email)`
  - `findByRole(Role role)`
- These methods are used for login, user validation, and role-based listing.

---

## 5. DTO Classes (Data Transfer Objects)

### File: `src/main/java/com/example/universityweb/dto/AuthRequest.java`
- Used for login requests.
- Contains:
  - `username`
  - `password`
- Validation is applied with `@NotBlank`.

### File: `src/main/java/com/example/universityweb/dto/AuthResponse.java`
- Used for login response.
- Returned by the backend after successful authentication.
- Contains:
  - `token`
  - `id`
  - `username`
  - `fullName`
  - `email`
  - `role`

### File: `src/main/java/com/example/universityweb/dto/UserDto.java`
- Used for CRUD operations on users.
- Includes fields for creating and updating a user profile.
- Contains:
  - `username`
  - `password`
  - `fullName`
  - `email`
  - `role`
  - `enabled`
- It is used by Admin, Teacher, and Student controllers to send user data to the frontend.

---

## 6. Business Logic Layer

### File: `src/main/java/com/example/universityweb/service/UserService.java`
- This is the main service class for user-related business logic.
- It implements `UserDetailsService`, which is required for Spring Security login.
- Main responsibilities:
  - load user by username for authentication
  - create new users
  - fetch all users
  - find user by id
  - list users by role
  - update user profile
  - delete user
  - validate duplicate username/email
  - encode passwords using BCrypt

Important method:
```java
@Override
public UserDetails loadUserByUsername(String username)
```
This method finds the user in the database and returns it to Spring Security during login.

---

## 7. Security Configuration and Authentication

### File: `src/main/java/com/example/universityweb/config/SecurityConfig.java`
- This file configures Spring Security.
- Main responsibilities:
  - disable CSRF for stateless API usage
  - allow public access to login and static pages
  - protect admin/student/teacher endpoints by role
  - configure JWT filter
  - configure password encoder
  - create `AuthenticationManager`

Important rules:
- Public endpoints:
  - `/api/auth/login`
  - `/api/auth/logout`
  - `/`
  - `/index.html`
  - `/dashboard.html`
  - CSS and JS files
- Admin endpoints:
  - `/api/admins/**`
- Teacher endpoints:
  - `POST /api/teachers` -> admin only
  - `GET /api/teachers/**` -> authenticated users
- Student endpoints:
  - similar role-based restrictions

Password encoding:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```
This ensures all user passwords are stored safely and compared using BCrypt.

---

## 8. JWT Authentication

### File: `src/main/java/com/example/universityweb/config/JwtService.java`
- This class generates and validates JWT tokens.
- It contains methods to:
  - extract username from token
  - generate a signed token
  - validate token expiration
  - read claims
- The token includes extra claims such as:
  - userId
  - role
  - fullName
  - email

This token is then returned to the frontend and used as proof of login.

### File: `src/main/java/com/example/universityweb/config/JwtAuthenticationFilter.java`
- This filter runs on every incoming HTTP request.
- It checks the `Authorization` header.
- If the header starts with `Bearer `, it:
  1. extracts the token
  2. reads the username from the token
  3. loads the user from the database
  4. validates the token
  5. sets the user into Spring Security context
- This allows the backend to know who the current user is on protected requests.

---

## 9. Login Flow (Full Explanation)

### Files involved:
- `src/main/resources/static/index.html`
- `src/main/resources/static/js/auth.js`
- `src/main/java/com/example/universityweb/controller/AuthController.java`
- `src/main/java/com/example/universityweb/service/UserService.java`
- `src/main/java/com/example/universityweb/config/SecurityConfig.java`
- `src/main/java/com/example/universityweb/config/JwtService.java`

### Flow:
1. User opens the login page.
2. The browser fills username and password.
3. `auth.js` listens to the form submit event.
4. It sends a POST request to `/api/auth/login`.
5. The backend receives `AuthRequest` in `AuthController`.
6. `AuthenticationManager.authenticate(...)` checks the username and password.
7. Spring Security loads the user by username using `UserService.loadUserByUsername()`.
8. If valid, a JWT token is generated by `JwtService`.
9. The token and user details are returned to the browser in `AuthResponse`.
10. The frontend stores the token in `localStorage`.
11. The user is redirected to the dashboard page.
12. Later requests include `Authorization: Bearer <token>`.
13. `JwtAuthenticationFilter` validates the token before allowing protected API access.

### Login API example
```json
{
  "username": "admin",
  "password": "admin123"
}
```

### Successful response example
```json
{
  "token": "...",
  "id": 1,
  "username": "admin",
  "fullName": "System Administrator",
  "email": "admin@university.edu",
  "role": "ADMIN"
}
```

---

## 10. Default Demo Users

### File: `src/main/java/com/example/universityweb/config/DataInitializer.java`
- This file automatically creates demo accounts when the application starts.
- It uses `UserRepository` and `PasswordEncoder`.
- It seeds users such as:
  - Admin: `admin` / `admin123`
  - Teacher: `teacher1` / `teacher123`
  - Student: `student1` / `student123`

This is useful for testing and demonstration without manually creating users.

---

## 11. Frontend Dashboard and UI

### File: `src/main/resources/static/index.html`
- This is the login page.
- It contains the login form and demo credentials section.

### File: `src/main/resources/static/js/auth.js`
- Handles login form submission.
- Sends the request to the backend.
- Stores token and user info in localStorage.
- Redirects to `/dashboard.html` after a successful login.

### File: `src/main/resources/static/dashboard.html`
- This is the main dashboard page after login.
- It loads the navigation bar and content area.
- It includes JavaScript files for API calls and dashboard behavior.

### File: `src/main/resources/static/js/api.js`
- Handles all API requests from the frontend.
- Adds the JWT token to the `Authorization` header.
- Redirects the user to login if the token is missing or invalid.
- Includes helper functions for notifications and modal UI.

### File: `src/main/resources/static/js/dashboard.js`
- Renders dashboard content dynamically.
- Shows teacher/student/admin lists depending on user role.
- Provides actions for viewing, editing, creating, and deleting records.

---

## 12. Admin CRUD Flow

### Files involved:
- `src/main/java/com/example/universityweb/controller/AdminController.java`
- `src/main/java/com/example/universityweb/service/UserService.java`
- `src/main/java/com/example/universityweb/dto/UserDto.java`
- `src/main/java/com/example/universityweb/repository/UserRepository.java`
- `src/main/resources/static/js/dashboard.js`
- `src/main/resources/static/js/api.js`

### Admin routes
Base path:
```text
/api/admins
```

### 1) Get all admins
```http
GET /api/admins
```
- `AdminController.getAllAdmins()`
- Calls `userService.getUsersByRole("ADMIN")`
- Returns a list of admin users

### 2) Get one admin by ID
```http
GET /api/admins/{id}
```
- Verifies that the returned user actually has the `ADMIN` role
- Returns 404 if the admin does not exist or is not an admin

### 3) Create admin
```http
POST /api/admins
```
- Accepts a `UserDto` body
- Sets `dto.setRole("ADMIN")`
- Calls `userService.createUser(dto)`
- Saves data to DB
- Password is encoded with BCrypt
- Response is `201 Created`

### 4) Update admin
```http
PUT /api/admins/{id}
```
- Validates that the target record exists and is an admin
- Sets role to `ADMIN`
- Calls `userService.updateUser(id, dto)`
- Updates name, email, password, role, and enabled status if provided

### 5) Delete admin
```http
DELETE /api/admins/{id}
```
- Checks current logged-in user
- Prevents the admin from deleting their own account
- Verifies the record is an admin before deletion
- Calls `userService.deleteUser(id)`

### Admin security rule
The class is protected with:
```java
@PreAuthorize("hasRole('ADMIN')")
```
This means only authenticated users with the `ADMIN` role can access admin endpoints.

---

## 13. Teacher and Student CRUD

The system also includes similar logic for teachers and students:

### Teacher endpoints
- `GET /api/teachers`
- `GET /api/teachers/{id}`
- `POST /api/teachers`
- `PUT /api/teachers/{id}`
- `DELETE /api/teachers/{id}`

### Student endpoints
- `GET /api/students`
- `GET /api/students/{id}`
- `POST /api/students`
- `PUT /api/students/{id}`
- `DELETE /api/students/{id}`

Their controllers are similar to the admin controller but restrict some update actions to the user themselves unless the current user is an admin.

---

## 14. End-to-End Flow of the System

1. Application starts.
2. `DataInitializer` creates demo users.
3. User opens the browser.
4. User enters login credentials.
5. Frontend sends login request to `/api/auth/login`.
6. Spring Security validates the username and password.
7. If valid, a JWT token is generated.
8. The browser saves the token and user metadata.
9. User reaches the dashboard.
10. Dashboard loads teacher/student/admin lists using authenticated API calls.
11. Admin can create, view, update, and delete users based on role restrictions.
12. Every request is protected by JWT and role checks.

---

## 15. In One Sentence

This project is a role-based university management application where users log in through Spring Security with JWT authentication, and admins manage other users through CRUD operations while teachers and students have limited access based on their roles.
