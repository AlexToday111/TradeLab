# Release Rollback

Target release: `v0.9.0-alpha.1`

## Triggers

- CI release workflow fails after tag creation.
- Docker smoke validation fails.
- OpenAPI artifacts are missing or malformed.
- Live trading safety guard blocks startup in the target environment.

## Rollback Flow

1. Keep `LIVE_TRADING_REAL_ORDER_SUBMISSION_ENABLED=false`.
2. Announce rollback in the release channel.
3. Stop the current stack with `docker compose down`.
4. Redeploy the last known good tag, currently `v0.8.0-alpha.1`.
5. Run `docker compose config -q`.
6. Run `scripts/docker-compose-smoke.sh`.
7. Verify:
   - `http://localhost:3000`
   - `http://localhost:18080/api/health`
   - `http://localhost:18000/health`
8. Document the failed artifact, workflow, or safety gate in a GitHub issue.

## Data Notes

This alpha does not include destructive migrations. If a future release adds migrations, capture database backup ID and restore point before rollback.
