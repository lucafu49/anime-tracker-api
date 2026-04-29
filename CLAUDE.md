# anime-tracker-api — Project Context

## Overview

Private anime rating web app for a fixed group of 3 users (no public registration). Users can add animes and rate them with a score from 0 to 5, similar to MyAnimeList.

- **Backend:** Spring Boot 4.x (Java 21), Maven
- **Frontend:** React + Vite (not scaffolded yet)
- **Database:** PostgreSQL hosted on freesqldatabase.com
- **Auth:** Stateless JWT via `io.jsonwebtoken` (jjwt) + Spring Security
- **Architecture:** Modular Monolith

## Critical Constraint: 5 MB Database Limit

The free PostgreSQL host enforces a strict 5 MB cap. All design decisions must respect this:

- Anime images are stored as **URLs only** (no binary/blob storage)
- No audit logs, history tables, or session records in DB
- No user registration from the UI — the 3 users are seeded via an initial SQL script
- JWT is stateless; no tokens stored in DB

## Data Model

### `User`
| Column      | Type              | Notes                    |
|-------------|-------------------|--------------------------|
| IDUser      | INT PK identity   |                          |
| Username    | VARCHAR UNIQUE    | Used for login           |
| HashedPass  | VARCHAR           | BCrypt hashed password   |

### `Anime`
| Column    | Type            | Notes                        |
|-----------|-----------------|------------------------------|
| IDAnime   | INT PK identity |                              |
| Name      | VARCHAR         |                              |
| ImageURL  | VARCHAR         | External URL, no binary data |
| Classic   | BOOLEAN         | Default false                |

### `Review`
| Column   | Type              | Notes                                         |
|----------|-------------------|-----------------------------------------------|
| IDReview | INT PK identity   |                                               |
| IDUser   | INT FK → User     |                                               |
| IDAnime  | INT FK → Anime    |                                               |
| Score    | DECIMAL(3,1) 0–5  | One review per user+anime; upsert on conflict |

### SQL Schema (PostgreSQL — freesqldatabase.com)

```sql
CREATE TABLE users (
    IDUser SERIAL PRIMARY KEY,
    Username VARCHAR(50) NOT NULL UNIQUE,
    HashedPass VARCHAR(255) NOT NULL
);

CREATE TABLE Anime (
    IDAnime SERIAL PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    ImageURL VARCHAR(500) NOT NULL
);

CREATE TABLE Review (
    IDReview SERIAL PRIMARY KEY,
    IDUser INT NOT NULL,
    IDAnime INT NOT NULL,
    Score DECIMAL(3,1) NOT NULL,
    CONSTRAINT FK_Review_User FOREIGN KEY (IDUser) REFERENCES users(IDUser),
    CONSTRAINT FK_Review_Anime FOREIGN KEY (IDAnime) REFERENCES Anime(IDAnime),
    CONSTRAINT UQ_Review_UserAnime UNIQUE (IDUser, IDAnime)
);

ALTER TABLE Review ALTER COLUMN Score TYPE DECIMAL(3,1);
ALTER TABLE Review ADD CONSTRAINT chk_score CHECK (Score >= 0 AND Score <= 5);

ALTER TABLE Anime ADD COLUMN Classic BOOLEAN NOT NULL DEFAULT FALSE;
```

## Module Structure

```
src/main/java/com/lucafu/anime_tracker_api/
├── AnimeTrackerApiApplication.java
├── modules/
│   ├── user/
│   │   ├── controller/    → UserController
│   │   ├── service/       → UserService (interface) + UserServiceImpl
│   │   ├── repository/    → UserRepository
│   │   ├── model/         → User (@Entity)
│   │   └── dto/           → UserRequestDto, UserResponseDto
│   ├── anime/
│   │   ├── controller/    → AnimeController
│   │   ├── service/       → AnimeService (interface) + AnimeServiceImpl
│   │   ├── repository/    → AnimeRepository
│   │   ├── model/         → Anime (@Entity)
│   │   └── dto/           → AnimeRequestDto, AnimeResponseDto
│   └── review/
│       ├── controller/    → ReviewController
│       ├── service/       → ReviewService (interface) + ReviewServiceImpl
│       ├── repository/    → ReviewRepository
│       ├── model/         → Review (@Entity)
│       └── dto/           → ReviewRequestDto, ReviewResponseDto
└── shared/
    ├── config/            → SecurityConfig, CorsConfig
    ├── security/          → JwtFilter, JwtUtil, AuthEntryPoint
    └── exception/         → GlobalExceptionHandler, ApiError
```

Each module is self-contained (controller → service → repository). `shared/` is the only cross-cutting package.

## Current State

The project is a Spring Boot shell — no domain modules are implemented yet.

- `AnimeTrackerApiApplication.java` — main entry point
- `application.properties` — reads DB connection from env vars: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- JPA DDL mode: `validate` (schema is managed manually via SQL script, not auto-generated)
- Dependencies already declared: Spring Data JPA, Spring Security, Spring Web, PostgreSQL JDBC, Lombok

## Auth Flow

1. 3 users exist from DB seed script; no registration endpoint
2. `POST /auth/login` → validates credentials → returns JWT
3. All protected endpoints require `Authorization: Bearer <token>` header
4. JWT secret and expiry configured via `application.properties` / env vars
