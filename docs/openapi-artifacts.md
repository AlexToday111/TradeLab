# OpenAPI Release Artifacts

Release `v0.9.0-alpha.1` exports API contracts as build artifacts after the Java API and Python parser are running.

```bash
docker compose up --build -d
scripts/export-openapi-artifacts.sh 0.9.0-alpha.1
```

Artifact path convention:

- `artifacts/openapi-java-v0.9.0-alpha.1.json`
- `artifacts/openapi-python-v0.9.0-alpha.1.json`

The script reads `JAVA_OPENAPI_URL` and `PYTHON_OPENAPI_URL` when non-default hosts are needed.
