# FlashBid

FlashBid is a full-stack real-time auction platform built around a Spring Boot backend and a React frontend. The application lets users register, browse auction listings, place bids, watch live auction activity, manage their own profile, and access seller or admin workflows based on role. The backend handles authentication, product and bid management, auction lifecycle rules, persistence, and live event broadcasting. The frontend provides the user-facing experience for browsing products, joining auctions, and operating seller and admin dashboards.

At a high level, the project combines REST APIs for core business actions, WebSockets for live auction updates, PostgreSQL for relational data, Redis for real-time and cache-related support, and JWT-based authentication for secure access to protected features. The repository contains both the backend application in the root project and the frontend application in the `frontend/` directory.

## Features

### User Experience

- User registration and login with JWT-based authentication
- Persistent client-side session state using local storage
- Public product discovery and product detail pages
- Personal profile management for authenticated users
- Seller request flow for users who want product creation access

### Auction and Product Flows

- Product creation for seller and admin roles
- Product editing and deletion flows
- Paginated product listing with filtering and sorting options
- Bid placement with backend validation rules
- Auction winner lookup for completed products
- Automatic auction start and close lifecycle processing

### Real-Time Behavior

- WebSocket-based live auction updates
- STOMP over SockJS integration in the frontend
- Product detail and product list live refresh support
- Scheduler-driven backend auction state transitions

### Admin and Management

- Admin-only user listing and moderation endpoints
- Seller approval and ban/unban actions
- Role-aware routing and guarded pages in the frontend


## Architecture Diagram

```text
                                 +---------------------------+
                                 |        React Frontend     |
                                 |  Vite + React Router      |
                                 |  AuthContext + Axios      |
                                 +------------+--------------+
                                              |
                         HTTP / REST          |          SockJS / STOMP
                                              |
                       +----------------------+----------------------+
                       |                                             |
                       v                                             v
              +--------+-----------------------------------------------+--------+
              |                    Spring Boot Backend                           |
              |------------------------------------------------------------------|
              | Controllers                                                      |
              | - /api/auth                                                      |
              | - /api/products                                                  |
              | - /api/bids                                                      |
              | - /api/user                                                      |
              | - /api/auctions                                                  |
              |                                                                  |
              | Services                                                         |
              | - auth                                                           |
              | - product                                                        |
              | - bid                                                            |
              | - user                                                           |
              | - auction management / winner / scheduler                        |
              |                                                                  |
              | Infrastructure                                                   |
              | - Spring Security + JWT filter                                   |
              | - WebSocket messaging                                            |
              | - Redis integration                                              |
              | - Global exception handling                                      |
              +----------------------+---------------------------+---------------+
                                     |                           |
                          JPA / Hibernate                 Cache / PubSub
                                     |                           |
                                     v                           v
                           +---------+--------+        +---------+--------+
                           |     PostgreSQL   |        |       Redis      |
                           | users, products, |        | live state,      |
                           | bids, auctions   |        | messaging/cache  |
                           +------------------+        +------------------+
```

## Project Structure

### Backend

- `src/main/java/com/example/flashbid/auth`
  Authentication controller, JWT support, security filter chain, auth services, and request/response DTOs.
- `src/main/java/com/example/flashbid/user`
  User entity, role management, profile operations, admin moderation, repositories, services, and controller endpoints.
- `src/main/java/com/example/flashbid/product`
  Product entity, DTOs, repository, business logic, and product-facing APIs.
- `src/main/java/com/example/flashbid/bid`
  Bid entity, DTOs, repository, bidding rules, and bid endpoints.
- `src/main/java/com/example/flashbid/auction`
  Auction domain logic, winner resolution, scheduler, repositories, and auction-related APIs.
- `src/main/java/com/example/flashbid/common`
  Shared configuration, Redis support, WebSocket setup, utility code, and exception handling.
- `src/main/resources`
  Application configuration, including datasource, Redis, JWT secret, timezone, and SpringDoc settings.

### Frontend

- `frontend/src/pages`
  Route-level pages such as home, login, register, products, product details, profile, seller studio, and admin screens.
- `frontend/src/component`
  Reusable UI building blocks including layout, cards, pagination, headers, and status badges.
- `frontend/src/api`
  Axios API client, formatting utilities, and live auction WebSocket client helpers.
- `frontend/src/state`
  Authentication state management via `AuthContext`.
- `frontend/src/App.jsx`
  App routing, protected route logic, and page registration.

## Tech Stack

### Backend

- Java 21
- Spring Boot 3.4.3
- PostgreSQL
- Redis
- JWT Authentication
- SpringDoc OpenAPI
- Maven

### Frontend

- React 19
- Vite 8
- Axios

### Infrastructure and Tooling

- Docker
- Docker Compose

## Running The Project

This repository contains two runtime parts:

- Backend: Spring Boot API in the repository root
- Frontend: React app in `frontend/`

### Prerequisites

- Java 21
- Maven or the included Maven wrapper
- Node.js 20+ and npm
- PostgreSQL
- Redis
- Docker and Docker Compose optional for containerized backend setup

### Frontend Environment

The frontend uses these optional environment variables:

- `VITE_API_BASE_URL`
  Base URL for REST API requests. If omitted, requests are made against the current origin.
- `VITE_WS_BASE_URL`
  Base URL used to derive the WebSocket endpoint. If omitted, the app falls back to `VITE_API_BASE_URL`, then the current origin.

Example:

```bash
VITE_API_BASE_URL=http://localhost:8080
VITE_WS_BASE_URL=http://localhost:8080
```

### Backend Environment Variables

- `PORT`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SPRING_DATA_REDIS_HOST`
- `SPRING_DATA_REDIS_PORT`
- `SPRING_JWT_SECRET`
- `APP_TIMEZONE`

### Local Setup

1. Start the backend services with Docker Compose:

```bash
docker-compose up --build
```

2. Start the frontend in another terminal:

```bash
cd frontend
npm install
VITE_API_BASE_URL=http://localhost:8080 VITE_WS_BASE_URL=http://localhost:8080 npm run dev
```

3. Open the frontend at `http://localhost:5173`.

## API Docs

The backend exposes OpenAPI documentation through SpringDoc.

### Swagger UI

- URL: `http://localhost:8080/swagger-ui.html`
- Purpose: interactive API documentation for testing backend endpoints in the browser

### OpenAPI JSON

- URL: `http://localhost:8080/api/apidocs`
- Purpose: machine-readable OpenAPI specification for tooling, import into API clients, and integration with external documentation systems

### Access Notes

- The documentation routes are publicly accessible through Spring Security configuration.
- Protected API endpoints still require a valid JWT token when called from Swagger UI.
- Authentication endpoints such as login and register can be tested without a token.

## WebSocket Documentation

FlashBid uses WebSockets for live auction updates so clients can receive bid and auction state changes without refreshing the page.

### Connection Details

| Item | Value |
| --- | --- |
| Endpoint | `/ws` |
| Protocol | SockJS + STOMP |
| Broker prefix | `/topic` |
| Application prefix | `/app` |

### Client Subscription Pattern

Frontend clients subscribe to product-specific auction channels using this topic pattern:

| Topic | Description |
| --- | --- |
| `/topic/auctions/{productId}` | Streams live updates for a single product auction |

### Event Flow

1. A bid is placed or an auction state changes in the backend.
2. The backend builds a live auction event and publishes it through Redis.
3. The Redis subscriber receives that payload.
4. Spring WebSocket broadcasting forwards the event to `/topic/auctions/{productId}`.
5. Connected frontend clients receive the updated auction summary and bid information in real time.

### Frontend Integration

- The frontend WebSocket client is implemented in `frontend/src/api/liveAuction.js`.
- The frontend connects through SockJS and STOMP.
- If `VITE_WS_BASE_URL` is set, that value is used to derive the WebSocket host.
- If `VITE_WS_BASE_URL` is not set, the app falls back to `VITE_API_BASE_URL`, then the browser origin.

## Concurrency Explanation

Auction bidding is concurrency-sensitive because multiple users may try to place bids on the same product at nearly the same time. The backend handles this by serializing bid writes at the auction level and publishing live updates only after the database transaction completes.

### How Bid Safety Works

1. When a bid request arrives, the backend loads the auction using a database lock.
2. While that transaction is active, competing bid requests for the same auction must wait.
3. The backend checks the current highest bid, minimum increment, auction status, ownership rules, and end time.
4. Only a valid bid is saved.
5. The live update is scheduled after transaction commit so users do not see an event for a bid that later rolls back.

### Why This Matters

- Prevents two overlapping bid requests from both using the same stale highest bid.
- Reduces race conditions around minimum increment validation.
- Keeps live auction updates consistent with committed database state.
- Helps ensure that users see the accepted bid order rather than an inconsistent temporary state.

## Frontend Pages

| Route | Description |
| --- | --- |
| `/` | Landing and overview page |
| `/login` | User sign-in flow |
| `/register` | User registration flow |
| `/products` | Product browsing and listing page |
| `/products/:productId` | Product detail and live auction page |
| `/products/new` | Seller and admin product creation page |
| `/profile` | Authenticated user profile page |
| `/seller` | Seller workspace |
| `/admin` | Admin dashboard |
| `/users` | Admin user management page |

## Backend API Areas

| Method | Path | Description |
| --- | --- | --- |
| `POST` | `/api/auth/register` | Register a new account |
| `POST` | `/api/auth/login` | Authenticate and receive a JWT token |
| `GET` | `/api/products` | List products with paging, sorting, and filters |
| `GET` | `/api/products/{productId}` | Fetch a single product |
| `POST` | `/api/products` | Create a product listing |
| `PUT` | `/api/products/{id}` | Update product information |
| `DELETE` | `/api/products/{productId}` | Delete a product |
| `POST` | `/api/bids` | Place a bid |
| `GET` | `/api/bids/product/{productId}` | Get bid history for a product |
| `GET` | `/api/bids/user/{userId}` | Get bids placed by a specific user |
| `GET` | `/api/auctions/winner/{productId}` | Get auction winner details for a completed product |
| `POST` | `/api/auctions/{productId}/close` | Force-close an auction as an admin |
| `GET` | `/api/user/me` | Get the currently authenticated user |
| `GET` | `/api/user/{id}` | Get a user by id |
| `GET` | `/api/user/all` | Admin user listing |

## Development Notes

- The backend is the only service currently containerized in `docker-compose.yml`.
- The frontend expects the backend to be reachable either on the same origin or through `VITE_API_BASE_URL`.
- Live auction updates are delivered from the backend WebSocket endpoint at `/ws`.
- Backend tests may require working infrastructure such as PostgreSQL, depending on the test context configuration.
