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
