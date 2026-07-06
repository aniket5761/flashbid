# FlashBid

> A full-stack real-time auction platform built with React, Spring Boot,
> PostgreSQL, Redis, and WebSockets.

FlashBid enables users to browse products, participate in live auctions,
and place bids with instant updates. The platform combines a React
frontend with a Spring Boot backend that provides authentication,
business logic, persistence, and real-time communication.

------------------------------------------------------------------------

## Table of Contents

-   Project Overview
-   Main Features
-   Tech Stack
-   Repository Structure
-   High-Level Architecture
-   How to Run Locally
-   Related Documentation

------------------------------------------------------------------------

## Project Overview

FlashBid is a full-stack real-time auction platform where users can
browse products, place bids, and follow live auction activity as it
happens.

The application consists of:

-   **React + Vite** frontend for a responsive user experience
-   **Spring Boot** backend for authentication, business logic, and APIs
-   **WebSockets (STOMP)** for live auction updates
-   **PostgreSQL** for persistent data storage
-   **Redis** for real-time auction state and event distribution

## Features

### Authentication & Security
- Secure user registration and login
- JWT-based authentication
- Role-based access control (User, Seller, Admin)

### Auction Experience
- Browse active auction listings
- View detailed product information
- Place bids in real time
- Receive live auction updates via WebSockets

### Seller Dashboard
- Create and publish auction listings
- Manage products and auctions
- Monitor auction activity

### Real-Time Communication
- Instant bid synchronization
- Live auction event broadcasting
- Redis-backed event distribution

### Developer Experience
- Interactive Swagger/OpenAPI documentation
- Docker Compose for local development
- Modular frontend and backend architecture

## Tech Stack

| Category | Technologies |
|----------|--------------|
| **Frontend** | React, Vite, React Router, Axios |
| **Backend** | Java 21, Spring Boot, Spring Security, Spring Data JPA |
| **Real-Time Communication** | Spring WebSocket (STOMP), Redis |
| **Database** | PostgreSQL |
| **Authentication** | JWT (JSON Web Token) |
| **API Documentation** | Swagger / OpenAPI |
| **Development & Deployment** | Docker, Docker Compose |

## Repository Structure

``` text
FlashBid/
├── backend/              # Spring Boot API
├── frontend/             # React application
├── docker-compose.yml    # Local infrastructure
└── README.md
```

------------------------------------------------------------------------

## Architecture

The frontend communicates with the backend in two ways:

-   REST API calls for authentication, products, users, bids, and
    auction actions
-   WebSocket (STOMP) connections for live auction updates

The backend stores application data in PostgreSQL while Redis supports
live auction state and event distribution.

### Architecture Diagram

``` mermaid
flowchart LR
    U[User Browser] --> F[Frontend<br/>React + Vite]
    F -->|HTTP REST API| B[Backend<br/>Spring Boot]
    F -->|WebSocket / STOMP| B
    B -->|Persistent Storage| DB[(PostgreSQL)]
    B -->|Live State / Pub-Sub| R[(Redis)]
```

------------------------------------------------------------------------

## How to Run Locally

### Prerequisites

-   Docker and Docker Compose installed
-   Node.js and npm installed

### 1. Start the Backend Stack

From the project root:

``` bash
docker compose up -d
```

This starts:

-   PostgreSQL (`localhost:5432`)
-   Redis (`localhost:6379`)
-   Spring Boot Backend (`localhost:8080`)

> **Note**
>
> Docker Compose starts PostgreSQL, Redis, and the backend service. The
> React frontend is started separately.

### 2. Start the Frontend

From the `frontend/` directory:

``` bash
npm install
npm run dev
```

Application URLs:

-   Frontend: http://localhost:5173
-   Backend API: http://localhost:8080
-   Swagger UI: http://localhost:8080/swagger-ui/index.html

------------------------------------------------------------------------

## Related Documentation

-   Backend: [backend/README.md](backend/README.md)
-   Frontend: [frontend/README.md](frontend/README.md)