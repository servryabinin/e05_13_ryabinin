package ru.rsreu.ryabinin0513.constant;

/**
 * Класс строковых констант приложения.
 *
 * <p>Содержит ключи переменных окружения, значения по умолчанию, имена полей
 * документов, тексты сообщений и форматы вывода таблиц. Использование единого
 * класса исключает дублирование строковых литералов в остальном коде.</p>
 */
public final class TextConstants {

    /**
     * Приватный конструктор предотвращает создание экземпляров класса.
     */
    private TextConstants() {
    }

    /* Ключи переменных окружения. */
    public static final String ENV_ELASTIC_HOST = "ELASTIC_HOST";
    public static final String ENV_ELASTIC_PORT = "ELASTIC_PORT";
    public static final String ENV_ELASTIC_SCHEME = "ELASTIC_SCHEME";
    public static final String ENV_ELASTIC_USER = "ELASTIC_USER";
    public static final String ENV_ELASTIC_PASSWORD = "ELASTIC_PASSWORD";
    public static final String ENV_ELASTIC_INDEX = "ELASTIC_INDEX";
    public static final String ENV_CSV_FILE = "CSV_FILE";

    /* Значения настроек по умолчанию. */
    public static final String DEFAULT_ELASTIC_HOST = "localhost";
    public static final String DEFAULT_ELASTIC_PORT = "9200";
    public static final String DEFAULT_ELASTIC_SCHEME = "http";
    public static final String DEFAULT_ELASTIC_USER = "elastic";
    public static final String DEFAULT_ELASTIC_PASSWORD = "";
    public static final String DEFAULT_ELASTIC_INDEX = "e05_13_ryabinin";
    public static final String DEFAULT_CSV_FILE = "e02_13_ryabinin.csv";

    /* Имена полей документов и элементов маппинга. */
    public static final String FIELD_ITEM_NAME = "item_name";
    public static final String FIELD_INVENTORY_NUMBER = "inventory_number";
    public static final String FIELD_ROOM_NUMBER = "room_number";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_STATUS = "status";
    public static final String FIELD_PURCHASE_PRICE = "purchase_price";
    public static final String FIELD_WARRANTY_MONTHS = "warranty_months";
    public static final String FIELD_PURCHASE_DATE = "purchase_date";
    public static final String FIELD_WRITE_OFF_DATE = "write_off_date";
    public static final String FIELD_DEPARTMENT = "department";
    public static final String FIELD_CATEGORIES = "categories";
    public static final String ITEM_NAME_KEYWORD_SUBFIELD = "as_keyword";

    /* Параметры поиска и агрегации. */
    public static final String SEARCH_FIELD = FIELD_DESCRIPTION;
    public static final String SEARCH_PHRASE = "Intel Core";
    public static final String AGGREGATION_FIELD = FIELD_DEPARTMENT;
    public static final String AGGREGATION_NAME_PREFIX = "by_";

    /* Параметры CSV. */
    public static final String CSV_CATEGORY_DELIMITER = ",";
    public static final String ERROR_CSV_RESOURCE_NOT_FOUND =
            "CSV-файл не найден в resources: %s";

    /* Сообщения о работе с индексом. */
    public static final String MESSAGE_INDEX_DELETING =
            "Удаляем существующий индекс: %s";
    public static final String MESSAGE_INDEX_CREATING =
            "\nСоздаём новый индекс: %s";
    public static final String MESSAGE_INDEX_CREATED =
            "Индекс '%s' создан";

    /* Сообщения о загрузке CSV. */
    public static final String MESSAGE_CSV_READING =
            "Читаем CSV файл: %s";
    public static final String MESSAGE_CSV_DOCUMENTS_READ =
            "Прочитано документов: %d";
    public static final String MESSAGE_BULK_ERRORS_FOUND =
            "ОШИБКИ при загрузке:";
    public static final String MESSAGE_BULK_ITEM_ERROR =
            " - %s: %s";
    public static final String MESSAGE_BULK_LOAD_SUCCESS =
            "Загрузка завершена успешно. В индекс %s%nЗагружено документов: %d";

    /* Сообщения поиска. */
    public static final String MESSAGE_SEARCH_TITLE =
            "\nПОИСК match_phrase";
    public static final String MESSAGE_SEARCH_PARAMETERS =
            "Поле: %s | Фраза: \"%s\"";
    public static final String MESSAGE_REQUEST_BODY =
            "Запрос: %s";
    public static final String MESSAGE_SEARCH_RESULT_COUNT =
            "Найдено документов: %d (всего совпадений: %d)";
    public static final String MESSAGE_SEARCH_NOTHING_FOUND =
            "Ничего не найдено.";

    /* Сообщения агрегации. */
    public static final String MESSAGE_AGGREGATION_TITLE =
            "\nАГРЕГАЦИЯ РЕДКИХ ТЕРМОВ (rare_terms)";
    public static final String MESSAGE_AGGREGATION_PARAMETERS =
            "Поле: %s | max_doc_count: %d";
    public static final String MESSAGE_AGGREGATION_NOTHING_FOUND =
            "Редких термов не найдено.";
    public static final String MESSAGE_AGGREGATION_TERMS_FOUND =
            "Найдено редких термов: %d";

    /* Сообщение о завершении работы. */
    public static final String MESSAGE_CLIENT_CLOSED =
            "\nКлиент Elasticsearch закрыт. Программа завершена.";

    /* Форматирование таблиц. */
    public static final String TABLE_SEPARATOR_SEARCH = "-".repeat(112);
    public static final String TABLE_SEPARATOR_AGGREGATION = "-".repeat(60);
    public static final String SEARCH_TABLE_HEADER_FORMAT =
            "%-4s | %-40s | %-10s | %-18s | %-15s | %-12s%n";
    public static final String SEARCH_TABLE_ROW_FORMAT =
            "%-4d | %-40s | %-10s | %-18s | %-15s | %-12s%n";
    public static final String AGGREGATION_TABLE_HEADER_FORMAT =
            "%-30s | %-15s%n";
    public static final String AGGREGATION_TABLE_ROW_FORMAT =
            "%-30s | %-15d%n";
    public static final String HEADER_NUMBER = "№";
    public static final String HEADER_ITEM_NAME = "Название предмета";
    public static final String HEADER_INVENTORY_NUMBER = "Инв. №";
    public static final String HEADER_DEPARTMENT = "Отдел";
    public static final String HEADER_STATUS = "Статус";
    public static final String HEADER_PURCHASE_DATE = "Поступление";
    public static final String AGGREGATION_COUNT_HEADER = "Кол-во документов";

    /* Настройки индекса. */
    public static final String INDEX_NUMBER_OF_SHARDS = "1";
    public static final String INDEX_NUMBER_OF_REPLICAS = "0";

    /* Прочие строковые значения. */
    public static final String EMPTY_STRING = "";
    public static final String TRUNCATION_SUFFIX = "...";
    public static final String FORMAT_LINE_END = "%n";
}
