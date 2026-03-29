# TradeLab

TradeLab - монорепозиторий проекта для исследования торговых идей, подготовки данных, бэктестинга и пользовательского интерфейса.

## Структура

```text
TradeLab/
|-- backend/   # Java API и Python parser/import service
|-- frontend/  # основное Next.js приложение
|-- landing/   # отдельный Vite landing / intro
|-- package.json
`-- docker-compose.yml
```

`frontend` и `landing` живут в одном репозитории, но собираются и деплоятся независимо.

## Корневые команды

```bash
npm run dev
npm run build
npm run lint
npm run typecheck

npm run dev:landing
npm run build:landing
npm run preview:landing
```

## Локальная разработка

### Основной frontend

```bash
cd frontend
npm install
npm run dev
```

### Landing

```bash
cd landing
npm install
npm run dev
```

### Backend stack в Docker

```bash
docker compose up --build
```

Сервисы:

- Frontend: `http://localhost:3000`
- Java API: `http://localhost:8080`
- Python parser: `http://localhost:8000`
- PostgreSQL: `localhost:5432`

## Отдельный деплой landing

Для Vercel, Netlify и аналогичных платформ указывай:

- Root directory: `landing`
- Install command: `npm install`
- Build command: `npm run build`
- Output directory: `dist`

Для контейнерного деплоя используй `landing/Dockerfile`.
