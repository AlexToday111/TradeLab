from __future__ import annotations

import importlib.util
import sys
from pathlib import Path
from types import ModuleType
from uuid import uuid4

from backtesting.strategy.base import Strategy as BaseStrategy


class StrategyLoadError(ValueError):
    """Raised when a strategy file cannot be loaded or validated."""


class StrategyLoader:
    def load(self, file_path: str | Path, *, params: dict | None = None) -> BaseStrategy:
        resolved_path = Path(file_path).expanduser().resolve(strict=True)
        if resolved_path.suffix != ".py":
            raise StrategyLoadError("Strategy file must be a Python module")

        module_name = f"backtesting_strategy_{uuid4().hex}"
        try:
            module = self._load_module(module_name, resolved_path)
            strategy_class = getattr(module, "Strategy", None)
            if strategy_class is None or not isinstance(strategy_class, type):
                raise StrategyLoadError("Strategy class not found")
            if not issubclass(strategy_class, BaseStrategy):
                raise StrategyLoadError(
                    "Strategy class must inherit from backtesting.strategy.base.Strategy"
                )

            self._validate_strategy_class(strategy_class)
            strategy = strategy_class(params or {})
            return strategy
        except FileNotFoundError as exc:
            raise StrategyLoadError(f"Strategy file not found: {resolved_path}") from exc
        except StrategyLoadError:
            raise
        except Exception as exc:  # noqa: BLE001
            raise StrategyLoadError(f"Failed to load strategy: {exc}") from exc
        finally:
            sys.modules.pop(module_name, None)

    @staticmethod
    def _load_module(module_name: str, file_path: Path) -> ModuleType:
        spec = importlib.util.spec_from_file_location(module_name, file_path)
        if spec is None or spec.loader is None:
            raise StrategyLoadError(f"Unable to load strategy module from {file_path}")

        module = importlib.util.module_from_spec(spec)
        sys.modules[module_name] = module
        spec.loader.exec_module(module)
        return module

    @staticmethod
    def _validate_strategy_class(strategy_class: type[BaseStrategy]) -> None:
        required_methods = ("initialize", "on_bar", "finalize")
        for method_name in required_methods:
            method = getattr(strategy_class, method_name, None)
            if not callable(method):
                raise StrategyLoadError(f"Strategy.{method_name} method not found")
