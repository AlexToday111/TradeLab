<h1 align="center">Testnet Drill для Live Trading</h1>

Целевой релиз: `v0.9.1-alpha.1`

<h2 align="center">Предусловия</h2>

- `LIVE_TRADING_REAL_ORDER_SUBMISSION_ENABLED=false`.
- Учетные данные Binance testnet сохранены через UI или API Live Trading.
- `LIVE_TRADING_CREDENTIAL_ENCRYPTION_KEY`, `SECURITY_JWT_SECRET` и `PYTHON_PARSER_INTERNAL_SECRET` не равны `change-me` вне локальной разработки.

<h2 align="center">Порядок drill</h2>

1. Запустить стек командой `docker compose up --build -d`.
2. Открыть `/settings` и проверить health для Java API, Python parser и frontend.
3. Открыть `/live` и убедиться, что отправка real orders показана как отключенная.
4. Сохранить активные учетные данные Binance testnet.
5. Запустить Binance testnet certification.
6. Проверить, что ответ помечен как testnet-only и содержит статус account/open-orders snapshot.
7. Создать live session со строгим symbol whitelist и низкими notional caps.
8. Включить session и отправить небольшой guarded test order.
9. Убедиться, что production submission не включен, а причины rejection видны при блокировке risk gates.

<h2 align="center">Критерии завершения</h2>

- Testnet certification завершилась успешно или вернула понятную оператору причину отказа.
- Состояния kill switch и circuit breaker видны в UI/API.
- Production order submission не включался.

<h2 align="center">Заметки по безопасности</h2>

- Real order submission остается выключенным по умолчанию.
- Testnet certification не означает production readiness.
- Любое включение production submission требует отдельного approval вне этого patch-релиза.
