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
