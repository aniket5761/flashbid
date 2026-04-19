# Backend

The backend is a Spring Boot 3.4.3 application built with Java 21 and Maven. It owns authentication, products, bids, auction lifecycle rules, websocket broadcasting, Redis-backed live state, and PostgreSQL persistence.

## Main Areas

- `src/main/java/com/example/flashbid/auth`
  Authentication, JWT, and security configuration.
- `src/main/java/com/example/flashbid/product`
  Product APIs and product business logic.
- `src/main/java/com/example/flashbid/bid`
  Bid APIs, concurrency-sensitive bidding rules, and bid persistence.
- `src/main/java/com/example/flashbid/auction`
  Auction management, winner resolution, and scheduler logic.
- `src/main/java/com/example/flashbid/common`
  Shared configuration, Redis, websocket support, utilities, and exception handling.
- `src/test`
  Backend tests.

## Local Development

Run the backend from this directory:

```bash
./mvnw spring-boot:run
```

## Build

```bash
./mvnw clean package
```

## Important Files

- `pom.xml`
- `Dockerfile`
- `.env`
- `report.txt`
