# Changelog

All notable changes to this project will be documented in this file.

---

## [0.5.0-alpha.1] - 2026-04-24

### Scalable Execution Foundation

### Added

* Database-backed execution job model with:

  * `execution_jobs`
  * queued/running/succeeded/failed/canceled/retrying statuses
  * attempts and max attempts
  * job locking fields (`locked_by`, `locked_at`)
  * cancel request flag
  * job timing and error fields
* Queue-like `/api/runs` flow: run creation now creates a queued execution job instead of executing inline.
* In-process Java execution worker with configurable max parallel jobs and polling interval.
* Run/job APIs:

  * `GET /api/runs/{id}/execution`
  * `POST /api/runs/{id}/retry`
  * `POST /api/runs/{id}/cancel`
  * `GET /api/execution-jobs`
  * `GET /api/execution-jobs/{id}`
* Python execution contract now accepts and returns `jobId`.
* Structured logs now include `job_id` in Java and Python execution paths.
* Frontend status mapping for queued/running/failed/canceled execution states and minimal retry/cancel actions.

### Changed

* Java orchestration now separates the `Run` domain entity from `ExecutionJob` scheduling lifecycle.
* Run snapshots mark execution mode as `queued-http`.
* Result and artifact persistence remain on the existing successful-run path after worker execution.

### Notes

* The MVP uses a database-backed queue and in-process worker. No Kafka, RabbitMQ, Celery, or Kubernetes scheduler was introduced.
* Running Python workloads cannot be interrupted yet; canceling a running job records `cancel_requested` and Java suppresses result persistence when the Python call returns.

---

## [0.4.0-alpha.1] - 2026-04-23

### Artifact Storage & Data Platform Foundation

### Added

* Run artifact metadata and JSON payload storage:

  * summary export
  * metrics export
  * trades export
  * equity curve export
  * run report JSON
* Artifact APIs:

  * `GET /api/runs/{id}/artifacts`
  * `GET /api/runs/{id}/artifacts/{artifactId}`
  * `GET /api/runs/{id}/artifacts/{artifactId}/download`
* Dataset snapshot/version tables and API:

  * `dataset_snapshots`
  * `dataset_quality_reports`
  * `GET /api/datasets/{id}/versions`
  * `GET /api/datasets/{id}/quality`
  * `GET /api/dataset-snapshots/{snapshotId}`
* Python import quality report with gaps, duplicates, ordering, timeframe consistency, empty dataset, and too-small dataset checks.
* Frontend display for run artifacts and dataset quality/snapshot metadata.

### Changed

* Run snapshots now include `dataset_snapshot_id` when a matching dataset snapshot exists.
* Dataset import/upsert now creates snapshot and quality report records from existing dataset metadata.

### Notes

* Artifact storage uses DB-backed JSON payloads for MVP; the schema keeps `storage_path` for future file/object storage.
* No S3/MinIO/Kafka or feature store layer was introduced.

---

## [0.1.0-alpha.1] - 2026-04-13

### Platform Skeleton

Initial alpha baseline of the Trade360Lab monorepo.

### Added

* Frontend (Next.js) with quality gates:

  * lint
  * typecheck
  * tests
  * production build
* Backend services:

  * Java API (datasets, candles, imports)
  * Python service (market data parser)
* PostgreSQL integration via Docker.
* Docker Compose setup for full stack orchestration.
* Backend quality gates:

  * Python: ruff, pytest
  * Java: mvn test, checkstyle
* Release documentation:

  * changelog
  * alpha release checklist

### Changed

* Cleaned root npm scripts to match repository structure.
* Updated README with actual directories and startup instructions.

### Notes

* Provides a working foundation but does not include execution or strategy lifecycle.

---

## [0.2.0-alpha.1] - 2026-04-21

### Reproducible Execution Engine

This release introduces the core execution pipeline and reproducibility layer.

### Added

* Run domain for strategy execution lifecycle (create, track, complete runs).
* End-to-end execution flow: Java API -> Python engine -> result persistence.
* Internal Python execution endpoint for running strategies/backtests.
* Run result storage:

  * metrics (PnL, drawdown, win rate, etc.)
  * summary (timings, status)
  * optional artifacts (equity curve, trades)
* Immutable run snapshots for reproducibility.
* Initial version tracking:

  * strategy version
  * dataset version
  * execution parameters snapshot
  * market assumptions snapshot
  * engine version

### Changed

* Backend architecture evolved into:

  * Java -> control plane (API, orchestration)
  * Python -> execution plane (compute engine)
* Python service extended from data parser into execution-capable engine.

### Notes

* First version where strategy results can be reproduced deterministically.
* Execution engine may still use simplified/stub strategies depending on implementation stage.

---

## [0.2.1-alpha.1] - 2026-04-22

### Observability Foundation

This release improves system visibility and execution transparency.

### Added

* Structured JSON logging in Java and Python with shared trace fields:

  * `service`
  * `correlation_id`
  * `run_id`
  * `error`
* End-to-end run correlation across Java API and Python execution requests and responses.
* Run diagnostics persistence:

  * `error_details_json`
  * `execution_duration_ms`
* Extended run API payloads with timing metadata and structured error details.
* Structured Python execution failure contract:

  * `errorCode`
  * `errorMessage`
  * `stacktrace`
  * execution timing metadata

### Changed

* Improved Java <-> Python interaction logging with request/response timing and status.
* Run lifecycle transitions are now explicitly logged:

  * `CREATED -> QUEUED`
  * `QUEUED -> RUNNING`
  * `RUNNING -> SUCCEEDED | FAILED`
* Java now persists Python-side diagnostics instead of collapsing failures into a single message.
* Engine version advanced to `python-execution-engine/0.2.1-alpha.1`.

### Notes

* Focus remains internal reliability, debuggability, and operational transparency.
* No external metrics stack was introduced in this release.

---

## [0.3.0-alpha.1] - 2026-04-22

### Multi-User & Security Base

This release introduces the first security and multi-user platform baseline.

### Added

* User model and JWT authentication:

  * register endpoint
  * login endpoint
  * password hashing with BCrypt
* Ownership model for:

  * strategies
  * datasets
  * runs
* User-scoped access control across the Java API.
* Frontend authentication flow:

  * login page
  * register page
  * JWT persistence
  * automatic `Authorization` propagation
  * 401 -> login redirect
* Internal Python shared-secret protection for `/internal/*` endpoints.

### Changed

* Backend transitions from single-user tool to user-aware platform services.
* Datasets, strategies, and runs now resolve through `user_id` ownership boundaries.
* Execution flow now preserves user context from frontend -> Java API -> run creation.
* Engine version advanced to `python-execution-engine/0.3.0-alpha.1`.

### Notes

* This release intentionally stops at ownership-based authorization and does not add RBAC yet.
* Shared-secret protection keeps the Python execution API internal without introducing service mesh or OAuth infrastructure.
