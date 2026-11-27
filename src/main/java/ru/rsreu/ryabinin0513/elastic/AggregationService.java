package ru.rsreu.ryabinin0513.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringRareTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonpUtils;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import ru.rsreu.ryabinin0513.constant.TextConstants;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Выполняет агрегацию редких терминов с помощью {@code rare_terms}
 * и выводит результат в таблицу.
 */
public final class AggregationService {

    /**
     * Приватный конструктор предотвращает создание экземпляров класса.
     */
    private AggregationService() {
    }

    /**
     * Агрегирует документы по указанному полю и возвращает редкие термины.
     *
     * @param client           клиент Elasticsearch
     * @param fieldName        имя поля для агрегации
     * @param maxDocumentCount максимальное количество документов, при котором
     *                         термин считается редким
     * @throws IOException если при выполнении агрегации возникла ошибка
     */
    public static void aggregateRareTermsByField(ElasticsearchClient client,
                                                 String fieldName,
                                                 long maxDocumentCount) throws IOException {
        SearchRequest request = buildRareTermsSearchRequest(fieldName, maxDocumentCount);

        printAggregationParameters(fieldName, maxDocumentCount, request);

        SearchResponse<Map> response = client.search(request, Map.class);

        printAggregationResponse(response, fieldName);
    }

    /**
     * Формирует запрос агрегации {@code rare_terms}.
     *
     * @param fieldName        имя поля для агрегации
     * @param maxDocumentCount максимальное количество документов для редкого термина
     * @return поисковый запрос с агрегацией
     */
    private static SearchRequest buildRareTermsSearchRequest(String fieldName,
                                                             long maxDocumentCount) {
        String aggregationName = buildAggregationName(fieldName);

        return SearchRequest.of(request -> request
                .index(IndexCreator.INDEX_NAME)
                .size(0)
                .aggregations(aggregationName, aggregation -> aggregation
                        .rareTerms(rareTerms -> rareTerms
                                .field(fieldName)
                                .maxDocCount(maxDocumentCount)
                        )
                )
        );
    }

    /**
     * Формирует имя агрегации по имени поля.
     *
     * @param fieldName имя поля
     * @return имя агрегации
     */
    private static String buildAggregationName(String fieldName) {
        return TextConstants.AGGREGATION_NAME_PREFIX + fieldName;
    }

    /**
     * Выводит параметры агрегации и тело запроса в формате JSON.
     *
     * @param fieldName        имя поля для агрегации
     * @param maxDocumentCount максимальное количество документов для редкого термина
     * @param request          запрос с агрегацией
     */
    private static void printAggregationParameters(String fieldName,
                                                   long maxDocumentCount,
                                                   SearchRequest request) {
        System.out.println(TextConstants.MESSAGE_AGGREGATION_TITLE);
        System.out.printf(
                TextConstants.MESSAGE_AGGREGATION_PARAMETERS + TextConstants.FORMAT_LINE_END,
                fieldName,
                maxDocumentCount
        );

        String requestJson = JsonpUtils.toJsonString(request, new JacksonJsonpMapper());
        System.out.printf(
                TextConstants.MESSAGE_REQUEST_BODY + TextConstants.FORMAT_LINE_END,
                requestJson
        );
    }

    /**
     * Выводит результат агрегации или сообщение об отсутствии редких терминов.
     *
     * @param response  ответ Elasticsearch на запрос агрегации
     * @param fieldName имя поля агрегации
     */
    private static void printAggregationResponse(SearchResponse<Map> response, String fieldName) {
        String aggregationName = buildAggregationName(fieldName);
        Aggregate aggregate = response.aggregations().get(aggregationName);

        boolean isAggregateMissing = aggregate == null;

        if (isAggregateMissing) {
            printAggregationNotFound();
            return;
        }

        List<StringRareTermsBucket> buckets = aggregate.srareterms().buckets().array();

        boolean isBucketsEmpty = buckets.isEmpty();

        if (isBucketsEmpty) {
            printAggregationNotFound();
            return;
        }

        System.out.printf(
                TextConstants.MESSAGE_AGGREGATION_TERMS_FOUND + TextConstants.FORMAT_LINE_END,
                buckets.size()
        );

        printAggregationTable(fieldName, buckets);
    }

    /**
     * Выводит сообщение об отсутствии редких терминов.
     */
    private static void printAggregationNotFound() {
        System.out.println(TextConstants.MESSAGE_AGGREGATION_NOTHING_FOUND);
    }

    /**
     * Выводит таблицу редких терминов.
     *
     * @param fieldName имя поля агрегации
     * @param buckets   найденные редкие термины
     */
    private static void printAggregationTable(String fieldName,
                                              List<StringRareTermsBucket> buckets) {
        System.out.println(TextConstants.TABLE_SEPARATOR_AGGREGATION);
        System.out.printf(TextConstants.AGGREGATION_TABLE_HEADER_FORMAT,
                fieldName, TextConstants.AGGREGATION_COUNT_HEADER);
        System.out.println(TextConstants.TABLE_SEPARATOR_AGGREGATION);

        for (StringRareTermsBucket bucket : buckets) {
            System.out.printf(TextConstants.AGGREGATION_TABLE_ROW_FORMAT,
                    bucket.key(), bucket.docCount());
        }

        System.out.println(TextConstants.TABLE_SEPARATOR_AGGREGATION);
    }
}
