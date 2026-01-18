# TradeLab UX/UI Design Specification

Документ описывает визуальный язык, ключевые экраны, компоненты и UX‑сценарии для веб‑приложения TradeLab. Фокус: воспроизводимость бэктестов и переносимость данных.

## 1) Визуальный стиль и дизайн‑язык

### Общая концепция
- Стиль: микс "VS Code Pro" + "Trading Terminal", строгий профессиональный интерфейс без декоративных эффектов.
- Компоновка: IDE‑подобная структура (левая панель / центральный холст / правая панель / нижняя панель).
- UX‑принцип: все важное доступно за 1–2 клика; высокая плотность информации без визуального шума.
- Без стекла, без glow, без лишних теней; читаемая иерархия и понятные состояния.

### Цветовая система (тёмная тема по умолчанию)
Используются глубокие оттенки графита, не pure black.

```
:root {
  --bg-0: #0f1116;   /* базовый фон */
  --bg-1: #141823;   /* основная поверхность */
  --bg-2: #1a2030;   /* карточки/панели */
  --bg-3: #20283a;   /* акцентные блоки */

  --text-1: #e6eaf2; /* основной текст */
  --text-2: #b3bac7; /* вторичный */
  --text-3: #8a92a3; /* hints */

  --accent-1: #8b5cf6; /* primary CTA */
  --accent-2: #7041d8; /* hover/secondary */
  --accent-3: #6aa1ff; /* холодный фиолетово-синий для графиков */

  --success: #3aa67a;
  --running: #3f7ecb;
  --warning: #c08a3e;
  --error: #c24d4d;

  --profit: #4bbd83;
  --loss: #d46363;

  --border-1: #2a3245;
  --border-2: #323b52;
}
```

Принципы:
- Акцентный фиолетовый используется дозированно: CTA, активные состояния, selected.
- Цвета статусов приглушённые, без кислотности.
- Profit/Loss — только как акцент в числах и маркерах.

### Типографика
- UI‑тексты: "Space Grotesk" (основной), fallback: "IBM Plex Sans", "Work Sans", sans-serif.
- Код и логи: "JetBrains Mono", fallback: "Iosevka", "Fira Code", monospace.
- Иерархия: section title → label → hint, минимум капса.

Пример шкалы:
- H1 24/30, 600
- H2 18/24, 600
- H3 14/20, 600
- Body 13/18, 400
- Label 12/16, 500
- Hint 11/14, 400

### Формы и геометрия
- Контейнеры и панели: radius 10.
- Таблицы и списки: radius 6 (строже).
- Кнопки и теги: radius 12 (мягче).
- Разделители тонкие, 1px, с низким контрастом.
- Панели ресайзятся, состояние сохраняется (local storage).

### Motion
- Анимации минимальные, функциональные: fade/slide 120–180ms.
- Staggered reveal для списков runs (50–80ms шаг).
- Скелетоны в таблицах и графиках при загрузке.

### Сетка и отступы
- Базовый шаг 4px, основные отступы кратны 8px.
- IDE‑layout:
  - Left panel: 240–280px, resizable.
  - Right panel: 320–380px, resizable.
  - Bottom panel: 200–260px, resizable.
  - Центральный холст: flexible, минимум 720px.

## 2) Навигация верхнего уровня

### Структура
- Левый вертикальный sidebar (иконки + подписи), поскольку IDE‑паттерн лучше всего ложится на частое переключение между режимами.
- Верхняя панель (top bar) для глобальных элементов.

### Разделы навигации
Workspace, Code, Data, Backtests, Research, Deploy (скрываемо флагом), Settings.

### Глобальные элементы (top bar)
- Project switcher.
- Глобальный поиск (Cmd/Ctrl+P).
- Индикатор окружения: local / remote / prod‑like (цветовой бейдж).
- User menu (профиль, API keys, team, billing).

## 3) Ключевая концепция: Run‑centered UX

### Стандартный Run‑хедер (повторяется на экранах)
- Run ID (копируемый).
- Статус: queued / running / done / failed.
- Изменения: индикатор diff по коду/данным/конфигу (три иконки с точками).
- Actions: Re‑run, Clone config, Diff vs…, Open code at commit, Open dataset version, Export/Import.

### Run‑метаданные
Версия кода (commit/snapshot), версия датасета, конфигурация (fees, slippage, risk, execution), период/символы/таймфреймы, результаты и артефакты.

## 4) Экран: Workspace

### Вне проекта
- Таблица проектов:
  - Название, описание.
  - Последние runs (count + mini‑chips).
  - Последний датасет.
  - Дата активности.
- CTA: New project (шаблон стратегии), Import repo, Open project.
- Empty state: "Создайте первый проект и подключите данные".

### Внутри проекта
- Левая панель: дерево папок (strategies/, data_pipelines/, configs/, reports/).
- Центральная область: pinned datasets и recent runs.
- Блок "Recent runs" сразу показывает Run ID и статус.

## 5) Экран: Code (основной рабочий)

### Левая панель
- Project Explorer (с git‑статусами).
- Поиск по файлам.
- Закреплённые файлы и runs.

### Центральная область
- Редактор кода: вкладки, breadcrumbs, minimap, подсветка синтаксиса.
- Линейка ошибок и warnings в gutter.

### Нижняя панель (tabbed)
Console, Logs, Problems, Backtest Output, Artifacts.

### Правая панель: Run / Params
- Strategy entrypoint selector.
- Dataset selector (symbols, timeframe, period).
- Fees/Slippage preset.
- Execution model preset.
- Risk model (risk per trade, max exposure).
- Data contract indicator:
  - "strategy expects" vs "dataset provides".
  - Несовместимости подсвечиваются ДО запуска.
- Buttons: Backtest, Optimize, Validate, Export.
- Run preview: оценка объёма данных, времени выполнения, warnings.

## 6) Экран: Data

### Источники данных
Карточки источников: биржи / API / CSV / S3.

### Pipeline builder
Этапы: download → clean → resample → features → store.

### Dataset versions
Список версий (v12, v13...):
- Период, TF, символы, размер, pipeline hash.
- Кнопка "Use in backtest".

### Data quality панель
Coverage, gaps, duplicates, outliers.

### Preview
Табличный preview + лёгкий OHLC/volume график.

## 7) Экран: Backtests (Runs list)

### Run‑очередь
Список runs как CI‑очередь:
- статус (queued / running / done / failed),
- стратегия,
- датасет,
- период,
- ключевые параметры.

### Метрики и теги
PnL, Sharpe, Max DD, Trades, Fees impact.
Теги: baseline, candidate, prod‑like.

### Фильтры и bulk‑actions
Сохранённые фильтры, выбор нескольких runs, bulk‑export.

## 8) Экран: Run details (ключевой)

### Header
Run ID, статус, стратегия, датасет, Re‑run/Clone/Diff/Export.

### Summary cards
PnL, Sharpe, Max DD, Winrate, Trades, Avg trade, Fees impact.

### Графики
Equity curve, drawdown, underwater, returns histogram.

### Trades table
Фильтры: символ, дата, тип сделки, PnL диапазон.

### Logs + Artifacts
Ссылки на логи, model artifacts, отчёты.

### Reproducibility
Commit hash, dataset version, config YAML/JSON (raw toggle).

## 9) Экран: Compare runs

### Выбор runs
2–5 runs, быстрые шаблоны сравнения (baseline vs candidate).

### Overlay графиков
Equity curve, drawdown, returns.

### Таблица метрик
Delta по ключевым показателям, highlight улучшений/ухудшений.

### Diff
Side‑by‑side diff конфигов и версий датасета + Data quality.

### Stability hints
Блок с возможными причинами различий (изменения в данных, шипы, комиссии).

## 10) UX‑детали

### Горячие клавиши
- Cmd/Ctrl+P: глобальный поиск.
- Cmd/Ctrl+Enter: запуск backtest.
- Cmd/Ctrl+Shift+F: поиск по проекту.
- Cmd/Ctrl+B: toggle left panel.
- Cmd/Ctrl+J: toggle bottom panel.

### Empty states
Понятные CTA на каждом экране (например: "Подключить источник данных").

### Ошибки
Прозрачные, actionable: "Датасет не содержит required fields".

### Онбординг
Create project → Connect data → Run backtest.

## 11) Компонентная библиотека (Design System)

### Навигация
- Sidebar (иконки + подписи).
- Top bar (project switcher, search, env, user menu).

### Основные компоненты
- Buttons: Primary (accent‑1), Secondary, Ghost, Danger.
- Inputs: текстовые, search, select, multi‑select.
- Tags/Badges: статусные и тематические (baseline, prod‑like).
- Tabs: горизонтальные и вертикальные.
- Cards: data source, run summary, project.
- Tables: плотные, с hover‑состоянием и sticky headers.
- Tooltips/Toasts: краткие, без лишней анимации.

### Специализированные
- Run header (ID + status + diff indicators).
- Run diff chips (code/data/config).
- Data contract indicator (expected/provided).
- KPI cards с микрографиками.
- Chart container с легендой и переключателями.
- Bulk action bar для runs.

## 12) User flows (5 ключевых сценариев)

1. Онбординг: Create project → Connect data source → Build pipeline → Run backtest.
2. Разработка стратегии: Code → Select dataset → Validate → Backtest → Review results.
3. Сравнение подходов: Run details → Clone config → Run → Compare runs.
4. Валидация воспроизводимости: Run details → Open code at commit → Open dataset version → Export.
5. Data iteration: Data → Create new dataset version → Use in backtest → Monitor impact.

## 13) MVP‑scope и что отложить

### MVP
- Workspace, Code, Data, Backtests, Run details, Compare runs.
- Run‑centered UX + action bar.
- Data contract indicator.
- Минимальные графики + таблицы.
- Горячие клавиши и ресайз панелей.

### Позже
- Research (advanced notebooks).
- Deploy (live/paper).
- Автоматические stability hints на ML‑уровне.
- Версионирование pipeline с lineage‑графом.
