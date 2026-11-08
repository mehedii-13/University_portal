# University Web App – Workflow Explanation

This project is a Spring Boot-based university management system. It combines:

- a Java backend with Spring Boot, Spring Security, JPA, and PostgreSQL
- a static frontend built with HTML, CSS, and JavaScript
- JWT-based authentication
- role-based access control for admin, teacher, and student operations

---

## 1. Application startup

The application starts from the main class:

- `UniversityWebApplication.java`

When the app starts:

1. Spring Boot loads the project configuration from `application.yml`.
2. It creates the database connection to PostgreSQL.
3. It starts the embedded Tomcat server on port `8082`.
4. It serves static files from `src/main/resources/static`.
5. It runs `DataInitializer` once, which inserts demo users into the database.

The main idea is:

- the front end is served as HTML pages (`index.html`, `dashboard.html`)
- the backend exposes REST APIs under `/api/...`
- the frontend calls those APIs and updates the page using JavaScript

---

## 2. Database and user model

The main data model is `User.java`.

This entity represents each user in the system and includes:

- `id`
- `username`
- `password`
- `fullName`
- `email`
- `role`
- `enabled`
- timestamps (`createdAt`, `updatedAt`)

The `User` class implements Spring Security’s `UserDetails`, which allows Spring Security to manage login and permission checks.

The repository is:

- `UserRepository.java`

This interface extends `JpaRepository<User, Long>` and provides database actions like:

- find user by username
- find users by role
- check whether a username or email exists
- count users by role

---

## 3. Seed data and default accounts

The project automatically creates user records when the app starts. This is handled by:

- `DataInitializer.java`

It creates demo accounts such as:

- admin / admin123
- teacher1 / teacher123
- student1 / student123

These default values are used for testing and login purposes.

---

## 4. Security configuration

The security rules are defined in:

- `SecurityConfig.java`

This class configures:

- public access for the login page and static assets
- public access for `/api/auth/login`
- role-based restrictions for admin, teacher, and student APIs
- stateless JWT authentication

Important behavior:

- CSRF is disabled for this API-based app
- requests are not kept in server-side sessions
- each request is validated by a JWT token

---

## 5. JWT authentication workflow

JWT logic is handled by:

- `JwtAuthenticationFilter.java`
- `JwtService.java`

### Step-by-step login flow

1. The user opens the login page.
2. The browser fills `username` and `password` in the form.
3. `auth.js` listens to the form submit event.
4. It sends a POST request to `/api/auth/login` with JSON like:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

5. `AuthController.java` receives the request.
6. It calls `AuthenticationManager.authenticate(...)` using `UsernamePasswordAuthenticationToken`.
7. Spring Security checks the username and password against the database and password encoder.
8. If the credentials are correct:
   - Spring Security creates the authenticated user principal
   - `JwtService` generates a JWT token
   - `AuthController` builds an `AuthResponse`
   - the backend sends it back to the frontend as JSON

The response looks like this:

```json
{
  "token": "...jwt...",
  "id": 1,
  "username": "admin",
  "fullName": "System Administrator",
  "email": "admin@university.edu",
  "role": "ADMIN"
}
```

The frontend then stores this data in `localStorage` and redirects the user to `dashboard.html`.

---

## 6. How responses are sent

The backend uses Spring MVC’s `ResponseEntity` class.

Examples:

- `return ResponseEntity.ok(response);`
- `return ResponseEntity.status(401).body(...);`
- `return ResponseEntity.status(HttpStatus.CREATED).body(created);`
- `return ResponseEntity.badRequest().body(Map.of("error", ...));`

This means:

- the backend sends HTTP status codes like `200`, `201`, `401`, `400`, `403`, `404`
- it sends JSON data as the body
- the frontend reads that JSON using `fetch()` and updates the page

For example, in `auth.js`:

```javascript
const response = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username, password })
});

const data = await response.json();
```

Then it checks `response.ok` and handles the result.

---

## 7. Frontend login flow

The frontend logic is in:

- `src/main/resources/static/js/auth.js`

This script does the following:

1. checks whether a token already exists in localStorage
2. if yes, redirects the user to the dashboard
3. listens for the login form submit
4. validates that both username and password are filled
5. sends the login request to the backend
6. receives the response and stores token/profile data
7. redirects to the dashboard
8. shows errors if the login fails

The static login page is in:

- `src/main/resources/static/index.html`

It contains the form inputs and the login button. The page is served directly by Spring Boot from `static/`.

---

## 8. Protected dashboard and API calls

Once the user is logged in, the browser uses the JWT token in future requests.

The token is usually sent in the request header:

```http
Authorization: Bearer <jwt-token>
```

The `JwtAuthenticationFilter` intercepts every request, extracts the header, validates the JWT, and adds the authenticated user to the Spring Security context.

After that, the controller methods can check the user’s role and decide whether access is allowed.

---

## 9. Role-based controllers

The backend exposes separate controllers for each user type:

- `AdminController.java`
- `TeacherController.java`
- `StudentController.java`

Each controller uses:

- `@RestController`
- `@RequestMapping("/api/...")`
- `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`

These endpoints are responsible for CRUD operations on users by role.

### Admin operations

- Create admin accounts
- View admin list
- Edit admin profiles
- Delete admins (with restrictions)

### Teacher operations

- View teacher list
- Create a teacher account
- Update teacher profile
- Delete teacher account

### Student operations

- View student list
- Create a student account
- Update student profile
- Delete student account

The actual logic is handled by the service layer, not the controllers themselves.

---

## 10. Service layer responsibilities

The service class is:

- `UserService.java`

It handles:

- loading a user by username for Spring Security
- reading all users
- reading a single user by ID
- listing users by role
- creating new users
- updating an existing user
- deleting a user
- password encoding before saving

This keeps the controllers thin and clean.

Example:

- `UserService.createUser(...)` checks if username/email already exists
- then it encodes the password with `BCryptPasswordEncoder`
- then it saves the user to the database

---

## 11. How CRUD and response logic works together

The overall flow is:

1. Browser sends HTTP request to a backend endpoint.
2. Spring MVC routes the request to the matching controller method.
3. The controller calls the service layer.
4. The service talks to `UserRepository` and the database.
5. The service returns a DTO or data object.
6. The controller wraps it in `ResponseEntity` and sends JSON back.
7. The frontend receives the JSON and updates the page.

This is the standard Spring Boot request/response cycle for the app.

---

## 12. Final summary

The project is structured around a simple pattern:

- static HTML pages are loaded in the browser
- JavaScript fetches JSON from backend APIs
- Spring Boot controllers process the requests
- Spring Security authenticates and authorizes users
- JWT tokens are used for secure state across requests
- the database stores all user records
- the dashboard displays the role-specific data and operations

In short, the app is a full-stack Java web application where the frontend is lightweight and the backend handles all real logic, validation, security, and data access.
