package ru.rsreu.ryabinin0513.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import ru.rsreu.ryabinin0513.util.CsvParser;

import java.util.List;
import java.util.Map;

public class CsvLoader {

    public static void loadData(ElasticsearchClient client, String csvResourceName) throws Exception {
        System.out.println("Читаем CSV файл: " + csvResourceName);
        List<Map<String, Object>> documents = CsvParser.parseCsv(csvResourceName);
        System.out.println("Прочитано документов: " + documents.size());

        BulkRequest.Builder bulkBuilder = new BulkRequest.Builder();

        for (Map<String, Object> doc : documents) {
            bulkBuilder.operations(op -> op
                    .index(idx -> idx
                            .index(IndexCreator.INDEX_NAME)
                            .document(doc)
                    )
            );
        }

        bulkBuilder.refresh(Refresh.WaitFor);

        BulkResponse response = client.bulk(bulkBuilder.build());

        if (response.errors()) {
            System.out.println("ОШИБКИ при загрузке:");
            for (BulkResponseItem item : response.items()) {
                if (item.error() != null) {
                    System.out.println(" - " + item.id() + ": " + item.error().reason());
                }
            }
        } else {
            System.out.println("Загрузка завершена успешно. В индекс " + IndexCreator.INDEX_NAME
                    + "\nЗагружено документов: " + response.items().size());
        }
    }
}
