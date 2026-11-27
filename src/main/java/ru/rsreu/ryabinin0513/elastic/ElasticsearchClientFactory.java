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

public class ElasticsearchClientFactory {

    public static class EsClients implements AutoCloseable {
        public final ElasticsearchClient client;
        private final ElasticsearchTransport transport;
        private final RestClient restClient;

        EsClients(ElasticsearchClient client, ElasticsearchTransport transport, RestClient restClient) {
            this.client = client;
            this.transport = transport;
            this.restClient = restClient;
        }

        @Override
        public void close() throws IOException {
            try {
                transport.close();
            } finally {
                restClient.close();
            }
        }
    }

    public static EsClients createClients() {

        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(Config.ELASTIC_USER, Config.ELASTIC_PASSWORD)
        );

        RestClientBuilder builder = RestClient.builder(
                new HttpHost(Config.ELASTIC_HOST, Config.ELASTIC_PORT, Config.ELASTIC_SCHEME)
        );

        builder.setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
        );

        RestClient restClient = builder.build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper()
        );

        ElasticsearchClient client = new ElasticsearchClient(transport);

        return new EsClients(client, transport, restClient);
    }
}
