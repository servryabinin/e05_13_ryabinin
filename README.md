# e05_13_ryabinin

Практическая работа **19.2.8.1 «Поиск и агрегация в программе на языке Java»**.

- Вариант: **13**
- Предметная область: **«Инвентаризация»** (учёт материальных ценностей учреждения)
- Поисковый запрос (таблица 19.2): **`match_phrase`**
- Агрегация (таблица 19.3): **агрегация редких терминов (`rare_terms`)**
- Индекс: **`e05_13_ryabinin`** (шаблон `exx_yy_zzzz`)
- Исходные данные: **`src/main/resources/e02_13_ryabinin.csv`**

## Что делает программа

1. Подключается к Elasticsearch (параметры подключения — в `.env`).
2. Удаляет существующий индекс `e05_13_ryabinin` (если он есть) и создаёт его заново
   с маппингом предметной области.
3. Читает CSV-файл `e02_13_ryabinin.csv` (разделитель `;`) и загружает документы
   в индекс через Bulk API.
4. Выполняет поиск `match_phrase` по полю `description` (фраза `Intel Core`)
   и выводит результаты в виде таблицы.
5. Выполняет агрегацию `rare_terms` по полю `department`
   (`max_doc_count = 2`) и выводит редкие отделы в виде таблицы.

## Структура проекта

```
src/main/java/ru/rsreu/ryabinin0513/
├── Main.java
├── config/Config.java
├── constant/TextConstants.java
├── elastic/
│   ├── AggregationService.java
│   ├── CsvLoader.java
│   ├── ElasticsearchClientFactory.java
│   ├── IndexCreator.java
│   └── SearchService.java
└── util/CsvParser.java
src/main/resources/e02_13_ryabinin.csv
elastic-project/          # конфигурация Elasticsearch + Kibana + Logstash
pom.xml
.env
```

## Маппинг индекса

| Поле | Тип Elasticsearch |
| :--- | :--- |
| `item_name` | `text` + мультиполе `as_keyword` (`keyword`) |
| `inventory_number` | `integer` |
| `room_number` | `keyword` |
| `description` | `text`, `fielddata: true` |
| `status` | `keyword` |
| `purchase_price` | `integer` |
| `warranty_months` | `integer` |
| `purchase_date` | `date` |
| `write_off_date` | `date` |
| `department` | `keyword` |
| `categories` | `keyword` (массив) |

## Запуск

1. Поднять Elasticsearch (и Kibana/Logstash при необходимости):

   ```bash
   cd elastic-project
   docker compose up -d
   ```

2. Собрать проект:

   ```bash
   mvn clean package
   ```

3. Запустить программу:

   ```bash
   mvn exec:java
   ```

   либо запустить класс `ru.rsreu.ryabinin0513.Main` из IntelliJ IDEA.

## Параметры подключения (`.env`)

```
ELASTIC_HOST=localhost
ELASTIC_PORT=9200
ELASTIC_SCHEME=http
ELASTIC_USER=elastic
ELASTIC_PASSWORD=yellow206
ELASTIC_INDEX=e05_13_ryabinin
CSV_FILE=e02_13_ryabinin.csv
```

> В `elastic-project/docker-compose.yml` безопасность Elasticsearch отключена
> (`xpack.security.enabled=false`), поэтому логин и пароль на работу не влияют.

## Пример вывода

```
ПОИСК match_phrase
Поле: description | Фраза: "Intel Core"
Запрос: {"query":{"match_phrase":{"description":{"query":"Intel Core"}}},"size":10}
Найдено документов: 2 (всего совпадений: 2)
----------------------------------------------------------------------------------------------------------------
№    | Название предмета                        | Инв. №     | Отдел              | Статус          | Поступление
----------------------------------------------------------------------------------------------------------------
1    | Ноутбук Lenovo ThinkPad                  | 100001     | Бухгалтерия        | В работе        | 2024-03-15
2    | Системный блок HP ProDesk                | 100006     | Бухгалтерия        | В работе        | 2024-01-20
----------------------------------------------------------------------------------------------------------------

АГРЕГАЦИЯ РЕДКИХ ТЕРМОВ (rare_terms)
Поле: department | max_doc_count: 2
...
Найдено редких термов: 6
------------------------------------------------------------
department                     | Кол-во документов
------------------------------------------------------------
Бухгалтерия                    | 2
Канцелярия                     | 1
...
------------------------------------------------------------
```
