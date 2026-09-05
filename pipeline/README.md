# Jenkins Pipeline "load-test-helper"

Запуск тестов осуществляется с помощью следующих пайплайнов:
* **pipeline-jmeter-run**  
  Пайплайн для запуска распределённого нагрузочного теста 
  Apache JMeter в режиме master/slave на нескольких генераторах нагрузки.
* **pipeline-jmeter-scenario**  
  Верхнеуровневый оркестратор: по имени сценария забирает его описание из приложения
  **load-test-helper** и параллельно запускает дочерний job (pipeline-jmeter-run) для каждой
  группы сценария.
* **pipeline-jmeter-stop-test**  
  Пайплайн для остановки и удаления запущенных нагрузочных тестов JMeter.

---

<details>
<summary>📗 pipeline-jmeter-run</summary>

## pipeline-jmeter-run
- бронирует генераторы и рассчитывает профиль нагрузки через приложение **load-test-helper**;
- забирает jmx-скрипты и сертификаты из git-репозитория, раскладывает их по серверам;
- поднимает `jmeter-server` на всех генераторах и запускает `jmeter` в режиме master;
- регистрирует запущенный тест в load-test-helper;
- при любой ошибке откатывает бронь серверов и завершает процессы JMeter.

### Требования

#### Jenkins-агент

- `curl` (с поддержкой `--fail-with-body`);
- `ssh`, `scp`, `ssh-keygen`, `ssh-keyscan`;
- плагины Jenkins: `Pipeline`, `SSH Agent`, `Git Parameter`, `Copy Artifact`, `Git`.

#### Генераторы нагрузки

- доступ по SSH под пользователем с ключом из credential;
- утилиты `unzip`, `nohup`, `ps`, `pkill`;
- для мониторинга JMX — отдельные скрипты запуска `jmeter-jmx` / `jmeter-server-jmx`
  (с подключённым jmx-exporter).

---

### Настройка (блок `environment` в начале файла)

| Переменная | Назначение |
|---|---|
| `userSSH` | пользователь для подключения к генераторам по SSH |
| `credentialSSH` | Jenkins credential с приватным SSH-ключом |
| `gitRepository` | репозиторий с архивами jmx-скриптов и сертификатами |
| `gitCredentialsId` | Jenkins credential для доступа к репозиторию |
| `gitPathScript` | путь в репозитории до архивов jmx |
| `gitPathCertificate` | путь в репозитории до сертификатов |
| `jmeterDir` | корневая `bin`-директория JMeter на генераторах |
| `homeDir` | домашняя директория, где создаётся рабочая папка теста |
| `workDir` | имя временной рабочей директории под запуск теста |
| `jmeter` | имя скрипта запуска JMeter |
| `jmeterServer` | имя скрипта запуска jmeter-server |
| `dataSourceBL` | адрес InfluxDB для Backend Listener |
| `jmxJmeterStartPort` | стартовый порт jmx-exporter для процессов `jmeter` |
| `jmxJmeterStartServerPort` | стартовый порт jmx-exporter для процессов `jmeter-server` |
| `hostTestHelper` | базовый URL приложения load-test-helper |
| `checkHttpError` | суффикс shell для маркировки неуспешного `curl` |

> Перед использованием обязательно поправьте: `userSSH`, `credentialSSH`, `gitRepository`, `gitCredentialsId`, `dataSourceBL`, `hostTestHelper`,
> `copyArtifactPermission('указать проект')`.

---

### Параметры сборки

| Параметр | Тип | Обяз. | Описание |
|---|---|---|---|
| `PROFILE` | string | да | Список профилей через запятую в формате `имя:throughput:threads:rampUp`. Пример: `script01:10:20:60,script02:20:40:60`. `имя` — это имя jmx-файла без расширения и имя `.zip`-архива в репозитории. |
| `SERVER` | string | нет | Количество генераторов **или** список серверов через запятую. Пусто — автоматический подбор количества по профилю. |
| `MASTER_RUN` | boolean | — | Должен ли master-сервер сам генерировать нагрузку. Если `false` — master только координирует. |
| `DURATION` | string | — | Длительность теста в секундах. Передаётся в jmx как `-Gduration`. |
| `STAND` | string | да | Имя стенда. Влияет на выбор URL и подставляется в имя jmx-файла (`<profile>-<STAND>.jmx`). |
| `DOMAIN` | string | да* | Имя домена. Влияет на выбор URL. Обязателен, если не задан `CUSTOM_URL`. |
| `CUSTOM_URL` | string | да* | Готовый URL для теста в обход load-test-helper. Обязателен, если не задан `DOMAIN`. |
| `CERTIFICATE` | string | нет | Имя файла сертификата (p12/keystore) с расширением. Копируется на генераторы и подключается через `-Djavax.net.ssl.keyStore`. |
| `CERTIFICATE_P` | string | нет | Пароль сертификата (`-Djavax.net.ssl.keyStorePassword`). |
| `CUSTOM_PARAM_RUN` | string | нет | Доп. параметры запуска `jmeter` (`-G/-D/-J/-L`...). Пример: `-Gparam01=hello`. |
| `CUSTOM_PARAM_SERVER` | string | нет | Доп. параметры запуска `jmeter-server`. Пример: `-Dparam01=hello`. |
| `JMX_EXPORTER_RUN` | boolean | — | Вкл/выкл мониторинг JMX-метрик. При `true` используются скрипты `jmeter-jmx` / `jmeter-server-jmx` и выделяются порты jmx-exporter. |
| `BRANCH` | git parameter | — | Ветка репозитория с jmx-скриптами. |

> Обязателен один из пары `DOMAIN` / `CUSTOM_URL`.  
> Проверка обязательных параметров (`PROFILE`, `STAND`, `DOMAIN`/`CUSTOM_URL`) выполняется
в начале — при их отсутствии сборка падает сразу.

---

### Стадии пайплайна

1. **Configuration Settings**
    - `deleteDir()` — очистка WORKSPACE;
    - бронирование генераторов:
        - `POST /server/serverBooking/{SERVER}` — если `SERVER` задан;
        - `POST /server/serverBookingAuto?profiles={PROFILE}` — если `SERVER` пуст;
    - первый сервер в ответе становится `masterServer`; при `MASTER_RUN=false` счётчик рабочих серверов уменьшается на 1;
    - расчёт профиля: `GET /profile/profileCalculation?profileName={PROFILE}&serverCount={serverCnt}` — возвращает массив `[имя, throughput, threads, rampUp]` на каждый профиль (нагрузка уже поделена на генераторы);
    - выбор URL: `GET /stand/get/url?standName={STAND}&domainName={DOMAIN}` (пропускается, если задан `CUSTOM_URL`);
    - обновление `~/.ssh/known_hosts` для всех генераторов (`ssh-keygen -R` + `ssh-keyscan`), параллельно.

2. **Info About Test**
    - формирует текстовую сводку (серверы, профили, параметры, порты jmx-exporter);
    - задаёт `currentBuild.displayName` = `<BUILD_ID>--<STAND>--<профили>` и `currentBuild.description`.

3. **Check Server Is Busy**
    - по SSH проверяет на каждом генераторе наличие процесса `ApacheJMeter.jar`;
    - если хоть один занят — сборка падает со статусом `ERROR-SERVER-BUSY` (список занятых/свободных выводится в лог).

4. **Checkout**
    - shallow-checkout репозитория `gitRepository` на ветке `BRANCH` (depth 1, без тегов).

5. **Copying Files** (параллельно по серверам)
    - пересоздаёт `homeDir/workDir` и подкаталог на каждый профиль;
    - `scp` архива `<profile>.zip`, распаковка, переименование `<profile>.jmx` → `<profile>-<STAND>.jmx`;
    - при заданном `CERTIFICATE` — копирование файла сертификата в каталог профиля.

6. **Start Jmeter Server** (параллельно по серверам, последовательно по профилям)
    - запускает `jmeter-server` на каждом генераторе;
    - RMI-порт (`SERVER_PORT`) начинается с `1099` и увеличивается на 1 для каждого профиля;
    - подключает сертификат и `CUSTOM_PARAM_SERVER`;
    - `sleep 10` после запуска.

7. **Start Test** (последовательно по профилям)
    - запускает `jmeter` в режиме master на `masterServer`:
      `-X -n -t <profile>-<STAND>.jmx -R <server1:порт,server2:порт,...>`
      `-Dmode=StrippedAsynch -DdataSourceBL=<InfluxDB>`
      `-Gthroughput= -Gthreads= -GrampUp= -Gduration= -Gurl=`;
    - при `MASTER_RUN=false` master исключается из списка `-R`;
    - `sleep 60` после запуска.

8. **Check Test**
    - проверяет, что число процессов `ApacheJMeter.jar` на master == `profileCnt * 2`
      (jmeter + jmeter-server на каждый профиль); иначе — `ERROR-JMETER-FAILED`;
    - создаёт запись о тесте: `POST /test/create` c телом `{stand, duration, server, profile}` → возвращает `testId`;
    - запрашивает `GET /test/info/{testId}`;
    - при успехе: `statusJob = RUN`, `resultJob = testId`.

---

### Завершение (`finally`)

Всегда пишется артефакт **`resultJob.txt`** с содержимым `resultJob`
(`testId` при успехе либо строка статуса ошибки).

Обработка по `statusJob`:

| `statusJob` | Действия |
|---|---|
| `RUN` | Тест успешно запущен, в лог выводится `Test id`. |
| `ERROR-SERVER-BOOKING` | Ничего откатывать не нужно (серверы не забронированы). |
| `ERROR`, `ERROR-PROFILE`, `ERROR-URL`, `ERROR-SERVER-BUSY` | Отмена брони: `POST /server/serverBooking/cancel?server=...`. |
| `ERROR-START-JMETER-SERVER`, `ERROR-START-TEST`, `ERROR-JMETER-FAILED` | Отмена брони + `stopJmeter(...)`. |
| `ERROR-CREATING-TEST` | Отмена брони + `stopJmeter(...)`. |
| `ERROR-TEST-INFO` | Удаление теста `DELETE /test/delete/{testId}` + `stopJmeter(...)`. |
| прочее | В лог: обработчик статуса не найден. |

---

### Вспомогательные функции

- **`deepConvert(obj)`** — рекурсивно преобразует `LazyMap`/`LazyList` из `JsonSlurper`
  в обычные `Map`/`List` (чтобы объекты были сериализуемыми для Pipeline).
- **`checkHttpStatus(connection, status, message)`** — если в ответе `curl` есть подстрока
  `ERROR` — падает с заданными `statusJob`/`resultJob` и сообщением; иначе пытается распарсить
  JSON и вернуть результат (или сырую строку).
- **`stopJmeter(userSSH, jmeterDir, profileCnt, serverArr, masterServer)`** — останавливает тест
  через `stoptest.sh <порт>` (порты с `4445`, по одному на профиль), ждёт завершения процессов
  (до 5 попыток по 10 сек), при неуспехе — принудительно `pkill -9 -f ApacheJMeter.jar`
  на всех генераторах.

---

### Используемые порты

| Назначение | Стартовое значение | Шаг |
|---|---|---|
| RMI-порт `jmeter-server` (`SERVER_PORT`) | `1099` | +1 на профиль |
| jmx-exporter для `jmeter` | `49200` | +1 на профиль |
| jmx-exporter для `jmeter-server` | `49250` | +1 на профиль |
| `stoptest.sh` (остановка теста) | `4445` | +1 на профиль |

</details>

---

<details>
<summary>📘 pipeline-jmeter-scenario</summary>

## pipeline-jmeter-scenario
- получает описание сценария: `GET /scenario/{SCENARIONAME}`;
- берёт из сценария общие значения (`stand`, `domain`, `duration`) и список групп (`groups`);
- при необходимости переопределяет стенд параметрами сборки (`STAND` / `STAND_CUSTOM`);
- для каждой группы вызывает дочерний job (`build job`) с параметрами группы,
  подставляя общие значения там, где в группе они не заданы;
- все группы стартуют **одновременно** (`parallel`);
- собирает артефакт `resultJob.txt` каждой дочерней сборки в общий список результатов
  и пишет его в `currentBuild.description`.

Сам по себе тесты не запускает и по SSH к генераторам не ходит — этим занимается
дочерний job (`pipeline-jmeter-run`).

---

### Требования

#### Jenkins-агент

- `curl` (с поддержкой `--fail-with-body`);
- плагины Jenkins: `Pipeline`, `SSH Agent`, `Git Parameter`, `Copy Artifact`.

---

### Настройка (блок `environment` в начале файла)

| Переменная | Назначение |
|---|---|
| `credentialSSH` | Jenkins credential с приватным SSH-ключом; используется в `sshagent`, оборачивающем стадию и `finally` |
| `gitRepository` | репозиторий с jmx-скриптами — источник веток для параметра `BRANCH` |
| `hostTestHelper` | базовый URL приложения load-test-helper |
| `checkHttpError` | суффикс shell для маркировки неуспешного `curl` |

Хранение сборок (`buildDiscarder`): по времени — 14 дней, по количеству — 30 сборок.

> Перед использованием обязательно поправьте: имя дочернего job в `childJobName`, `credentialSSH`, `gitRepository`,
> `hostTestHelper`.

---

### Параметры сборки

| Параметр | Тип | Обяз. | Описание |
|---|---|---|---|
| `SCENARIONAME` | string | да | Имя сценария в load-test-helper. Пример: `someName`. Подставляется в `GET /scenario/{...}` и в `displayName` сборки. |
| `STAND` | choice (`Null`, `LT1`, `LT2`) | нет | Переопределение стенда для всех групп. `Null` — брать стенд из сценария. |
| `STAND_CUSTOM` | string | нет | Свой вариант стенда. Имеет наивысший приоритет — перекрывает и сценарий, и `STAND`. |
| `BRANCH` | git parameter | — | Ветка репозитория с jmx-скриптами, передаётся в дочерний job. |

#### Приоритет выбора стенда

1. `STAND_CUSTOM` (если заполнен);
2. `STAND` (если не равен `Null`);
3. `scenario.stand` из описания сценария.

---

### Ход выполнения

#### Стадия `Start scenario`

1. `deleteDir()` — очистка WORKSPACE.
2. `currentBuild.displayName` = `<BUILD_ID>--<SCENARIONAME>`.
3. `GET /scenario/{SCENARIONAME}` → `checkHttpStatus` парсит JSON.
   При ошибке — `statusJob = ERROR-START-SCENARIO`, сборка падает.
4. Из сценария берутся `stand`, `domain`, `duration`, `groups`; вычисляется итоговый стенд.
5. Для каждой группы формируется замыкание с вызовом `startJMeterRun(...)`.
6. `parallel groupMap` — все группы запускаются одновременно.
7. При успешном завершении всех групп — `statusJob = SUCCESS`.

#### `startJMeterRun(...)` (для каждой группы)

- логирует параметры группы;
- `build job: childJobName` с параметрами группы, `propagate: false`, `wait: true`
  (падение дочерней сборки не роняет сценарий);
- `copyArtifacts` — забирает `resultJob.txt` из конкретной дочерней сборки
  (`SpecificBuildSelector` по её номеру);
- содержимое `resultJob.txt` добавляется в `testResultArr`.

#### Завершение (`catch` / `finally`)

- любое исключение логируется (`Failed because of {...}`);
- в `finally` по значению `statusJob`:

| `statusJob` | Действия |
|---|---|
| `SUCCESS` | Лог `Successful success!`, результат групп пишется в `currentBuild.description`. |
| прочее (`ERROR`, `ERROR-START-SCENARIO`, ...) | Лог `The end.`, результат групп (что успело собраться) пишется в `currentBuild.description`. |

Отдельного отката (отмена брони, остановка JMeter) в этом пайплайне нет — за это отвечает
дочерний job.

---

## Вспомогательные функции

- **`deepConvert(obj)`** — рекурсивно преобразует `LazyMap`/`LazyList` из `JsonSlurper`
  в обычные `Map`/`List` (чтобы объекты были сериализуемыми для Pipeline).
- **`checkHttpStatus(connection, status, message)`** — если в ответе `curl` есть подстрока
  `ERROR` — выставляет `statusJob` и падает с сообщением; иначе пытается распарсить JSON
  и вернуть результат (или сырую строку, если это не JSON).
- **`startJMeterRun(profile, server, masterRun, duration, stand, domain, certificate, customParamRun, customParamServer, branchGit)`**
  — запускает дочерний job и собирает его `resultJob.txt` в `testResultArr`.

</details>

---

<details>
<summary>📙 pipeline-jmeter-stop-test</summary>

## pipeline-jmeter-stop-test
Для каждого `TEST_ID` из списка:
- `GET /test/info/{testId}` в load-test-helper — получает данные о тесте (профили, список серверов).
- Останавливает JMeter
- `DELETE /test/delete/{testId}` в load-test-helper — удаляет тест.
- Формируется статус задания и списки проблемных тестов.
- 
---

### Требования

#### Jenkins-агент

- `curl` (с поддержкой `--fail-with-body`);
- плагины Jenkins: `Pipeline`, `SSH Agent`, `Git Parameter`, `Copy Artifact`.

---

### Параметры сборки

| Параметр  | Тип    | Описание |
|-----------|--------|----------|
| `TEST_ID` | string | Id или список id тестов через запятую. Пример: `10,20,30` |

---

## Настройка (блок `environment` в начале файла)

| Переменная       | Назначение |
|------------------|------------|
| `userSSH`        | Пользователь для подключения по SSH к серверам нагрузки |
| `credentialSSH`  | Jenkins credential (SSH-ключ) для `sshagent` |
| `jmeterDir`      | Каталог JMeter на серверах нагрузки (там лежит `stoptest.sh`) |
| `workDir`        | Временная директория запуска теста |
| `hostTestHelper` | Адрес сервиса load-test-helper |
| `checkHttpError` | Хвост для `curl`: помечает неуспешный запрос строкой `ERROR` |

> Перед использованием обязательно поправьте: `userSSH`, `credentialSSH` и `hostTestHelper`.

---

## Стадии

| Стадия      | Действия |
|-------------|----------|
| `Stop Test` | Очистка workspace, цикл по `TEST_ID`: info → обновление known_hosts → `stopJmeter` → delete |

Финальный блок `finally` печатает итог по `statusJob`.

---

## Статусы задания

| `statusJob` | Результат сборки        | Когда |
|-------------|-------------------------|-------|
| `SUCCESS`   | норм                    | Все тесты найдены, остановлены и удалены |
| `WARN`      | `UNSTABLE`              | Есть ненайденные (`testNotFoundArr`) или неудалённые (`testNotDeleteArr`) тесты |
| `ERROR`     | по умолчанию / исключение | Необработанная ошибка (лог: `Failed because of {...}`) |

В логе выводятся списки:
- `testNotFound` — id, по которым `GET /test/info` вернул ошибку;
- `testNotDelete` — id, по которым `DELETE /test/delete` вернул ошибку.

---

## Функции

### `checkHttpStatus(connection, stage, TEST_ID)`
Разбирает результат `curl`. Для `stage = 'INFO'`: при `ERROR` добавляет id в `testNotFoundArr` и возвращает `false`, 
иначе парсит JSON и возвращает `HashMap`. Для `stage = 'DELETE'`: при `ERROR` добавляет id в `testNotDeleteArr` 
и возвращает `false`, иначе `true`.

### `stopJmeter(userSSH, jmeterDir, profileCnt, serverArr, masterServer)`
Останавливает тест через `stoptest.sh` на master-сервере, ждёт завершения 
процессов JMeter (5 попыток × 10 сек), при неудаче — `pkill -9 -f ApacheJMeter.jar` на всех серверах.

</details>
