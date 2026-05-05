<h1 align="center">Политика безопасности</h1>

<h2 align="center">Поддерживаемые версии</h2>

Trade360Lab находится в активной разработке.  
Исправления безопасности применяются к последней версии основной ветки.

---

<h2 align="center">Сообщение об уязвимости</h2>

Если вы нашли уязвимость безопасности, **не открывайте публичный GitHub issue**.

Сообщите о проблеме приватно через Telegram: **@ba6kir**.

По возможности приложите:

- краткое описание проблемы
- затронутые компоненты
- шаги воспроизведения
- ожидаемое и фактическое поведение
- возможное влияние
- logs, screenshots или proof of concept, если они доступны

---

<h2 align="center">Политика раскрытия</h2>

Сообщайте об уязвимостях ответственно и избегайте публичного раскрытия до тех пор, пока проблема не будет изучена и исправлена.

Все валидные сообщения будут рассмотрены с учетом severity и potential impact.

---

<h2 align="center">Область проверки</h2>

Сообщения о безопасности особенно важны для:

- проблем authentication и authorization
- раскрытия API keys или secrets
- небезопасной коммуникации между сервисами
- небезопасного выполнения торговых стратегий
- injection-уязвимостей: SQL, command и подобных
- проблем контроля доступа к базе данных
- утечек sensitive data через logs или API responses
- уязвимых dependencies с реальным impact

---

<h2 align="center">Практики безопасности</h2>

Trade360Lab проектируется с учетом security и isolation:

- нет hardcoded secrets
- конфигурация задается через environment
- frontend, orchestration, execution и database layers разделены
- exchange credentials обрабатываются контролируемо
- выполняется базовый dependency review и updates

<h2 align="center">Граница безопасности Live Trading</h2>

Live exchange credentials хранятся encrypted at rest, а API responses возвращают только masked `keyReference`. Raw API keys и secrets нельзя логировать, возвращать через REST responses или коммитить через environment files.

Live order submission защищен enabled session, active credentials, exchange health, mandatory risk validation, circuit breakers и manual kill switch. Rejected orders сохраняются с явными причинами и не доходят до exchange adapter.

Real exchange order submission отключен по умолчанию через `LIVE_TRADING_REAL_ORDER_SUBMISSION_ENABLED=false`. Операторы должны задать сильный `LIVE_TRADING_CREDENTIAL_ENCRYPTION_KEY` и проверить exchange/testnet behavior перед включением live signed submission.

<h2 align="center">Граница выполнения стратегий</h2>

Uploaded strategy files привязаны к owner и проходят validation перед activation. Текущий Python validation flow проверяет syntax, required entrypoints, metadata и parameter schema, но все еще импортирует Python module для анализа runtime metadata. Это не полноценный sandbox. Не запускайте untrusted strategy source в shared environments, пока не добавлен process/container sandboxing.

---

<h2 align="center">Заметки</h2>

Не сообщайте об уязвимостях через публичные GitHub issues.

Responsible disclosure помогает защищать пользователей, инфраструктуру и исследовательские системы.
