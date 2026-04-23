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

## GET `/api/runs/{id}/artifacts`

Возвращает список артефактов запуска, доступный только владельцу run.

### Пример ответа

```json
[
  {
    "id": 1,
    "runId": 101,
    "artifactType": "EQUITY_CURVE",
    "artifactName": "equity_curve.json",
    "contentType": "application/json",
    "storagePath": "db://run_artifacts/101/equity_curve.json",
    "sizeBytes": 2048,
    "createdAt": "2026-04-23T12:00:00Z"
  }
]
```

## GET `/api/runs/{id}/artifacts/{artifactId}`

Возвращает metadata и JSON payload конкретного артефакта.

## GET `/api/runs/{id}/artifacts/{artifactId}/download`

Возвращает артефакт как downloadable JSON file.

## GET `/api/datasets/{id}`

Возвращает payload датасета, latest snapshot и latest quality report.

## GET `/api/datasets/{id}/versions`

Возвращает список dataset snapshots/versions.

## GET `/api/datasets/{id}/quality`

Возвращает сохраненные quality reports датасета.

## GET `/api/dataset-snapshots/{snapshotId}`

Возвращает metadata конкретного dataset snapshot.

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
