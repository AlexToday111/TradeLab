# Модель данных

## `runs`

Главная таблица запусков.

Основные поля:

- `id`
- `strategy_id`
- `strategy_version_id`
- `parameter_preset_id`
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
- связывает запуск с exact strategy version через `strategy_version_id`, если run создан через strategy management flow

## `strategy_files`

Root strategy registry table, сохраненная под историческим именем для совместимости с `runs.strategy_id`.

Основные поля:

- `id`
- `user_id`
- `strategy_key`
- `name`
- `description`
- `strategy_type`
- `lifecycle_status`
- `latest_version`
- `latest_version_id`
- `filename`
- `storage_path`
- `status`
- `validation_error`
- `parameters_schema_json`
- `metadata_json`
- `tags_json`
- `content_type`
- `size_bytes`
- `checksum`
- `uploaded_at`
- `created_at`
- `updated_at`

Связь:

- `user_id -> users.id`
- `latest_version_id -> strategy_versions.id` logically references the active/latest source version

`lifecycle_status`: `DRAFT`, `ACTIVE`, `DEPRECATED`, `ARCHIVED`.
`status`: latest validation status compatibility field, currently `PENDING`, `VALID`, `INVALID`.

## `strategy_versions`

Immutable source/version registry for strategies.

Основные поля:

- `id`
- `strategy_id`
- `version`
- `file_path`
- `filename`
- `content_type`
- `size_bytes`
- `checksum`
- `validation_status`
- `validation_report`
- `parameters_schema_json`
- `metadata_json`
- `execution_engine_version`
- `created_at`
- `created_by`

Связь:

- `strategy_id -> strategy_files.id`
- `created_by -> users.id`

`validation_status`: `PENDING`, `VALID`, `WARNING`, `INVALID`.
Files are version-linked by checksum and storage path. The platform does not execute arbitrary uploaded files until validation passes and run creation resolves an executable version.

## `strategy_templates`

System-owned starter templates.

Основные поля:

- `id`
- `template_key`
- `name`
- `description`
- `strategy_type`
- `category`
- `default_parameters_json`
- `template_reference`
- `metadata_json`
- `created_at`

## `strategy_parameter_presets`

Reusable owner-scoped parameter payloads for strategies.

Основные поля:

- `id`
- `strategy_id`
- `user_id`
- `name`
- `preset_payload`
- `created_at`
- `updated_at`

Связь:

- `strategy_id -> strategy_files.id`
- `user_id -> users.id`

Runs copy the effective preset payload into `run_snapshots.parameter_preset_snapshot_json`.

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

## `live_exchange_credentials`

Encrypted owner-scoped exchange credentials.

Основные поля:

- `id`
- `user_id`
- `exchange`
- `key_reference`
- `encrypted_api_key`
- `encrypted_api_secret`
- `is_active`
- `created_at`
- `updated_at`

Raw API keys and secrets are not returned by API responses.

## `live_trading_sessions`

Manual live execution boundary with per-session risk limits.

Основные поля:

- `id`
- `user_id`
- `name`
- `exchange`
- `symbol`
- `base_currency`
- `quote_currency`
- `status`
- `max_order_notional`
- `max_position_notional`
- `max_daily_notional`
- `symbol_whitelist`
- `created_at`
- `updated_at`

`status`: `CREATED`, `ENABLED`, `DISABLED`.

## `live_orders`

Auditable live order lifecycle.

Основные поля:

- `id`
- `user_id`
- `session_id`
- `strategy_id`
- `strategy_version_id`
- `exchange`
- `symbol`
- `side`
- `type`
- `quantity`
- `requested_price`
- `executed_price`
- `status`
- `exchange_order_id`
- `submitted_at`
- `updated_at`
- `filled_at`
- `rejected_reason`
- `source_run_id`

`status`: `CREATED`, `SUBMITTED`, `ACCEPTED`, `PARTIALLY_FILLED`, `FILLED`, `CANCELED`, `REJECTED`, `FAILED`.

## `live_positions`

Local live position snapshots synchronized from exchange adapters.

Основные поля:

- `id`
- `user_id`
- `exchange`
- `symbol`
- `quantity`
- `average_entry_price`
- `realized_pnl`
- `unrealized_pnl`
- `updated_at`
- `sync_status`

## `risk_events`

Audit trail for live risk decisions and safety state changes.

## `circuit_breaker_state`

Per-user/per-exchange automatic safety stop state.

## `kill_switch_state`

Per-user emergency stop state. When active, new live orders are blocked before adapter submission.
