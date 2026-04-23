CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_users_email
    ON users (email);


CREATE TABLE IF NOT EXISTS datasets (
                                        id VARCHAR(64) PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    source VARCHAR(64),
    symbol VARCHAR(64),
    "interval" VARCHAR(32),
    imported_at TIMESTAMPTZ,
    rows_count INTEGER,
    start_at TIMESTAMPTZ,
    end_at TIMESTAMPTZ,
    version VARCHAR(128),
    fingerprint VARCHAR(128),
    quality_flags_json TEXT,
    lineage_json TEXT,
    payload TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_datasets_created_at
    ON datasets (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_datasets_user_created_at
    ON datasets (user_id, created_at DESC);


CREATE TABLE IF NOT EXISTS strategy_files (
                                              id BIGSERIAL PRIMARY KEY,
                                              user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                                              name VARCHAR(255),
    filename VARCHAR(255) NOT NULL,
    storage_path TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    validation_error TEXT,
    parameters_schema_json TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_strategy_files_created_at
    ON strategy_files (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_strategy_files_user_created_at
    ON strategy_files (user_id, created_at DESC);


CREATE TABLE IF NOT EXISTS candles (
                                        id BIGSERIAL PRIMARY KEY,
                                        exchange VARCHAR(32) NOT NULL,
    symbol VARCHAR(32) NOT NULL,
    "interval" VARCHAR(16) NOT NULL,
    open_time TIMESTAMPTZ NOT NULL,
    close_time TIMESTAMPTZ NOT NULL,
    open NUMERIC(20, 8) NOT NULL,
    high NUMERIC(20, 8) NOT NULL,
    low NUMERIC(20, 8) NOT NULL,
    close NUMERIC(20, 8) NOT NULL,
    volume NUMERIC(28, 8) NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_candles_market_range
    ON candles (exchange, symbol, "interval", open_time);


CREATE TABLE IF NOT EXISTS runs (
                                    id BIGSERIAL PRIMARY KEY,
                                    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
                                    strategy_id BIGINT NOT NULL REFERENCES strategy_files(id) ON DELETE RESTRICT,
    strategy_name VARCHAR(255) NOT NULL,
    dataset_id VARCHAR(64),
    run_name VARCHAR(255),
    correlation_id VARCHAR(128) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    exchange VARCHAR(64) NOT NULL,
    symbol VARCHAR(64) NOT NULL,
    "interval" VARCHAR(32) NOT NULL,
    date_from TIMESTAMPTZ NOT NULL,
    date_to TIMESTAMPTZ NOT NULL,
    params_json TEXT,
    summary_json TEXT,
    metrics_json TEXT,
    artifacts_json TEXT,
    error_message TEXT,
    error_details_json TEXT,
    engine_version VARCHAR(128),
    execution_duration_ms BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ
    );

CREATE INDEX IF NOT EXISTS idx_runs_status
    ON runs (status);

CREATE INDEX IF NOT EXISTS idx_runs_created_at
    ON runs (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_runs_user_created_at
    ON runs (user_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_runs_strategy_id
    ON runs (strategy_id);

CREATE TABLE IF NOT EXISTS run_artifacts (
                                             id BIGSERIAL PRIMARY KEY,
                                             run_id BIGINT NOT NULL REFERENCES runs(id) ON DELETE CASCADE,
    artifact_type VARCHAR(64) NOT NULL,
    artifact_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(128) NOT NULL,
    storage_path TEXT,
    payload_json TEXT,
    size_bytes BIGINT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_run_artifacts_run_id_created_at
    ON run_artifacts (run_id, created_at);

CREATE INDEX IF NOT EXISTS idx_run_artifacts_run_id_type
    ON run_artifacts (run_id, artifact_type);

ALTER TABLE runs
    ADD COLUMN IF NOT EXISTS run_name VARCHAR(255);

ALTER TABLE runs
    ADD COLUMN IF NOT EXISTS summary_json TEXT;

ALTER TABLE runs
    ADD COLUMN IF NOT EXISTS engine_version VARCHAR(128);

ALTER TABLE runs
    ADD COLUMN IF NOT EXISTS error_details_json TEXT;

ALTER TABLE runs
    ADD COLUMN IF NOT EXISTS execution_duration_ms BIGINT;

ALTER TABLE datasets
    ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE strategy_files
    ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE runs
    ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users(id) ON DELETE CASCADE;


CREATE TABLE IF NOT EXISTS run_snapshots (
                                             run_id BIGINT PRIMARY KEY REFERENCES runs(id) ON DELETE CASCADE,
    strategy_version VARCHAR(128) NOT NULL,
    dataset_version VARCHAR(128) NOT NULL,
    dataset_snapshot_id BIGINT,
    params_snapshot_json TEXT NOT NULL,
    execution_config_snapshot_json TEXT NOT NULL,
    market_assumptions_snapshot_json TEXT NOT NULL,
    engine_version VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

ALTER TABLE run_snapshots
    ADD COLUMN IF NOT EXISTS dataset_snapshot_id BIGINT;

CREATE TABLE IF NOT EXISTS dataset_snapshots (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 dataset_id VARCHAR(64) NOT NULL REFERENCES datasets(id) ON DELETE CASCADE,
    dataset_version VARCHAR(128) NOT NULL,
    source_exchange VARCHAR(64),
    symbol VARCHAR(64),
    "interval" VARCHAR(32),
    start_time TIMESTAMPTZ,
    end_time TIMESTAMPTZ,
    row_count INTEGER,
    checksum VARCHAR(128),
    source_metadata_json TEXT,
    coverage_metadata_json TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

CREATE UNIQUE INDEX IF NOT EXISTS idx_dataset_snapshots_dataset_version
    ON dataset_snapshots (dataset_id, dataset_version);

CREATE INDEX IF NOT EXISTS idx_dataset_snapshots_dataset_created_at
    ON dataset_snapshots (dataset_id, created_at DESC);

CREATE TABLE IF NOT EXISTS dataset_quality_reports (
                                                       id BIGSERIAL PRIMARY KEY,
                                                       dataset_id VARCHAR(64) NOT NULL REFERENCES datasets(id) ON DELETE CASCADE,
    dataset_snapshot_id BIGINT REFERENCES dataset_snapshots(id) ON DELETE CASCADE,
    quality_status VARCHAR(32) NOT NULL,
    issues_json TEXT NOT NULL,
    checked_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );

CREATE INDEX IF NOT EXISTS idx_dataset_quality_reports_dataset_checked_at
    ON dataset_quality_reports (dataset_id, checked_at DESC);

CREATE INDEX IF NOT EXISTS idx_dataset_quality_reports_snapshot_checked_at
    ON dataset_quality_reports (dataset_snapshot_id, checked_at DESC);


CREATE TABLE IF NOT EXISTS backtest_trades (
                                               id BIGSERIAL PRIMARY KEY,
                                               run_id BIGINT NOT NULL REFERENCES runs(id) ON DELETE CASCADE,
    entry_time TIMESTAMPTZ,
    exit_time TIMESTAMPTZ,
    entry_price DOUBLE PRECISION NOT NULL,
    exit_price DOUBLE PRECISION NOT NULL,
    quantity DOUBLE PRECISION NOT NULL,
    pnl DOUBLE PRECISION NOT NULL,
    fee DOUBLE PRECISION NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_backtest_trades_run_id
    ON backtest_trades (run_id);

CREATE INDEX IF NOT EXISTS idx_backtest_trades_run_id_entry_time
    ON backtest_trades (run_id, entry_time);


CREATE TABLE IF NOT EXISTS backtest_equity_points (
                                                      id BIGSERIAL PRIMARY KEY,
                                                      run_id BIGINT NOT NULL REFERENCES runs(id) ON DELETE CASCADE,
    timestamp TIMESTAMPTZ NOT NULL,
    equity DOUBLE PRECISION NOT NULL
    );

CREATE INDEX IF NOT EXISTS idx_backtest_equity_points_run_id_timestamp
    ON backtest_equity_points (run_id, timestamp);
