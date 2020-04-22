package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoTileGridAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.geogrid.ParsedGeoTileGrid;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.reactive.ReactiveElasticsearchClient;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
public class CellTowerRepositoryImpl implements CellTowerRepository {

    @Autowired
    private ReactiveElasticsearchClient elasticsearchClient;

    @Autowired
    private ReactiveRestHighLevelClient reactiveRestHighLevelClient;

    @Autowired
    private ReactiveElasticsearchTemplate elasticsearchTemplate;

    @Value("${elastic.maptile.granularityStep}")
    int granularityStep;

    @Override
    public Flux<CellTower> findNear(double latitude, double longitude, String distance, QueryBuilder additionalFilter, Integer maximumNumberOfResults) {
        BoolQueryBuilder searchQuery = QueryBuilders.boolQuery()
                .filter(geoDistanceQuery(latitude, longitude, distance));
        if (additionalFilter != null) {
            searchQuery.filter(additionalFilter);
        }

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder().withQuery(searchQuery);
        if (maximumNumberOfResults != null && maximumNumberOfResults > 0) {
            queryBuilder.withPageable(PageRequest.of(0, maximumNumberOfResults)); // FIXME: use query.setMaxResults() in future when available
        }

        return elasticsearchTemplate.find(queryBuilder.build(), CellTower.class)
                .retryBackoff(2, Duration.ofMillis(250));
    }

    @Override
    public Mono<Long> countInRegion(BoundingBox boundingBox, QueryBuilder additionalFilter) {
        return queryElasticsearch(createQuery(boundingBox, additionalFilter))
                .name("elastic-counts")
                .metrics();
    }

    @Override
    public Mono<Long> countAll(QueryBuilder additionalFilter) {
        return queryElasticsearch(createAllQuery(additionalFilter))
                .name("elastic-counts")
                .metrics();
    }

    @Override
    public Mono<ParsedGeoTileGrid> aggregateForGeographicalCoordinates(MapTileCoordinates coordinates, String termFieldSubAggregation, QueryBuilder additionalFilter) {
        return queryElasticsearch(coordinates, termFieldSubAggregation, additionalFilter)
                .flatMapMany(searchResponse -> Flux.just(searchResponse.getAggregations().get("agg")))
                .name("elastic-aggregation")
                .metrics()
                .cast(ParsedGeoTileGrid.class)
                .single();
    }

    Mono<Long> queryElasticsearch(QueryBuilder query) {
        CountRequest countRequest = new CountRequest("cell-towers");
        countRequest.query(query);
        return elasticsearchClient.count(countRequest);
    }

    Mono<SearchResponse> queryElasticsearch(MapTileCoordinates coordinates, String termFieldSubAggregation, QueryBuilder additionalFilter) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(createQuery(coordinates, additionalFilter));
        searchSourceBuilder.size(0);

        GeoTileGridAggregationBuilder aggregrationBuilder = AggregationBuilders.geotileGrid("agg");
        aggregrationBuilder.field("location").precision(calculatePrecision(coordinates));
        aggregrationBuilder.size(500000);

        if (termFieldSubAggregation != null) {
            aggregrationBuilder.subAggregation(AggregationBuilders.terms(termFieldSubAggregation).field(termFieldSubAggregation));
        }

        searchSourceBuilder.aggregation(aggregrationBuilder);

        SearchRequest searchRequest = new SearchRequest("cell-towers");

        searchRequest.source(searchSourceBuilder);
        return reactiveRestHighLevelClient.search(searchRequest)
                .retryBackoff(2, Duration.ofMillis(250));
    }

    private int calculatePrecision(MapTileCoordinates coordinates) {
        return Math.min(coordinates.getZ() + granularityStep, 29);
    }

    private GeoDistanceQueryBuilder geoDistanceQuery(double latitude, double longitude, String distance) {
        return QueryBuilders.geoDistanceQuery("location").point(latitude, longitude).distance(distance);
    }

    private QueryBuilder createQuery(MapTileCoordinates coordinates, QueryBuilder additionalFilter) {
        BoolQueryBuilder searchQuery = QueryBuilders.boolQuery()
                .filter(geoQuery(coordinates));
        if(additionalFilter != null) {
            searchQuery.filter(additionalFilter);
        }
        return searchQuery;
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

    private GeoBoundingBoxQueryBuilder geoQuery(MapTileCoordinates coordinates) {
        BoundingBox boundingBox = new BoundingBox(coordinates);
        return QueryBuilders.geoBoundingBoxQuery("location").setCorners(boundingBox.getNorth(), boundingBox.getWest(), boundingBox.getSouth(), boundingBox.getEast());
    }

    private GeoBoundingBoxQueryBuilder geoQuery(BoundingBox boundingBox) {
        return QueryBuilders.geoBoundingBoxQuery("location").setCorners(boundingBox.getNorth(), boundingBox.getWest(), boundingBox.getSouth(), boundingBox.getEast());
    }
}
