package ru.rsreu.ryabinin0513.util;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVParserBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvParser {

    /** CSV из книги Excel использует точку с запятой в качестве разделителя. */
    private static final char SEPARATOR = ';';

    /**
     * Читает CSV из resources и возвращает список документов (Map).
     */
    public static List<Map<String, Object>> parseCsv(String resourceName) throws IOException, CsvException {
        List<Map<String, Object>> documents = new ArrayList<>();

        InputStream is = CsvParser.class.getClassLoader().getResourceAsStream(resourceName);
        if (is == null) {
            throw new IOException("CSV-файл не найден в resources: " + resourceName);
        }

        try (CSVReader reader = new CSVReaderBuilder(new InputStreamReader(is, StandardCharsets.UTF_8))
                .withCSVParser(new CSVParserBuilder().withSeparator(SEPARATOR).build())
                .withSkipLines(1) // пропускаем заголовок
                .build()) {

            List<String[]> rows = reader.readAll();
            for (String[] row : rows) {
                documents.add(rowToDocument(row));
            }
        }

        return documents;
    }

    private static Map<String, Object> rowToDocument(String[] row) {
        Map<String, Object> doc = new HashMap<>();

        doc.put("item_name",        row[0]);
        doc.put("inventory_number", parseIntSafe(row[1]));
        doc.put("room_number",      row[2]);
        doc.put("description",      row[3]);
        doc.put("status",           row[4]);
        doc.put("purchase_price",   parseIntSafe(row[5]));
        doc.put("warranty_months",  parseIntSafe(row[6]));

        // Даты могут быть пустыми: write_off_date заполняется только для списанных предметов,
        // поэтому пустое значение не отправляем в индекс (иначе Elasticsearch не разберёт дату).
        putIfNotBlank(doc, "purchase_date", row[7]);
        putIfNotBlank(doc, "write_off_date", row[8]);

        doc.put("department",       row[9]);
        doc.put("categories",       splitToArray(row[10], ","));

        return doc;
    }

    private static void putIfNotBlank(Map<String, Object> doc, String field, String value) {
        if (value != null && !value.isBlank()) {
            doc.put(field, value.trim());
        }
    }

    /** Разбивает строку по разделителю, обрезает пробелы, отбрасывает пустые элементы. */
    private static List<String> splitToArray(String value, String delimiter) {
        if (value == null || value.isBlank()) return Collections.emptyList();
        List<String> result = new ArrayList<>();
        for (String part : value.split(delimiter)) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) result.add(trimmed);
        }
        return result;
    }

    private static Integer parseIntSafe(String value) {
        if (value == null || value.isBlank()) return 0;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
