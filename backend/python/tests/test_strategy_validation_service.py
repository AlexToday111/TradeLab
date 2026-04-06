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
