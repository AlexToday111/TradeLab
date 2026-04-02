<p align="center">
  <img src="./frontend/public/Logo.png" alt="TradeLab Logo" />
</p>
<h1 align="center">Trade360Lab</h1>

Trade360Lab — это монорепозиторий платформы для исследования, подготовки данных, запуска и сравнения торговых сценариев. Основной интерфейс находится во `frontend` и построен на Next.js: в нём собраны рабочее пространство, экран данных, бэктесты, карточки запусков и сравнение результатов. Папка `backend` содержит серверный scaffold для дальнейшего развития API и служебной логики. Дополнительно в репозитории есть `docs` с проектной документацией и `archive` с архивными материалами, которые не участвуют в активной сборке.

<h2 align="center">Архитектура</h2>

```mermaid
flowchart TB
    A[Trade360Lab]

    A --> L[Landing]
    A --> F[Frontend]
    A --> B[Backend]

    %% Frontend
    subgraph Frontend
        direction TB
        F --> F1[Pages]
        F --> F2[Reusable Components]
        F --> F3[Charts & Visualization]
        F --> F4[API Integration]
        F --> F5[Strategy Upload]
    end

    %% Backend
    subgraph Backend
        direction TB
        B --> J[Java API]
        B --> D[(PostgreSQL)]
        B --> P[Python Engine]
    end

    %% Java Layer
    subgraph "Java API Layer"
        direction TB
        J --> J1[Controllers]
        J --> J2[Services]
        J --> J3[Dataset API]
        J --> J4[Strategy Management]
        J --> J5[Run Control]
    end

    %% Python Layer
    subgraph "Python Engine Layer"
        direction TB
        P --> P1[Parser]
        P --> P2[Strategy Runner]
        P --> P3[Backtesting]
        P --> P4[Indicators]
        P --> P5[Exchange Adapters]
    end

    %% Data flow
    F4 --> J
    J --> D
    J --> P
    P --> D

    %% Landing connection
    L --> F
```

<h2 align="center">Текущая структура проекта</h2>

```text
TradeLab/
|-- frontend/               # Next.js приложение (UI + API proxy)
|   |-- app/
|   |-- components/
|   |-- features/
|   |-- lib/
|   `-- public/
|-- backend/
|   |-- java/               # Spring Boot API
|   `-- python/             # FastAPI parser/import service
|-- docs/                   # Проектная документация
|-- archive/                # Архивные материалы
`-- docker-compose.yml      # Оркестрация всего стека
```

<h2 align="center">Стек технологий</h2>

<h3 align="center">Фронтенд</h3>

<p align="center">
  <img alt="Next.js" src="https://img.shields.io/badge/Next.js-16-000000?logo=next.js&logoColor=white" />
  <img alt="React" src="https://img.shields.io/badge/React-18-20232A?logo=react&logoColor=61DAFB" />
  <img alt="TypeScript" src="https://img.shields.io/badge/TypeScript-3178C6?logo=typescript&logoColor=white" />
  <img alt="Tailwind CSS" src="https://img.shields.io/badge/Tailwind_CSS-06B6D4?logo=tailwindcss&logoColor=white" />
  <img alt="Radix UI" src="https://img.shields.io/badge/Radix_UI-161618?logo=radixui&logoColor=white" />
  <img alt="Recharts" src="https://img.shields.io/badge/Recharts-22C55E?logo=chartdotjs&logoColor=white" />
</p>

<h3 align="center">Бэкенд</h3>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white" />
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring_Boot-3-6DB33F?logo=springboot&logoColor=white" />
  <img alt="Python" src="https://img.shields.io/badge/Python-3.11+-3776AB?logo=python&logoColor=white" />
  <img alt="FastAPI" src="https://img.shields.io/badge/FastAPI-009688?logo=fastapi&logoColor=white" />
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white" />
  <img alt="Docker" src="https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white" />
</p>

<h2 align="center">Быстрый старт</h2>

<h3 align="center">Вариант A: весь стек в Docker (рекомендуется)</h3>

```bash
docker compose up --build
```

Сервисы:
- Frontend: `http://localhost:3000`
- Java API: `http://localhost:8080`
- Python parser: `http://localhost:8000`
- PostgreSQL: `localhost:5432`

<h3 align="center">Вариант B: локальная разработка</h3>

1. Фронтенд
```bash
cd frontend
npm install
npm run dev
```

2. Python parser
```bash
cd backend/python
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
uvicorn parser.main:app --host 0.0.0.0 --port 8000
```

3. Java API
```bash
cd backend/java
mvn spring-boot:run
```

<h2 align="center">Подробная документация</h2>

- Фронтенд: [`frontend/README.md`](./frontend/README.md)
- Обзор бэкенда: [`backend/README.md`](./backend/README.md)
- Java API: [`backend/java/README.md`](./backend/java/README.md)
- Python parser: [`backend/python/README.md`](./backend/python/README.md)

