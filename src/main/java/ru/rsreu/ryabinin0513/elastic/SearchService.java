package ru.rsreu.ryabinin0513.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonpUtils;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import ru.rsreu.ryabinin0513.constant.TextConstants;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Выполняет поиск документов по точной фразе с помощью запроса
 * {@code match_phrase} и выводит результаты в таблицу.
 */
public final class SearchService {

    private static final int ITEM_NAME_MAX_LENGTH = 40;
    private static final int INVENTORY_NUMBER_MAX_LENGTH = 10;
    private static final int DEPARTMENT_MAX_LENGTH = 18;
    private static final int STATUS_MAX_LENGTH = 15;
    private static final int PURCHASE_DATE_MAX_LENGTH = 12;
    private static final int DATE_PART_LENGTH = 10;

    /**
     * Приватный конструктор предотвращает создание экземпляров класса.
     */
    private SearchService() {
    }

    /**
     * Ищет документы, содержащие заданную фразу в указанном поле.
     *
     * @param client      клиент Elasticsearch
     * @param fieldName   имя поля для поиска
     * @param phrase      искомая фраза
     * @param resultSize  максимальное количество возвращаемых документов
     * @throws IOException если при выполнении поиска возникла ошибка
     */
    public static void searchDocumentsByPhrase(ElasticsearchClient client,
                                               String fieldName,
                                               String phrase,
                                               int resultSize) throws IOException {
        SearchRequest request = buildPhraseSearchRequest(fieldName, phrase, resultSize);

        printSearchParameters(fieldName, phrase, request);

        SearchResponse<Map> response = client.search(request, Map.class);

        printSearchResponse(response);
    }

    /**
     * Формирует поисковый запрос {@code match_phrase}.
     *
     * @param fieldName  имя поля для поиска
     * @param phrase     искомая фраза
     * @param resultSize максимальное количество возвращаемых документов
     * @return поисковый запрос
     */
    private static SearchRequest buildPhraseSearchRequest(String fieldName,
                                                          String phrase,
                                                          int resultSize) {
        return SearchRequest.of(request -> request
                .index(IndexCreator.INDEX_NAME)
                .size(resultSize)
                .query(query -> query
                        .matchPhrase(matchPhrase -> matchPhrase
                                .field(fieldName)
                                .query(phrase)
                        )
                )
        );
    }

    /**
     * Выводит параметры поиска и тело запроса в формате JSON.
     *
     * @param fieldName имя поля для поиска
     * @param phrase    искомая фраза
     * @param request   поисковый запрос
     */
    private static void printSearchParameters(String fieldName,
                                              String phrase,
                                              SearchRequest request) {
        System.out.println(TextConstants.MESSAGE_SEARCH_TITLE);
        System.out.printf(
                TextConstants.MESSAGE_SEARCH_PARAMETERS + TextConstants.FORMAT_LINE_END,
                fieldName,
                phrase
        );

        String requestJson = JsonpUtils.toJsonString(request, new JacksonJsonpMapper());
        System.out.printf(
                TextConstants.MESSAGE_REQUEST_BODY + TextConstants.FORMAT_LINE_END,
                requestJson
        );
    }

    /**
     * Выводит количество найденных документов и таблицу результатов.
     *
     * @param response ответ Elasticsearch на поисковый запрос
     */
    private static void printSearchResponse(SearchResponse<Map> response) {
        List<Hit<Map>> hits = response.hits().hits();
        long totalHits = response.hits().total().value();

        System.out.printf(
                TextConstants.MESSAGE_SEARCH_RESULT_COUNT + TextConstants.FORMAT_LINE_END,
                hits.size(),
                totalHits
        );

        boolean isHitsEmpty = hits.isEmpty();

        if (isHitsEmpty) {
            System.out.println(TextConstants.MESSAGE_SEARCH_NOTHING_FOUND);
            return;
        }

        printSearchTable(hits);
    }

    /**
     * Выводит таблицу результатов поиска.
     *
     * @param hits найденные документы
     */
    private static void printSearchTable(List<Hit<Map>> hits) {
        printSearchTableHeader();

        int rowNumber = 1;

        for (Hit<Map> hit : hits) {
            printSearchTableRow(rowNumber, hit.source());
            rowNumber++;
        }

        System.out.println(TextConstants.TABLE_SEPARATOR_SEARCH);
    }

    /**
     * Выводит заголовок таблицы результатов поиска.
     */
    private static void printSearchTableHeader() {
        System.out.println(TextConstants.TABLE_SEPARATOR_SEARCH);
        System.out.printf(TextConstants.SEARCH_TABLE_HEADER_FORMAT,
                TextConstants.HEADER_NUMBER,
                TextConstants.HEADER_ITEM_NAME,
                TextConstants.HEADER_INVENTORY_NUMBER,
                TextConstants.HEADER_DEPARTMENT,
                TextConstants.HEADER_STATUS,
                TextConstants.HEADER_PURCHASE_DATE);
        System.out.println(TextConstants.TABLE_SEPARATOR_SEARCH);
    }

    /**
     * Выводит одну строку таблицы результатов поиска.
     *
     * @param rowNumber номер строки
     * @param source    поля найденного документа
     */
    private static void printSearchTableRow(int rowNumber, Map<String, Object> source) {
        String itemName = Objects.toString(
                source.get(TextConstants.FIELD_ITEM_NAME), TextConstants.EMPTY_STRING);
        String inventoryNumber = Objects.toString(
                source.get(TextConstants.FIELD_INVENTORY_NUMBER), TextConstants.EMPTY_STRING);
        String department = Objects.toString(
                source.get(TextConstants.FIELD_DEPARTMENT), TextConstants.EMPTY_STRING);
        String status = Objects.toString(
                source.get(TextConstants.FIELD_STATUS), TextConstants.EMPTY_STRING);
        String purchaseDate = Objects.toString(
                source.get(TextConstants.FIELD_PURCHASE_DATE), TextConstants.EMPTY_STRING);

        System.out.printf(TextConstants.SEARCH_TABLE_ROW_FORMAT,
                rowNumber,
                truncateText(itemName, ITEM_NAME_MAX_LENGTH),
                truncateText(inventoryNumber, INVENTORY_NUMBER_MAX_LENGTH),
                truncateText(department, DEPARTMENT_MAX_LENGTH),
                truncateText(status, STATUS_MAX_LENGTH),
                truncateText(extractDatePart(purchaseDate), PURCHASE_DATE_MAX_LENGTH));
    }

    /**
     * Обрезает текст до заданной длины и добавляет многоточие.
     *
     * @param text      исходный текст
     * @param maxLength максимальная длина результата
     * @return обрезанный текст
     */
    private static String truncateText(String text, int maxLength) {
        boolean isTextNull = text == null;

        if (isTextNull) {
            return TextConstants.EMPTY_STRING;
        }

        boolean isTextShortEnough = text.length() <= maxLength;

        if (isTextShortEnough) {
            return text;
        }

        int suffixLength = TextConstants.TRUNCATION_SUFFIX.length();
        return text.substring(0, maxLength - suffixLength) + TextConstants.TRUNCATION_SUFFIX;
    }

    /**
     * Извлекает из даты и времени только часть с датой.
     *
     * @param dateTime дата и время в формате ISO
     * @return дата в формате {@code yyyy-MM-dd}
     */
    private static String extractDatePart(String dateTime) {
        boolean isDateNull = dateTime == null;

        if (isDateNull) {
            return TextConstants.EMPTY_STRING;
        }

        boolean isDateLongEnough = dateTime.length() > DATE_PART_LENGTH;

        return isDateLongEnough
                ? dateTime.substring(0, DATE_PART_LENGTH)
                : dateTime;
    }
}
