# Secure User API

A Spring Boot backend implementing a complete authentication and authorization system from scratch — built as Project 3 in a self-directed backend mentorship series (Projects 1 & 2 covered core CRUD, relationships, and transactional integrity without security).

## Overview

This project focuses entirely on securing a Spring Boot REST API using industry-standard practices: password hashing, stateless JWT authentication, role-based authorization, and token lifecycle management (issuance, refresh, rotation, and revocation).

## Tech Stack

- **Java 17+ / Spring Boot**
- **Spring Security** — filter chain, method-level security
- **Spring Data JPA / Hibernate** — MySQL/MariaDB
- **JJWT (io.jsonwebtoken)** — JWT generation & verification
- **BCrypt** — password hashing
- **Jakarta Bean Validation** — request validation

## Features

- **Register / Login** — BCrypt-hashed passwords, no plaintext ever stored or returned
- **JWT Authentication** — stateless, signed (HS256) access tokens carrying username + roles
- **Custom Security Filter** — per-request token verification via `OncePerRequestFilter`
- **Role-Based Access Control (RBAC)** — multi-role support (`Set<Role>`), enforced via `@PreAuthorize`
- **Refresh Tokens** — DB-backed, UUID-based, with **rotation** (old token invalidated on every use, limiting replay/theft impact)
- **Access Token Blacklisting** — in-memory revocation store so logout takes effect immediately, not just at natural token expiry
- **Global Exception Handling** — consistent JSON error format (`timestamp`, `status`, `error`, `message`, `path`) across validation, auth, and authorization failures
- **Current-user resolution** — `/users/me` derives identity purely from the JWT (`@AuthenticationPrincipal`), never from client-supplied IDs

## Architecture

Entity → DTO → Mapper → Repository → Service → Controller, built as complete vertical slices per feature rather than horizontally across layers.

```
entity/       User, Role, RefreshToken
dto/          request/ (Register, Login, Refresh) · response/ (User, Auth, Error)
mapper/       Entity <-> DTO conversion (no business logic)
repository/   Spring Data JPA (derived queries + JPQL)
security/     JwtService, JwtAuthFilter, CustomUserDetailsService, TokenBlacklistService
service/      AuthService, UserService, RefreshTokenService
controller/   AuthController (/auth/**), UserController (/users/**)
exception/    GlobalExceptionHandler + domain exceptions
```

## Key Endpoints

| Method | Endpoint | Access |
|---|---|---|
| POST | `/auth/register` | Public |
| POST | `/auth/login` | Public |
| POST | `/auth/refresh` | Public (requires valid refresh token) |
| GET | `/users/me` | Authenticated |
| POST | `/users/logout` | Authenticated |
| DELETE | `/users/{id}` | ADMIN only |

## Known Limitations (documented intentionally)

- Access token blacklist is in-memory (`ConcurrentHashMap`) — not shared across multiple server instances. Production would use Redis or similar.
- Refresh tokens are stored raw in the DB rather than hashed (like passwords are).

## What This Project Was Built to Practice

Password hashing, Spring Security's filter chain, JWT structure & signing, custom `UserDetailsService`, method-level authorization, `SecurityContextHolder`, and the access-token/refresh-token tradeoff — including the security reasoning behind each design choice, not just the implementation.