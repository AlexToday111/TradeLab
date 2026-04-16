import json
import logging
from contextlib import contextmanager
from contextvars import ContextVar
from datetime import UTC, datetime

_correlation_id: ContextVar[str | None] = ContextVar("correlation_id", default=None)
_run_id: ContextVar[str | None] = ContextVar("run_id", default=None)


class JsonLogFormatter(logging.Formatter):
    def format(self, record: logging.LogRecord) -> str:
        payload: dict[str, object] = {
            "timestamp": datetime.now(tz=UTC).isoformat().replace("+00:00", "Z"),
            "level": record.levelname,
            "logger": record.name,
            "message": record.getMessage(),
            "correlationId": _correlation_id.get(),
            "runId": _run_id.get(),
        }
        if record.exc_info:
            payload["exception"] = self.formatException(record.exc_info)
        return json.dumps(payload, ensure_ascii=True)


def configure_logging() -> None:
    handler = logging.StreamHandler()
    handler.setFormatter(JsonLogFormatter())
    logging.basicConfig(level=logging.INFO, handlers=[handler], force=True)


@contextmanager
def bind_log_context(*, correlation_id: str | None = None, run_id: str | None = None):
    correlation_token = _correlation_id.set(correlation_id)
    run_token = _run_id.set(run_id)
    try:
        yield
    finally:
        _correlation_id.reset(correlation_token)
        _run_id.reset(run_token)
