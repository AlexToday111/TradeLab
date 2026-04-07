from __future__ import annotations

from typing import Any


class Strategy:
    def __init__(self, params: dict[str, Any]) -> None:
        self.params = dict(params)

    def initialize(self, context) -> None:
        return None

    def on_bar(self, bar, context) -> None:
        raise NotImplementedError

    def finalize(self, context) -> None:
        return None

