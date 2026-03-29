<h1 align="center">Trade360Lab Frontend</h1>

<p align="center">
  Фронтенд-приложение на Next.js (App Router) для работы с проектами, датасетами, бэктестами и результатами запусков.
</p>

<h2 align="center">Стек</h2>

<p align="center">
  <img alt="Next.js" src="https://img.shields.io/badge/Next.js-16-000000?logo=next.js&logoColor=white" />
  <img alt="React" src="https://img.shields.io/badge/React-18-20232A?logo=react&logoColor=61DAFB" />
  <img alt="TypeScript" src="https://img.shields.io/badge/TypeScript-3178C6?logo=typescript&logoColor=white" />
  <img alt="Tailwind CSS" src="https://img.shields.io/badge/Tailwind_CSS-06B6D4?logo=tailwindcss&logoColor=white" />
  <img alt="Radix UI" src="https://img.shields.io/badge/Radix_UI-161618?logo=radixui&logoColor=white" />
  <img alt="Recharts" src="https://img.shields.io/badge/Recharts-22C55E?logo=chartdotjs&logoColor=white" />
</p>

<h2 align="center">Структура</h2>

```text
frontend/
|-- app/                 # страницы и API proxy маршруты
|-- components/          # shell, shared, ui
|-- features/            # доменная логика интерфейса
|-- lib/                 # демо-данные, типы, утилиты
|-- public/              # статические ассеты (иконки, лого, favicon)
`-- styles/              # глобальные стили и токены
```

<h2 align="center">Ключевые маршруты</h2>

- `/workspace` - обзорный дашборд
- `/desktop` - рабочий стол проекта
- `/data` - датасеты и импорт
- `/backtests` - запуски и фильтрация
- `/runs/[id]` - карточка запуска
- `/compare` - сравнение запусков

<h2 align="center">Интеграция с API</h2>

Фронтенд использует Next API routes (`app/api/*`) как proxy к Java API.

Переменная окружения:
- `BACKEND_API_BASE_URL` (по умолчанию: `http://127.0.0.1:8080`)

<h2 align="center">Локальный запуск</h2>

```bash
cd frontend
npm install
npm run dev

```

<h2 align="center">Полезные команды</h2>

- `npm run dev`
- `npm run build`
- `npm run start`
- `npm run lint`
- `npm run typecheck`

