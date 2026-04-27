from pathlib import Path

from parser.strategies.services.strategy_validation_service import StrategyValidationService


def write_strategy(tmp_path: Path, name: str, body: str) -> str:
    file_path = tmp_path / name
    file_path.write_text(body, encoding="utf-8")
    return str(file_path)


def test_validate_accepts_strategy_with_required_contract(tmp_path):
    strategy_path = write_strategy(
        tmp_path,
        "valid_strategy.py",
        """
class Strategy:
    name = "EMA Cross"
    category = "MOMENTUM"
    markets_supported = ["binance"]
    timeframes = ["1h"]
    tags = ["ema"]

    @staticmethod
    def parameters():
        return {"type": "object", "properties": {"fast": {"type": "integer"}}}

    def run(self, candles, params):
        return {"metrics": {"total_return": 0.1}}
""".strip(),
    )

    response = StrategyValidationService().validate(strategy_path)

    assert response.valid is True
    assert response.name == "EMA Cross"
    assert response.parameters_schema == {
        "type": "object",
        "properties": {"fast": {"type": "integer"}},
    }
    assert response.validation_status == "VALID"
    assert response.validation_report["checks"]["syntax"] is True
    assert response.metadata["category"] == "MOMENTUM"
    assert response.error is None


def test_validate_rejects_missing_strategy_class(tmp_path):
    strategy_path = write_strategy(
        tmp_path,
        "invalid_strategy.py",
        "VALUE = 1\n",
    )

    response = StrategyValidationService().validate(strategy_path)

    assert response.valid is False
    assert response.error == "Strategy class not found"
    assert response.validation_status == "INVALID"
    assert "Strategy class not found" in response.validation_report["errors"]


def test_validate_returns_warning_when_optional_metadata_is_missing(tmp_path):
    strategy_path = write_strategy(
        tmp_path,
        "warning_strategy.py",
        """
class Strategy:
    name = "Minimal"

    @staticmethod
    def parameters():
        return {}

    def run(self, candles, params):
        return {"metrics": {"total_return": 0.1}}
""".strip(),
    )

    response = StrategyValidationService().validate(strategy_path)

    assert response.valid is True
    assert response.validation_status == "WARNING"
    assert "Strategy.category is not set" in response.validation_report["warnings"]


def test_validate_rejects_python_syntax_error(tmp_path):
    strategy_path = write_strategy(
        tmp_path,
        "broken_strategy.py",
        "class Strategy(:\n",
    )

    response = StrategyValidationService().validate(strategy_path)

    assert response.valid is False
    assert response.validation_status == "INVALID"
    assert response.error.startswith("Python syntax error:")
