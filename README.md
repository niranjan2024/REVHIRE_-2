# RevHire Complete Structure

RevHire is a multi-module monorepo for a recruitment platform.
Each module contains:
- `backend` (Spring Boot, Java 21, MySQL)
- `frontend` (Angular 16)

The repository includes full-feature and feature-focused variants (authentication-only, job-seeker-focused, employer-focused, notification-focused, validation-focused, and security-focused).

## Modules

| Module | Purpose |
|---|---|
| `Security` | Most complete implementation (JWT, role-based APIs, full recruiter + seeker flows) |
| `validation` | Full flow with strict validation behavior |
| `application` | Near-full flow variant |
| `authentication` | Authentication-focused variant |
| `Autherization` | OTP/email registration variant |
| `employer` | Employer-focused features |
| `job-search` | Job search-focused features |
| `job-seeker` | Job seeker profile/resume-focused features |
| `notification` | Notification-focused variant |
| `project` | Snapshot/reference copy (contains `froentend` folder typo) |

## Project Structure

```text
RevHire-Complete Structure/
+-- application/
|   +-- backend/
|   +-- frontend/
+-- authentication/
|   +-- backend/
|   +-- frontend/
+-- Autherization/
|   +-- backend/
|   +-- frontend/
+-- employer/
|   +-- backend/
|   +-- frontend/
+-- job-search/
|   +-- backend/
|   +-- frontend/
+-- job-seeker/
|   +-- backend/
|   +-- frontend/
+-- notification/
|   +-- backend/
|   +-- frontend/
+-- Security/
|   +-- backend/
|   +-- frontend/
|   +-- docs/
+-- validation/
|   +-- backend/
|   +-- frontend/
+-- project/ (reference snapshot)
+-- README.md
```

## Tech Stack

### Backend
- Java 21
- Spring Boot 3.3.2
- Spring Web, Spring Data JPA, Spring Security, Validation
- MySQL
- Log4j2

### Frontend
- Angular 16
- RxJS
- Jasmine + Karma

## Core Architecture

Backend layers:
- `controller` -> REST APIs
- `service` -> business logic
- `repository` -> persistence
- `entity` -> domain model
- `dto` -> request/response contracts
- `config/security` -> authentication and authorization setup

Frontend layers:
- `components` -> UI screens
- `services` -> API wrappers/state helpers
- `guards` -> role-based route protection
- `models` -> TypeScript interfaces
- `jwt.interceptor` -> bearer token injection (JWT modules)

## Main Domain Model

- `User`
- `EmployerProfile`
- `JobPosting`
- `JobApplication`
- `JobSeekerProfile`
- `Resume`
- `Notification`
- `PasswordResetToken`

Enums:
- `Role`
- `EmploymentStatus`
- `JobStatus`
- `ApplicationStatus`
- `NotificationType`

## Security Model

Two auth modes are used across modules:
- Session-based login (stores Spring Security context in session)
- JWT-based login (`Security` module, bearer token on requests)

Typical role protection:
- Public: register/login/forgot-password/reset-password
- Employer APIs: `/api/employer/**`
- Job seeker APIs: `/api/jobseeker/**`, `/api/resume/**`

## Registration Form Examples

Use endpoint:
- `POST /api/register`

### Job Seeker Registration (example payload)

```json
{
  "username": "seeker01",
  "email": "seeker01@example.com",
  "password": "Seeker@123",
  "confirmPassword": "Seeker@123",
  "fullName": "Ravi Kumar",
  "mobileNumber": "9876543210",
  "securityQuestion": "What is your pet name?",
  "securityAnswer": "Bruno",
  "location": "Hyderabad",
  "employmentStatus": "FRESHER",
  "role": "JOB_SEEKER"
}
```

### Employer Registration (example payload)

```json
{
  "username": "employer01",
  "email": "hr@acme.com",
  "password": "Employer@123",
  "confirmPassword": "Employer@123",
  "fullName": "Acme HR",
  "mobileNumber": "9123456789",
  "securityQuestion": "What is your favorite city?",
  "securityAnswer": "Pune",
  "location": "Bengaluru",
  "employmentStatus": "EMPLOYED",
  "role": "EMPLOYER"
}
```

Notes:
- Password must meet policy (upper/lower/number/special, 8-64 chars).
- `mobileNumber` must be 10 digits.
- Backend registration DTO stores base user data only; employer company profile details are managed separately.

### Employer Company Details (after employer registration/login)

Use endpoint:
- `PUT /api/employer/company-profile`

Example payload:

```json
{
  "companyName": "Acme Technologies Pvt Ltd",
  "industry": "Information Technology",
  "companySize": "250",
  "companyDescription": "Product engineering company focused on hiring and staffing platforms.",
  "website": "https://acme.example.com",
  "companyLocation": "Bengaluru"
}
```

Notes:
- `companyName` and `industry` require 3+ characters.
- `companySize` is validated as numeric by service logic.
- `website` must be a valid URL when provided.

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 18+ (or compatible with Angular 16)
- npm 9+
- MySQL 8+

## Run Backend

From module root:

```powershell
cd <module>\backend
mvn spring-boot:run
```

Most modules are configured on port `8080` by default.
Run one backend module at a time unless you change ports.

## Run Frontend

From module root:

```powershell
cd <module>\frontend
npm install
npm start
```

## Run Tests

### Backend
```powershell
cd <module>\backend
mvn test
```

### Frontend
```powershell
cd <module>\frontend
npm test -- --watch=false --browsers=ChromeHeadless
```

## Security Module Documentation

Generated docs (in this repo):
- `Security/docs/Security-API-Implementation.pdf`
- `Security/docs/Security-API-Implementation.html`
- `Security/docs/Entire-Project-Implementation-Guide.pdf`
- `Security/docs/Entire-Project-Implementation-Guide.html`

## Current Testing Status (Security Module)

- Backend unit tests implemented under:
  - `Security/backend/src/test/java/com/revhire`
- Frontend unit tests implemented under:
  - `Security/frontend/src/app/**/*.spec.ts`

## Notes

- Log4j2 is generally console-appender based.
- Files like `boot8081.out.log` / `boot8081.err.log` are created by process output redirection.
- `Autherization` and `project/froentend` names are present as-is in the repository.

## Recommended Module for Deep-Dive

If you want to understand and explain the project end-to-end, start with:
- `Security/backend`
- `Security/frontend`

Then compare differences in other modules (especially `authentication` and `Autherization`).