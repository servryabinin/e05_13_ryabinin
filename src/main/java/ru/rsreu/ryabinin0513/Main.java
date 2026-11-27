package ru.rsreu.ryabinin0513;

import ru.rsreu.ryabinin0513.config.Config;
import ru.rsreu.ryabinin0513.elastic.AggregationService;
import ru.rsreu.ryabinin0513.elastic.CsvLoader;
import ru.rsreu.ryabinin0513.elastic.ElasticsearchClientFactory;
import ru.rsreu.ryabinin0513.elastic.IndexCreator;
import ru.rsreu.ryabinin0513.elastic.SearchService;

/**
 * Практическая работа 19.2.8.1.
 * Вариант 13: поисковый запрос match_phrase и агрегация редких терминов (rare_terms).
 * Предметная область: "Инвентаризация".
 */
public class Main {

    /** Поле, по которому выполняется поиск фразы. */
    private static final String SEARCH_FIELD = "description";

    /** Искомая фраза: должна встретиться в описании подряд, без промежуточных слов. */
    private static final String SEARCH_PHRASE = "Intel Core";

    /** Количество выводимых результатов поиска. */
    private static final int SEARCH_SIZE = 10;

    /** Поле для агрегации редких терминов (keyword). */
    private static final String AGGREGATION_FIELD = "department";

    /** Терм считается редким, если встречается не более чем в 2 документах. */
    private static final long RARE_TERMS_MAX_DOC_COUNT = 2;

    public static void main(String[] args) throws Exception {
        try (var es = ElasticsearchClientFactory.createClients()) {

            IndexCreator.createIndexIfNotExists(es.client);

            CsvLoader.loadData(es.client, Config.CSV_FILE);

            SearchService.searchByPhrase(es.client, SEARCH_FIELD, SEARCH_PHRASE, SEARCH_SIZE);

            AggregationService.rareTermsByField(es.client, AGGREGATION_FIELD, RARE_TERMS_MAX_DOC_COUNT);
        }
        System.out.println("\nКлиент Elasticsearch закрыт. Программа завершена.");
    }
}
