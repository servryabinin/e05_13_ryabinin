package ru.rsreu.ryabinin0513.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringRareTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonpUtils;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class AggregationService {

    public static void rareTermsByField(ElasticsearchClient client,
                                        String field,
                                        long maxDocCount) throws IOException {
        String aggregationName = "by_".concat(field);
        System.out.println("\nАГРЕГАЦИЯ РЕДКИХ ТЕРМОВ (rare_terms)");
        System.out.println("Поле: " + field + " | max_doc_count: " + maxDocCount);

        SearchRequest request = SearchRequest.of(s -> s
                .index(IndexCreator.INDEX_NAME)
                .size(0)
                .aggregations(aggregationName, a -> a
                        .rareTerms(r -> r
                                .field(field)
                                .maxDocCount(maxDocCount)
                        )
                )
        );

        String requestJson = JsonpUtils.toJsonString(request, new JacksonJsonpMapper());
        System.out.println("Запрос: " + requestJson);

        SearchResponse<Map> response = client.search(request, Map.class);

        Aggregate agg = response.aggregations().get(aggregationName);

        if (agg == null) {
            System.out.println("Редких термов не найдено.");
            return;
        }

        List<StringRareTermsBucket> buckets = agg.srareterms().buckets().array();

        if (buckets.isEmpty()) {
            System.out.println("Редких термов не найдено.");
            return;
        }

        System.out.println("Найдено редких термов: " + buckets.size());
        System.out.println("-".repeat(60));
        System.out.printf("%-30s | %-15s%n", field, "Кол-во документов");
        System.out.println("-".repeat(60));

        for (StringRareTermsBucket bucket : buckets) {
            System.out.printf("%-30s | %-15d%n", bucket.key(), bucket.docCount());
        }
        System.out.println("-".repeat(60));
    }
}
