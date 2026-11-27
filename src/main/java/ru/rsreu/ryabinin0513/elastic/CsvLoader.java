package ru.rsreu.ryabinin0513.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Refresh;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import com.opencsv.exceptions.CsvException;
import ru.rsreu.ryabinin0513.constant.TextConstants;
import ru.rsreu.ryabinin0513.util.CsvParser;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Загружает документы из CSV-файла в индекс Elasticsearch.
 */
public final class CsvLoader {

    /**
     * Приватный конструктор предотвращает создание экземпляров класса.
     */
    private CsvLoader() {
    }

    /**
     * Читает CSV-файл и загружает его документы в индекс Elasticsearch.
     *
     * @param client          клиент Elasticsearch
     * @param csvResourceName имя CSV-файла в classpath
     * @throws IOException  если при чтении CSV или загрузке документов возникла ошибка
     * @throws CsvException если CSV-файл имеет некорректный формат
     */
    public static void loadDocumentsFromCsv(ElasticsearchClient client, String csvResourceName)
            throws IOException, CsvException {
        List<Map<String, Object>> documents = readDocuments(csvResourceName);
        BulkResponse response = executeBulkRequest(client, documents);

        printBulkResult(response);
    }

    /**
     * Читает документы из CSV-файла и выводит информацию о чтении.
     *
     * @param csvResourceName имя CSV-файла в classpath
     * @return список прочитанных документов
     * @throws IOException  если при чтении файла возникла ошибка
     * @throws CsvException если CSV-файл имеет некорректный формат
     */
    private static List<Map<String, Object>> readDocuments(String csvResourceName)
            throws IOException, CsvException {
        System.out.printf(
                TextConstants.MESSAGE_CSV_READING + TextConstants.FORMAT_LINE_END,
                csvResourceName
        );

        List<Map<String, Object>> documents = CsvParser.parseDocumentsFromCsv(csvResourceName);

        System.out.printf(
                TextConstants.MESSAGE_CSV_DOCUMENTS_READ + TextConstants.FORMAT_LINE_END,
                documents.size()
        );

        return documents;
    }

    /**
     * Формирует и выполняет Bulk-запрос для загрузки документов.
     *
     * @param client    клиент Elasticsearch
     * @param documents список документов
     * @return ответ Elasticsearch на Bulk-запрос
     * @throws IOException если при выполнении запроса возникла ошибка
     */
    private static BulkResponse executeBulkRequest(ElasticsearchClient client,
                                                   List<Map<String, Object>> documents)
            throws IOException {
        BulkRequest bulkRequest = buildBulkRequest(documents);
        return client.bulk(bulkRequest);
    }

    /**
     * Формирует Bulk-запрос из списка документов.
     *
     * @param documents список документов
     * @return Bulk-запрос
     */
    private static BulkRequest buildBulkRequest(List<Map<String, Object>> documents) {
        BulkRequest.Builder builder = new BulkRequest.Builder();

        for (Map<String, Object> document : documents) {
            builder.operations(operation -> operation
                    .index(index -> index
                            .index(IndexCreator.INDEX_NAME)
                            .document(document)
                    )
            );
        }

        builder.refresh(Refresh.WaitFor);

        return builder.build();
    }

    /**
     * Выводит результат загрузки документов.
     *
     * @param response ответ Elasticsearch на Bulk-запрос
     */
    private static void printBulkResult(BulkResponse response) {
        boolean isErrorsPresent = response.errors();

        if (isErrorsPresent) {
            System.out.println(TextConstants.MESSAGE_BULK_ERRORS_FOUND);
            printBulkErrors(response);
            return;
        }

        System.out.printf(
                TextConstants.MESSAGE_BULK_LOAD_SUCCESS + TextConstants.FORMAT_LINE_END,
                IndexCreator.INDEX_NAME,
                response.items().size()
        );
    }

    /**
     * Выводит ошибки, возникшие при загрузке отдельных документов.
     *
     * @param response ответ Elasticsearch на Bulk-запрос
     */
    private static void printBulkErrors(BulkResponse response) {
        for (BulkResponseItem item : response.items()) {
            boolean isItemErrorPresent = item.error() != null;

            if (isItemErrorPresent) {
                System.out.printf(
                        TextConstants.MESSAGE_BULK_ITEM_ERROR + TextConstants.FORMAT_LINE_END,
                        item.id(),
                        item.error().reason()
                );
            }
        }
    }
}
