# TradeLab Landing

Отдельное intro-приложение для проекта `TradeLab`. Оно живет в этом репозитории, но собирается и деплоится независимо от основного `frontend`.

## Локальный запуск

```bash
cd landing
npm install
npm run dev
```

## Команды

- `npm run dev` - dev server Vite
- `npm run build` - production build в `dist/`
- `npm run preview` - локальный preview production-сборки

## Переменные окружения

Скопируй `.env.example` в `.env.local` или задай переменные в платформе деплоя:

- `VITE_REPOSITORY_URL` - ссылка на репозиторий
- `VITE_APP_URL` - ссылка на основное приложение, например `https://app.example.com/workspace`

## Отдельный деплой

### Vercel / Netlify

- Root directory: `landing`
- Install command: `npm install`
- Build command: `npm run build`
- Output directory: `dist`

### Docker

```bash
docker build -t tradelab-landing ./landing
docker run --rm -p 4173:80 tradelab-landing
```
