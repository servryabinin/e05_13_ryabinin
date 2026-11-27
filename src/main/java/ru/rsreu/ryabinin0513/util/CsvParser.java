package ru.rsreu.ryabinin0513.util;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import ru.rsreu.ryabinin0513.constant.TextConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Читает CSV-файл предметной области и преобразует его строки в документы
 * Elasticsearch.
 */
public final class CsvParser {

    /** Разделитель столбцов в CSV-файле. */
    private static final char CSV_SEPARATOR = ';';

    /* Индексы столбцов CSV-файла. */
    private static final int COLUMN_ITEM_NAME = 0;
    private static final int COLUMN_INVENTORY_NUMBER = 1;
    private static final int COLUMN_ROOM_NUMBER = 2;
    private static final int COLUMN_DESCRIPTION = 3;
    private static final int COLUMN_STATUS = 4;
    private static final int COLUMN_PURCHASE_PRICE = 5;
    private static final int COLUMN_WARRANTY_MONTHS = 6;
    private static final int COLUMN_PURCHASE_DATE = 7;
    private static final int COLUMN_WRITE_OFF_DATE = 8;
    private static final int COLUMN_DEPARTMENT = 9;
    private static final int COLUMN_CATEGORIES = 10;

    /**
     * Приватный конструктор предотвращает создание экземпляров класса.
     */
    private CsvParser() {
    }

    /**
     * Читает CSV-файл из ресурсов и возвращает список документов.
     *
     * @param resourceName имя CSV-файла в classpath
     * @return список документов в виде пар «поле — значение»
     * @throws IOException  если файл не найден или не может быть прочитан
     * @throws CsvException если CSV-файл имеет некорректный формат
     */
    public static List<Map<String, Object>> parseDocumentsFromCsv(String resourceName)
            throws IOException, CsvException {
        InputStream inputStream = openCsvResource(resourceName);

        try (CSVReader reader = createCsvReader(inputStream)) {
            return convertRowsToDocuments(reader.readAll());
        }
    }

    /**
     * Открывает CSV-файл из ресурсов classpath.
     *
     * @param resourceName имя файла в classpath
     * @return поток чтения файла
     * @throws IOException если файл не найден в ресурсах
     */
    private static InputStream openCsvResource(String resourceName) throws IOException {
        InputStream inputStream = CsvParser.class
                .getClassLoader()
                .getResourceAsStream(resourceName);

        if (inputStream == null) {
            throw new IOException(String.format(
                    TextConstants.ERROR_CSV_RESOURCE_NOT_FOUND, resourceName));
        }

        return inputStream;
    }

    /**
     * Создаёт CSV-читатель с разделителем «;» и пропуском строки заголовков.
     *
     * @param inputStream поток чтения CSV-файла
     * @return настроенный CSV-читатель
     */
    private static CSVReader createCsvReader(InputStream inputStream) {
        CSVParser openCsvParser = new CSVParserBuilder()
                .withSeparator(CSV_SEPARATOR)
                .build();

        return new CSVReaderBuilder(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .withCSVParser(openCsvParser)
                .withSkipLines(1)
                .build();
    }

    /**
     * Преобразует все строки CSV-файла в документы.
     *
     * @param rows строки CSV-файла без заголовка
     * @return список документов
     */
    private static List<Map<String, Object>> convertRowsToDocuments(List<String[]> rows) {
        List<Map<String, Object>> documents = new ArrayList<>();

        for (String[] row : rows) {
            documents.add(convertRowToDocument(row));
        }

        return documents;
    }

    /**
     * Преобразует одну строку CSV-файла в документ Elasticsearch.
     *
     * @param row строка CSV-файла
     * @return документ в виде пары «поле — значение»
     */
    private static Map<String, Object> convertRowToDocument(String[] row) {
        Map<String, Object> document = new HashMap<>();

        document.put(TextConstants.FIELD_ITEM_NAME, row[COLUMN_ITEM_NAME]);
        document.put(TextConstants.FIELD_INVENTORY_NUMBER,
                parseIntegerSafely(row[COLUMN_INVENTORY_NUMBER]));
        document.put(TextConstants.FIELD_ROOM_NUMBER, row[COLUMN_ROOM_NUMBER]);
        document.put(TextConstants.FIELD_DESCRIPTION, row[COLUMN_DESCRIPTION]);
        document.put(TextConstants.FIELD_STATUS, row[COLUMN_STATUS]);
        document.put(TextConstants.FIELD_PURCHASE_PRICE,
                parseIntegerSafely(row[COLUMN_PURCHASE_PRICE]));
        document.put(TextConstants.FIELD_WARRANTY_MONTHS,
                parseIntegerSafely(row[COLUMN_WARRANTY_MONTHS]));

        // Даты могут быть пустыми: write_off_date заполняется только для списанных
        // предметов, поэтому пустое значение не отправляется в индекс.
        putStringIfNotBlank(document, TextConstants.FIELD_PURCHASE_DATE,
                row[COLUMN_PURCHASE_DATE]);
        putStringIfNotBlank(document, TextConstants.FIELD_WRITE_OFF_DATE,
                row[COLUMN_WRITE_OFF_DATE]);

        document.put(TextConstants.FIELD_DEPARTMENT, row[COLUMN_DEPARTMENT]);
        document.put(TextConstants.FIELD_CATEGORIES,
                splitStringToArray(row[COLUMN_CATEGORIES],
                        TextConstants.CSV_CATEGORY_DELIMITER));

        return document;
    }

    /**
     * Добавляет строковое поле в документ, если его значение не пустое.
     *
     * @param document  документ, в который добавляется поле
     * @param fieldName имя поля
     * @param value     значение поля
     */
    private static void putStringIfNotBlank(Map<String, Object> document,
                                            String fieldName,
                                            String value) {
        boolean isValuePresent = value != null && !value.isBlank();

        if (isValuePresent) {
            document.put(fieldName, value.trim());
        }
    }

    /**
     * Разбивает строку по разделителю, удаляет пробелы и пустые элементы.
     *
     * @param value     исходная строка
     * @param delimiter разделитель
     * @return список непустых значений
     */
    private static List<String> splitStringToArray(String value, String delimiter) {
        boolean isValueBlank = value == null || value.isBlank();

        if (isValueBlank) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();

        for (String part : value.split(delimiter)) {
            String trimmedPart = part.trim();
            boolean isPartNotEmpty = !trimmedPart.isEmpty();

            if (isPartNotEmpty) {
                result.add(trimmedPart);
            }
        }

        return result;
    }

    /**
     * Преобразует строку в целое число без выбрасывания исключения.
     *
     * @param value строковое значение
     * @return целое число или {@code 0}, если значение пустое или некорректное
     */
    private static int parseIntegerSafely(String value) {
        boolean isValueBlank = value == null || value.isBlank();

        if (isValueBlank) {
            return 0;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
