# Alpha Release Checklist

## Scope
- [ ] Release tag/version chosen (example: `0.1.0-alpha.1`)
- [ ] Changelog updated for this release
- [ ] Release notes drafted

## Code Quality
- [ ] Frontend: `npm --prefix frontend run lint`
- [ ] Frontend: `npm --prefix frontend run typecheck`
- [ ] Frontend: `npm --prefix frontend run test:ci`
- [ ] Frontend: `npm --prefix frontend run build`
- [ ] Python: `cd backend/python && .venv/bin/python -m ruff check .`
- [ ] Python: `cd backend/python && .venv/bin/python -m pytest`
- [ ] Java: `cd backend/java && mvn -B test`

## Runtime Validation
- [ ] `docker compose config -q`
- [ ] `docker compose up --build` starts all services
- [ ] Frontend health: `http://localhost:3000`
- [ ] Java health: `http://localhost:18080/api/health`
- [ ] Python health: `http://localhost:18000/health`

## Config And Security
- [ ] Non-dev credentials are configured for target environment
- [ ] Telegram integration vars are set only if feature is enabled
- [ ] `.env` files are not committed

## Release Completion
- [ ] Release commit pushed to `main`
- [ ] Release tag created
- [ ] Rollback plan documented
