# Open Ballot: secure voting system

A full-stack voting app. A **React** single-page frontend talks to a **Spring Boot** backend only through a
**REST API**, secured with **JWT**, **BCrypt**, **role-based access control** and server-side validation.

```
 React (Vite)  --JSON over HTTP + Bearer JWT-->  Spring Boot REST API  --JPA/Flyway-->  MySQL
 localhost:5173                                   localhost:8080                         localhost:3306
```

**Stack:** Java 17, Spring Boot 3.5, Spring Security 6, Spring Data JPA, Flyway, MySQL, jjwt, springdoc (Swagger),
JUnit 5 / MockMvc / H2, React 18, React Router, Vite.

## Run it

You need Java 17+, Maven, Node 18+, and Docker (or any MySQL 8).

```bash
# 1. Secrets: copy and fill in .env  (never commit it)
cp .env.example .env
openssl rand -base64 32          # paste the output as JWT_SECRET
# also set DB_PASSWORD, MYSQL_ROOT_PASSWORD, ADMIN_PASSWORD (12+ chars)

# 2. Database
docker compose up -d

# 3. Backend (loads .env into the environment, then starts on :8080)
cd backend
set -a; source ../.env; set +a
mvn spring-boot:run

# 4. Frontend (new terminal)
cd frontend
cp .env.example .env
npm install
npm run dev                      # http://localhost:5173
```

In an IDE (Eclipse/STS/IntelliJ) set the same variables in the Run Configuration instead of using `source`.
Swagger UI: http://localhost:8080/swagger-ui.html (click **Authorize** and paste a token from `/api/auth/login`).

Sign in with `ADMIN_EMAIL` / `ADMIN_PASSWORD` to add candidates, then register a voter in another browser profile.

Tests (no MySQL needed, they use in-memory H2): `cd backend && mvn test`

## API

| Method | Path | Who | Purpose |
|---|---|---|---|
| POST | `/api/auth/register` | public | Create a voter account (role is always `USER`) |
| POST | `/api/auth/login` | public | Returns a JWT access token |
| GET | `/api/auth/me` | signed in | Current user |
| GET | `/api/candidates` | signed in | Candidates on the ballot |
| GET | `/api/votes/status` | voter | Has this user voted? |
| POST | `/api/votes` | voter | Cast the single vote `{ "candidateId": 1 }` |
| POST/PUT/DELETE | `/api/admin/candidates[/{id}]` | admin | Manage candidates (delete = remove from ballot) |
| GET | `/api/admin/results` | admin | Votes per candidate |
| GET | `/api/admin/stats` | admin | Registered voters, votes cast, turnout |

Errors share one JSON shape: `{ timestamp, status, error, message, fieldErrors? }`.

## Security: what is protected and where

| Concern | How it's handled | Where |
|---|---|---|
| Password storage | BCrypt (strength 12), never stored or returned in plain text | `SecurityConfig`, `AuthService` |
| Authentication | Stateless JWT (HS256, 30 min expiry); user re-loaded from DB on every request | `JwtService`, `JwtAuthenticationFilter` |
| Weak/missing secrets | Secrets come from environment variables; app refuses to start with a key under 256 bits | `application.properties`, `JwtService` |
| Authorization | Role rules: `/api/admin/**` = ADMIN, `/api/votes/**` = USER, everything else needs a token | `SecurityConfig` |
| Brute-force logins | 5 failed attempts lock that account's login for 15 minutes; same error for unknown email / wrong password | `LoginAttemptService`, `AuthService` |
| Mass assignment / privilege escalation | Requests bind to small DTOs; clients can't send `role`, `id` or `hasVoted` | `dto/*`, `AuthService.register` |
| Input validation | Bean Validation on every request body, strong password policy | `dto/*`, `GlobalExceptionHandler` |
| Double voting | Atomic `UPDATE ... SET has_voted = true WHERE id = ? AND has_voted = false` in the same transaction as the ballot insert | `VoteService`, `AppUserRepository` |
| Ballot secrecy | `votes` table has no user reference; logs record that a vote happened, not for whom | `V1__init.sql`, `VoteService` |
| CORS | Only the configured frontend origin may call the API from a browser | `SecurityConfig` |
| Error leakage | No stack traces or SQL in responses; consistent JSON for 401/403/404/500 | `GlobalExceptionHandler`, `RestAuthHandlers` |
| Schema safety | Flyway migrations own the schema; Hibernate `ddl-auto=none`; least-privilege DB user | `db/migration`, `docker-compose.yml` |
| CSRF | Disabled deliberately: stateless API, token in a header (not a cookie) | `SecurityConfig` |
| Frontend | Route guards by role, token cleared on 401, no `dangerouslySetInnerHTML` | `App.jsx`, `api.js` |

## Design decisions worth knowing for interviews

- **Why a `has_voted` flag instead of a unique `(user, candidate)` row?** It keeps ballots anonymous *and* makes
  "one vote per person" atomic. The conditional UPDATE takes a row lock, so concurrent requests cannot both win.
- **Why soft-delete candidates?** Past ballots reference them; deleting would break results or lose votes.
- **Why re-load the user on each request?** A disabled account or changed role takes effect immediately instead of
  waiting for the token to expire.
- **Why DTOs (Java records)?** The API contract is separate from the database model, and it prevents mass assignment.

## Known limitations and next steps

- Login lockout is in memory (per instance, resets on restart). Use Redis for a shared, expiring counter, and add
  per-IP rate limiting.
- The token is kept in `sessionStorage`. An httpOnly cookie plus refresh-token rotation is stronger against XSS.
- Locking by email lets someone deliberately lock another user out; real systems add CAPTCHA/step-up checks.
- No email verification, password reset, or multi-election support (add an `elections` table and scope `has_voted` to it).
- Add a MySQL integration test with Testcontainers that fires concurrent votes from one user (tests here use H2).
- Add a Dockerfile for the backend and a GitHub Actions workflow running `mvn test` and `npm run build`.
- Use HTTPS in any real deployment, and set `CORS_ALLOWED_ORIGINS` to the deployed frontend URL.
