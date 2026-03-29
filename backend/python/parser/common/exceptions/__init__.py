class AppError(Exception):
    def __init__(self, message: str, status_code: int = 400) -> None:
        super().__init__(message)
        self.message = message
        self.status_code = status_code


class ValidationError(AppError):
    def __init__(self, message: str) -> None:
        super().__init__(message=message, status_code=400)


class ExchangeError(AppError):
    def __init__(self, message: str) -> None:
        super().__init__(message=message, status_code=502)


class RepositoryError(AppError):
    def __init__(self, message: str) -> None:
        super().__init__(message=message, status_code=500)
