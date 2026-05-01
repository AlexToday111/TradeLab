# Alpha Release Checklist

Target release: `v0.9.0-alpha.1`

## Scope
- [x] Release tag/version chosen: `0.9.0-alpha.1`
- [x] Changelog updated for this release
- [x] Release notes drafted
- [ ] GitHub release created from tag `v0.9.0-alpha.1`

## Code Quality
- [ ] Frontend: `npm --prefix frontend ci`
- [ ] Frontend: `npm --prefix frontend run lint`
- [ ] Frontend: `npm --prefix frontend run typecheck`
- [ ] Frontend: `npm --prefix frontend run test:ci`
- [ ] Frontend: `npm --prefix frontend run test:smoke`
- [ ] Frontend: `npm --prefix frontend run build`
- [ ] Python: `cd backend/python && pip install -r requirements.txt -r requirements-dev.txt`
- [ ] Python: `cd backend/python && python -m ruff check .`
- [ ] Python: `cd backend/python && python -m pytest`
- [ ] Java: `cd backend/java && mvn -B test`
- [ ] Java: `cd backend/java && mvn -B package -DskipTests`

## Runtime Validation
- [ ] `docker compose config -q`
- [ ] `docker compose up --build` starts all services
- [ ] Frontend health: `http://localhost:3000`
- [ ] Java health: `http://localhost:18080/api/health`
- [ ] Python health: `http://localhost:18000/health`
- [ ] `scripts/docker-compose-smoke.sh` completes successfully
- [ ] `scripts/export-openapi-artifacts.sh 0.9.0-alpha.1` writes:
  - `artifacts/openapi-java-v0.9.0-alpha.1.json`
  - `artifacts/openapi-python-v0.9.0-alpha.1.json`

## Config And Security
- [ ] Non-dev credentials are configured for target environment
- [ ] `SECURITY_JWT_SECRET` is not a `change-me` value outside local development
- [ ] `PYTHON_PARSER_INTERNAL_SECRET` is not a `change-me` value outside local development
- [ ] `LIVE_TRADING_CREDENTIAL_ENCRYPTION_KEY` is not a `change-me` value when live trading is enabled
- [ ] `LIVE_TRADING_REAL_ORDER_SUBMISSION_ENABLED=false` unless a separate production approval exists
- [ ] Telegram integration vars are set only if feature is enabled
- [ ] `.env` files are not committed

## Release Completion
- [ ] Release commit pushed to `main`
- [ ] Release tag created
- [ ] Rollback plan documented
- [ ] Release notes state: real order submission remains disabled by default
- [ ] Release notes state: testnet certification does not imply production readiness
