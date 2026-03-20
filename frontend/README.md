# GreenERP Frontend

A Vue 3 frontend for the GreenERP application, with Keycloak authentication.

## Prerequisites

- Node.js 18+
- Keycloak running on `http://localhost:8080` (via docker-compose)
- GreenERP backend running on `http://localhost:9000`

## Keycloak Setup

Before running the frontend, you need to create a **public client** in Keycloak:

1. Open Keycloak Admin Console: `http://localhost:8080`
2. Log in with `admin` / `nimda`
3. Select (or create) the **green-erp** realm
4. Go to **Clients** → **Create client**
5. Set:
   - **Client ID**: `green-erp`
   - **Client type**: `OpenID Connect`
   - **Client authentication**: `OFF` (public client)
6. In **Valid redirect URIs**, add: `http://localhost:3000/*`
7. In **Valid post logout redirect URIs**, add: `http://localhost:3000/*`
8. In **Web origins**, add: `http://localhost:3000`
9. Save

## Development

```bash
# Install dependencies
npm install

# Start dev server (runs on http://localhost:3000)
npm run dev
```

## How it works

- On page load, the app redirects to Keycloak for login (`login-required` mode)
- After authentication, the JWT token is attached to all API requests
- The Vite dev server proxies `/api/*` requests to the Spring Boot backend at `localhost:9000`
- The Contractors page fetches `GET /contractors` (requires `ADMIN` role)

## Project Structure

```
src/
├── api.js              # Axios instance with Keycloak token interceptor
├── App.vue             # Main layout with header, nav, and logout
├── keycloak.js         # Keycloak configuration
├── main.js             # App entry point with Keycloak init
├── router.js           # Vue Router configuration
└── views/
    └── ContractorsList.vue  # Contractors list page
```
