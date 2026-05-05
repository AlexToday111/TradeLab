<h1 align="center">Архитектура</h1>

<h2 align="center">Обзор</h2>

Система состоит из четырех основных слоев:

1. `frontend`
   Пользовательский интерфейс создает запросы на запуск бэктеста и отображает статусы, summary, сделки и equity curve.

2. `backend/java`
   Spring Boot backend принимает REST-запросы, управляет жизненным циклом запуска, создает execution jobs, хранит run artifacts, управляет dataset snapshots/quality metadata, обслуживает paper trading API и сохраняет результаты в БД.

3. `backend/python`
   Python execution/data plane импортирует и нормализует market data, считает data quality report и выполняет стратегии по сохраненным свечам.

4. `PostgreSQL`
   База данных хранит стратегии, свечи, запуски, сделки и точки кривой капитала.

<h2 align="center">Масштабируемый поток выполнения</h2>

1. Клиент вызывает `POST /api/runs`.
2. Java backend создает `runs` и immutable `run_snapshots`.
3. Java backend создает `execution_jobs` со статусом `QUEUED`.
4. In-process worker забирает следующую queued job, выставляет `RUNNING`, `locked_by`, `locked_at` и увеличивает `attempt_count`.
5. Worker вызывает Python `/internal/runs/execute` с `runId`, `jobId` и `correlationId`.
6. Python выполняет strategy workload и возвращает структурированный success или error payload.
7. Java при успехе сохраняет metrics, trades, equity curve и run artifacts.
8. Java обновляет `runs.status` и `execution_jobs.status`.

Legacy endpoint `/backtests` остается синхронным compatibility path. Новые сценарии масштабируемого выполнения должны использовать `/api/runs`.

<h2 align="center">Execution Jobs</h2>

`ExecutionJob` описывает scheduling и worker lifecycle. `Run` остается domain entity и якорем воспроизводимости.

Поддерживаемые статусы job:

- `QUEUED`
- `RUNNING`
- `SUCCEEDED`
- `FAILED`
- `CANCELED`
- `RETRYING`

Готовность worker обеспечивается database-backed claiming с pessimistic locking, `locked_by`, `locked_at`, защитой от duplicate active job, настраиваемым `max-attempts` и настраиваемым `max-parallel-jobs`.

Running Python execution пока нельзя прервать напрямую. Отмена running job выставляет `cancel_requested`; Java помечает run как canceled и отбрасывает Python result, когда вызов возвращается.

<h2 align="center">Хранение артефактов</h2>

Artifact storage реализован как MVP через PostgreSQL:

- metadata и JSON payload хранятся в `run_artifacts`
- артефакты привязаны к `runs.id`
- ownership проверяется через user-scoped lookup запуска
- базовые артефакты создаются при успешном run: summary, metrics, trades, equity curve, run report

<h2 align="center">Основа Data Platform</h2>

Data layer разделяет:

- raw exchange rows внутри Python import flow
- canonical candles в `candles`
- dataset metadata в `datasets`
- versioned dataset snapshots в `dataset_snapshots`
- quality reports в `dataset_quality_reports`

Run reproducibility продолжает использовать `run_snapshots.dataset_version` и дополнительно сохраняет `dataset_snapshot_id`, если найден matching snapshot.

<h2 align="center">Слой управления стратегиями</h2>

Strategy management делает стратегии first-class entities:

- root registry хранится в `strategy_files` для совместимости с текущими `runs.strategy_id`
- immutable source versions хранятся в `strategy_versions`
- starter templates хранятся в `strategy_templates`
- reusable parameter payloads хранятся в `strategy_parameter_presets`

Execution flow больше не должен зависеть от неоднозначности “latest file”. Новый `/api/runs` записывает `strategy_version_id` в run и `run_snapshots.strategy_version_id` в snapshot воспроизводимости. Если старый client отправляет только `strategyId`, Java определяет текущую latest version и сохраняет найденный version id.

Validation выполняется на уровне version. Python выполняет syntax checks, contract checks, проверки сериализации parameter schema и извлечение metadata до того, как Java сохранит validation report. Версии `INVALID` и `PENDING` нельзя активировать или выполнять.

Граница безопасности: strategy source validation все еще импортирует strategy modules для анализа runtime metadata. Это не полноценный sandbox; uploaded source нужно считать trusted user code, пока не добавлен process-level sandboxing.

<h2 align="center">Слой Paper Trading</h2>

Paper trading реализован в Java control plane как безопасный simulated execution слой:

- `paper_trading_sessions` задают owner, exchange, symbol, timeframe, lifecycle и balances
- `paper_orders` хранят simulated order lifecycle и rejection reasons
- `paper_fills` хранят fill/trade history
- `paper_positions` хранят long-only position state
- `ExchangeAdapter` задает будущий adapter contract
- `PaperExchangeAdapter` использует latest stored candle close как simulated price source

Risk checks выполняются до acceptance/fill: session должна быть `RUNNING`, quantity должна быть положительной, symbol должен совпадать с session, BUY требует достаточного quote balance, SELL требует достаточной long position, а order notional ограничен лимитом.

<h2 align="center">Слой Live Trading</h2>

Live trading расширяет paper architecture отдельным Java control-plane module. Он не переиспользует paper orders для live execution и не обходит границу paper simulation.

- `LiveExchangeAdapter` задает replaceable live adapter contract.
- `BinanceLiveExchangeAdapter` предоставляет основу первого real REST adapter.
- `live_exchange_credentials` хранит encrypted credential material и masked key references.
- `live_trading_sessions` задают вручную включаемые границы account/session и risk caps.
- `live_orders` сохраняет полный live order lifecycle, включая rejected и failed orders.
- `live_positions` хранит local live position snapshots.
- `risk_events`, `circuit_breaker_state` и `kill_switch_state` сохраняют auditability и operational safety state.

Перед тем как live order может дойти до adapter, `LiveTradingService` требует enabled session, inactive kill switch, inactive circuit breaker, active credentials, adapter health, positive quantity, valid limit price, совпадение symbol whitelist, notional limits, duplicate-order protection и available balance, если adapter предоставляет balance data.

Real order submission отключен по умолчанию через `LIVE_TRADING_REAL_ORDER_SUBMISSION_ENABLED=false`. Это сохраняет foundation production-safe до тех пор, пока оператор явно не включит signed exchange submission в контролируемом окружении.

<h2 align="center">Границы ответственности</h2>

- Контроллеры принимают и возвращают DTO.
- `RunOrchestrationService` отвечает за queued run execution.
- `ExecutionJobService` отвечает за job lifecycle, retry, cancel, claim и ownership-aware job API.
- `PaperTradingService` отвечает за paper session lifecycle, risk checks, simulated orders/fills, balances, positions и ownership-aware paper API.
- `LiveTradingService` отвечает за encrypted credentials, guarded live sessions, live order risk gates, adapter submission, position sync, circuit breakers и kill switch.
- `BacktestService` сохраняет legacy synchronous `/backtests` flow.
- Репозитории работают только с persistence.
- Python engine не знает о REST и БД Java backend.
