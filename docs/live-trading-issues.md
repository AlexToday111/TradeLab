<h1 align="center">Задачи Live Trading</h1>

<h2 align="center">Закрытые задачи</h2>

- Основа live exchange adapter: реализованы Java `LiveExchangeAdapter`, основа Binance adapter, Python adapter contract и adapter tests.
- Безопасное управление API key: реализованы encrypted credential persistence, masked status responses и отсутствие secret fields в API responses.
- Live order lifecycle: реализованы owner-scoped live orders, статусы, submission flow, cancellation и сохранение rejection/failure reasons.
- Live risk engine: реализованы обязательные проверки session, kill switch, circuit breaker, connectivity, credentials, quantity, price, notional, duplicate-order, whitelist и available balance там, где adapter предоставляет balance data.
- Circuit breakers: реализованы per-user/per-exchange state, automatic trigger framework, risk events и manual reset.
- Kill switch: реализованы manual activate/reset endpoints и UI controls.
- Position sync: реализованы local live positions, sync endpoint, adapter contract и visibility.
- Monitoring и audit events: реализованы structured logs и persisted risk events для rejection, acceptance, safety stops, resets и sync failure.
- Операционная видимость во frontend: реализована базовая Live page на существующих cards, tables, badges и controls.
- Документация и operational safety: обновлены changelog, API docs, architecture, data model, security notes и live safety runbook.

<h2 align="center">Открытые задачи</h2>

- Advanced portfolio risk engine с cross-symbol exposure, drawdown tracking и realized intraday loss limits.
- Несколько production exchange adapters сверх основы Binance.
- Exchange WebSocket synchronization для orders и positions.
- Advanced slippage checks и abnormal market-data/staleness detection.
- Automatic failover и обработка exchange degradation.
- Multi-account execution и account-level permissions.
- Production exchange certification с real sandbox/testnet order reconciliation.
- Live strategy auto-rotation и autonomous deployment workflows.
- Margin, futures, leverage, short-selling и liquidation protection.
