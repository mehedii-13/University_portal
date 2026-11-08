# How Data Gets Fetched in This Project

This project fetches data from the backend through JavaScript using the browser Fetch API. The flow is:

1. User logs in successfully.
2. The backend returns a JWT token and user details.
3. The frontend stores the token and user info in localStorage.
4. Every later API request sends the token in the `Authorization` header.
5. The backend verifies the token and returns the requested data.

---

## 1. Login and token storage

### File: `src/main/resources/static/js/auth.js`
When the user submits the login form, the browser sends a POST request to:

```http
/api/auth/login
```

The request body contains:

```json
{
  "username": "admin",
  "password": "admin123"
}
```

If login succeeds, the frontend stores values like:

```javascript
localStorage.setItem('token', data.token);
localStorage.setItem('userId', data.id);
localStorage.setItem('username', data.username);
localStorage.setItem('fullName', data.fullName);
localStorage.setItem('email', data.email);
localStorage.setItem('role', data.role);
```

This means the browser now has everything required to call protected APIs.

---

## 2. Central API helper

### File: `src/main/resources/static/js/api.js`
This file defines the main API helper object:

```javascript
const API = {
    getToken() {
        return localStorage.getItem('token');
    },

    getHeaders() {
        return {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.getToken()}`
        };
    },

    async request(method, url, body = null) {
        const options = {
            method,
            headers: this.getHeaders()
        };

        if (body) {
            options.body = JSON.stringify(body);
        }

        const response = await fetch(url, options);
```

This is the main place where data fetching happens.

### Important behavior
- Every request attaches the JWT token in the Authorization header.
- If the status is `401` or `403`, the app clears localStorage and redirects to login.
- If the backend returns an error JSON, it throws an error message.

So every fetch request goes through this single helper.

---

## 3. Dashboard data loading

### File: `src/main/resources/static/js/dashboard.js`
Once the dashboard loads, it calls `startDashboard()`:

```javascript
this.user = {
    id: localStorage.getItem('userId') ? parseInt(localStorage.getItem('userId')) : null,
    role: role.toUpperCase(),
    fullName: localStorage.getItem('fullName') || 'User',
    username: localStorage.getItem('username') || '',
    email: localStorage.getItem('email') || ''
};
```

Then it calls:

```javascript
this.switchPanel('overview');
```

That triggers `refreshOverview()`, which fetches data for teachers and students:

```javascript
const [teachers, students] = await Promise.all([
    API.get('/api/teachers'),
    API.get('/api/students')
]);
```

If the user is an admin, it also fetches:

```javascript
const admins = await API.get('/api/admins');
```

This is how the dashboard gets the list of people in the system.

---

## 4. Role-based fetching

The dashboard loads different endpoints based on the user role:

- Teachers: `/api/teachers`
- Students: `/api/students`
- Admins: `/api/admins`

Examples:

```javascript
async loadTeacherRoster() {
    const teachers = await API.get('/api/teachers');
    this.cachedRecords.teachers = teachers || [];
    this.renderDirectoryTable('teachers', this.cachedRecords.teachers);
}
```

```javascript
async loadStudentRoster() {
    const students = await API.get('/api/students');
    this.cachedRecords.students = students || [];
    this.renderDirectoryTable('students', this.cachedRecords.students);
}
```

```javascript
async loadAdminRoster() {
    const admins = await API.get('/api/admins');
    this.cachedRecords.admins = admins || [];
    this.renderDirectoryTable('admins', this.cachedRecords.admins);
}
```

---

## 5. Backend endpoints that return data

### Admin controller
File: `src/main/java/com/example/universityweb/controller/AdminController.java`

Example:

```java
@GetMapping
public ResponseEntity<List<UserDto>> getAllAdmins() {
    return ResponseEntity.ok(userService.getUsersByRole("ADMIN"));
}
```

### Teacher controller
File: `src/main/java/com/example/universityweb/controller/TeacherController.java`

```java
@GetMapping
public ResponseEntity<List<UserDto>> getAllTeachers() {
    return ResponseEntity.ok(userService.getUsersByRole("TEACHER"));
}
```

### Student controller
File: `src/main/java/com/example/universityweb/controller/StudentController.java`

```java
@GetMapping
public ResponseEntity<List<UserDto>> getAllStudents() {
    return ResponseEntity.ok(userService.getUsersByRole("STUDENT"));
}
```

These methods return user data from the database.

---

## 6. Service layer for database queries

### File: `src/main/java/com/example/universityweb/service/UserService.java`
This service calls the repository to fetch data by role:

```java
public List<UserDto> getUsersByRole(String role) {
    Role r = Role.valueOf(role.toUpperCase());
    return userRepository.findByRole(r).stream()
            .map(this::toDto)
            .collect(Collectors.toList());
}
```

The repository then queries the database:

```java
List<User> findByRole(Role role);
```

So the data flow is:

Frontend JS -> API helper -> REST endpoint -> UserService -> UserRepository -> database -> response JSON -> dashboard rendering

---

## 7. Example fetch flow for teacher data

1. User clicks Teachers in the dashboard.
2. `loadTeacherRoster()` runs.
3. It calls `API.get('/api/teachers')`.
4. The browser sends GET request with Bearer token.
5. Spring Security verifies the JWT.
6. `TeacherController.getAllTeachers()` is triggered.
7. `UserService.getUsersByRole("TEACHER")` runs.
8. `UserRepository.findByRole(Role.TEACHER)` fetches matching users.
9. The backend converts them to DTOs and returns JSON.
10. The frontend stores the JSON into `cachedRecords.teachers`.
11. `renderDirectoryTable()` builds the HTML table on the page.

---

## 8. Example fetch flow for profile data

When the user opens My Profile:

```javascript
latestUser = await API.get(`${endpoint}/${this.user.id}`);
```

This loads the current user details from:

- `/api/admins/{id}`
- `/api/teachers/{id}`
- `/api/students/{id}`

Then the page fills the profile form with the returned values.

---

## 9. In one sentence

The application fetches data by sending authenticated HTTP requests from the frontend, which include the JWT token in the Authorization header, and the backend responds with JSON data from the database using the controller-service-repository flow.
