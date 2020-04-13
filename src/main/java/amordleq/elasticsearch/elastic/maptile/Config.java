package amordleq.elasticsearch.elastic.maptile;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.client.reactive.ReactiveRestClients;
import org.springframework.web.reactive.function.client.ExchangeStrategies;

@Configuration
public class Config {

    @Bean
    ReactiveElasticsearchClient client() {
        ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo("localhost:9200")
                .withWebClientConfigurer(webClient -> {
                    ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                            .codecs(configurer -> configurer.defaultCodecs()
                                    .maxInMemorySize(-1))
                            .build();
                    return webClient.mutate().exchangeStrategies(exchangeStrategies).build();
                })
                .build();
        return ReactiveRestClients.create(clientConfiguration);
    }


    @Bean
    RestHighLevelClient restHighLevelClient() {
        return new RestHighLevelClient(
                RestClient
                        .builder(new HttpHost("localhost", 9200))
                        .setRequestConfigCallback(config -> config
                                .setConnectTimeout(5000)
                                .setConnectionRequestTimeout(5000)
                                .setSocketTimeout(5000)
                        ));
    }
}
