# TradeLab Monorepo

This repository is now split into two top-level apps:

- `frontend/` - Next.js UI
- `backend/` - backend service scaffold

## Quick start

1. Install frontend dependencies:

```bash
npm run install:frontend
```

2. Start frontend from repo root:

```bash
npm run dev
```

Frontend runs on `http://localhost:3000`.

## Backend scaffold

Run backend in watch mode:

```bash
npm run dev:backend
```

Backend runs on `http://localhost:4000` and exposes `GET /health`.

## Useful scripts (from repo root)

- `npm run dev` - run frontend
- `npm run build` - build frontend
- `npm run start` - start frontend build
- `npm run lint` - lint frontend
- `npm run typecheck` - type-check frontend
- `npm run dev:backend` - run backend
