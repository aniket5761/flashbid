# FlashBid

FlashBid is a full-stack real-time auction platform with a Spring Boot backend, a React frontend, and a dedicated k6 load-testing workspace.

## Repository Layout

- `backend/`
  Spring Boot API, Maven wrapper, Dockerfile, backend report, source code, tests, and build artifacts.
- `frontend/`
  React + Vite client application.
- `k6/`
  Load-test scripts, test-user data, and saved results.
- `docker-compose.yml`
  Root orchestration file for local backend infrastructure and the backend container.

## Architecture

```text
frontend/  -> React + Vite UI
backend/   -> Spring Boot API + PostgreSQL + Redis integration
k6/        -> Load and concurrency testing scripts
```

## Local Development

### Backend

From `backend/`:

```bash
./mvnw spring-boot:run
```

### Frontend

From `frontend/`:

```bash
npm install
VITE_API_BASE_URL=http://localhost:8080 VITE_WS_BASE_URL=http://localhost:8080 npm run dev
```

### Docker Compose

From the repository root:

```bash
docker-compose up --build
```

This starts PostgreSQL, Redis, and the backend using the Dockerfile in `backend/`.

## Load Testing

The load-testing assets now live under `k6/`.

- k6 guide: [k6/README.md](/home/aniket/IdeaProjects/flashbid/k6/README.md)
- frontend guide: [frontend/README.md](/home/aniket/IdeaProjects/flashbid/frontend/README.md)
- backend guide: [backend/README.md](/home/aniket/IdeaProjects/flashbid/backend/README.md)

Browse test from the repo root:

```bash
docker run --rm -i \
  -v "$PWD:/work" \
  -w /work/k6 \
  -e BASE_URL="https://flashbid-production-e7f4.up.railway.app" \
  -e PRODUCT_ID="7" \
  grafana/k6 run scripts/browse.js
```

## Key Features

- JWT authentication
- Product listing and detail flows
- Real-time bidding with websocket updates
- Redis-backed live auction state
- PostgreSQL persistence
- k6-based concurrency testing
