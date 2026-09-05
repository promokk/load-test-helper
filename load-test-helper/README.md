# Load-test-helper
Backend-сервис для автоматизации рутинных задач при проведении нагрузочного тестирования.
Приложение хранит профили и сценарии нагрузки, ссылки на стенды, статус генераторов нагрузки, запуски тестов.

Решает ряд задач:
* поиск информации о запущенных тестах
* поиск свободных генераторов нагрузки
* хранение профилей / сценариев нагрузки
* хранение ссылок на тестовые стенды

---

## Оглавление
* [Stack](#stack)
* [API](#api)
* [Архитектура БД](#db)
* [Дополнительно](#other)

---

## Stack <a id="stack"></a>
**Язык и рантайм**
- Java 17
- Groovy 4

**Фреймворк**
- Spring Boot 3.5.6
    - Spring Web
    - Spring Data JPA / Hibernate ORM
    - Spring Boot Actuator
    - Spring Task Scheduling
- Bean Validation: Jakarta Validation API + Hibernate Validator

**База данных**
- PostgreSQL
- HikariCP
- Схема через Hibernate
- H2 — опционально, для локального запуска

**Логирование**
- SLF4J + Logback

**Сборка**
- Gradle 8.14.3 (wrapper)

---

## API  <a id="api"></a>

**Base URL:** `http://localhost:8088`  
**Формат:** JSON (`Content-Type: application/json`)  
**Ошибки:** единый обработчик (`GlobalExceptionHandler`)  
- `400 Bad Request` — невалидный запрос / неверные параметры
- `404 Not Found` — сущность не найдена
- `200 / 201 / 204` — успех

<details>
<summary>📌 Методы</summary>

### Profile — профили нагрузки (`/profile`)

| Метод | Путь | Описание |
|---|---|---|
| GET | `/profile` | Список всех профилей |
| GET | `/profile/{name}` | Профиль по имени |
| GET | `/profile/profileCalculation?profileName={name}&serverCount={n}` | Пересчёт профиля под заданное кол-во серверов. `profileName` — `{name}` или `{name}:{throughput}:{threads}:{rampUp}` |
| POST | `/profile/add` | Добавить профиль |
| DELETE | `/profile/{name}` | Удалить профиль |

Тело `POST /profile/add`:
```json
{
  "name": "profile-1",
  "throughput": 100.0,
  "threads": 10,
  "rampUp": 60
}
```
Ограничения: `name` — not blank, `throughput` — not null, `threads` ≥ 1, `rampUp` ≥ 1.

---

### Scenario — сценарии нагрузки (`/scenario`)

| Метод | Путь | Описание |
|---|---|---|
| GET | `/scenario` | Список сценариев |
| GET | `/scenario/{name}` | Сценарий по имени |
| POST | `/scenario/add` | Добавить сценарий (вместе с группами) |
| POST | `/scenario/{name}/add/group` | Добавить группу в сценарий |
| PUT | `/scenario/{name}/group/{groupId}` | Редактировать группу (`groupId` — Integer) |
| DELETE | `/scenario/{name}` | Удалить сценарий |
| DELETE | `/scenario/draft/deleteAll` | Удалить все черновые сценарии (`draft = true`) |
| DELETE | `/scenario/{name}/group/{groupId}` | Удалить группу из сценария |

Тело `POST /scenario/add`:
```json
{
  "name": "scenario-1",
  "stand": "stand-1",
  "domain": "domain-1",
  "duration": 600,
  "draft": false,
  "groups": [
    {
      "profile": "profile-1",
      "server": "gen-01",
      "masterRun": true,
      "domain": "domain-1",
      "duration": 600,
      "certificate": null,
      "testParam": null,
      "serverParam": null
    }
  ]
}
```
Ограничения: `name` — not blank, `stand` — необязательное, `domain` — not blank, `duration` ≥ 1, `draft` — not null, `groups` — не пустой; в группе `profile` — not blank, `duration` ≥ 1.

---

### Server — генераторы нагрузки (`/server`)

| Метод | Путь | Описание |
|---|---|---|
| GET | `/server` | Список всех серверов |
| GET | `/server/{name}` | Сервер по имени |
| GET | `/server/free?state={true\|false}` | Список свободных / занятых серверов |
| GET | `/server/count?state={true\|false}` | Кол-во свободных / занятых серверов |
| POST | `/server/add` | Добавить сервер |
| POST | `/server/serverBooking/{cnt}` | Бронирование: `cnt` = число → забронировать N любых свободных; `cnt` = список имён через запятую → забронировать конкретные |
| POST | `/server/serverBookingAuto?profiles={profiles}` | Автоброкирование нужного кол-ва серверов по профилям (`{name}` или `{name}:{throughput}:{threads}:{rampUp}`, через запятую) |
| POST | `/server/serverBooking/cancel?server={name1,name2}` | Снять бронь с указанных серверов |
| POST | `/server/serverBooking/cancel/all` | Снять бронь со всех серверов |
| DELETE | `/server/{name}` | Удалить сервер |

Тело `POST /server/add`:
```json
{
  "name": "gen-01",
  "free": true
}
```

---

### Stand — тестовые стенды (`/stand`)

| Метод | Путь | Описание |
|---|---|---|
| GET | `/stand` | Список стендов |
| GET | `/stand/{name}` | Стенд по имени |
| GET | `/stand/get/url?standName={s}&domainName={d}` | URL по стенду и домену |
| POST | `/stand/add` | Добавить стенд (вместе с доменами) |
| POST | `/stand/{name}/add/domain` | Добавить / изменить домен стенда |
| DELETE | `/stand/{name}` | Удалить стенд |
| DELETE | `/stand/{name}/domain/{domainName}` | Удалить домен стенда |

Тело `POST /stand/add`:
```json
{
  "name": "stand-1",
  "domains": [
    { "name": "domain-1", "url": "https://app.example.com" }
  ]
}
```
Ограничения: `name` — not blank, `domains` — не пустой; в домене `name` и `url` — not blank.

---

### Test — запуски тестов (`/test`)

| Метод | Путь | Описание |
|---|---|---|
| GET | `/test/info` | Список тестов |
| GET | `/test/info/{id}` | Тест по id |
| POST | `/test/create` | Создать тест (возвращает id). Бронирует серверы, `endedAt = createdAt + duration` |
| DELETE | `/test/delete/{id}` | Удалить тест (снимает бронь серверов) |
| DELETE | `/test/delete/all` | Удалить все тесты (снимает бронь со всех серверов) |

Тело `POST /test/create`:
```json
{
  "stand": "stand-1",
  "duration": 600,
  "server": ["gen-01", "gen-02"],
  "profile": "profile-1"
}
```
Ограничения: `stand` — not blank, `duration` ≥ 1, `server` — не пустой, `profile` — not blank.

---

### Actuator (мониторинг)

| Метод | Путь | Описание |
|---|---|---|
| GET | `/actuator/**` | Все эндпоинты Spring Boot Actuator открыты (`health`, `info`, `metrics` и т.д.) |

</details>

---

## Архитектура БД  <a id="db"></a>
**СУБД:** PostgreSQL, все таблицы в схеме `load_test_helper`.  
**Управление схемой:** Hibernate `ddl-auto=update`.  
**Генерация ключей:** `GenerationType.SEQUENCE` для числовых id.  
**Дата/время:** хранится в UTC

<details>
<summary>📌 Таблицы</summary>

**`profile`** — профиль нагрузки

| Колонка | Тип | Ограничения |
|---|---|---|
| name | varchar | PK |
| throughput | double | not null |
| threads | integer | not null |
| ramp_up | integer | not null |

**`server`** — генератор нагрузки

| Колонка | Тип | Ограничения |
|---|---|---|
| name | varchar | PK |
| free | boolean | not null (признак свободен/занят) |

**`stand`** — тестовый стенд

| Колонка | Тип | Ограничения |
|---|---|---|
| name | varchar | PK |

**`domain`** — домен (URL) в рамках стенда

| Колонка | Тип | Ограничения |
|---|---|---|
| id | integer | PK (sequence) |
| stand_id | varchar | FK → `stand.name` |
| name | varchar | not null |
| url | varchar | not null |

**`scenario`** — сценарий нагрузочного теста

| Колонка | Тип | Ограничения |
|---|---|---|
| name | varchar | PK |
| stand | varchar | nullable |
| domain | varchar | not null |
| duration | integer | not null |
| draft | boolean | not null |

**`group`** — группа внутри сценария (профиль + сервер + параметры)

| Колонка | Тип | Ограничения |
|---|---|---|
| id | integer | PK (sequence) |
| scenario_id | varchar | FK → `scenario.name` |
| profile | varchar | not null |
| server | varchar | nullable |
| masterRun | boolean | nullable |
| domain | varchar | nullable |
| duration | integer | nullable |
| certificate | varchar | nullable |
| testParam | varchar | nullable |
| serverParam | varchar | nullable |

**`test`** — запись о запущенном тесте

| Колонка | Тип | Ограничения |
|---|---|---|
| id | integer | PK (sequence) |
| createdAt | timestamp | not null |
| endedAt | timestamp | not null (createdAt + duration) |
| stand | varchar | not null |
| duration | integer | not null |

**`test_server_profile`** — пары сервер/профиль для теста (`@ElementCollection`)

| Колонка | Тип |
|---|---|
| test_id | integer (FK → `test.id`) |
| server | varchar |
| profile | varchar |

</details>

## Дополнительно <a id="other"></a>

### Файлы начальных данных: `serverList.dat` и `profileList.dat`
Эти два файла — источник **первичного наполнения БД** при старте приложения. За загрузку отвечает компонент 
`DataLoader`: в методах `@PostConstruct` он проверяет соответствующую таблицу и, **только если она пустая**, читает 
файл и создаёт записи. Если в таблице уже есть данные — файл игнорируется (в лог пишется «БД преднаполнена»).
Файлы читаются по относительному пути, то есть должны лежать в рабочем каталоге, из которого запускается приложение 
(обычно корень модуля `load-test-helper`). Имена файлов настраиваются в 
`application.properties` (`serverListFile`, `profileListFile`).

