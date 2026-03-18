<h1 align="center">TradeLab Monorepo</h1>

![TradeLab Logo](./frontend/public/logo.png)

This repository is now split into two top-level apps:

- `frontend/` - Next.js UI
- `backend/` - backend service scaffold

<h2 align="center">Quick start</h2>

1. Install frontend dependencies:

```bash
npm run install:frontend
```

2. Start frontend from repo root:

```bash
npm run dev
```

Frontend runs on `http://localhost:3000`.

<h2 align="center">Backend scaffold</h2>

Run backend in watch mode:

```bash
npm run dev:backend
```

Backend runs on `http://localhost:4000` and exposes `GET /health`.

<h2 align="center">Useful scripts (from repo root)</h2>

- `npm run dev` - run frontend
- `npm run build` - build frontend
- `npm run start` - start frontend build
- `npm run lint` - lint frontend
- `npm run typecheck` - type-check frontend
- `npm run dev:backend` - run backend
