# Contributing to Trade360Lab

## 1. Introduction

Trade360Lab is a monorepo for a trading platform focused on research, market data preparation, strategy execution, and result analysis.

This `CONTRIBUTING.md` explains how to work with the repository, how to make safe changes, and how to prepare clean pull requests.

It is intended for both internal developers and external contributors who want to improve the platform without breaking existing flows.

## 2. Project Structure

Trade360Lab is organized as a monorepo with separate runtime responsibilities.

- `frontend/`
  - Next.js application.
  - Contains UI, feature modules, API integration, and user-facing workflows.
- `backend/java/`
  - Spring Boot service.
  - Acts as the control plane: public API, orchestration, run management, persistence of metadata and results.
- `backend/python/`
  - FastAPI service plus execution modules.
  - Acts as the execution and data plane: market data import, strategy validation, strategy execution, and backtesting logic.
- `backend/java/src/main/resources/schema.sql`
  - Java-side database schema initialization.
- `backend/python/parser/schema.sql`
  - Python-side schema initialization for parser-owned tables.
- `docker-compose.yml`
  - Starts the full local stack: PostgreSQL, Python service, Java API, and frontend.
- `docs/`
  - Project and release documentation.
- `CHANGELOG.md`
  - Release history and notable user-visible or architecture-relevant changes.

At the moment, the repository does not use a dedicated migration framework such as Flyway or Alembic. Schema changes are handled through the existing SQL schema files.

## 3. Getting Started

### Prerequisites

Install the following tools before working on the project:

- Node.js 20+ and npm
- Java 17+
- Maven 3.9+
- Python 3.11+
- Docker and Docker Compose

### Install Dependencies

Frontend:

```bash
cd frontend
npm install
```

Python backend:

```bash
cd backend/python
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

Java backend:

```bash
cd backend/java
mvn test -q
```

### Run the Full Stack

Preferred local setup:

```bash
docker compose up --build
```

Default endpoints:

- Frontend: `http://localhost:3000`
- Java API: `http://localhost:18080`
- Python service: `http://localhost:18000`
- PostgreSQL: `localhost:55432`

### Run Services Individually

Frontend:

```bash
cd frontend
npm run dev
```

Python service:

```bash
cd backend/python
source .venv/bin/activate
uvicorn parser.main:app --host 0.0.0.0 --port 8000
```

Java service:

```bash
cd backend/java
mvn spring-boot:run
```

Running services individually is useful when you are working on one part of the system and want faster iteration than rebuilding Docker images.

## 4. Development Workflow

Use short-lived branches for focused changes.

Recommended branch prefixes:

- `feature/`
- `fix/`
- `chore/`
- `docs/`
- `refactor/`
- `test/`

Examples:

- `feature/run-snapshot-layer`
- `fix/python-execution-timeout`
- `chore/update-local-docs`

Recommended workflow:

1. Create a branch from the current main integration branch.
2. Read the existing implementation before adding new code.
3. Extend existing flows where possible instead of creating parallel ones.
4. Keep the change scoped to one problem.
5. Run tests before opening a PR.

If your change touches several layers, keep it vertical and coherent. For example, a new run field should usually include schema, entity, DTO, service, and tests in the same task.

## 5. Commit Convention

Trade360Lab uses Conventional Commits.

Format:

```text
type(scope): message
```

Examples:

- `feat(backend/java): add run execution flow`
- `feat(backend/python): add execution endpoint`
- `fix(backend/java): handle python timeout`
- `chore(backend/db): add run snapshot table`
- `docs(root): update changelog`

Supported types:

- `feat`
- `fix`
- `refactor`
- `chore`
- `docs`
- `test`

Common scopes:

- `backend/java`
- `backend/python`
- `backend/db`
- `frontend`
- `root`

Guidelines:

- Use the smallest correct scope.
- Write messages that describe the actual change.
- Avoid vague commit messages such as `update code` or `fix bug`.
- Split unrelated changes into separate commits.

## 6. Pull Request Guidelines

PRs must be atomic and reviewable.

Rules:

- One PR should solve one problem or one closely related vertical flow.
- Do not mix unrelated backend, frontend, and schema work unless they belong to the same feature.
- Describe the change clearly.

Every PR should include:

- What was changed
- Why it was changed
- How to verify it
- Linked issue, if one exists

Checklist before merge:

- Tests pass
- Lint or static checks pass where applicable
- New logic has tests
- `CHANGELOG.md` is updated when the change affects release notes, behavior, or architecture

## 7. Coding Standards

### Java

- Follow the existing Spring Boot structure: controller, service, repository, entity, dto.
- Use DTOs at API boundaries.
- Keep controllers thin.
- Put business logic and orchestration in services.
- Do not mix persistence logic into controllers.
- Do not collapse layers for convenience.

### Python

- Follow FastAPI-style structure already present in `backend/python`.
- Type hints are required for new code.
- Keep endpoint logic thin.
- Put business logic in services.
- Separate API, DTO, repository, and execution logic clearly.

### Frontend

- Use TypeScript for new code.
- Linting and type checking must stay clean.
- Follow the existing Next.js application structure.
- Keep UI changes aligned with backend API contracts.

## 8. Testing

Run the relevant test suite before opening a PR.

Java:

```bash
cd backend/java
mvn test
```

Python:

```bash
cd backend/python
source .venv/bin/activate
pytest
```

Frontend:

```bash
cd frontend
npm test
```

Useful frontend validation:

```bash
cd frontend
npm run lint
npm run typecheck
```

Minimum expectation:

- Every new behavior should have at least one test.
- Bug fixes should include a regression test when practical.
- If a change affects a full flow, prefer integration-style coverage over mocks only.

## 9. Database Changes

Database changes must be conservative and backward-compatible.

Current state:

- There is no dedicated migration framework yet.
- Schema is initialized through:
  - `backend/java/src/main/resources/schema.sql`
  - `backend/python/parser/schema.sql`

Rules:

- Add new tables and columns in a backward-compatible way.
- Prefer additive changes over destructive changes.
- Do not rename or remove fields casually.
- Update schema, application code, and tests together.
- Do not break Docker startup or test schema initialization.

If both Java and Python depend on the same table, validate both paths before finalizing the change.

## 10. Release Process

The project uses alpha-stage versioning such as:

- `v0.2.0-alpha.1`
- `v0.3.0-alpha.1`

Rules:

- `CHANGELOG.md` is required for meaningful release changes.
- Release tags may be created manually or through CI, depending on the current workflow.
- Releases may also have semantic milestone names, for example:
  - `Reproducible Execution Engine`
  - `Observability Foundation`
  - `Multi-User & Security Base`

If your change affects architecture, public API, reproducibility, or release behavior, it should be reflected in the changelog.

## 11. Architecture Principles

Keep these principles in mind when contributing:

- Java = control plane
- Python = execution plane
- Minimize coupling between services
- Do not introduce microservices prematurely
- Build a working flow first, optimize second

In practice:

- Public API and orchestration belong in Java
- Execution-heavy logic belongs in Python
- Shared behavior should be expressed through contracts, not duplicated logic

## 12. What NOT to Do

- Do not duplicate entities that already exist.
- Do not create parallel run, result, strategy, or dataset models without a strong reason.
- Do not break an existing API without explicit justification.
- Do not introduce Kafka, Celery, distributed workers, or similar heavy infrastructure without real need.
- Do not over-engineer for speculative scale.
- Do not write “ideal architecture” that does not produce a working scenario.
- Do not rewrite large modules when a minimal extension is enough.

## 13. Communication

Ask questions early when ownership, behavior, or expected architecture is unclear.

When discussing changes:

- Be concrete
- Be technical
- Be constructive
- Explain tradeoffs
- Prefer evidence over opinion

Good discussion topics:

- Why a contract should change
- Why a schema change is needed
- How to preserve backward compatibility
- How to test an end-to-end flow safely

If you disagree with an approach, suggest a better one and explain why it is better.

Thanks for contributing to Trade360Lab.
