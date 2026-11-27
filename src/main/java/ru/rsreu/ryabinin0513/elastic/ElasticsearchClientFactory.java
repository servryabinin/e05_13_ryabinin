package ru.rsreu.ryabinin0513.elastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import ru.rsreu.ryabinin0513.config.Config;

import java.io.IOException;

/**
 * Фабрика для создания клиентов Elasticsearch.
 *
 * <p>Собирает цепочку объектов {@link RestClient} → {@link ElasticsearchTransport}
 * → {@link ElasticsearchClient} и управляет их освобождением.</p>
 */
public final class ElasticsearchClientFactory {

    /**
     * Приватный конструктор предотвращает создание экземпляров класса.
     */
    private ElasticsearchClientFactory() {
    }

    /**
     * Набор связанных клиентов Elasticsearch, которые необходимо закрыть
     * после завершения работы.
     */
    public static final class ElasticsearchClients implements AutoCloseable {

        private final ElasticsearchClient client;
        private final ElasticsearchTransport transport;
        private final RestClient restClient;

        /**
         * Создаёт набор клиентов Elasticsearch.
         *
         * @param client     типизированный клиент Elasticsearch
         * @param transport  транспорт для сериализации запросов
         * @param restClient низкоуровневый HTTP-клиент
         */
        private ElasticsearchClients(ElasticsearchClient client,
                                     ElasticsearchTransport transport,
                                     RestClient restClient) {
            this.client = client;
            this.transport = transport;
            this.restClient = restClient;
        }

        /**
         * Возвращает типизированный клиент Elasticsearch.
         *
         * @return клиент Elasticsearch
         */
        public ElasticsearchClient getClient() {
            return client;
        }

        /**
         * Закрывает транспорт и низкоуровневый HTTP-клиент.
         *
         * @throws IOException если при закрытии возникла ошибка ввода-вывода
         */
        @Override
        public void close() throws IOException {
            try {
                transport.close();
            } finally {
                restClient.close();
            }
        }
    }

    /**
     * Создаёт набор клиентов Elasticsearch с параметрами из конфигурации.
     *
     * @return набор связанных клиентов Elasticsearch
     */
    public static ElasticsearchClients createElasticsearchClients() {
        CredentialsProvider credentialsProvider = createCredentialsProvider();
        RestClient restClient = createRestClient(credentialsProvider);
        ElasticsearchTransport transport = createTransport(restClient);
        ElasticsearchClient client = new ElasticsearchClient(transport);

        return new ElasticsearchClients(client, transport, restClient);
    }

    /**
     * Создаёт провайдер учётных данных для подключения к Elasticsearch.
     *
     * @return провайдер учётных данных
     */
    private static CredentialsProvider createCredentialsProvider() {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(Config.ELASTIC_USER, Config.ELASTIC_PASSWORD)
        );
        return credentialsProvider;
    }

    /**
     * Создаёт низкоуровневый HTTP-клиент Elasticsearch.
     *
     * @param credentialsProvider провайдер учётных данных
     * @return настроенный HTTP-клиент
     */
    private static RestClient createRestClient(CredentialsProvider credentialsProvider) {
        RestClientBuilder builder = RestClient.builder(
                new HttpHost(Config.ELASTIC_HOST, Config.ELASTIC_PORT, Config.ELASTIC_SCHEME)
        );

        builder.setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
        );

        return builder.build();
    }

    /**
     * Создаёт транспорт, преобразующий Java-объекты в JSON и обратно.
     *
     * @param restClient низкоуровневый HTTP-клиент
     * @return транспорт Elasticsearch
     */
    private static ElasticsearchTransport createTransport(RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }
}
