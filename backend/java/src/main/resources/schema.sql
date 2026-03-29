CREATE TABLE IF NOT EXISTS datasets (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_datasets_created_at ON datasets (created_at DESC);

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

CREATE INDEX IF NOT EXISTS idx_strategy_files_created_at ON strategy_files (created_at DESC);

CREATE TABLE if NOT EXISTS runs (
    id bigserial primary key,
    strategy_id bigint not null references strategy_files(id) on delete restrict,
    status varchar(32) not null,
    exchange varchar(64) not null,
    symbol varchar(64) not null,
    interval varchar(32) not null,
    date_from timestamptz not null,
    date_to timestamptz not null,
    params_json text,
    metrics_json text,
    error_message text,
    created_at timestamptz not null default now(),
    finished_at timestamptz
);