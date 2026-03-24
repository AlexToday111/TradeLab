<h1 align="center">TradeLab Python Parser</h1>

<p align="center">
  FastAPI сервис для импорта рыночных свечей в PostgreSQL.
</p>

<h2 align="center">Стек</h2>

<p align="center">
  <img alt="Python" src="https://img.shields.io/badge/Python-3.11+-3776AB?logo=python&logoColor=white" />
  <img alt="FastAPI" src="https://img.shields.io/badge/FastAPI-009688?logo=fastapi&logoColor=white" />
  <img alt="Uvicorn" src="https://img.shields.io/badge/Uvicorn-111827?logo=gunicorn&logoColor=white" />
  <img alt="Psycopg" src="https://img.shields.io/badge/Psycopg-2E5EAA?logo=postgresql&logoColor=white" />
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white" />
  <img alt="Requests" src="https://img.shields.io/badge/Requests-2.x-5A29E4?logo=python&logoColor=white" />
</p>

<h2 align="center">Структура</h2>

```text
backend/python/
|-- parser/
|   |-- main.py
|   |-- config.py
|   |-- db.py
|   |-- schema.sql
|   |-- exchanges/
|   |   `-- binance/
|   |-- repositories/
|   `-- services/
|-- requirements.txt
`-- run_local_postgres.ps1
```

<h2 align="center">API</h2>

- `GET /health`
- `POST /internal/import/candles`

Пример запроса:

```json
{
  "exchange": "binance",
  "symbol": "BTCUSDT",
  "interval": "1h",
  "from": "2024-01-01T00:00:00Z",
  "to": "2024-01-10T00:00:00Z"
}
```

<h2 align="center">Переменные окружения</h2>

Создай `.env` в `backend/python` на основе `.env.example`.

Ключевые переменные:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `BINANCE_BASE_URL`
- `BINANCE_API_KEY`
- `BINANCE_API_SECRET`
- `PYTHON_SERVICE_PORT`

<h2 align="center">Запуск локально</h2>

```bash
cd backend/python
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
uvicorn parser.main:app --host 0.0.0.0 --port 8000
```

При старте сервис применяет `parser/schema.sql`.

<h2 align="center">Локальный PostgreSQL (опционально)</h2>

```powershell
cd backend/python
powershell -ExecutionPolicy Bypass -File .\run_local_postgres.ps1
```

Скрипт поднимает кластер на `localhost:55432`:

- БД: `tradelab`
- Пользователь: `postgres`
- Пароль: `postgres`

<h2 align="center">Docker</h2>

```bash
docker build -t tradelab-python ./backend/python
```

