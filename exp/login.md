# Login System Explanation

This project uses a Spring Boot + Spring Security login system with JWT (JSON Web Token) authentication.

The login flow is:

1. User enters username and password in the frontend
2. Frontend sends those values to the backend API
3. Spring Security validates the username and password
4. If valid, the backend creates a JWT token
5. The frontend stores the token in localStorage
6. Later requests send the token in the Authorization header
7. The backend checks the token before allowing protected API calls

---

## 1. Frontend login form

The login page is in:

- `src/main/resources/static/index.html`
- `src/main/resources/static/js/auth.js`

When the user submits the form, JavaScript reads the username and password values.

Then it sends a POST request to:

```http
/api/auth/login
```

with JSON like:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

This is done in the `handleLoginSubmit` function in `auth.js`.

If the login is successful, the browser stores the token and user details in localStorage:

```javascript
localStorage.setItem('token', data.token);
localStorage.setItem('userId', data.id);
localStorage.setItem('username', data.username);
localStorage.setItem('role', data.role);
```

Then the user is redirected to the dashboard page.

---

## 2. Backend login endpoint

The endpoint is defined in:

- `src/main/java/com/example/universityweb/controller/AuthController.java`

It is mapped to:

```java
@PostMapping("/login")
```

The method receives a request body of type `AuthRequest`.

The code does:

```java
Authentication authentication = authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(
        request.getUsername(),
        request.getPassword()
    )
);
```

This is the actual password verification step.

Spring Security uses the configured `AuthenticationManager` and `UserDetailsService` to check whether the username and password match a saved user.

---

## 3. User model and Spring Security identity

The user entity is in:

- `src/main/java/com/example/universityweb/model/User.java`

This class implements `UserDetails`, which is required by Spring Security.

That means the `User` object can provide:

- username
- password
- roles/authorities
- account enabled status

The `getAuthorities()` method returns a role like:

```java
return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
```

So a user with role `ADMIN` gets authority `ROLE_ADMIN`.

A teacher gets `ROLE_TEACHER` and a student gets `ROLE_STUDENT`.

---

## 4. User loading and password matching

The user lookup service is in:

- `src/main/java/com/example/universityweb/service/UserService.java`

It implements `UserDetailsService` and loads a user by username:

```java
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
}
```

This is how Spring Security finds the user from the database.

Passwords are stored using BCrypt encoding.

The password encoder is configured in:

- `src/main/java/com/example/universityweb/config/SecurityConfig.java`

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

So even when a user logs in, the system compares the typed password with the encoded version in the database.

---

## 5. JWT generation

After successful authentication, the backend creates a JWT token using `JwtService`.

The JWT service is in:

- `src/main/java/com/example/universityweb/config/JwtService.java`

This service adds extra claims such as:

- userId
- role
- fullName
- email

Then it signs the token and returns it to the frontend.

Example response:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "id": 1,
  "username": "admin",
  "fullName": "System Administrator",
  "email": "admin@university.edu",
  "role": "ADMIN"
}
```

This token is used as proof that the user has logged in.

---

## 6. Security configuration

The main security rules are set in:

- `src/main/java/com/example/universityweb/config/SecurityConfig.java`

The application allows public access to:

- `/api/auth/login`
- `/api/auth/logout`
- static pages like `/` and `/index.html`
- CSS and JS files

Protected routes are restricted by role:

```java
.requestMatchers("/api/admins/**").hasRole("ADMIN")
.requestMatchers(HttpMethod.POST, "/api/teachers").hasRole("ADMIN")
.requestMatchers(HttpMethod.GET, "/api/teachers/**").authenticated()
```

This means some API endpoints are available only to admins, while others are only accessible to authenticated users.

---

## 7. JWT filter for later requests

Every request after login may include the JWT in the Authorization header:

```http
Authorization: Bearer <token>
```

This is handled by:

- `src/main/java/com/example/universityweb/config/JwtAuthenticationFilter.java`

The filter does:

1. Reads the `Authorization` header
2. Extracts the JWT token
3. Reads the username from the token
4. Loads the user from the database
5. Verifies token validity and user match
6. Sets Spring Security authentication context for the request

If the token is valid, the user is treated as authenticated for that request.

This allows the system to protect APIs without using server-side sessions.

---

## 8. Default demo accounts

The app automatically creates demo users at startup using `DataInitializer`.

File:

- `src/main/java/com/example/universityweb/config/DataInitializer.java`

It seeds the following users:

- Admin: `admin` / `admin123`
- Teacher: `teacher1` / `teacher123`
- Student: `student1` / `student123`

These credentials are also shown on the login page in `index.html`.

---

## 9. Overall flow summary

The whole login process works like this:

1. User enters username and password
2. Browser sends POST `/api/auth/login`
3. Spring Security checks the user in the database
4. Password is validated using BCrypt
5. If correct, a JWT token is generated
6. Token is sent back to the browser
7. Browser stores token in localStorage
8. Later API requests send token in the `Authorization` header
9. JWT filter validates token and grants access to protected endpoints

---

## In one sentence

The login system is a Spring Security + JWT authentication system where the frontend submits username/password, the backend checks them against the database, and a signed token is returned and used for secure access to protected API routes.
