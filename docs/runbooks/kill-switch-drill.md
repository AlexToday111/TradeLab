<h1 align="center">Kill Switch Drill</h1>

Целевой релиз: `v0.9.0-alpha.1`

<h2 align="center">Предусловия</h2>

- Пользователь может авторизоваться во frontend.
- Существует хотя бы одна live trading session.
- Real order submission остается отключенным, если нет отдельного production approval.

<h2 align="center">Порядок drill</h2>

1. Открыть `/live`.
2. Нажать `Kill switch`.
3. Убедиться, что `/live` показывает kill switch в состоянии `ACTIVE`.
4. Отправить guarded live order.
5. Проверить, что order отклонен с причиной `Kill switch is active`.
6. Открыть `/settings` и убедиться, что safety dashboard показывает активный kill switch.
7. Нажать `Reset`.
8. Убедиться, что `/live` и `/settings` вернулись к чистому состоянию kill switch.

<h2 align="center">Свидетельства выполнения</h2>

- Скриншот safety state на `/live` до и после reset.
- Строка risk audit для `KILL_SWITCH_ACTIVATED`.
- Причина rejected order для заблокированной заявки.
