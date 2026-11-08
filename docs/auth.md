# Authorization Flow in the University Web App

This project uses Spring Security with JWT-based authorization. The flow starts from the browser login page and ends with the backend checking the token before allowing access to protected routes.

---

## 1. Login request starts from the frontend

### File: src/main/resources/static/index.html
This is the login screen HTML page.

### File: src/main/resources/static/js/auth.js
This file sends the username and password to the backend login API.

Example logic:

```javascript
const response = await fetch('/api/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username, password })
});
```

After a successful login, the browser stores the token in localStorage.

```javascript
localStorage.setItem('token', data.token);
```

---

## 2. Authentication endpoint creates JWT

### File: src/main/java/com/example/universityweb/controller/AuthController.java
This class handles `/api/auth/login`.

Important code flow:

1. The controller receives the username and password.
2. It calls `AuthenticationManager.authenticate(...)`.
3. Spring checks the user credentials.
4. If credentials are valid, it creates a JWT using `JwtService`.
5. The token is returned to the browser in `AuthResponse`.

```java
Authentication authentication = authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(
        request.getUsername(),
        request.getPassword()
    )
);
```

Then it adds extra claims:

```java
extraClaims.put("userId", user.getId());
extraClaims.put("role", user.getRole().name());
extraClaims.put("fullName", user.getFullName());
extraClaims.put("email", user.getEmail());
```

And generates a token:

```java
String token = jwtService.generateToken(extraClaims, user);
```

The response returned to the client includes:

- token
- id
- username
- fullName
- email
- role

---

## 3. JWT generation and validation

### File: src/main/java/com/example/universityweb/config/JwtService.java
This class creates and validates JWT tokens.

Key methods:

- `generateToken(...)`
- `extractUsername(...)`
- `isTokenValid(...)`

The token is signed using a secret key and has an expiration time.

```java
return Jwts.builder()
    .claims(extraClaims)
    .subject(userDetails.getUsername())
    .issuedAt(new Date(System.currentTimeMillis()))
    .expiration(new Date(System.currentTimeMillis() + expiration))
    .signWith(getSignInKey())
    .compact();
```

Validation logic:

```java
public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
}
```

This ensures that:

- token belongs to the same user
- token is not expired
- token signature is valid

---

## 4. Spring Security rules are defined here

### File: src/main/java/com/example/universityweb/config/SecurityConfig.java
This is the main authorization policy file.

It configures:

- public endpoints
- authenticated endpoints
- role-based access
- stateless JWT sessions
- JWT filter registration

Important configuration:

```java
.requestMatchers("/api/auth/login", "/api/auth/logout").permitAll()
.requestMatchers("/api/admins/**").hasRole("ADMIN")
.requestMatchers(HttpMethod.POST, "/api/teachers").hasRole("ADMIN")
.requestMatchers(HttpMethod.PUT, "/api/teachers/**").hasAnyRole("ADMIN", "TEACHER")
.requestMatchers(HttpMethod.GET, "/api/students/**").authenticated()
```

This means:

- login and logout are public
- only admins can access admin APIs
- certain teacher/student operations require role checks
- all other routes require authentication

Security is set to stateless:

```java
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

This is important because JWT-based authorization does not use a server-side session.

---

## 5. JWT filter intercepts every request

### File: src/main/java/com/example/universityweb/config/JwtAuthenticationFilter.java
This filter runs before the controller logic for each incoming request.

It reads the Authorization header:

```java
final String authHeader = request.getHeader("Authorization");
```

Then it checks whether the token begins with `Bearer `.

```java
if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    filterChain.doFilter(request, response);
    return;
}
```

If a valid token is found, it loads the user and validates the token:

```java
UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

if (jwtService.isTokenValid(jwt, userDetails)) {
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails.getAuthorities()
    );
    SecurityContextHolder.getContext().setAuthentication(authToken);
}
```

Once authentication is set in the Spring Security context, the request is treated as an authenticated user.

If the token is invalid, it logs a warning and continues without authentication.

---

## 6. Frontend sends token on every protected request

### File: src/main/resources/static/js/api.js
This is the main API helper used by the dashboard.

It adds the token to every request:

```javascript
getHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getToken()}`
    };
}
```

The fetch wrapper then sends the token in the request headers.

```javascript
const response = await fetch(url, options);
```

If the server responds with 401 or 403, the app clears localStorage and redirects to the login page:

```javascript
if (response.status === 401 || response.status === 403) {
    localStorage.clear();
    window.location.href = '/';
    return null;
}
```

This is the client-side authorization check after a failed protected request.

---

## 7. How role-based access works in practice

### Example: Admin access
Admin dashboard/API requests are restricted in `SecurityConfig.java`:

```java
.requestMatchers("/api/admins/**").hasRole("ADMIN")
```

So if a logged-in user has role `ADMIN`, they are allowed.
If they do not have the admin role, the backend denies access.

### Example: Teacher or student update

```java
.requestMatchers(HttpMethod.PUT, "/api/teachers/**").hasAnyRole("ADMIN", "TEACHER")
.requestMatchers(HttpMethod.PUT, "/api/students/**").hasAnyRole("ADMIN", "STUDENT")
```

This allows the owner or admin to update a profile, depending on the role.

---

## 8. End-to-end flow summary

The entire authorization flow is:

1. User enters username/password on the login page.
   - File: `src/main/resources/static/js/auth.js`

2. Browser sends request to `/api/auth/login`.
   - File: `src/main/java/com/example/universityweb/controller/AuthController.java`

3. `AuthenticationManager` validates the credentials.

4. If valid, `JwtService` creates a signed JWT.

5. Browser stores the JWT in localStorage.

6. Each later API request sends `Authorization: Bearer <token>`.
   - File: `src/main/resources/static/js/api.js`

7. `JwtAuthenticationFilter` reads the token.
   - File: `src/main/java/com/example/universityweb/config/JwtAuthenticationFilter.java`

8. Spring Security validates the token and sets authentication in the context.

9. `SecurityConfig` checks role rules before allowing access to the endpoint.

10. If authorized, the controller method executes; otherwise request fails with 401 or 403.

---

## 9. Main files involved in authorization

- src/main/resources/static/js/auth.js
- src/main/resources/static/js/api.js
- src/main/java/com/example/universityweb/controller/AuthController.java
- src/main/java/com/example/universityweb/config/SecurityConfig.java
- src/main/java/com/example/universityweb/config/JwtAuthenticationFilter.java
- src/main/java/com/example/universityweb/config/JwtService.java
- src/main/java/com/example/universityweb/service/UserService.java
- src/main/java/com/example/universityweb/model/User.java

---

## Final idea

The application uses JWT for stateless authorization. The browser logs in once, receives a token, and includes that token in subsequent requests. Spring Security checks the token, verifies the user, and applies the role-based access rules defined in `SecurityConfig.java` before allowing controller logic to run.
