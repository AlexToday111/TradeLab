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
- `summary_json`
- `artifacts_json`
- `error_message`
- `created_at`
- `started_at`
- `finished_at`

Назначение:

- хранит идентичность запуска и его жизненный цикл
- хранит сериализованный запрос в `params_json`
- хранит metrics в `metrics_json` и summary в `summary_json`
- связывается с воспроизводимым dataset snapshot через `run_snapshots`
- хранит текст ошибки в `error_message`

## `run_snapshots`

Immutable snapshot для воспроизводимости запуска.

Основные поля:

- `run_id`
- `strategy_version`
- `dataset_version`
- `dataset_snapshot_id`
- `dataset_snapshot_json`
- `params_snapshot_json`
- `execution_config_snapshot_json`
- `market_assumptions_snapshot_json`
- `engine_version`

## `run_artifacts`

Метаданные и JSON payload артефактов запуска.

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

Типы артефактов:

- `EQUITY_CURVE`
- `TRADES`
- `METRICS_JSON`
- `SUMMARY_JSON`
- `REPORT_JSON`

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

## `datasets`

Canonical metadata датасета.

Основные поля:

- `id`
- `user_id`
- `source`
- `symbol`
- `interval`
- `rows_count`
- `start_at`
- `end_at`
- `version`
- `fingerprint`
- `quality_flags_json`
- `lineage_json`
- `payload`

## `dataset_snapshots`

Version/snapshot layer датасета.

Основные поля:

- `id`
- `dataset_id`
- `user_id`
- `dataset_version`
- `source_exchange`
- `symbol`
- `timeframe`
- `start_time`
- `end_time`
- `row_count`
- `checksum`
- `source_metadata_json`
- `coverage_metadata_json`
- `created_at`

## `dataset_quality_reports`

Сохраненный результат quality checks для dataset snapshot.

Основные поля:

- `id`
- `dataset_id`
- `snapshot_id`
- `user_id`
- `quality_status`
- `issues_json`
- `checked_at`
