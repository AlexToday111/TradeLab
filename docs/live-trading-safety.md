<h1 align="center">Модель безопасности Live Trading</h1>

Целевой релиз: `v0.9.1-alpha.1` — bugfix stabilization для release hardening и testnet safety.

<h2 align="center">Граница по умолчанию</h2>

Live trading безопасен по умолчанию:

- real order submission отключен, если явно не задано `LIVE_TRADING_REAL_ORDER_SUBMISSION_ENABLED=true`
- startup отклоняет `change-me` secrets, когда real order submission включен
- startup допускает local placeholder secrets только пока real order submission отключен
- Binance certification работает только для testnet и read-only
- credentials должны сохраняться через `/api/live/credentials`
- sessions включаются только вручную
- каждый order проходит mandatory risk checks до adapter submission
- rejected orders сохраняются и не доходят до exchange
- kill switch и circuit breakers блокируют новые live orders

<h2 align="center">Операционный поток</h2>

1. Настроить `LIVE_TRADING_CREDENTIAL_ENCRYPTION_KEY` сильным значением для конкретного окружения.
2. Держать `LIVE_TRADING_REAL_ORDER_SUBMISSION_ENABLED=false` для local и staging validation.
3. Выполнить testnet drill до продвижения любого release candidate.

<h2 align="center">Testnet Certification</h2>

Endpoint Binance testnet certification проверяет read-only account и open-order snapshots через настроенный testnet base URL. Он не отправляет production orders и не подтверждает production readiness.

1. Сохранить exchange credentials через live credentials API.
2. Создать live session с консервативными notional limits и symbol whitelist.
3. Включить session только после валидных exchange health и credential status.
4. Отправить небольшой order и проверить order lifecycle, logs и risk events.
5. Немедленно использовать kill switch при неожиданном поведении.

<h2 align="center">Kill Switch</h2>

`POST /api/live/kill-switch/activate` sets emergency stop state for the current user. While active, new live orders are rejected before adapter submission.

`POST /api/live/kill-switch/reset` clears the emergency stop manually.

<h2 align="center">Circuit Breakers</h2>

Circuit breakers работают на уровне user и exchange. Текущий framework срабатывает при повторяющихся failed или rejected live orders. В активном состоянии новые live orders для этого exchange отклоняются до ручного reset через `/api/live/circuit-breakers/reset`.

<h2 align="center">Credential Handling</h2>

Credentials шифруются через AES-GCM с ключом, производным от `LIVE_TRADING_CREDENTIAL_ENCRYPTION_KEY`. API responses возвращают только masked key references. Logs содержат user, strategy, order, exchange, symbol, status и reason fields, но не значения credentials.

<h2 align="center">Ограничения</h2>

- Binance account balance и position signed snapshots подготовлены на уровне contract, но не полностью реализованы в этом alpha.
- WebSocket updates для order/position не реализованы.
- Advanced portfolio risk, margin/futures/leverage protection и multi-account execution остаются future work.
- Production exchange certification и runbook drills обязательны перед включением real order submission.
