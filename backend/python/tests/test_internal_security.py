from fastapi.testclient import TestClient

from parser.main import app


def test_internal_run_endpoint_requires_shared_secret() -> None:
    client = TestClient(app)

    response = client.post(
        "/internal/runs/execute",
        json={
            "strategyFilePath": "strategy.py",
            "exchange": "binance",
            "symbol": "BTCUSDT",
            "interval": "1h",
            "from": "2024-01-01T00:00:00Z",
            "to": "2024-01-01T01:00:00Z",
            "params": {},
            "runId": "1",
            "correlationId": "run-1",
        },
    )

    assert response.status_code == 401
    assert response.json()["message"] == "Unauthorized internal request"
