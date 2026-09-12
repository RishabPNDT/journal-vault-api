# Journal App

A full-stack personal journaling app: a Spring Boot + MongoDB REST API with JWT authentication, paired with a React (Vite) frontend. Built to practice the same security fundamentals as my [Task Management API](#) — ownership enforcement, clean error handling, and no client-trusted IDs — in a MongoDB/document-store context instead of a relational one.

## Tech Stack

**Backend:** Java 17, Spring Boot, Spring Security, Spring Data MongoDB, JWT (`io.jsonwebtoken`)
**Frontend:** React (Vite)
**Database:** MongoDB

## Features

- JWT-based stateless authentication (register/login)
- Every journal entry is scoped to its owner — reads, updates, and deletes are all checked against the authenticated user, never trusted from the URL alone
- CORS locked to a single configured origin, not wildcarded
- Centralized exception handling with consistent JSON error responses across validation errors, auth failures, not-found cases, and unexpected errors
- React frontend with JWT-authenticated API calls

## Security decisions worth knowing about

**Ownership via `@DBRef`, not a manually checked foreign key.** Each `User` document holds a list of `@DBRef` references to their own `JournalEntry` documents. Update/delete operations only ever look inside the *authenticated user's own* reference list for the requested entry ID — an entry ID that exists but belongs to someone else is filtered out before it's ever loaded, so there's no code path where one user's session can reach another user's document. A missing entry and someone else's entry return the exact same `404` and message, so a client probing IDs can't tell the two cases apart.

**Consistent, non-leaking error responses.** A `@RestControllerAdvice` maps every expected failure (validation, wrong login credentials, entry not found/not owned, duplicate username) to a clean JSON `{"message": "..."}` shape with the correct HTTP status, and a catch-all handler ensures any *unexpected* error (a DB hiccup, a null pointer) is logged server-side but never returns a raw stack trace to the client.

**Login errors don't confirm whether a username exists.** Both "wrong password" and "no such user" return the identical `401` and message — this is deliberate, since differentiating the two responses is a classic way to let an attacker enumerate valid usernames.

### Known tradeoff (not a bug, worth being able to discuss)
The frontend stores the JWT in `localStorage` (see `frontend/src/api.js`). This is simple and works, but it's readable by any JavaScript running on the page, so it's exposed if an XSS vulnerability ever existed. An `httpOnly` cookie is more resistant to that but adds real complexity (CSRF protection, cookie-based CORS config) — for a project this size, `localStorage` is a reasonable, explainable tradeoff rather than an oversight.

## Running locally

### Prerequisites
- Java 17+
- MongoDB running locally on the default port (`27017`)
- Node.js (for the frontend)

### Backend configuration
Sensitive config is read from environment variables with local-only fallbacks in `application.properties` — **set real values via environment variables, never commit real secrets.**

| Variable | Purpose | Local default |
|---|---|---|
| `JWT_SECRET` | JWT signing key | placeholder — change this |
| `JWT_EXPIRATION_MS` | Token lifetime (ms) | `86400000` (24h) |
| `CORS_ALLOWED_ORIGIN` | Frontend origin allowed by CORS | `http://localhost:5173` |

### Run the backend
```bash
./mvnw spring-boot:run
```
API starts on `http://localhost:8080`.

### Run the frontend
```bash
cd frontend
npm install
npm run dev
```
Frontend starts on `http://localhost:5173` (Vite default).

## API testing

A Postman collection is included at `/postman/Journal_App_Postman_Collection.json`, covering registration, login (including wrong-password and nonexistent-username cases), entry CRUD, and — most importantly — IDOR checks confirming one user can never read, update, or delete another user's entries. Import into Postman, set `baseUrl`, and run the collection.

## Endpoints

| Method | Path | Access |
|---|---|---|
| GET | `/health-check` | Public |
| POST | `/api/auth/register` | Public |
| POST | `/api/auth/login` | Public |
| GET | `/api/entries` | Authenticated (own entries only) |
| POST | `/api/entries` | Authenticated |
| PUT / DELETE | `/api/entries/{id}` | Owner only (404 for others) |

## Possible next steps
- Add pagination to `GET /api/entries` for users with many entries
- Move from `@DBRef` (N+1 query pattern) to storing entry IDs directly and batch-fetching, or embed entries outright since they're never shared across users
- Deploy backend + frontend to a free-tier host for a live demo link
