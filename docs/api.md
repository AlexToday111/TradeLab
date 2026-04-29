# REST API

## POST `/backtests`

Создает запуск и синхронно выполняет бэктест.

### Пример запроса

```json
{
  "strategyId": 42,
  "exchange": "binance",
  "symbol": "BTCUSDT",
  "interval": "1h",
  "from": "2024-01-01T00:00:00Z",
  "to": "2024-01-03T00:00:00Z",
  "params": {
    "fastPeriod": 10,
    "slowPeriod": 21
  },
  "initialCash": 10000.0,
  "feeRate": 0.001,
  "slippageBps": 5.0,
  "strictData": true
}
```

### Пример ответа

```json
{
  "runId": 101
}
```

## GET `/backtests/{id}`

Возвращает состояние запуска.

### Пример ответа

```json
{
  "runId": 101,
  "strategyId": 42,
  "status": "COMPLETED",
  "exchange": "binance",
  "symbol": "BTCUSDT",
  "interval": "1h",
  "from": "2024-01-01T00:00:00Z",
  "to": "2024-01-03T00:00:00Z",
  "params": {
    "fastPeriod": 10,
    "slowPeriod": 21
  },
  "summary": {
    "profit": 12.5,
    "sharpe": 1.3
  },
  "errorMessage": null,
  "createdAt": "2024-01-01T00:00:00Z",
  "startedAt": "2024-01-01T00:00:01Z",
  "finishedAt": "2024-01-01T00:00:09Z"
}
```

## GET `/backtests/{id}/trades`

Возвращает сделки запуска.

### Пример ответа

```json
[
  {
    "entry_time": "2024-01-01T00:00:00Z",
    "exit_time": "2024-01-01T01:00:00Z",
    "entry_price": 100.0,
    "exit_price": 109.5,
    "qty": 1.0,
    "pnl": 9.5,
    "fee": 0.2
  }
]
```

## GET `/backtests/{id}/equity`

Возвращает кривую капитала.

### Пример ответа

```json
[
  {
    "timestamp": "2024-01-01T01:00:00Z",
    "equity": 10009.5
  }
]
```

## Scalable runs API

### POST `/api/runs`

Создает `Run`, immutable snapshot и `ExecutionJob` со статусом `QUEUED`.
`strategyVersionId` и `parameterPresetId` опциональны. Если `strategyVersionId` не передан, backend использует latest version стратегии для совместимости и сохраняет выбранную версию в `runs.strategy_version_id` и `run_snapshots.strategy_version_id`.

```json
{
  "strategyId": 42,
  "strategyVersionId": 101,
  "parameterPresetId": 7,
  "exchange": "binance",
  "symbol": "BTCUSDT",
  "interval": "1h",
  "from": "2024-01-01T00:00:00Z",
  "to": "2024-01-03T00:00:00Z",
  "params": {
    "fastPeriod": 10
  }
}
```

### GET `/api/runs/{id}/execution`

Возвращает latest execution job текущего пользователя для запуска.

### POST `/api/runs/{id}/retry`

Повторно ставит failed job в очередь, если лимит `maxAttempts` не исчерпан. Canceled jobs не retry-ятся.

### POST `/api/runs/{id}/cancel`

Отменяет queued job сразу. Для running job выставляет `cancelRequested=true`; Python interruption будет добавлен позже.

### GET `/api/execution-jobs`

Возвращает execution jobs текущего пользователя.

### GET `/api/execution-jobs/{id}`

Возвращает execution job по id с ownership-проверкой.

## Strategy management API

Все endpoints требуют JWT-auth, кроме внутренних Python endpoints. User-owned resources возвращаются только текущему пользователю.

### POST `/api/strategies`

Создает draft strategy registry record без source version.

### GET `/api/strategies`

Возвращает стратегии текущего пользователя.

### GET `/api/strategies/{id}`

Возвращает одну owned strategy.

### PATCH `/api/strategies/{id}`

Обновляет editable metadata: `name`, `description`, `strategyType`, `lifecycleStatus`, `metadata`, `tags`.

### POST `/api/strategies/{id}/archive`

Переводит strategy lifecycle в `ARCHIVED`.

### POST `/api/strategies/upload`

Compatibility upload flow. Создает strategy registry record и initial immutable version из `.py` файла.

### POST `/api/strategies/{id}/versions`

Загружает новый `.py` файл как immutable strategy version. Version получает checksum, file metadata и persisted validation result.

### GET `/api/strategies/{id}/versions`

Возвращает version history стратегии.

### GET `/api/strategy-versions/{versionId}`

Возвращает одну owned strategy version.

### POST `/api/strategy-versions/{versionId}/validate`

Повторно запускает validation contract и сохраняет validation report.

### POST `/api/strategy-versions/{versionId}/activate`

Активирует только `VALID` или `WARNING` version. `INVALID` и `PENDING` версии не активируются.

### GET `/api/strategy-templates`

Возвращает system-owned starter templates.

### GET `/api/strategy-templates/{id}`

Возвращает один template.

### POST `/api/strategies/{id}/presets`

Создает owner-scoped parameter preset.

```json
{
  "name": "BTC 1h default",
  "presetPayload": {
    "fastPeriod": 10,
    "slowPeriod": 21
  }
}
```

### GET `/api/strategies/{id}/presets`

Возвращает presets owned user для strategy.

### PATCH `/api/strategy-presets/{presetId}`

Обновляет preset name/payload.

### DELETE `/api/strategy-presets/{presetId}`

Удаляет owned preset.

## Run artifacts API

### GET `/api/runs/{id}/artifacts`

Возвращает metadata артефактов запуска, доступных текущему пользователю.

### GET `/api/runs/{id}/artifacts/{artifactId}`

Возвращает metadata и JSON payload конкретного артефакта.

### GET `/api/runs/{id}/artifacts/{artifactId}/download`

Возвращает содержимое артефакта как downloadable file.

## Dataset platform API

### GET `/api/datasets/{id}`

Возвращает dataset payload, latest snapshot и latest quality report.

### GET `/api/datasets/{id}/versions`

Возвращает snapshot/version history dataset.

### GET `/api/datasets/{id}/quality`

Возвращает сохраненные quality reports dataset.

### GET `/api/dataset-snapshots/{snapshotId}`

Возвращает metadata конкретного dataset snapshot с ownership-проверкой через parent dataset.

## Paper trading API

Все endpoints требуют JWT-auth и возвращают только ресурсы текущего пользователя.

### POST `/api/paper/sessions`

Создает paper trading session со статусом `CREATED`.

```json
{
  "name": "BTC paper",
  "exchange": "binance",
  "symbol": "BTCUSDT",
  "timeframe": "1h",
  "initialBalance": 10000,
  "baseCurrency": "BTC",
  "quoteCurrency": "USDT"
}
```

### GET `/api/paper/sessions`

Возвращает sessions текущего пользователя.

### GET `/api/paper/sessions/{id}`

Возвращает одну owned session.

### POST `/api/paper/sessions/{id}/start`

Переводит session в `RUNNING`.

### POST `/api/paper/sessions/{id}/pause`

Переводит running session в `PAUSED`.

### POST `/api/paper/sessions/{id}/stop`

Переводит session в `STOPPED`.

### POST `/api/paper/sessions/{id}/orders`

Создает simulated order. Market orders fill immediately at the latest stored candle close. Limit orders are accepted and fill at submission time only when the latest close crosses the limit.

```json
{
  "side": "BUY",
  "type": "MARKET",
  "quantity": 0.01
}
```

```json
{
  "side": "BUY",
  "type": "LIMIT",
  "quantity": 0.01,
  "price": 60000
}
```

Rejected orders are persisted with `status=REJECTED` and `rejectedReason`; no fill is created.

### GET `/api/paper/sessions/{id}/orders`

Returns orders for an owned session.

### GET `/api/paper/orders/{orderId}`

Returns one owned paper order.

### POST `/api/paper/orders/{orderId}/cancel`

Cancels a `NEW` or `ACCEPTED` paper order.

### GET `/api/paper/sessions/{id}/positions`

Returns paper positions for an owned session.

### GET `/api/paper/sessions/{id}/fills`

Returns simulated fills for an owned session.

### GET `/api/paper/sessions/{id}/summary`

Returns balance, PnL, equity, order count, fill count, and open position count for an owned session.

No endpoint in this release places real exchange orders.

## Live trading API

Все endpoints требуют JWT-auth и возвращают только ресурсы текущего пользователя.

### POST `/api/live/credentials`

Stores encrypted live exchange credentials. API key and secret are never returned in responses.

```json
{
  "exchange": "binance",
  "apiKey": "******",
  "apiSecret": "******",
  "active": true
}
```

### GET `/api/live/credentials/status`

Returns masked credential status: `id`, `exchange`, `keyReference`, `active`, `createdAt`, `updatedAt`.

### POST `/api/live/sessions`

Creates an owner-scoped guarded live trading session with explicit risk limits.

```json
{
  "name": "BTC live guarded",
  "exchange": "binance",
  "symbol": "BTCUSDT",
  "baseCurrency": "BTC",
  "quoteCurrency": "USDT",
  "maxOrderNotional": 100,
  "maxPositionNotional": 500,
  "maxDailyNotional": 1000,
  "symbolWhitelist": "BTCUSDT"
}
```

### GET `/api/live/sessions`

Returns live sessions for the authenticated user.

### POST `/api/live/sessions/{id}/enable`

Enables a live session only when active credentials exist.

### POST `/api/live/sessions/{id}/disable`

Disables a live session and blocks new live order placement through that session.

### POST `/api/live/orders`

Creates a live order. Mandatory risk checks run before adapter submission. Rejected orders are persisted and never reach the exchange.

```json
{
  "sessionId": 1,
  "strategyId": 12,
  "strategyVersionId": 44,
  "side": "BUY",
  "type": "MARKET",
  "quantity": 0.001,
  "sourceRunId": 55
}
```

### GET `/api/live/orders`

Returns live orders owned by the current user.

### GET `/api/live/orders/{id}`

Returns one owned live order.

### POST `/api/live/orders/{id}/cancel`

Cancels an open live order. Ownership is enforced before adapter cancellation.

### GET `/api/live/positions`

Returns local live position state.

### POST `/api/live/positions/sync`

Synchronizes positions from configured exchange adapters where supported.

### GET `/api/live/balances`

Returns adapter balance snapshots where supported.

### GET `/api/live/risk/status`

Returns kill switch and circuit breaker state.

### GET `/api/live/risk/events`

Returns recent live risk and safety events.

### POST `/api/live/kill-switch/activate`

Activates manual emergency stop and blocks new live orders.

```json
{
  "reason": "Manual emergency stop",
  "cancelOpenOrders": false
}
```

### POST `/api/live/kill-switch/reset`

Manually resets the kill switch.

### POST `/api/live/circuit-breakers/reset`

Manually resets circuit breaker state for the current user.

### GET `/api/live/exchange/health?exchange=binance`

Checks adapter connectivity, credential presence/validity, and whether real order submission is enabled.

## Ошибки

Все ошибки возвращаются в JSON:

```json
{
  "timestamp": "2024-01-01T00:00:05Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Field 'from' must be before 'to'",
  "path": "/backtests"
}
```
