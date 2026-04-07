# Архитектура

## Обзор

Система состоит из четырех основных слоев:

1. `frontend`
   Пользовательский интерфейс создает запросы на запуск бэктеста и отображает статусы, summary, сделки и equity curve.

2. `backend/java`
   Spring Boot backend принимает REST-запросы, управляет жизненным циклом запуска, читает свечи из PostgreSQL и сохраняет результаты в БД.

3. `backend/python`
   Python backtesting engine выполняет расчет стратегии на CSV-данных и возвращает JSON-результат.

4. `PostgreSQL`
   База данных хранит стратегии, свечи, запуски, сделки и точки кривой капитала.

## Поток данных

1. Клиент вызывает `POST /backtests`.
2. Java backend создает запись в `runs` со статусом `PENDING`.
3. `BacktestService` переводит запуск в `RUNNING`.
4. Сервис читает OHLCV из таблицы `candles` и формирует временный CSV.
5. `PythonBacktestExecutor` запускает Python-скрипт через `ProcessBuilder`.
6. Python возвращает JSON с `summary`, `trades`, `equity_curve`.
7. Java backend сохраняет summary в `runs.metrics_json`, сделки в `backtest_trades`, equity curve в `backtest_equity_points`.
8. Запуск переводится в `COMPLETED` или `FAILED`.

## Границы ответственности

- Контроллеры принимают и возвращают DTO.
- `BacktestService` является единственной точкой orchestration.
- Репозитории работают только с persistence.
- Python engine не знает о REST и БД Java backend.
