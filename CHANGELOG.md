# Changelog

All notable changes to this project will be documented in this file.

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
* End-to-end execution flow: Java API → Python engine → result persistence.
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

  * Java → control plane (API, orchestration)
  * Python → execution plane (compute engine)
* Python service extended from data parser into execution-capable engine.

### Notes

* First version where strategy results can be reproduced deterministically.
* Execution engine may still use simplified/stub strategies depending on implementation stage.

---

## [0.2.1-alpha.1] - 2026-04-22

### Observability Foundation

This release improves system visibility and execution transparency.

### Added

* Structured logging across backend services.
* Run execution logs and error tracking.
* Correlation identifiers for tracing execution (run_id / job_id).
* Extended run status tracking:

  * progress
  * failure reasons
* Basic execution timing metrics.

### Changed

* Improved error handling in Java ↔ Python integration.
* Enhanced run lifecycle visibility for debugging and monitoring.

### Notes

* Focus is on internal reliability and debugging capabilities.
* No major product-facing changes.

---

## [0.3.0-alpha.1] - 2026-04-23

### Multi-User & Security Base

This release introduces user awareness and access control foundations.

### Added

* User model and authentication (JWT/session-based).
* Ownership model for:

  * strategies
  * datasets
  * runs
* Initial permission handling (resource-level ownership).
* Foundation for secure handling of sensitive data (API keys, secrets).

### Changed

* Backend transitions from single-user tool to multi-user platform.
* API updated to respect ownership boundaries.

### Notes

* First step toward SaaS-ready architecture.
* Role-based access control (RBAC) will be extended in future releases.