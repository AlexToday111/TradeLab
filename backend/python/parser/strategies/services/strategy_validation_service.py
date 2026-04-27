import ast
import importlib.util
import json
import logging
import sys
from pathlib import Path
from types import ModuleType
from typing import Any
from uuid import uuid4

from parser.runs.services.strategy_execution_service import ENGINE_VERSION
from parser.strategies.dto.strategy_validation_dto import StrategyValidationResponse

logger = logging.getLogger(__name__)


class StrategyValidationService:
    def validate(self, file_path: str) -> StrategyValidationResponse:
        resolved_path = Path(file_path).expanduser()

        if not resolved_path.exists():
            return self._invalid(f"Strategy file not found: {resolved_path}")

        if not resolved_path.is_file():
            return self._invalid(f"Strategy path is not a file: {resolved_path}")

        if resolved_path.suffix != ".py":
            return self._invalid("Strategy file must use .py extension")

        try:
            source = resolved_path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            return self._invalid("Strategy file must be UTF-8 encoded Python source")

        syntax_report = self._validate_syntax_and_contract(source)
        if syntax_report["errors"]:
            return self._invalid(
                str(syntax_report["errors"][0]),
                validation_report=syntax_report,
            )

        module_name = f"strategy_validation_{uuid4().hex}"

        try:
            module = self._load_module(module_name, resolved_path)
            strategy_class = getattr(module, "Strategy", None)
            if strategy_class is None or not isinstance(strategy_class, type):
                return self._invalid("Strategy class not found", validation_report=syntax_report)

            strategy_name = getattr(strategy_class, "name", None)
            if not isinstance(strategy_name, str) or not strategy_name.strip():
                return self._invalid(
                    "Strategy.name attribute is missing or invalid",
                    validation_report=syntax_report,
                )

            parameters_method = getattr(strategy_class, "parameters", None)
            if not callable(parameters_method):
                return self._invalid("Strategy.parameters method not found", validation_report=syntax_report)

            run_method = getattr(strategy_class, "run", None)
            if not callable(run_method):
                return self._invalid("Strategy.run method not found", validation_report=syntax_report)

            try:
                parameters_schema = parameters_method()
            except Exception as exc:  # noqa: BLE001
                logger.warning(
                    "Strategy.parameters() execution failed for %s",
                    resolved_path,
                    exc_info=True,
                )
                return self._invalid(
                    f"Strategy.parameters() failed: {exc}",
                    validation_report=syntax_report,
                )

            if not isinstance(parameters_schema, dict):
                return self._invalid(
                    "Strategy.parameters() must return dict",
                    validation_report=syntax_report,
                )

            try:
                json.dumps(parameters_schema)
            except TypeError as exc:
                return self._invalid(
                    f"Strategy.parameters() must return JSON-serializable dict: {exc}",
                    validation_report=syntax_report,
                )

            metadata = self._metadata_from_class(strategy_class)
            warnings = list(syntax_report["warnings"])
            warnings.extend(self._metadata_warnings(metadata))
            validation_status = "WARNING" if warnings else "VALID"
            validation_report = {
                **syntax_report,
                "checks": {
                    **syntax_report["checks"],
                    "parameters_schema_json_serializable": True,
                    "execution_engine_version": ENGINE_VERSION,
                },
                "warnings": warnings,
            }

            logger.info(
                "Strategy validation succeeded for %s with name '%s'",
                resolved_path,
                strategy_name,
            )
            return StrategyValidationResponse(
                valid=True,
                name=strategy_name.strip(),
                parametersSchema=parameters_schema,
                error=None,
                validationStatus=validation_status,
                validationReport=validation_report,
                metadata=metadata,
                engineVersion=ENGINE_VERSION,
            )
        except Exception as exc:  # noqa: BLE001
            logger.exception("Strategy validation failed for %s", resolved_path)
            return self._invalid(str(exc), validation_report=syntax_report)
        finally:
            sys.modules.pop(module_name, None)

    @staticmethod
    def _load_module(module_name: str, file_path: Path) -> ModuleType:
        spec = importlib.util.spec_from_file_location(module_name, file_path)
        if spec is None or spec.loader is None:
            raise ValueError(f"Unable to load strategy module from {file_path}")

        module = importlib.util.module_from_spec(spec)
        sys.modules[module_name] = module
        spec.loader.exec_module(module)
        return module

    def _validate_syntax_and_contract(self, source: str) -> dict[str, Any]:
        report: dict[str, Any] = {
            "checks": {
                "syntax": False,
                "strategy_class": False,
                "strategy_name": False,
                "parameters_entrypoint": False,
                "run_entrypoint": False,
            },
            "warnings": [],
            "errors": [],
        }

        try:
            tree = ast.parse(source)
        except SyntaxError as exc:
            report["errors"].append(f"Python syntax error: {exc.msg} at line {exc.lineno}")
            return report

        report["checks"]["syntax"] = True
        strategy_class = self._find_strategy_class(tree)
        if strategy_class is None:
            report["errors"].append("Strategy class not found")
            return report

        report["checks"]["strategy_class"] = True
        report["checks"]["strategy_name"] = self._has_string_class_attr(strategy_class, "name")
        report["checks"]["parameters_entrypoint"] = self._has_method(strategy_class, "parameters")
        report["checks"]["run_entrypoint"] = self._has_method(strategy_class, "run")

        for check_name, message in (
            ("strategy_name", "Strategy.name attribute is missing or invalid"),
            ("parameters_entrypoint", "Strategy.parameters method not found"),
            ("run_entrypoint", "Strategy.run method not found"),
        ):
            if not report["checks"][check_name]:
                report["errors"].append(message)

        return report

    @staticmethod
    def _find_strategy_class(tree: ast.AST) -> ast.ClassDef | None:
        for node in ast.walk(tree):
            if isinstance(node, ast.ClassDef) and node.name == "Strategy":
                return node
        return None

    @staticmethod
    def _has_method(strategy_class: ast.ClassDef, method_name: str) -> bool:
        return any(isinstance(node, ast.FunctionDef) and node.name == method_name for node in strategy_class.body)

    @staticmethod
    def _has_string_class_attr(strategy_class: ast.ClassDef, attr_name: str) -> bool:
        for node in strategy_class.body:
            if not isinstance(node, ast.Assign):
                continue
            if not any(isinstance(target, ast.Name) and target.id == attr_name for target in node.targets):
                continue
            if isinstance(node.value, ast.Constant) and isinstance(node.value.value, str):
                return bool(node.value.value.strip())
        return False

    @staticmethod
    def _metadata_from_class(strategy_class: type) -> dict[str, Any]:
        return {
            "category": StrategyValidationService._coerce_optional_string(
                getattr(strategy_class, "category", None)
            ),
            "marketsSupported": StrategyValidationService._coerce_string_list(
                getattr(strategy_class, "markets_supported", getattr(strategy_class, "markets", []))
            ),
            "timeframes": StrategyValidationService._coerce_string_list(
                getattr(strategy_class, "timeframes", [])
            ),
            "symbolConstraints": StrategyValidationService._coerce_optional_string(
                getattr(strategy_class, "symbol_constraints", None)
            ),
            "expectedInputs": ["candles", "params"],
            "tags": StrategyValidationService._coerce_string_list(getattr(strategy_class, "tags", [])),
            "notes": StrategyValidationService._coerce_optional_string(getattr(strategy_class, "notes", None)),
        }

    @staticmethod
    def _metadata_warnings(metadata: dict[str, Any]) -> list[str]:
        warnings = []
        if not metadata.get("category"):
            warnings.append("Strategy.category is not set")
        if not metadata.get("marketsSupported"):
            warnings.append("Strategy.markets_supported is not set")
        if not metadata.get("timeframes"):
            warnings.append("Strategy.timeframes is not set")
        return warnings

    @staticmethod
    def _coerce_string_list(value: Any) -> list[str]:
        if not isinstance(value, (list, tuple, set)):
            return []
        return [str(item).strip() for item in value if str(item).strip()]

    @staticmethod
    def _coerce_optional_string(value: Any) -> str | None:
        if not isinstance(value, str) or not value.strip():
            return None
        return value.strip()

    @staticmethod
    def _invalid(
        error: str,
        *,
        validation_report: dict[str, Any] | None = None,
    ) -> StrategyValidationResponse:
        logger.warning("Strategy validation failed: %s", error)
        report = validation_report or {"checks": {}, "warnings": [], "errors": [error]}
        if error not in report.get("errors", []):
            report = {**report, "errors": [*report.get("errors", []), error]}
        return StrategyValidationResponse(
            valid=False,
            name=None,
            parametersSchema=None,
            error=error,
            validationStatus="INVALID",
            validationReport=report,
            metadata={},
            engineVersion=ENGINE_VERSION,
        )
