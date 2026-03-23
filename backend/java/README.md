<h1 align="center">TradeLab Java API</h1>

<p align="center">
  Spring Boot сервис для работы с датасетами, свечами и оркестрации импорта через Python parser.
</p>

<h2 align="center">Стек</h2>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white" />
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring_Boot-3-6DB33F?logo=springboot&logoColor=white" />
  <img alt="Spring Web" src="https://img.shields.io/badge/Spring_Web-6DB33F?logo=spring&logoColor=white" />
  <img alt="Spring Data JPA" src="https://img.shields.io/badge/Spring_Data_JPA-6DB33F?logo=spring&logoColor=white" />
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white" />
  <img alt="Maven" src="https://img.shields.io/badge/Maven-C71A36?logo=apachemaven&logoColor=white" />
</p>

<h2 align="center">Структура</h2>

```text
backend/java/
|-- src/main/java/com/example/back/
|   |-- controller/
|   |-- service/
|   |-- repository/
|   |-- entity/
|   |-- dto/
|   `-- client/           # HTTP-клиент Python parser
`-- src/main/resources/
    |-- application.yml
    `-- schema.sql
```

<h2 align="center">Endpoints</h2>

- `GET /api/health`
- `GET /api/python/health`
- `GET /api/datasets`
- `POST /api/datasets`
- `PATCH /api/datasets/{id}`
- `POST /api/datasets/{id}/duplicate`
- `DELETE /api/datasets/{id}`
- `GET /api/candles`
- `POST /api/imports/candles`

<h2 align="center">Конфигурация</h2>

Основные переменные окружения (см. `application.yml`):

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `PYTHON_PARSER_BASE_URL`

Порты по умолчанию:
- Java API: `8080`
- Python parser: `8000`
- PostgreSQL: `5432`

<h2 align="center">Запуск локально</h2>

```bash
cd backend/java
mvn spring-boot:run
```

<h2 align="center">Сборка</h2>

```bash
cd backend/java
mvn clean package -DskipTests
```

<h2 align="center">Docker</h2>

```bash
docker build -t tradelab-java ./backend/java
```

