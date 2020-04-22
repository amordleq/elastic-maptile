package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

// FIXME: this should be discarded in favor of the spring-provided client once spring-data-elasticsearch releases 4.0
@Component
public class ReactiveRestHighLevelClient {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    // FIXME: i believe this doesn't work nearly as well as i'd hoped.  see CellTowerRepositoryTests for the discussion on
    // why blockhound isn't working for the CellTowerRepository.  i think we may be bounded here by a different thread pool
    // by virtue of relying on restHighLevelClient.searchAsync().
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
