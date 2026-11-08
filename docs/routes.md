# Route Flow and Response Handling

This project is a frontend + backend web app. The browser calls REST API endpoints, and the backend controller methods respond with JSON data or success/error messages.

---

## 1. Where routes are called from

### Frontend JavaScript call points

#### Login route
File: `src/main/resources/static/js/auth.js`

```javascript
const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
});
```

This is the entry point for login.

#### Dashboard data routes
File: `src/main/resources/static/js/dashboard.js`

```javascript
API.get('/api/teachers')
API.get('/api/students')
API.get('/api/admins')
```

These are triggered when the dashboard loads or a user switches tabs.

#### CRUD actions
Also in `dashboard.js`:

```javascript
API.post(`/api/${type}`, { ... })
API.put(`/api/${type}/${id}`, payload)
API.delete(`/api/${type}/${id}`)
```

These are called when an admin creates, edits, or deletes a teacher/student/admin.

---

## 2. Central API request layer

### File: `src/main/resources/static/js/api.js`
This file is the actual route caller used by the dashboard.

```javascript
async request(method, url, body = null) {
    const options = {
        method,
        headers: this.getHeaders()
    };

    if (body) {
        options.body = JSON.stringify(body);
    }

    const response = await fetch(url, options);
    const data = await response.json();
    return data;
}
```

This is where all API calls are centrally managed.

It adds:

```javascript
'Authorization': `Bearer ${this.getToken()}`
```

So every protected request goes to the backend with JWT authentication.

---

## 3. Where the responses go

### Frontend response handling
The result of each request is returned to the JavaScript that called it.

Examples:

```javascript
const teachers = await API.get('/api/teachers');
this.cachedRecords.teachers = teachers || [];
this.renderDirectoryTable('teachers', this.cachedRecords.teachers);
```

Or:

```javascript
const updated = await API.put(endpoint, payload);
this.user.fullName = updated.fullName;
```

So, the server response is sent back into the current page state and UI rendering logic.

If the response is an error:

```javascript
throw new Error(data.error || 'Request failed');
```

Then the caught error is shown as a notification:

```javascript
showNotification(e.message || 'Failed to update profile', 'error');
```

---

## 4. Backend endpoints receiving the calls

### Authentication routes
File: `src/main/java/com/example/universityweb/controller/AuthController.java`

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<?> login(...)

    @PostMapping("/logout")
    public ResponseEntity<?> logout()
}
```

### Admin routes
File: `src/main/java/com/example/universityweb/controller/AdminController.java`

```java
@RestController
@RequestMapping("/api/admins")
public class AdminController {

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllAdmins()

    @GetMapping("/{id}")
    public ResponseEntity<?> getAdminById(...)

    @PostMapping
    public ResponseEntity<?> createAdmin(...)

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdmin(...)

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAdmin(...)
}
```

### Teacher routes
File: `src/main/java/com/example/universityweb/controller/TeacherController.java`

```java
@RestController
@RequestMapping("/api/teachers")
public class TeacherController {
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllTeachers()
    @GetMapping("/{id}")
    public ResponseEntity<?> getTeacherById(...)
    @PostMapping
    public ResponseEntity<?> createTeacher(...)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTeacher(...)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTeacher(...)
}
```

### Student routes
File: `src/main/java/com/example/universityweb/controller/StudentController.java`

```java
@RestController
@RequestMapping("/api/students")
public class StudentController {
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllStudents()
    @GetMapping("/{id}")
    public ResponseEntity<?> getStudentById(...)
    @PostMapping
    public ResponseEntity<?> createStudent(...)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStudent(...)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudent(...)
}
```

---

## 5. Route flow example

### Example: Get all teachers

Frontend call:
```javascript
API.get('/api/teachers')
```

This goes to:
```java
@GetMapping
public ResponseEntity<List<UserDto>> getAllTeachers()
```

The controller calls:
```java
userService.getUsersByRole("TEACHER")
```

The service calls repository:
```java
userRepository.findByRole(Role.TEACHER)
```

Then the database result is converted into JSON and returned.

The JSON response then goes back to the browser, where the dashboard renders the table.

---

## 6. Example: Create a teacher

Frontend call:
```javascript
await API.post('/api/teachers', { fullName, username, email, password, role })
```

This maps to:
```java
@PostMapping
public ResponseEntity<?> createTeacher(@Valid @RequestBody UserDto dto)
```

Inside controller:
```java
dto.setRole("TEACHER");
UserDto created = userService.createUser(dto);
```

Then the service saves the user and returns a response.

That response is received in JS and displayed using `showNotification()` or by refreshing the table.

---

## 7. Security route flow

The backend also checks JWT before allowing access.

### File: `src/main/java/com/example/universityweb/config/JwtAuthenticationFilter.java`
This filter reads the `Authorization` header:

```java
final String authHeader = request.getHeader("Authorization");
```

If it contains a valid Bearer token, it authenticates the user before the controller method is called.

That means the controller only executes if the request is valid and authorized.

---

## 8. Summary of the flow

Frontend JS code calls API routes ->
`fetch(...)` sends request to backend ->
Spring controller receives the route ->
service layer handles business logic ->
repository fetches or updates database ->
response JSON goes back to frontend ->
frontend updates UI state or shows notification.

---

## 9. Main route pattern

```text
Browser (JS) -> /api/xxx -> Controller -> Service -> Repository -> Database
                                      ^
                                      |
                               JSON Response back
```

This is the overall route and response flow of the system.
