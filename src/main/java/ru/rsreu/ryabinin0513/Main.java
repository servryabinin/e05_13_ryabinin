package ru.rsreu.ryabinin0513;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.opencsv.exceptions.CsvException;
import ru.rsreu.ryabinin0513.config.Config;
import ru.rsreu.ryabinin0513.constant.TextConstants;
import ru.rsreu.ryabinin0513.elastic.AggregationService;
import ru.rsreu.ryabinin0513.elastic.CsvLoader;
import ru.rsreu.ryabinin0513.elastic.ElasticsearchClientFactory;
import ru.rsreu.ryabinin0513.elastic.IndexCreator;
import ru.rsreu.ryabinin0513.elastic.SearchService;

import java.io.IOException;

/**
 * Точка входа демонстрационной программы практической работы 19.2.8.1.
 *
 * <p>Программа создаёт индекс Elasticsearch, загружает данные из CSV-файла,
 * выполняет поиск по фразе {@code match_phrase} и агрегацию редких терминов
 * {@code rare_terms} для предметной области «Инвентаризация».</p>
 */
public class Main {

    /** Максимальное количество документов, выводимых в результатах поиска. */
    private static final int SEARCH_RESULT_SIZE = 10;

    /** Максимальное количество документов для редкого термина. */
    private static final long RARE_TERMS_MAX_DOCUMENT_COUNT = 2;

    /**
     * Запускает программу.
     *
     * @param args аргументы командной строки (не используются)
     * @throws Exception если при выполнении программы возникла ошибка
     */
    public static void main(String[] args) throws Exception {
        runApplication();
    }

    /**
     * Выполняет полный сценарий работы программы.
     *
     * @throws IOException  если при работе с Elasticsearch или CSV возникла ошибка
     * @throws CsvException если CSV-файл имеет некорректный формат
     */
    private static void runApplication() throws IOException, CsvException {
        try (var clients = ElasticsearchClientFactory.createElasticsearchClients()) {
            ElasticsearchClient client = clients.getClient();

            prepareIndexAndLoadDocuments(client);
            executeDemonstrationQueries(client);
        }

        System.out.println(TextConstants.MESSAGE_CLIENT_CLOSED);
    }

    /**
     * Создаёт индекс и загружает в него документы из CSV-файла.
     *
     * @param client клиент Elasticsearch
     * @throws IOException  если при работе с Elasticsearch или CSV возникла ошибка
     * @throws CsvException если CSV-файл имеет некорректный формат
     */
    private static void prepareIndexAndLoadDocuments(ElasticsearchClient client)
            throws IOException, CsvException {
        IndexCreator.recreateIndex(client);
        CsvLoader.loadDocumentsFromCsv(client, Config.CSV_FILE);
    }

    /**
     * Выполняет демонстрационные запросы поиска и агрегации.
     *
     * @param client клиент Elasticsearch
     * @throws IOException если при выполнении запросов возникла ошибка
     */
    private static void executeDemonstrationQueries(ElasticsearchClient client)
            throws IOException {
        SearchService.searchDocumentsByPhrase(
                client,
                TextConstants.SEARCH_FIELD,
                TextConstants.SEARCH_PHRASE,
                SEARCH_RESULT_SIZE
        );

        AggregationService.aggregateRareTermsByField(
                client,
                TextConstants.AGGREGATION_FIELD,
                RARE_TERMS_MAX_DOCUMENT_COUNT
        );
    }
}
