# Frontend API Integration Report

## 1) Project Snapshot
- **Backend:** Spring Boot 3 (`training-center-management`)
- **Architecture:** Controller -> Service -> Repository -> Entity
- **Persistence:** Spring Data JPA + H2 (current `application.properties`)
- **Security:** HTTP Basic auth (custom `UserDetailsService`)
- **Payments:** Stripe PaymentIntent + webhook callbacks
- **API Docs:** Swagger UI (Springdoc)

## 2) Swagger / OpenAPI
After running the backend:
- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

## 3) Auth Contract (Important for FE)
- **Auth type:** HTTP Basic
- FE sends `Authorization: Basic base64(username:password)` for protected endpoints.
- Public endpoints are currently allowed by `SecurityConfig` route rules (see caveat below).

### Caveat
`SecurityConfig` currently uses path-based `permitAll()` without HTTP method restrictions on many routes (e.g. `/api/courses/**`), so some write operations may be publicly accessible even if not intended.

## 4) Core Endpoint Catalog

### 4.1 Auth
- `POST /api/auth/login`

### 4.2 Users
- `POST /api/users`
- `GET /api/users`
- `GET /api/users/{id}`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}`

### 4.3 Students
- `POST /api/students`
- `GET /api/students`
- `GET /api/students/{id}`
- `PUT /api/students/{id}`
- `DELETE /api/students/{id}`
- `GET /api/students/{id}/training-hours`
- `GET /api/students/{id}/completion-percentage`
- `GET /api/students/{id}/weekly-schedule`

### 4.4 Teachers
- `POST /api/teachers`
- `GET /api/teachers`
- `GET /api/teachers/{id}`
- `PUT /api/teachers/{id}`
- `DELETE /api/teachers/{id}`
- `GET /api/teachers/{id}/course-progress`
- `GET /api/teachers/{id}/weekly-schedule`

### 4.5 Courses
- `POST /api/courses`
- `GET /api/courses/{id}`
- `GET /api/courses/search?name=...&tenantId=...`
- `GET /api/courses/category/{categoryId}/tenant/{tenantId}`
- `GET /api/courses/tenant/{tenantId}`
- `GET /api/courses/active`
- `PUT /api/courses/{id}`
- `DELETE /api/courses/{id}`

### 4.6 Training Sessions
- `POST /api/training-sessions`
- `GET /api/training-sessions/{id}`
- `GET /api/training-sessions/sessions-with-filter?category=&instituteName=&minPrice=&maxPrice=&location=`
- `GET /api/training-sessions/institute/{instituteId}`
- `GET /api/training-sessions/tenant/{tenantId}`
- `GET /api/training-sessions/active`
- `PUT /api/training-sessions/{id}`
- `DELETE /api/training-sessions/{id}`

### 4.7 Institutes
- `POST /api/institutes`
- `GET /api/institutes`
- `GET /api/institutes/{id}`
- `GET /api/institutes/tenant/{tenantId}`
- `GET /api/institutes/{id}/registration-monthly?year=2026`
- `PUT /api/institutes/{id}`
- `DELETE /api/institutes/{id}`

### 4.8 Classrooms
- `POST /api/classrooms`
- `GET /api/classrooms/{id}`
- `GET /api/classrooms/institute/{instituteId}`
- `GET /api/classrooms/search/device?device=...&instituteId=...`
- `PUT /api/classrooms/{id}`
- `DELETE /api/classrooms/{id}`

### 4.9 Lectures
- `GET /api/lectures`
- `GET /api/lectures/{id}`
- `GET /api/lectures/session/{sessionId}`
- `POST /api/lectures/session/{sessionId}`
- `PUT /api/lectures/{id}`
- `DELETE /api/lectures/{id}`

### 4.10 Attendance
- `POST /api/attendance/bulk`
- `GET /api/attendance/lecture/{lectureId}`
- `GET /api/attendance/student/{studentId}`

### 4.11 Enrollments
- `POST /api/enrollments`
- `DELETE /api/enrollments/{id}`
- `GET /api/enrollments/sessions/{sessionId}`
- `GET /api/enrollments/students/distinct`

### 4.12 Quiz / Grades / Ratings
- Quiz:
  - `POST /api/quizzes`
  - `PUT /api/quizzes/{id}`
  - `DELETE /api/quizzes/{id}`
  - `GET /api/quizzes/{id}`
  - `GET /api/training-sessions/{sessionId}/quizzes`
- Grade:
  - `POST /api/grades`
  - `PUT /api/grades/{id}`
  - `GET /api/grades/student/{studentId}/quiz/{quizId}`
  - `GET /api/grades/student/{studentId}`
  - `GET /api/grades/quiz/{quizId}`
  - `DELETE /api/grades/{id}`
- Rating:
  - `POST /api/courses/{courseId}/ratings?studentId=...`
  - `PUT /api/ratings/{ratingId}?studentId=...`
  - `DELETE /api/ratings/{ratingId}?studentId=...`

### 4.13 OTP / Register / Tenant / Category / Payment / Webhook
- OTP:
  - `POST /api/otp/send?email=...`
  - `POST /api/otp/verify?email=...&code=...`
- Register:
  - `POST /api/registers`
  - `GET /api/registers/{id}`
- Tenant:
  - `POST /api/tenants`
  - `GET /api/tenants`
  - `GET /api/tenants/{id}`
  - `PUT /api/tenants/{id}`
  - `DELETE /api/tenants/{id}`
- Category:
  - `POST /api/categories`
  - `GET /api/categories`
  - `DELETE /api/categories/{id}`
- Payment:
  - `POST /api/payments/initiate/{sessionId}`
- Stripe Webhook:
  - `POST /webhook/stripe`

## 5) Frontend Payload Contracts (Most Important)

### 5.1 Create Student (Unified: User + Student)
`POST /api/students`
```json
{
  "username": "student1",
  "email": "student1@example.com",
  "password": "Pass@123",
  "confirmPassword": "Pass@123",
  "contactInfo": "0790000000",
  "image": "https://...",
  "firstName": "Ali",
  "lastName": "Ahmad",
  "gender": "Male",
  "birthDate": "2002-01-15",
  "address": "Amman",
  "interest": "IT"
}
```

### 5.2 Create Teacher (supports two flows)
`POST /api/teachers`

**Flow A - Unified registration (recommended for FE form):**
```json
{
  "firstName": "Sara",
  "lastName": "Hassan",
  "username": "sara.teacher",
  "email": "sara@example.com",
  "password": "Pass@123",
  "confirmPassword": "Pass@123",
  "specialization": "Java",
  "phone": "0791111111",
  "address": "Amman",
  "yearsOfExperience": 5,
  "cv": "https://example.com/cv.pdf"
}
```

**Flow B - Link to existing user:**
```json
{
  "userId": 12,
  "firstName": "Sara",
  "lastName": "Hassan",
  "specialization": "Java",
  "address": "Amman",
  "experienceYears": 5
}
```

### 5.3 Create Enrollment
`POST /api/enrollments`
```json
{
  "studentId": 1,
  "trainingSessionId": 10
}
```

### 5.4 Bulk Attendance
`POST /api/attendance/bulk`
```json
{
  "lectureId": 100,
  "records": [
    { "studentId": 1, "status": "PRESENT" },
    { "studentId": 2, "status": "ABSENT" }
  ]
}
```

## 6) Dashboard-Specific Endpoints for FE Charts

### 6.1 Institute monthly registrations (new)
- `GET /api/institutes/{id}/registration-monthly?year=2026`
- Response example:
```json
[
  { "month": 1, "registrations": 12 },
  { "month": 2, "registrations": 18 },
  { "month": 3, "registrations": 7 }
]
```
- Note: service returns all 12 months (missing months as `0`).

### 6.2 Student analytics
- `GET /api/students/{id}/training-hours`
- `GET /api/students/{id}/completion-percentage`

### 6.3 Teacher analytics
- `GET /api/teachers/{id}/course-progress`
- `GET /api/teachers/{id}/weekly-schedule`

## 7) Error Handling Contract
From `GlobalExceptionHandler`:
- `404`: `ResourceNotFoundException` -> plain message text
- `400`: `DuplicateResourceException`, `BadRequestException` -> plain message text
- `409`: `DataIntegrityViolationException` -> conflict text
- `400` malformed body -> JSON with `status`, `error`, `message`
- `500`: generic text message

### FE Recommendation
- Handle both plain-text and JSON error bodies.
- On 401/403, redirect to login or refresh credentials.

## 8) Data Types and Enums FE Must Respect
- `SessionStatus`: `UPCOMING | ACTIVE | COMPLETED | CANCELLED`
- `AttendanceStatus`: `PRESENT | ABSENT`
- `UserType`: `ADMIN | STUDENT | TEACHER`

## 9) File Upload Note
- Current teacher `cv` is a **String field**, not multipart upload endpoint.
- If FE uploads files, upload to storage service first and send URL/string in `cv`.

## 10) Integration Readiness Checklist (Frontend)
- Configure base URL and Basic Auth header helper.
- Start from Swagger UI to generate typed API client models.
- Use endpoint groups above per page/module.
- Normalize error parser (text/json).
- Treat date/time fields as ISO strings.
- For charts, use institute monthly endpoint + map `month` to localized labels.

## 11) Suggested Next Backend Hardening (optional)
- Restrict `permitAll()` to GET-only where intended using `HttpMethod.GET` matchers.
- Standardize all errors to one JSON envelope.
- Add pagination for heavy list endpoints.
- Add migration script for `Enrollment.createdAt` backfill in persistent DB environments.

