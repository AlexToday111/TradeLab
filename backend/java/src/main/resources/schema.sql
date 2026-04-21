CREATE TABLE IF NOT EXISTS datasets (
                                        id VARCHAR(64) PRIMARY KEY,
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


CREATE TABLE IF NOT EXISTS strategy_files (
                                              id BIGSERIAL PRIMARY KEY,
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
    engine_version VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    started_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ
    );

CREATE INDEX IF NOT EXISTS idx_runs_status
    ON runs (status);

CREATE INDEX IF NOT EXISTS idx_runs_created_at
    ON runs (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_runs_strategy_id
    ON runs (strategy_id);

ALTER TABLE runs
    ADD COLUMN IF NOT EXISTS run_name VARCHAR(255);

ALTER TABLE runs
    ADD COLUMN IF NOT EXISTS summary_json TEXT;

ALTER TABLE runs
    ADD COLUMN IF NOT EXISTS engine_version VARCHAR(128);


CREATE TABLE IF NOT EXISTS run_snapshots (
                                             run_id BIGINT PRIMARY KEY REFERENCES runs(id) ON DELETE CASCADE,
    strategy_version VARCHAR(128) NOT NULL,
    dataset_version VARCHAR(128) NOT NULL,
    params_snapshot_json TEXT NOT NULL,
    execution_config_snapshot_json TEXT NOT NULL,
    market_assumptions_snapshot_json TEXT NOT NULL,
    engine_version VARCHAR(128),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
    );


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
