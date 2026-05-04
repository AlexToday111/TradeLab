# Live Trading Testnet Drill

Target release: `v0.9.0-alpha.1`

## Preconditions

- `LIVE_TRADING_REAL_ORDER_SUBMISSION_ENABLED=false`.
- Binance testnet credentials are stored through the Live Trading UI or API.
- `LIVE_TRADING_CREDENTIAL_ENCRYPTION_KEY`, `SECURITY_JWT_SECRET`, and `PYTHON_PARSER_INTERNAL_SECRET` are not `change-me` values outside local development.

## Drill

1. Start the stack with `docker compose up --build -d`.
2. Open `/settings` and verify Java API, Python parser, and frontend health.
3. Open `/live` and verify submission is shown as disabled.
4. Store active Binance testnet credentials.
5. Run Binance testnet certification.
6. Confirm the response says testnet only and includes account/open-orders snapshot status.
7. Create a live session with a strict symbol whitelist and low notional caps.
8. Enable the session and submit a small guarded test order.
9. Confirm no production submission is enabled and rejected reasons are visible when risk gates block an order.

## Exit Criteria

- Testnet certification completed or produced an explicit operator-facing failure reason.
- Kill switch and circuit breaker state are visible.
- No production order submission was enabled.

## Safety Notes

- Real order submission remains disabled by default.
- Testnet certification does not imply production readiness.
