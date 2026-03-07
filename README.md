# ⚡ FlashBid - Real-Time Auction System

FlashBid is a high-performance, real-time bidding platform built with **Spring Boot 3.4.3** and **Java 21**. It features a clean, layered architecture and provides a robust engine for managing automated auction lifecycles.

## 🚀 Key Features

### ⚡ Real-Time Bidding System
- **Live Updates**: Uses WebSockets to broadcast incoming bids instantly to all connected users interested in a product.
- **Validation Logic**: Automatically enforces that every new bid is higher than the current highest bid.
- **Anti-Shill Protection**: Prevents product owners from bidding on their own items to artificially inflate prices.

### 🔐 Secure Authentication & Authorization
- **JWT Security**: Implements stateless authentication using JSON Web Tokens (JWT).
- **Role-Based Access (RBAC)**:
  - **USER**: Can list products, place bids, and view auction winners.
  - **ADMIN**: Has elevated permissions, including the ability to delete any product or managed users.

### 🕒 Automated Auction Lifecycle
- **Scheduled Starts**: Sellers can set a future start time; the system automatically opens the auction at the exact second.
- **Auto-Closing**: A background scheduler monitors auction end times, closes expired auctions, and determines the winner automatically.
- **Status Tracking**: Products move through a strict state machine: `SCHEDULED` → `OPEN` → `CLOSED`.

### 📦 Product & Content Management
- **Smart Search**: Supports advanced filtering by name, category (ProductStatus), and price (High-to-Low / Low-to-High).
- **Pagination**: All listing APIs use database-level pagination to ensure fast loading.
- **Redis Caching**: Transparently caches product details in Redis to reduce database load and speed up page hits.

### 🛠 Developer-Friendly Infrastructure
- **Swagger/OpenAPI**: Fully documented interactive API dashboard available at `/swagger-ui.html`.
- **Docker Ready**: Includes a multi-stage Docker setup and `docker-compose.yml` for easy deployment.
- **Unified Configuration**: Environment-aware properties for PostgreSQL and Redis settings.
- **Standardized Error Handling**: Global exception handling ensures consistent and helpful error messages.

## 🛠 Tech Stack

- **Backend**: Java 21, Spring Boot 3.4.3
- **Security**: Spring Security, JWT (JJWT)
- **Database**: PostgreSQL
- **Caching**: Redis
- **Real-Time**: Spring WebSocket
- **API Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Build Tool**: Maven
- **Utilities**: Lombok, Jackson JSR310

## 🚦 Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 21 (for local development)
- Maven (for local development)

### 🐳 Running with Docker
1. Clone the repository.
2. Run the following command in the root directory:
   ```bash
   docker-compose up --build
   ```
3. The application will be available at `http://localhost:8080`.

### ⚙️ Local Development
1. Ensure PostgreSQL and Redis are running.
2. Configure credentials in `src/main/resources/application.yml` or via environment variables.
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

## 📖 API Documentation

The full API documentation is available via Swagger UI once the application is running:
`http://localhost:8080/swagger-ui/index.html`

### 🔑 Authentication
- `POST /api/auth/register` - Register a new user.
- `POST /api/auth/login` - Login and receive a JWT token.

### 📦 Products
- `GET /api/products` - List products with optional filters (`name`, `productStatus`, `sortBy`, `sortDir`).
- `GET /api/products/{productId}` - Get detailed information for a single product.
- `POST /api/products` - Create a new product auction (Requires Authentication).
- `PUT /api/products/{id}` - Edit product details.
- `DELETE /api/products/{productId}` - Delete a product.

### 🔨 Bidding
- `POST /api/bids` - Place a new bid on an open auction.
- `GET /api/bids/product/{productId}` - Get bid history for a product.

### 🏆 Auctions
- `GET /api/auctions/winner/{productId}` - Retrieve the winner details for a closed auction.

### 👤 Users
- `GET /api/user/{id}` - Get user profile.
- `GET /api/user/all` - List all users (Paginated).

## 📂 Project Architecture

The project follows a **Clean, Layered Architecture**:
- `auth/`: Security configuration, JWT, and Auth logic.
- `user/`: User entity and management.
- `product/`: Auction item management and lifecycle.
- `bid/`: Real-time bidding logic.
- `auction/`: Winner determination and automated scheduler.
- `common/`: Global exceptions, WebSocket handlers, and shared utilities.


