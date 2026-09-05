# Grafana-дашборд "load-test-helper"

Дашборд Grafana (`load-test-helper-1788600981432.json`) для мониторинга состояния приложения 
**load-test-helper**: серверов, тестов, профилей, сценариев, групп и стендов/доменов. Данные читаются напрямую из 
PostgreSQL-схемы приложения.

---

## Требования

- Grafana **>= 10.0.3**
- Установленный источник данных **PostgreSQL**
- Доступ к базе данных load-test-helper)

---

## Импорт

1. В Grafana: **Dashboards → New → Import**.
2. Загрузить файл `load-test-helper.json`.
3. На экране импорта заполнить параметры:
    - **PostgreSQL** — выбрать существующий datasource, подключённый к БД load-test-helper.
    - **db_name** (`VAR_DB_NAME`) — имя схемы в БД.
4. Нажать **Import**.

---

## Структура дашборда

### Секция Server
- **Count Server** — общее количество зарегистрированных серверов (`stat`).
- **Count Status** — количество свободных (`free`) и занятых (`busy`) серверов (`bargauge`).
- **Server Status** — таблица серверов с их текущим статусом (FREE/BUSY, подсвечивается цветом).

![server - картинка](https://raw.githubusercontent.com/promokk/load-test-helper/main/dashboard/data/server.png)

### Секция Test
- **Active Test** — таблица тестов: id, стенд, длительность, время создания/окончания, сервер(-ы) и профиль(-и).
  Дополнительно рассчитывается прогресс выполнения теста (`Run`, в %), отображается прогресс-баром (gauge) в ячейке.

![test - картинка](https://raw.githubusercontent.com/promokk/load-test-helper/main/dashboard/data/test.png)

### Секция Profile / Scenario / Group
- **Profile** — таблица профилей нагрузки: имя, throughput, threads, ramp up.
- **Scenario** — таблица сценариев: имя, стенд, домен, длительность, черновик (draft), связанные группы.
- **Group** — таблица групп: id, сценарий, сервер, профиль, master run, домен, длительность, сертификат, кастомные параметры, параметры теста/сервера.

![profile - картинка](https://raw.githubusercontent.com/promokk/load-test-helper/main/dashboard/data/profile.png)
![scenario - картинка](https://raw.githubusercontent.com/promokk/load-test-helper/main/dashboard/data/scenario.png)
![group - картинка](https://raw.githubusercontent.com/promokk/load-test-helper/main/dashboard/data/group.png)

### Секция Stand / Domain / URL
- **Stand / Domain / URL** — таблица соответствия стендов, доменов и URL.

![stand - картинка](https://raw.githubusercontent.com/promokk/load-test-helper/main/dashboard/data/stand.png)

---

## Переменные шаблонов

| Переменная | Тип | Назначение |
|---|---|---|
| `data_sourse` | datasource | Выбор источника данных PostgreSQL (скрыт из панели, служебная) |
| `db_name` | constant | Имя схемы БД, подставляется во все SQL-запросы как `${db_name}` |
| `now` | query (`SELECT NOW()`) | Текущее время БД, используется в расчёте прогресса активного теста |