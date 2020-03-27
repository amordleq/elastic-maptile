package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

@Component
public class ReactiveRestHighLevelClient {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    public Mono<SearchResponse> search(final SearchRequest searchRequest) {
        return Mono.create(sink -> {
            try {
                restHighLevelClient.searchAsync(searchRequest, RequestOptions.DEFAULT, listenerToSink(sink));
            } catch (Exception e) {
                throw Exceptions.propagate(e);
            }
        });
    }

    private ActionListener<SearchResponse> listenerToSink(MonoSink<SearchResponse> sink) {
        return new ActionListener<>() {
            @Override
            public void onResponse(SearchResponse searchResponse) {
                sink.success(searchResponse);
            }

            @Override
            public void onFailure(Exception e) {
                sink.error(e);
            }
        };
    }
}
