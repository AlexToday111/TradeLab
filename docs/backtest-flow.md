<h1 align="center">Жизненный цикл бэктеста</h1>

<h2 align="center">Статусы</h2>

- `PENDING`
  Запуск создан, но выполнение еще не началось.

- `RUNNING`
  Java backend уже подготовил данные и выполняет Python backtest.

- `COMPLETED`
  Python завершился успешно, summary и артефакты сохранены в БД.

- `FAILED`
  Во время подготовки данных, выполнения Python или сохранения результата произошла ошибка.

<h2 align="center">Поток выполнения</h2>

1. Создание запуска:
   `BacktestService.createRun(...)` валидирует входные данные и создает запись в `runs`.

2. Старт выполнения:
   `BacktestService.executeRun(runId)` переводит запуск в `RUNNING`, очищает старые артефакты и фиксирует `started_at`.

3. Подготовка данных:
   сервис читает свечи по `exchange`, `symbol`, `interval`, `from`, `to` и пишет временный CSV.

4. Вызов Python:
   `PythonBacktestExecutor` сериализует запрос в JSON, передает его через stdin и читает stdout/stderr.

5. Сохранение результата:
   summary сохраняется в `runs.metrics_json`, сделки в `backtest_trades`, equity curve в `backtest_equity_points`.

6. Завершение:
   при успехе статус становится `COMPLETED`, при ошибке `FAILED`. Поле `finished_at` выставляется всегда.

<h2 align="center">Ошибки</h2>

- При некорректном request API возвращает `400`.
- Если запуск не найден, API возвращает `404`.
- Если Python завершился с ошибкой или вернул некорректный JSON, API возвращает `500`.
- Даже при `500` сервис пытается сохранить статус `FAILED` и текст ошибки в `runs.error_message`.
