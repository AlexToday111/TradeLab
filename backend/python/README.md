# TradeLab Python Parser Service

Python service for importing exchange candles into PostgreSQL. The service exposes an internal HTTP API and is designed to be called later from the Java backend.

## Requirements

- Python 3.11+
- PostgreSQL

## Install

```bash
cd backend/python
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
```

## Environment

Create `.env` in `backend/python` from `.env.example`.

Required variables:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `BINANCE_BASE_URL`
- `BINANCE_API_KEY`
- `BINANCE_API_SECRET`
- `PYTHON_SERVICE_PORT`

## Run service

```bash
cd backend/python
uvicorn parser.main:app --host 0.0.0.0 --port 8000
```

Or:

```bash
cd backend/python
python -m parser.main
```

On startup the service connects to PostgreSQL and applies `parser/schema.sql`.

## Health check

```bash
curl http://localhost:8000/health
```

Expected response:

```json
{
  "status": "ok",
  "service": "python-parser"
}
```

## Import candles

```bash
curl -X POST http://localhost:8000/internal/import/candles \
  -H "Content-Type: application/json" \
  -d '{
    "exchange": "binance",
    "symbol": "BTCUSDT",
    "interval": "1h",
    "from": "2024-01-01T00:00:00Z",
    "to": "2024-01-10T00:00:00Z"
  }'
```

Expected response shape:

```json
{
  "status": "success",
  "exchange": "binance",
  "symbol": "BTCUSDT",
  "interval": "1h",
  "imported": 123,
  "from": "2024-01-01T00:00:00Z",
  "to": "2024-01-10T00:00:00Z"
}
```

## Notes for Java integration

- `GET /health` can be used for readiness checks.
- `POST /internal/import/candles` accepts a fixed JSON contract and returns a synchronous import result.
- The service currently supports only `binance`.
- Candle persistence is idempotent-friendly through PostgreSQL `ON CONFLICT DO UPDATE` on `(exchange, symbol, interval, open_time)`.
