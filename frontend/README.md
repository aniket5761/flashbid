# Frontend

The frontend is a React 19 + Vite 8 application for browsing products, authenticating users, joining auctions, and receiving live auction updates.

## Overview

The frontend is responsible for:

- rendering the public product browsing experience
- handling login and registration flows
- protecting authenticated and role-based routes
- calling backend REST APIs
- subscribing to live auction updates over SockJS/STOMP

## Main Areas

- `src/pages`
  Route-level pages such as home, login, register, products, product detail, profile, seller studio, admin dashboard, and users
- `src/component`
  Shared UI components and layout building blocks
- `src/api`
  Axios setup, API helpers, formatting helpers, and live auction websocket helpers
- `src/state`
  Authentication context and shared session state

## Local Development

From this directory, install dependencies once:

```bash
npm install
```

Then start the dev server:

```bash
npm run dev
```

The app is served at `http://localhost:5173`.

For local backend integration, the backend should already be running on `http://localhost:8080` through the root Docker Compose setup.

## Environment Variables

- `VITE_API_BASE_URL`
  Backend REST base URL
- `VITE_WS_BASE_URL`
  Backend websocket base URL

