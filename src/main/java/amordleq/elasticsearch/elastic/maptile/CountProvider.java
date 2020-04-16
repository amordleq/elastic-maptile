package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoBoundingBoxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CountProvider {

    ReactiveElasticsearchClient elasticsearchClient;

    public CountProvider(ReactiveElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    public Mono<Long> countInRegion(BoundingBox boundingBox) {
        return countInRegion(boundingBox, null);
    }

    public Mono<Long> countInRegion(BoundingBox boundingBox, QueryBuilder additionalFilter) {
        return queryElasticsearch(createQuery(boundingBox, additionalFilter))
                .name("elastic-counts")
                .metrics();
    }

    public Mono<Long> countAll() {
        return countAll(null);
    }

    public Mono<Long> countAll(QueryBuilder additionalFilter) {
        return queryElasticsearch(createAllQuery(additionalFilter))
                .name("elastic-counts")
                .metrics();
    }

    Mono<Long> queryElasticsearch(QueryBuilder query) {
        CountRequest countRequest = new CountRequest("cell-towers");
        countRequest.query(query);
        return elasticsearchClient.count(countRequest);
    }

    private QueryBuilder createQuery(BoundingBox boundingBox, QueryBuilder additionalFilter) {
        BoolQueryBuilder searchQuery = QueryBuilders.boolQuery()
                .filter(geoQuery(boundingBox));
        if (additionalFilter != null) {
            searchQuery.filter(additionalFilter);
        }
        return searchQuery;
    }

    private QueryBuilder createAllQuery(QueryBuilder additionalFilter) {
        if (additionalFilter != null) {
            return additionalFilter;
        } else {
            return QueryBuilders.matchAllQuery();
        }
    }

    private GeoBoundingBoxQueryBuilder geoQuery(BoundingBox boundingBox) {
        return QueryBuilders.geoBoundingBoxQuery("location").setCorners(boundingBox.getNorth(), boundingBox.getWest(), boundingBox.getSouth(), boundingBox.getEast());
    }
}
