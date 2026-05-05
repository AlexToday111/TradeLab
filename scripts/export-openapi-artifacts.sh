#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:-0.9.1-alpha.1}"
ARTIFACT_DIR="${ARTIFACT_DIR:-artifacts}"
JAVA_OPENAPI_URL="${JAVA_OPENAPI_URL:-http://127.0.0.1:18080/v3/api-docs}"
PYTHON_OPENAPI_URL="${PYTHON_OPENAPI_URL:-http://127.0.0.1:18000/openapi.json}"
ATTEMPTS="${OPENAPI_EXPORT_ATTEMPTS:-60}"
DELAY_SECONDS="${OPENAPI_EXPORT_DELAY_SECONDS:-2}"

mkdir -p "${ARTIFACT_DIR}"

download_openapi() {
  local name="$1"
  local url="$2"
  local output="$3"
  local tmp="${output}.tmp"

  for attempt in $(seq 1 "${ATTEMPTS}"); do
    if curl -fsSL --max-time 10 "${url}" -o "${tmp}"; then
      mv "${tmp}" "${output}"
      printf 'Exported %s OpenAPI artifact to %s\n' "${name}" "${output}"
      return 0
    fi
    printf 'Waiting for %s OpenAPI at %s (%s/%s)\n' "${name}" "${url}" "${attempt}" "${ATTEMPTS}"
    sleep "${DELAY_SECONDS}"
  done

  printf 'Failed to export %s OpenAPI from %s\n' "${name}" "${url}" >&2
  return 1
}

download_openapi "java" "${JAVA_OPENAPI_URL}" "${ARTIFACT_DIR}/openapi-java-v${VERSION}.json"
download_openapi "python" "${PYTHON_OPENAPI_URL}" "${ARTIFACT_DIR}/openapi-python-v${VERSION}.json"

printf 'Exported OpenAPI artifacts to %s\n' "${ARTIFACT_DIR}"
