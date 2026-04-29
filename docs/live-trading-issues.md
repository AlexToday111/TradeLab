# Live Trading Issues

## Closed Issues

- Live exchange adapter foundation: implemented Java `LiveExchangeAdapter`, Binance adapter foundation, Python adapter contract, and adapter tests.
- Secure API key management: implemented encrypted credential persistence, masked status responses, and no secret fields in API responses.
- Live order lifecycle: implemented owner-scoped live orders, statuses, submission flow, cancellation, and persisted rejection/failure reasons.
- Live risk engine: implemented mandatory session, kill switch, circuit breaker, connectivity, credential, quantity, price, notional, duplicate-order, whitelist, and available-balance checks where adapter balance data exists.
- Circuit breakers: implemented per-user/per-exchange state, automatic trigger framework, risk events, and manual reset.
- Kill switch: implemented manual activate/reset endpoints and UI controls.
- Position sync: implemented local live positions, sync endpoint, adapter contract, and visibility.
- Monitoring and audit events: implemented structured logs and persisted risk events for rejection, acceptance, safety stops, resets, and sync failure.
- Frontend operational visibility: implemented minimal Live page using existing cards, tables, badges, and controls.
- Docs and operational safety: updated changelog, API docs, architecture, data model, security notes, and live safety runbook.

## Open Issues

- Advanced portfolio risk engine with cross-symbol exposure, drawdown tracking, and realized intraday loss limits.
- Multiple production exchange adapters beyond the Binance foundation.
- Exchange websocket order and position synchronization.
- Advanced slippage checks and abnormal market-data/staleness detection.
- Automatic failover and exchange degradation handling.
- Multi-account execution and account-level permissions.
- Production exchange certification with real sandbox/testnet order reconciliation.
- Live strategy auto-rotation and autonomous deployment workflows.
- Margin, futures, leverage, short-selling, and liquidation protection.
