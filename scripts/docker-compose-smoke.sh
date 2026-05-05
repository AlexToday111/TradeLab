#!/usr/bin/env bash
set -euo pipefail

cleanup() {
  if [ "${COMPOSE_SMOKE_KEEP_RUNNING:-false}" = "true" ]; then
    return
  fi
  docker compose down --remove-orphans
}
trap cleanup EXIT

docker compose config -q
docker compose up --build -d

wait_for() {
  local name="$1"
  local url="$2"
  local attempts="${3:-60}"
  local delay="${4:-2}"

  for attempt in $(seq 1 "${attempts}"); do
    if curl -fsS --max-time 5 "${url}" >/dev/null; then
      printf '%s healthy at %s\n' "${name}" "${url}"
      return 0
    fi
    printf 'Waiting for %s at %s (%s/%s)\n' "${name}" "${url}" "${attempt}" "${attempts}"
    sleep "${delay}"
  done

  printf '%s did not become healthy at %s\n' "${name}" "${url}" >&2
  docker compose ps
  docker compose logs --tail=120
  return 1
}

wait_for "python-parser" "http://127.0.0.1:${PYTHON_PARSER_HOST_PORT:-18000}/health"
wait_for "java-api" "http://127.0.0.1:${JAVA_API_HOST_PORT:-18080}/api/health"
wait_for "frontend" "http://127.0.0.1:${FRONTEND_HOST_PORT:-3000}"
