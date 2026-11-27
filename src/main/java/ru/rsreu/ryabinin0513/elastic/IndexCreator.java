package ru.rsreu.ryabinin0513.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import ru.rsreu.ryabinin0513.config.Config;
import ru.rsreu.ryabinin0513.constant.TextConstants;

import java.io.IOException;

/**
 * Создаёт индекс Elasticsearch с маппингом предметной области
 * «Инвентаризация».
 */
public final class IndexCreator {

    /** Имя индекса, загружаемое из конфигурации. */
    public static final String INDEX_NAME = Config.ELASTIC_INDEX;

    /**
     * Приватный конструктор предотвращает создание экземпляров класса.
     */
    private IndexCreator() {
    }

    /**
     * Удаляет существующий индекс и создаёт новый с актуальным маппингом.
     *
     * @param client клиент Elasticsearch
     * @throws IOException если при работе с индексом возникла ошибка
     */
    public static void recreateIndex(ElasticsearchClient client) throws IOException {
        deleteIndexIfExists(client);
        createIndex(client);
    }

    /**
     * Удаляет индекс, если он уже существует.
     *
     * @param client клиент Elasticsearch
     * @throws IOException если при проверке или удалении индекса возникла ошибка
     */
    private static void deleteIndexIfExists(ElasticsearchClient client) throws IOException {
        boolean isIndexExists = client.indices()
                .exists(request -> request.index(INDEX_NAME))
                .value();

        if (!isIndexExists) {
            return;
        }

        System.out.printf(
                TextConstants.MESSAGE_INDEX_DELETING + TextConstants.FORMAT_LINE_END,
                INDEX_NAME
        );
        client.indices().delete(request -> request.index(INDEX_NAME));
    }

    /**
     * Создаёт индекс с настройками и маппингом.
     *
     * @param client клиент Elasticsearch
     * @throws IOException если при создании индекса возникла ошибка
     */
    private static void createIndex(ElasticsearchClient client) throws IOException {
        System.out.printf(
                TextConstants.MESSAGE_INDEX_CREATING + TextConstants.FORMAT_LINE_END,
                INDEX_NAME
        );
        client.indices().create(buildCreateIndexRequest());
        System.out.printf(
                TextConstants.MESSAGE_INDEX_CREATED + TextConstants.FORMAT_LINE_END,
                INDEX_NAME
        );
    }

    /**
     * Формирует запрос на создание индекса.
     *
     * @return запрос создания индекса
     */
    private static CreateIndexRequest buildCreateIndexRequest() {
        return CreateIndexRequest.of(builder -> builder
                .index(INDEX_NAME)
                .settings(buildIndexSettings())
                .mappings(buildIndexMappings())
        );
    }

    /**
     * Формирует настройки индекса.
     *
     * @return настройки индекса
     */
    private static IndexSettings buildIndexSettings() {
        return IndexSettings.of(settings -> settings
                .numberOfShards(TextConstants.INDEX_NUMBER_OF_SHARDS)
                .numberOfReplicas(TextConstants.INDEX_NUMBER_OF_REPLICAS)
        );
    }

    /**
     * Формирует маппинг полей документов.
     *
     * @return маппинг индекса
     */
    private static TypeMapping buildIndexMappings() {
        return TypeMapping.of(mapping -> mapping
                .properties(TextConstants.FIELD_ITEM_NAME, property -> property
                        .text(text -> text
                                .fields(TextConstants.ITEM_NAME_KEYWORD_SUBFIELD,
                                        field -> field.keyword(keyword -> keyword))
                        )
                )
                .properties(TextConstants.FIELD_INVENTORY_NUMBER, property -> property.integer(integer -> integer))
                .properties(TextConstants.FIELD_ROOM_NUMBER, property -> property.keyword(keyword -> keyword))
                .properties(TextConstants.FIELD_DESCRIPTION, property -> property
                        .text(text -> text.fielddata(true))
                )
                .properties(TextConstants.FIELD_STATUS, property -> property.keyword(keyword -> keyword))
                .properties(TextConstants.FIELD_PURCHASE_PRICE, property -> property.integer(integer -> integer))
                .properties(TextConstants.FIELD_WARRANTY_MONTHS, property -> property.integer(integer -> integer))
                .properties(TextConstants.FIELD_PURCHASE_DATE, property -> property.date(date -> date))
                .properties(TextConstants.FIELD_WRITE_OFF_DATE, property -> property.date(date -> date))
                .properties(TextConstants.FIELD_DEPARTMENT, property -> property.keyword(keyword -> keyword))
                .properties(TextConstants.FIELD_CATEGORIES, property -> property.keyword(keyword -> keyword))
        );
    }
}
