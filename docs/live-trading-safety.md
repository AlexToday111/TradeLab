# Live Trading Safety Model

Target release: `v0.9.0-alpha.1` — Release Hardening & Testnet Safety.

## Default Boundary

Live trading is safe by default:

- real order submission is disabled unless `LIVE_TRADING_REAL_ORDER_SUBMISSION_ENABLED=true`
- startup rejects `change-me` secrets when real order submission is enabled
- Binance certification is testnet-only and read-only
- credentials must be stored through `/api/live/credentials`
- sessions must be manually enabled
- every order passes mandatory risk checks before adapter submission
- rejected orders are persisted and do not reach the exchange
- kill switch and circuit breakers block new live orders

## Operational Flow

1. Configure `LIVE_TRADING_CREDENTIAL_ENCRYPTION_KEY` with a strong environment-specific value.
2. Keep `LIVE_TRADING_REAL_ORDER_SUBMISSION_ENABLED=false` for local and staging validation.
3. Run the testnet drill before any release candidate is promoted.

## Testnet Certification

The Binance testnet certification endpoint checks read-only account and open-order snapshots against the configured testnet base URL. It does not submit production orders and does not certify production readiness.
3. Store exchange credentials through the live credentials API.
4. Create a live session with conservative notional limits and a symbol whitelist.
5. Enable the session only after exchange health and credential status are valid.
6. Submit a small order and verify order lifecycle, logs, and risk events.
7. Use the kill switch immediately if behavior is unexpected.

## Kill Switch

`POST /api/live/kill-switch/activate` sets emergency stop state for the current user. While active, new live orders are rejected before adapter submission.

`POST /api/live/kill-switch/reset` clears the emergency stop manually.

## Circuit Breakers

Circuit breakers are per user and exchange. The current framework triggers on repeated failed or rejected live orders. When active, new live orders for that exchange are rejected until manually reset with `/api/live/circuit-breakers/reset`.

## Credential Handling

Credentials are encrypted with AES-GCM using a key derived from `LIVE_TRADING_CREDENTIAL_ENCRYPTION_KEY`. API responses only return masked key references. Logs include user, strategy, order, exchange, symbol, status, and reason fields, but not credential values.

## Limitations

- Binance account balance and position signed snapshots are contract-ready but not fully implemented in this alpha.
- Websocket order/position updates are not implemented.
- Advanced portfolio risk, margin/futures/leverage protection, and multi-account execution remain future work.
- Production exchange certification and runbook drills are required before enabling real order submission.
