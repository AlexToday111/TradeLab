# Архитектура

## Обзор

Система состоит из четырех основных слоев:

1. `frontend`
   Пользовательский интерфейс создает запросы на запуск бэктеста и отображает статусы, summary, сделки и equity curve.

2. `backend/java`
   Spring Boot backend принимает REST-запросы, управляет жизненным циклом запуска, хранит run artifacts, управляет dataset snapshots/quality metadata и сохраняет результаты в БД.

3. `backend/python`
   Python execution/data plane импортирует и нормализует market data, считает data quality report и выполняет стратегии по сохраненным свечам.

4. `PostgreSQL`
   База данных хранит стратегии, свечи, запуски, сделки и точки кривой капитала.

## Поток данных

1. Клиент вызывает `POST /backtests`.
2. Java backend создает запись в `runs` со статусом `PENDING`.
3. `BacktestService` переводит запуск в `RUNNING`.
4. Сервис читает OHLCV из таблицы `candles` и формирует временный CSV.
5. `PythonBacktestExecutor` запускает Python-скрипт через `ProcessBuilder`.
6. Python возвращает JSON с `summary`, `trades`, `equity_curve`.
7. Java backend сохраняет summary/metrics в `runs`, сделки в `backtest_trades`, equity curve в `backtest_equity_points` и JSON artifacts в `run_artifacts`.
8. Запуск переводится в `COMPLETED` или `FAILED`.

## Artifact Storage

Artifact storage реализован как MVP через PostgreSQL:

- metadata и JSON payload хранятся в `run_artifacts`
- артефакты привязаны к `runs.id`
- ownership проверяется через user-scoped run lookup
- базовые артефакты создаются при успешном run: summary, metrics, trades, equity curve, run report

## Data Platform Foundation

Data layer разделяет:

- raw exchange rows внутри Python import flow
- canonical candles в `candles`
- dataset metadata в `datasets`
- versioned dataset snapshots в `dataset_snapshots`
- quality reports в `dataset_quality_reports`

Run reproducibility продолжает использовать `run_snapshots.dataset_version` и дополнительно сохраняет `dataset_snapshot_id`, если matching snapshot найден.

## Границы ответственности

- Контроллеры принимают и возвращают DTO.
- `BacktestService` является единственной точкой orchestration.
- Репозитории работают только с persistence.
- Python engine не знает о REST и БД Java backend.
