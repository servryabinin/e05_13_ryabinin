package ru.rsreu.ryabinin0513.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import ru.rsreu.ryabinin0513.config.Config;

import java.io.IOException;

public class IndexCreator {

    public static final String INDEX_NAME = Config.ELASTIC_INDEX;

    public static void createIndexIfNotExists(ElasticsearchClient client) throws IOException {
        boolean exists = client.indices().exists(e -> e.index(INDEX_NAME)).value();

        if (exists) {
            System.out.println("Удаляем существующий индекс: " + INDEX_NAME);
            client.indices().delete(d -> d.index(INDEX_NAME));
        }

        System.out.println("\nСоздаём новый индекс: " + INDEX_NAME);

        CreateIndexRequest request = CreateIndexRequest.of(b -> b
                .index(INDEX_NAME)
                .settings(IndexSettings.of(s -> s
                        .numberOfShards("1")
                        .numberOfReplicas("0")
                ))
                .mappings(m -> m
                        .properties("item_name", p -> p
                                .text(t -> t
                                        .fields("as_keyword", f -> f.keyword(k -> k))
                                ))
                        .properties("inventory_number", p -> p.integer(i -> i))
                        .properties("room_number", p -> p.keyword(k -> k))
                        .properties("description", p -> p.text(t -> t.fielddata(true)))
                        .properties("status", p -> p.keyword(k -> k))
                        .properties("purchase_price", p -> p.integer(i -> i))
                        .properties("warranty_months", p -> p.integer(i -> i))
                        .properties("purchase_date", p -> p.date(d -> d))
                        .properties("write_off_date", p -> p.date(d -> d))
                        .properties("department", p -> p.keyword(k -> k))
                        .properties("categories", p -> p.keyword(k -> k))
                )
        );

        client.indices().create(request);
        System.out.println("Индекс '" + INDEX_NAME + "' создан");
    }
}
