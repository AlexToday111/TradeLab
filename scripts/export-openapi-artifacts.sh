#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:-0.9.0-alpha.1}"
ARTIFACT_DIR="${ARTIFACT_DIR:-artifacts}"
JAVA_OPENAPI_URL="${JAVA_OPENAPI_URL:-http://127.0.0.1:18080/v3/api-docs}"
PYTHON_OPENAPI_URL="${PYTHON_OPENAPI_URL:-http://127.0.0.1:18000/openapi.json}"

mkdir -p "${ARTIFACT_DIR}"

curl -fsSL "${JAVA_OPENAPI_URL}" -o "${ARTIFACT_DIR}/openapi-java-v${VERSION}.json"
curl -fsSL "${PYTHON_OPENAPI_URL}" -o "${ARTIFACT_DIR}/openapi-python-v${VERSION}.json"

printf 'Exported OpenAPI artifacts to %s\n' "${ARTIFACT_DIR}"
