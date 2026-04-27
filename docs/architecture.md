# Архитектура

## Обзор

Система состоит из четырех основных слоев:

1. `frontend`
   Пользовательский интерфейс создает запросы на запуск бэктеста и отображает статусы, summary, сделки и equity curve.

2. `backend/java`
   Spring Boot backend принимает REST-запросы, управляет жизненным циклом запуска, создает execution jobs, хранит run artifacts, управляет dataset snapshots/quality metadata, обслуживает paper trading APIs и сохраняет результаты в БД.

3. `backend/python`
   Python execution/data plane импортирует и нормализует market data, считает data quality report и выполняет стратегии по сохраненным свечам.

4. `PostgreSQL`
   База данных хранит стратегии, свечи, запуски, сделки и точки кривой капитала.

## Scalable Execution Flow

1. Клиент вызывает `POST /api/runs`.
2. Java backend создает `runs` и immutable `run_snapshots`.
3. Java backend создает `execution_jobs` со статусом `QUEUED`.
4. In-process worker claims next queued job, sets `RUNNING`, `locked_by`, `locked_at`, and increments `attempt_count`.
5. Worker calls Python `/internal/runs/execute` with `runId`, `jobId`, and `correlationId`.
6. Python executes the strategy workload and returns structured success or error payload.
7. Java persists metrics, trades, equity curve, and run artifacts on success.
8. Java updates both `runs.status` and `execution_jobs.status`.

The legacy `/backtests` endpoint remains a synchronous compatibility path. New scalable execution work should use `/api/runs`.

## Execution Jobs

`ExecutionJob` represents scheduling and worker lifecycle. `Run` remains the domain entity and reproducibility anchor.

Supported job statuses:

- `QUEUED`
- `RUNNING`
- `SUCCEEDED`
- `FAILED`
- `CANCELED`
- `RETRYING`

Worker readiness is provided through database-backed claiming with pessimistic locking, `locked_by`, `locked_at`, duplicate-active-job guardrails, configurable `max-attempts`, and configurable `max-parallel-jobs`.

Running Python execution cannot be interrupted yet. Canceling a running job sets `cancel_requested`; Java marks the run canceled and discards the Python result when the call returns.

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

## Strategy Management Layer

Strategy management делает стратегии first-class entities:

- root registry хранится в `strategy_files` для совместимости с текущими `runs.strategy_id`
- immutable source versions хранятся в `strategy_versions`
- starter templates хранятся в `strategy_templates`
- reusable parameter payloads хранятся в `strategy_parameter_presets`

Execution flow больше не должен зависеть от “latest file” ambiguity. New `/api/runs` records `strategy_version_id` on the run and `run_snapshots.strategy_version_id` in the reproducibility snapshot. If an older client sends only `strategyId`, Java resolves the current latest version and persists the resolved version id.

Validation is version-scoped. Python performs syntax checks, contract checks, parameter schema serialization checks, and metadata extraction before Java stores the validation report. `INVALID` and `PENDING` versions cannot be activated or executed.

Security boundary: strategy source validation still imports strategy modules to inspect runtime metadata. This is not a full sandbox; uploaded source should be treated as trusted user code until process-level sandboxing is added.

## Paper Trading Layer

Paper trading реализован в Java control plane как безопасный simulated execution слой:

- `paper_trading_sessions` задают owner, exchange, symbol, timeframe, lifecycle и balances
- `paper_orders` хранят simulated order lifecycle и rejection reasons
- `paper_fills` хранят fill/trade history
- `paper_positions` хранят long-only position state
- `ExchangeAdapter` задает будущий adapter contract
- `PaperExchangeAdapter` использует latest stored candle close as simulated price source

Risk checks выполняются до acceptance/fill: session must be `RUNNING`, quantity must be positive, symbol must match the session, BUY requires sufficient quote balance, SELL requires sufficient long position, and order notional is capped.

Live exchange order placement is intentionally not implemented in this layer.

## Границы ответственности

- Контроллеры принимают и возвращают DTO.
- `RunOrchestrationService` отвечает за queued run execution.
- `ExecutionJobService` отвечает за job lifecycle, retry, cancel, claim, and ownership-aware job APIs.
- `PaperTradingService` отвечает за paper session lifecycle, risk checks, simulated orders/fills, balances, positions and ownership-aware paper APIs.
- `BacktestService` сохраняет legacy synchronous `/backtests` flow.
- Репозитории работают только с persistence.
- Python engine не знает о REST и БД Java backend.
