# Spring Security Implementation Guide

## Overview
A simple, learning-level Spring Security setup using HTTP Basic authentication. Designed to be easily replaceable with JWT in the future.

## Components Added

### 1. **SecurityConfig** (`com.trainingcenter.management.security.SecurityConfig`)
Central security configuration class.

**Features:**
- Password encoding using BCrypt
- DAO-based authentication with custom UserDetailsService
- HTTP Basic authentication (stateless)
- CSRF disabled for API simplicity
- Role-based authorization mapping from `UserType` enum

**Authentication & Authorization:**
- **Public endpoints** (no auth required):
  - `GET /api/**` (all read operations)
  - `POST /api/auth/login`
  - `/actuator/health`
  - `/api/webhooks/**` (verified by Stripe signature)

- **Protected endpoints** (authentication required):
  - `POST /api/**` (all create operations)
  - `PUT /api/**` (all update operations)
  - `DELETE /api/**` (all delete operations)

### 2. **CustomUserDetailsService** (`com.trainingcenter.management.security.CustomUserDetailsService`)
Loads user details from database using `UserRepository.findByUsername()`.

**How it works:**
1. Receives username
2. Queries User entity from database
3. Maps `UserType` enum to Spring Security `ROLE_*` authorities
4. Returns Spring Security `UserDetails` object

**Authority Mapping:**
- `ADMIN` → `ROLE_ADMIN`
- `STUDENT` → `ROLE_STUDENT`
- `TEACHER` → `ROLE_TEACHER`

### 3. **LoginController** (`com.trainingcenter.management.controller.LoginController`)
Simple login endpoint for testing and development.

**Endpoint:**
```
POST /api/auth/login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "password123"
}
```

**Response (Success - 200):**
```json
{
  "id": 1,
  "username": "johndoe",
  "email": "john@example.com",
  "userType": "STUDENT",
  "message": "Login successful"
}
```

**Response (Failure - 404):**
```json
"User not found"
```

### 4. **Updated UserService**
- Passwords are now **hashed with BCrypt** before saving
- Both `createUser()` and `updateUser()` methods use `PasswordEncoder`

### 5. **Updated UserRepository**
- Added method: `Optional<User> findByUsername(String username)`

### 6. **Updated pom.xml**
- Added dependency: `spring-boot-starter-security`

## How Authentication Works

### Registration Flow
1. User calls `POST /api/users` with username, password, etc.
2. `UserService.createUser()` hashes the password
3. User is saved to database with hashed password
4. Can now login with credentials

### Login Flow
1. User calls `POST /api/auth/login` with username/password
2. `LoginController` finds user by username
3. Verifies password using `PasswordEncoder.matches()`
4. Returns user info (or 404 if invalid)

### Protected Endpoint Access (HTTP Basic Auth)
For any protected endpoint (POST, PUT, DELETE):

```bash
curl -X POST http://localhost:8080/api/training-sessions \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic <base64(username:password)>" \
  -d '{ ... }'
```

Or with curl:
```bash
curl -X POST http://localhost:8080/api/training-sessions \
  -H "Content-Type: application/json" \
  -u username:password \
  -d '{ ... }'
```

## Future JWT Migration

To replace HTTP Basic with JWT:

1. **Remove HTTP Basic:**
   ```java
   .httpBasic(basic -> {}) // Remove this line
   ```

2. **Add JWT Filter:**
   Create `JwtAuthenticationFilter extends OncePerRequestFilter`
   - Extract token from Authorization header
   - Validate JWT signature
   - Create Authentication object

3. **Add JWT Provider:**
   Create `JwtTokenProvider`
   - Generate tokens on login
   - Validate and parse tokens

4. **Update LoginController:**
   - Generate JWT token on successful login
   - Return token to client

5. **Keep SessionCreationPolicy.STATELESS:**
   - Already configured, ready for JWT

## Security Best Practices Applied

✅ **Password Hashing:** BCrypt with configurable strength (default 10 rounds)
✅ **Stateless API:** SessionCreationPolicy.STATELESS (ready for JWT)
✅ **CSRF Disabled:** Appropriate for stateless API
✅ **Role-Based Access:** UserType enum maps to Spring Security roles
✅ **Centralized Config:** All security rules in one class
✅ **Clean Separation:** Business logic untouched

## Testing

### 1. Create a User
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123",
    "userType": "STUDENT",
    "contactInfo": "555-1234"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "password123"
  }'
```

### 3. Access Protected Endpoint
```bash
curl -X POST http://localhost:8080/api/training-sessions \
  -H "Content-Type: application/json" \
  -u johndoe:password123 \
  -d '{
    "price": 199.99,
    "availableSeats": 30,
    ...
  }'
```

### 4. Public Read Endpoint (No Auth)
```bash
curl -X GET http://localhost:8080/api/training-sessions
```

## Configuration Files

No additional properties needed yet. When adding JWT, add to `application.properties`:
```properties
app.jwt.secret=your-secret-key-min-32-chars
app.jwt.expiration=86400000
```

## Directory Structure
```
src/main/java/com/trainingcenter/management/
├── security/
│   ├── SecurityConfig.java
│   └── CustomUserDetailsService.java
├── controller/
│   └── LoginController.java
└── ... (other existing code)
```

## Summary

- ✅ Simple HTTP Basic authentication (easy to understand)
- ✅ JWT-ready architecture (stateless sessions)
- ✅ Role-based authorization (ADMIN, STUDENT, TEACHER)
- ✅ BCrypt password hashing
- ✅ No business logic mixing with security
- ✅ Easy to test and extend

