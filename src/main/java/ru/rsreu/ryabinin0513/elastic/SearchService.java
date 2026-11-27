package ru.rsreu.ryabinin0513.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonpUtils;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SearchService {

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }

    private static String dateOnly(String s) {
        if (s == null) return "";
        return s.length() > 10 ? s.substring(0, 10) : s;
    }

    public static void searchByPhrase(ElasticsearchClient client,
                                      String field,
                                      String phrase,
                                      int size) throws IOException {

        System.out.println("\nПОИСК match_phrase");
        System.out.println("Поле: " + field + " | Фраза: \"" + phrase + "\"");

        SearchRequest request = SearchRequest.of(s -> s
                .index(IndexCreator.INDEX_NAME)
                .size(size)
                .query(q -> q
                        .matchPhrase(m -> m
                                .field(field)
                                .query(phrase)
                        )
                )
        );

        // Вывод сформированного запроса в консоль
        String requestJson = JsonpUtils.toJsonString(request, new JacksonJsonpMapper());
        System.out.println("Запрос: " + requestJson);

        SearchResponse<Map> response = client.search(request, Map.class);

        List<Hit<Map>> hits = response.hits().hits();

        System.out.println("Найдено документов: " + hits.size()
                + " (всего совпадений: " + response.hits().total().value() + ")");

        if (hits.isEmpty()) {
            System.out.println("Ничего не найдено.");
            return;
        }

        System.out.println("-".repeat(112));
        System.out.printf("%-4s | %-40s | %-10s | %-18s | %-15s | %-12s%n",
                "№", "Название предмета", "Инв. №", "Отдел", "Статус", "Поступление");
        System.out.println("-".repeat(112));

        int i = 1;
        for (Hit<Map> hit : hits) {
            Map<String, Object> src = hit.source();
            System.out.printf("%-4d | %-40s | %-10s | %-18s | %-15s | %-12s%n",
                    i++,
                    truncate(String.valueOf(src.get("item_name")), 40),
                    truncate(String.valueOf(src.get("inventory_number")), 10),
                    truncate(String.valueOf(src.get("department")), 18),
                    truncate(String.valueOf(src.get("status")), 15),
                    truncate(dateOnly(String.valueOf(src.get("purchase_date"))), 12)
            );
        }
        System.out.println("-".repeat(112));
    }
}
