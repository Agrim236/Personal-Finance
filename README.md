<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring_Boot-4.1.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white" alt="Spring Security"/>
  <img src="https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven"/>
  <img src="https://img.shields.io/badge/H2-In--Memory-003B57?style=for-the-badge" alt="H2"/>
  <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"/>
</p>

<h1 align="center">Personal Finance Manager</h1>

<p align="center">
  REST API to register users, track income and expenses, manage categories,<br/>
  set savings goals, and generate monthly / yearly reports.
</p>

<p align="center">
  <img src="https://skillicons.dev/icons?i=java,spring,maven,hibernate,docker,github,bash" alt="Tech stack"/>
</p>

<p align="center">
  <a href="#live-demo">Live demo</a> ·
  <a href="#tech-stack">Tech stack</a> ·
  <a href="#features">Features</a> ·
  <a href="#architecture">Architecture</a> ·
  <a href="#modular-structure">Structure</a> ·
  <a href="#test-cases">Test cases</a> ·
  <a href="#api-documentation">API</a> ·
  <a href="#getting-started">Setup</a> ·
  <a href="#security">Security</a>
</p>

---

## Live demo

> Replace the placeholder after you deploy. The README can go live on GitHub **before** hosting exists.

| | Link                                                               |
|---|--------------------------------------------------------------------|
| **Deployed API** | `https://personal-finance-r9ce.onrender.com/api/health`            |
| **Health check** | `GET https://personal-finance-r9ce.onrender.com/api/health` → `OK` |


## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.1 |
| Security | Spring Security (session-based) |
| Persistence | Spring Data JPA |
| Database | H2 (in-memory) |
| Validation | Jakarta Bean Validation |
| Build | Maven |
| Container | Docker |

---

## Features

### User management
- Registration with email, password, full name, and phone number
- Login / logout with an HTTP-only session cookie (`FINANCE_SESSION`)
- Passwords stored with BCrypt
- Full isolation: a user never sees another user’s data

### Transactions
- Create, list, update, and delete
- Filter by date range and category
- Date is **not** allowed to change on update
- Sorted newest first

### Categories
- Default set created on startup
- Custom income and expense categories
- Defaults cannot be deleted
- Categories still used by transactions cannot be deleted

| Type | Defaults |
|---|---|
| Income | Salary |
| Expense | Food, Rent, Transportation, Entertainment, Healthcare, Utilities |

### Savings goals
- Name, target amount, target date
- Progress = total income − total expenses since the goal start date
- Progress is clamped between `0` and the target
- Response includes remaining amount and progress percentage

### Reports
- Monthly: income by category, expenses by category, net savings
- Yearly: same breakdown for the full calendar year

---

## Architecture

```
Client (curl / Postman / grader)
        │
        ▼
┌──────────────────────────────────────┐
│  Controller     REST + HTTP codes    │
│  Service        business rules       │
│  Repository     JPA                  │
│  Entity / DTO   persistence vs API   │
│  Security       session cookie       │
│  Advice         global 4xx / 5xx     │
└──────────────────────────────────────┘
        │
        ▼
       H2
```

Request flow: **Controller → Service → Repository → Entity / H2**. DTOs never leak into persistence; entities never go out as JSON.

---

## Modular structure

Each feature (auth, transactions, categories, goals, reports) is split the same way so a recruiter can open one package and see a single responsibility.

```
skye/
├── pom.xml
├── Dockerfile
├── render.yaml
├── financial_manager_tests.sh
└── src/main/
    ├── resources/
    │   └── application.properties
    └── java/com/example/skye/
        ├── PersonalFinanceManagerApplication.java
        ├── config/
        │   ├── SecurityConfig.java
        │   └── SessionAuthenticationFilter.java
        ├── controller/
        │   ├── AuthController.java
        │   ├── TransactionController.java
        │   ├── CategoryController.java
        │   ├── SavingsGoalController.java
        │   ├── ReportController.java
        │   └── HealthController.java
        ├── service/
        │   ├── UserService.java
        │   ├── TransactionService.java
        │   ├── CategoryService.java
        │   ├── SavingsGoalService.java
        │   └── ReportService.java
        ├── repository/
        │   ├── UserRepository.java
        │   ├── TransactionRepository.java
        │   ├── CategoryRepository.java
        │   └── SavingsGoalRepository.java
        ├── entity/
        │   ├── User.java
        │   ├── Transaction.java
        │   ├── Category.java
        │   └── SavingsGoal.java
        ├── dto/
        │   ├── UserRegistrationDto.java
        │   ├── UserLoginDto.java
        │   ├── TransactionDto.java
        │   ├── CategoryDto.java
        │   ├── SavingsGoalDto.java
        │   ├── MonthlyReportDto.java
        │   └── YearlyReportDto.java
        └── exception/
            ├── GlobalExceptionHandler.java
            ├── DuplicateResourceException.java
            ├── InvalidCredentialsException.java
            └── ResourceNotFoundException.java
```

| Module | Package | What it owns |
|---|---|---|
| **API** | `controller` | HTTP routes, status codes, wrapping lists (`transactions` / `categories` / `goals`) |
| **Business** | `service` | Auth, CRUD rules, goal progress math, report aggregation |
| **Data** | `repository` | Spring Data JPA queries (filters, sums by category) |
| **Domain** | `entity` | Tables: users, transactions, categories, savings_goals |
| **Contract** | `dto` | JSON in/out + Bean Validation |
| **Security** | `config` | Permit register/login/health; session cookie; logout |
| **Errors** | `exception` | One `@ControllerAdvice` for 400 / 401 / 404 / 409 |

| Feature | Controller | Service | Repository | Entity / DTO |
|---|---|---|---|---|
| Users | `AuthController` | `UserService` | `UserRepository` | `User` |
| Transactions | `TransactionController` | `TransactionService` | `TransactionRepository` | `Transaction` / `TransactionDto` |
| Categories | `CategoryController` | `CategoryService` | `CategoryRepository` | `Category` / `CategoryDto` |
| Goals | `SavingsGoalController` | `SavingsGoalService` | `SavingsGoalRepository` | `SavingsGoal` / `SavingsGoalDto` |
| Reports | `ReportController` | `ReportService` | (uses `TransactionRepository`) | `MonthlyReportDto` / `YearlyReportDto` |

---

## Test cases

Every functional requirement in the specification, written as a verifiable case. The `financial_manager_tests.sh` suite exercises these over HTTP (**86 checks**).

### 1. User management and authentication

| # | Case | Expected |
|---|---|---|
| A1 | Register with username (email), password, full name, phone number | `201 Created` · `{ "message": "...", "userId": 1 }` |
| A2 | Username is not a valid email address | `400 Bad Request` |
| A3 | Missing any mandatory field (password / full name / phone) | `400 Bad Request` |
| A4 | Phone number format invalid | `400 Bad Request` |
| A5 | Username already registered | `409 Conflict` |
| A6 | Login with valid credentials | `200 OK` · session cookie returned |
| A7 | Login with wrong password | `401 Unauthorized` |
| A8 | Login with unknown username | `401 Unauthorized` |
| A9 | Session cookie authorises later calls | subsequent requests succeed |
| A10 | Logout | `200 OK` · session invalidated |
| A11 | Reuse cookie after logout | `401 Unauthorized` |
| A12 | Any protected endpoint without a session | `401 Unauthorized` |
| A13 | **Data isolation** — user A lists transactions | only user A's rows |
| A14 | User A reads / edits user B's transaction | denied, no data leak |
| A15 | User A reads user B's savings goal | denied, no data leak |

### 2. Transaction management

| # | Case | Expected |
|---|---|---|
| T1 | Create with amount, date, category, description | `201 Created` · response includes `type` |
| T2 | Description omitted (optional field) | `201 Created` |
| T3 | Amount zero or negative | `400 Bad Request` |
| T4 | Date in the future | `400 Bad Request` |
| T5 | Date not in `YYYY-MM-DD` format | `400 Bad Request` |
| T6 | Category not accessible to the user | `404 Not Found` |
| T7 | List all transactions | **sorted newest first** |
| T8 | Filter by `startDate` and `endDate` | only rows inside the range |
| T9 | Filter by category | only that category |
| T10 | Filter by transaction type (income / expense) | only that type |
| T11 | Update amount, category, description | `200 OK` |
| T12 | **Send a new date on update** | date is **unchanged** |
| T13 | Update a transaction id that does not exist | `404 Not Found` |
| T14 | Delete a transaction | `200 OK` |
| T15 | Deleted transaction excluded from **savings goals** | progress recalculated |
| T16 | Deleted transaction excluded from **reports** | totals recalculated |

### 3. Category management

| # | Case | Expected |
|---|---|---|
| C1 | List categories | defaults + that user's custom, with `isCustom` flag |
| C2 | Defaults present on startup | Salary · Food · Rent · Transportation · Entertainment · Healthcare · Utilities |
| C3 | Create custom `INCOME` category | `201 Created` · `isCustom: true` |
| C4 | Create custom `EXPENSE` category | `201 Created` |
| C5 | Custom name duplicated for the same user | `409 Conflict` |
| C6 | Same custom name used by a **different** user | allowed (unique per user) |
| C7 | Invalid type (not `INCOME` / `EXPENSE`) | `400 Bad Request` |
| C8 | **Delete a default category** | rejected — defaults cannot be deleted or modified |
| C9 | **Delete a category referenced by a transaction** | rejected |
| C10 | Delete an unused custom category | `200 OK` |
| C11 | Delete a category name that does not exist | `404 Not Found` |
| C12 | Delete another user's custom category | `403 Forbidden` / `404 Not Found` |

### 4. Savings goals

| # | Case | Expected |
|---|---|---|
| G1 | Create with goal name, target amount, target date | `201 Created` |
| G2 | Start date omitted | defaults to creation date |
| G3 | Target amount zero or negative | `400 Bad Request` |
| G4 | Target date in the past | `400 Bad Request` |
| G5 | Start date after target date | `400 Bad Request` |
| G6 | **Progress formula** | `(total income − total expenses)` since start date |
| G7 | Net savings negative | progress `0`, never negative |
| G8 | Net savings above target | progress capped at target amount |
| G9 | Response fields | `currentProgress`, `progressPercentage`, `remainingAmount` |
| G10 | Percentage formatting | `20.0`, `50.0`, `65.5`, `16.67` |
| G11 | **Multiple goals track independently** | each uses its own start date |
| G12 | List all goals | `{ "goals": [ ... ] }` |
| G13 | Get a single goal by id | `200 OK` |
| G14 | Update target amount / target date | `200 OK` · progress recalculated |
| G15 | Delete a goal | `200 OK` |
| G16 | Goal id that does not exist | `404 Not Found` |
| G17 | Another user's goal id | `403 Forbidden` / `404 Not Found` |

### 5. Reports and analytics

| # | Case | Expected |
|---|---|---|
| R1 | Monthly report for a year / month | total income **by category** |
| R2 | Monthly report | total expenses **by category** |
| R3 | Monthly report | `netSavings` = income − expenses |
| R4 | Month with no transactions | empty maps · `netSavings` 0 |
| R5 | Month outside 1–12 | `400 Bad Request` |
| R6 | Yearly report | aggregates the whole year by category |
| R7 | Yearly `netSavings` | income − expenses for the year |
| R8 | Report request without a session | `401 Unauthorized` |
| R9 | Reports only include the caller's data | isolated per user |

### 6. Error handling and status codes

| # | Case | Expected |
|---|---|---|
| E1 | Validation error or malformed input | `400 Bad Request` |
| E2 | Invalid credentials or expired session | `401 Unauthorized` |
| E3 | Accessing another user's data | `403 Forbidden` |
| E4 | Unknown resource | `404 Not Found` |
| E5 | Duplicate username or category name | `409 Conflict` |
| E6 | **Any known scenario** | never a `5xx` |
| E7 | Error body | clear, descriptive message |

### 7. End-to-end grading script

```bash
# local
bash financial_manager_tests.sh http://localhost:8080/api

# deployed
bash financial_manager_tests.sh https://personal-finance-1-xvko.onrender.com/api
```

```
Total Tests Executed: 86
Tests Passed: 86
Tests Failed: 0
Success Rate: 100%
```

---

## API documentation

Local Base URL: `http://localhost:8080/api`  
Deployed Base URL: `https://personal-finance-1-xvko.onrender.com/api/auth`  
Deployed Health: `https://personal-finance-1-xvko.onrender.com/api/health` → `OK`  
Cookie: `FINANCE_SESSION` (set on login)

### Auth

**Register**

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "password123",
  "fullName": "John Doe",
  "phoneNumber": "+1234567890"
}
```

```json
{ "message": "User registered successfully", "userId": 1 }
```

**Login**

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "user@example.com",
  "password": "password123"
}
```

```json
{ "message": "Login successful" }
```

**Logout**

```http
POST /api/auth/logout
```

### Transactions

```http
POST /api/transactions
GET  /api/transactions
GET  /api/transactions?startDate=2024-01-01&endDate=2024-01-31&category=Salary
PUT  /api/transactions/{id}
DELETE /api/transactions/{id}
```

```json
{
  "amount": 50000.00,
  "date": "2024-01-15",
  "category": "Salary",
  "description": "January salary"
}
```

List shape: `{ "transactions": [ ... ] }`

### Categories

```http
GET    /api/categories
POST   /api/categories
DELETE /api/categories/{name}
```

```json
{ "name": "Freelance", "type": "INCOME" }
```

List shape: `{ "categories": [ ... ] }`

### Savings goals

```http
POST   /api/goals
GET    /api/goals
GET    /api/goals/{id}
PUT    /api/goals/{id}
DELETE /api/goals/{id}
```

```json
{
  "goalName": "Emergency Fund",
  "targetAmount": 5000.00,
  "targetDate": "2027-01-01"
}
```

List shape: `{ "goals": [ ... ] }`

### Reports

```http
GET /api/reports/monthly/{year}/{month}
GET /api/reports/yearly/{year}
```

### Health

```http
GET /api/health
```

**Deployed health check:** [`GET /api/health`](https://personal-finance-1-xvko.onrender.com/api/health) → `OK`

The deployed health endpoint should return `OK` when the service is running.


---

## Getting started

**Need:** Java 21+ and Maven 3.9+

```bash
git clone https://github.com/Agrim236/skye.git
cd skye
mvn spring-boot:run
```

| | URL |
|---|---|
| Local API | http://localhost:8080/api |
| Local Health | http://localhost:8080/api/health |
| **Deployed API** | https://personal-finance-1-xvko.onrender.com/api |
| **Deployed Health** | https://personal-finance-1-xvko.onrender.com/api/health |
| H2 console | http://localhost:8080/api/h2-console |

H2: `jdbc:h2:mem:skye_finance` · username `sa` · password empty

---

## Security

- Session authentication (not JWT)
- HTTP-only cookie `FINANCE_SESSION`
- Session timeout 30 minutes
- BCrypt password hashing
- Register, login, and health are public; everything else requires a session

---

## Error handling

| Status | When |
|---|---|
| 200 / 201 | Success / created |
| 400 | Validation or bad body |
| 401 | Bad login or missing session |
| 404 | Unknown resource |
| 409 | Duplicate email or category |

```json
{ "error": "Username already exists" }
```

Validation:

```json
{ "username": "Username must be a valid email address" }
```

---

## Database (logical)

| Table | Main fields                                                                     |
|---|---------------------------------------------------------------------------------|
| `users` | id, username (email, unique), password, full_name, phone_number, created_at     |
| `categories` | id, name, type (`INCOME` / `EXPENSE`), is_custom, user_id (null for defaults)   |
| `transactions` | id, amount, date, description, category_id, user_id, created_at                 |
| `savings_goals` | id, goal_name, target_amount, target_date, start_date, progress fields, user_id |

---

## Author

**Agrim Agrawal**  
[github.com/Agrim236](https://github.com/Agrim236)
