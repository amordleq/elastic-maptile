package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

// TODO: It would be nice to be able to use Spring's client instead of rolling my own here, but they don't seem
// to expose a way to get at the aggregation results.
@Component
public class ReactiveRestHighLevelClient {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    public Mono<CountResponse> count(final CountRequest countRequest) {
        return Mono.create(sink -> {
            restHighLevelClient.countAsync(countRequest, RequestOptions.DEFAULT, new ActionListener<CountResponse>() {
                @Override
                public void onResponse(CountResponse countResponse) { sink.success(countResponse); }

                @Override
                public void onFailure(Exception e) { sink.error(e); }
            });
        });
    }

    public Mono<SearchResponse> search(final SearchRequest searchRequest) {
        return Mono.create(sink -> {
                restHighLevelClient.searchAsync(searchRequest, RequestOptions.DEFAULT, new ActionListener<SearchResponse>() {
                    @Override
                    public void onResponse(SearchResponse searchResponse) { sink.success(searchResponse); }

                    @Override
                    public void onFailure(Exception e) { sink.error(e); }
                });
        });
    }

}
