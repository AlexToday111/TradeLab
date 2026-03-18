<h1 align="center">TradeLab Monorepo</h1>

Trade360Lab — это монорепозиторий платформы для исследования, подготовки данных, запуска и сравнения торговых сценариев. Основной интерфейс находится во `frontend` и построен на Next.js: в нём собраны рабочее пространство, экран данных, бэктесты, карточки запусков и сравнение результатов. Папка `backend` содержит серверный scaffold для дальнейшего развития API и служебной логики. Дополнительно в репозитории есть `docs` с проектной документацией и `archive` с архивными материалами, которые не участвуют в активной сборке.

## Mermaid-схема

```mermaid
flowchart TD
    A[Trade360Lab]

<h2 align="center">Quick start</h2>

## Быстрый старт

Требования:

- Node.js 20+
- npm 10+

Установка зависимостей:

```bash
npm run install:all
```

Запуск фронтенда:

```bash
npm run dev
```

Frontend runs on `http://localhost:3000`.

<h2 align="center">Backend scaffold</h2>

Запуск backend:

```bash
npm run dev:backend
```

Backend будет доступен по адресу `http://localhost:4000` и отдаёт `GET /health`.

<h2 align="center">Useful scripts (from repo root)</h2>

- `npm run dev` — запустить фронтенд
- `npm run build` — собрать фронтенд
- `npm run start` — запустить production-сборку фронтенда
- `npm run typecheck` — проверить типы во фронтенде
- `npm run lint` — запустить линтинг фронтенда
- `npm run dev:frontend` — явно запустить фронтенд
- `npm run dev:backend` — запустить backend
- `npm run install:frontend` — установить зависимости фронтенда
- `npm run install:backend` — установить зависимости backend
- `npm run install:all` — установить все зависимости
