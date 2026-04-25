# Модель данных

## `runs`

Главная таблица запусков.

Основные поля:

- `id`
- `strategy_id`
- `status`
- `exchange`
- `symbol`
- `interval`
- `date_from`
- `date_to`
- `params_json`
- `metrics_json`
- `error_message`
- `created_at`
- `started_at`
- `finished_at`

Назначение:

- хранит идентичность запуска и его жизненный цикл
- хранит сериализованный запрос в `params_json`
- хранит summary результата в `metrics_json`
- хранит текст ошибки в `error_message`

## `execution_jobs`

Таблица queue-like lifecycle для выполнения runs.

Основные поля:

- `id`
- `run_id`
- `user_id`
- `status`
- `priority`
- `attempt_count`
- `max_attempts`
- `queued_at`
- `started_at`
- `finished_at`
- `locked_at`
- `locked_by`
- `cancel_requested`
- `error_code`
- `error_message`
- `created_at`
- `updated_at`

Связь:

- `run_id -> runs.id`

Активный job (`QUEUED`, `RETRYING`, `RUNNING`) должен быть единственным для run. Это защищает run от duplicate active execution.

## `backtest_trades`

Таблица сделок конкретного запуска.

Основные поля:

- `id`
- `run_id`
- `entry_time`
- `exit_time`
- `entry_price`
- `exit_price`
- `quantity`
- `pnl`
- `fee`

Связь:

- `run_id -> runs.id`

## `backtest_equity_points`

Таблица точек кривой капитала.

Основные поля:

- `id`
- `run_id`
- `timestamp`
- `equity`

Связь:

- `run_id -> runs.id`

## `run_artifacts`

Metadata и JSON payload артефактов запуска.

Основные поля:

- `id`
- `run_id`
- `artifact_type`
- `artifact_name`
- `content_type`
- `storage_path`
- `payload_json`
- `size_bytes`
- `created_at`

Связь:

- `run_id -> runs.id`

Минимальные типы артефактов:

- `EQUITY_CURVE`
- `TRADES`
- `METRICS_JSON`
- `SUMMARY_JSON`
- `REPORT_JSON`

## `dataset_snapshots`

Версионированное представление dataset metadata для воспроизводимости.

Основные поля:

- `id`
- `dataset_id`
- `dataset_version`
- `source_exchange`
- `symbol`
- `interval`
- `start_time`
- `end_time`
- `row_count`
- `checksum`
- `source_metadata_json`
- `coverage_metadata_json`
- `created_at`

## `dataset_quality_reports`

Сохраненные результаты data quality checks.

Основные поля:

- `id`
- `dataset_id`
- `dataset_snapshot_id`
- `quality_status`
- `issues_json`
- `checked_at`

`quality_status`: `OK`, `WARNING`, `FAILED`.

## Дополнительная таблица `candles`

Для выполнения бэктеста сервис берет исходные OHLCV-данные из `candles`.

Основные поля:

- `exchange`
- `symbol`
- `interval`
- `open_time`
- `close_time`
- `open`
- `high`
- `low`
- `close`
- `volume`

## `paper_trading_sessions`

Paper trading session принадлежит пользователю и задает market/context для simulated execution.

Основные поля:

- `id`
- `user_id`
- `name`
- `exchange`
- `symbol`
- `timeframe`
- `status`
- `initial_balance`
- `current_balance`
- `base_currency`
- `quote_currency`
- `started_at`
- `stopped_at`
- `created_at`
- `updated_at`

`status`: `CREATED`, `RUNNING`, `PAUSED`, `STOPPED`, `FAILED`.

## `paper_orders`

Simulated order внутри paper session.

Основные поля:

- `id`
- `session_id`
- `user_id`
- `symbol`
- `side`
- `type`
- `status`
- `quantity`
- `price`
- `filled_quantity`
- `average_fill_price`
- `created_at`
- `updated_at`
- `filled_at`
- `rejected_reason`

Rejected orders сохраняются для audit/history, но не создают fills.

## `paper_fills`

История simulated fills/trades для восстановления paper trading history.

Основные поля:

- `id`
- `order_id`
- `session_id`
- `symbol`
- `side`
- `quantity`
- `price`
- `fee`
- `fee_currency`
- `executed_at`

## `paper_positions`

Минимальная long-only позиция по session/symbol.

Основные поля:

- `id`
- `session_id`
- `symbol`
- `quantity`
- `average_entry_price`
- `realized_pnl`
- `unrealized_pnl`
- `updated_at`

MVP не моделирует margin, futures, leverage или short selling.
