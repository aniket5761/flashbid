# Frontend

The frontend is a React 19 + Vite 8 application for browsing products, joining auctions, authenticating users, and viewing live auction updates.

## Main Areas

- `src/pages`
  Route-level pages such as home, login, register, products, product detail, profile, seller tools, and admin views.
- `src/component`
  Reusable UI building blocks.
- `src/api`
  Axios setup, formatting helpers, and live auction websocket helpers.
- `src/state`
  Authentication and shared client-side state.

## Local Development

Run the frontend from this directory:

```bash
npm install
VITE_API_BASE_URL=http://localhost:8080 VITE_WS_BASE_URL=http://localhost:8080 npm run dev
```

The app is served at `http://localhost:5173`.

## Environment Variables

- `VITE_API_BASE_URL`
  Backend REST base URL.
- `VITE_WS_BASE_URL`
  Backend websocket base URL.
