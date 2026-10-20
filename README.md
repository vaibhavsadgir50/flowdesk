# FlowDesk

AI-assisted workflow manager. Teams log work items (support requests, bug reports, internal
tasks), move them through a status workflow, and use an AI assistant to summarise each item,
categorise it and draft a reply.

**Stack:** Java 17, Spring Boot 3, Hibernate/JPA, PostgreSQL, React 18, TypeScript, Vite,
JUnit 5, Docker, GitHub Actions.

## Features

- REST API for work items: create, read, update, delete, filter by status
- Status workflow (open, in progress, resolved, closed) and priorities
- **AI assistant**: one click generates a summary, a category and a draft reply, stored on the item
  - Backed by an LLM messages API when `LLM_API_KEY` is set
  - Falls back to an offline rule-based assistant when no key is configured, so the app always works
  - Tolerates malformed model output (code fences, unknown categories) and returns a clear error if the model is unavailable
- React + TypeScript dashboard with status filter and per-item AI panel
- Validation and consistent JSON error responses
- CI runs backend and frontend tests; pushes to `main` build and publish Docker images

## Architecture

```
React (Vite) --> /api --> Spring Boot controller --> service --> JPA repository --> PostgreSQL
                                                        |
                                                        +--> AiProvider (LlmAiProvider | HeuristicAiProvider)
```

`AiProvider` is an interface so the LLM integration is swappable and easy to test. The LLM call
runs outside a database transaction so a slow response never holds a connection.

## Run with Docker

```bash
cp .env.example .env      # optionally set LLM_API_KEY
docker compose up --build
```

The dashboard is served on port 3000 and the API on port 8080.

## Run locally

```bash
# database
docker compose up db -d

# backend (port 8080)
cd backend && mvn spring-boot:run

# frontend (port 5173, proxies /api to the backend)
cd frontend && npm install && npm run dev
```

## API

| Method | Path | Description |
| --- | --- | --- |
| GET | `/api/items?status=OPEN` | List items, optionally by status |
| POST | `/api/items` | Create an item |
| GET | `/api/items/{id}` | Get one item |
| PUT | `/api/items/{id}` | Update an item |
| DELETE | `/api/items/{id}` | Delete an item |
| POST | `/api/items/{id}/ai/analyze` | Generate summary, category and draft reply |

## Configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | local Postgres | Database connection |
| `LLM_API_KEY` | empty | Enables the LLM-backed assistant |
| `LLM_MODEL` | `claude-haiku-5-5` | Model used for analysis |
| `LLM_BASE_URL` | provider default | Override the LLM endpoint |
| `CORS_ORIGINS` | `http://localhost:5173` | Allowed browser origins |

## Tests

```bash
cd backend && mvn test      # service, AI provider and controller tests (H2 in-memory)
cd frontend && npm test     # component tests (Vitest + Testing Library)
```

## License

MIT
