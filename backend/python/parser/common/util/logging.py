import json
import logging
from contextlib import contextmanager
from contextvars import ContextVar
from datetime import UTC, datetime

_correlation_id: ContextVar[str | None] = ContextVar("correlation_id", default=None)
_run_id: ContextVar[str | None] = ContextVar("run_id", default=None)
_STANDARD_LOG_RECORD_KEYS = set(logging.makeLogRecord({}).__dict__.keys())


class JsonLogFormatter(logging.Formatter):
    def format(self, record: logging.LogRecord) -> str:
        payload: dict[str, object] = {
            "timestamp": datetime.now(tz=UTC).isoformat().replace("+00:00", "Z"),
            "level": record.levelname,
            "service": "python-parser",
            "logger": record.name,
            "message": record.getMessage(),
            "correlation_id": _correlation_id.get(),
            "run_id": _run_id.get(),
        }
        for key, value in record.__dict__.items():
            if key in _STANDARD_LOG_RECORD_KEYS or key.startswith("_"):
                continue
            payload[key] = value
        if record.exc_info:
            payload["error"] = self.formatException(record.exc_info)
        return json.dumps(payload, ensure_ascii=True, default=str)


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
