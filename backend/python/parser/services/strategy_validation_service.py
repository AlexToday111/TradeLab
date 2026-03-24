import importlib.util
import logging
import sys
from pathlib import Path
from types import ModuleType
from uuid import uuid4

from parser.models.dto import StrategyValidationResponse


logger = logging.getLogger(__name__)


class StrategyValidationService:
    def validate(self, file_path: str) -> StrategyValidationResponse:
        resolved_path = Path(file_path).expanduser()

        if not resolved_path.exists():
            message = f"Strategy file not found: {resolved_path}"
            logger.warning(message)
            return StrategyValidationResponse(valid=False, name=None, parametersSchema=None, error=message)

        if not resolved_path.is_file():
            message = f"Strategy path is not a file: {resolved_path}"
            logger.warning(message)
            return StrategyValidationResponse(valid=False, name=None, parametersSchema=None, error=message)

        module_name = f"strategy_validation_{uuid4().hex}"

        try:
            module = self._load_module(module_name, resolved_path)
            strategy_class = getattr(module, "Strategy", None)
            if strategy_class is None or not isinstance(strategy_class, type):
                return self._invalid("Strategy class not found")

            strategy_name = getattr(strategy_class, "name", None)
            if not isinstance(strategy_name, str) or not strategy_name.strip():
                return self._invalid("Strategy.name attribute is missing or invalid")

            parameters_method = getattr(strategy_class, "parameters", None)
            if not callable(parameters_method):
                return self._invalid("Strategy.parameters method not found")

            run_method = getattr(strategy_class, "run", None)
            if not callable(run_method):
                return self._invalid("Strategy.run method not found")

            try:
                parameters_schema = parameters_method()
            except Exception as exc:  # noqa: BLE001
                logger.warning("Strategy.parameters() execution failed for %s", resolved_path, exc_info=True)
                return self._invalid(f"Strategy.parameters() failed: {exc}")

            if not isinstance(parameters_schema, dict):
                return self._invalid("Strategy.parameters() must return dict")

            logger.info("Strategy validation succeeded for %s with name '%s'", resolved_path, strategy_name)
            return StrategyValidationResponse(
                valid=True,
                name=strategy_name,
                parametersSchema=parameters_schema,
                error=None,
            )
        except Exception as exc:  # noqa: BLE001
            logger.exception("Strategy validation failed for %s", resolved_path)
            return self._invalid(str(exc))
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

    @staticmethod
    def _invalid(error: str) -> StrategyValidationResponse:
        logger.warning("Strategy validation failed: %s", error)
        return StrategyValidationResponse(valid=False, name=None, parametersSchema=None, error=error)
