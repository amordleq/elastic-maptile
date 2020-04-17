package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoBoundingBoxQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoTileGridAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.geogrid.ParsedGeoTileGrid;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class MapTileGenerator {

    // It would be great to use this, as I think it's probably a better implementation that just wrapping
    // async calls.  However, they don't appear to expose any API that allows for dealing with aggregation
    // results, so we have to fall back to wrapping the high level REST client
//    @Autowired
//    ReactiveElasticsearchClient elasticsearchClient;

    @Autowired
    ReactiveRestHighLevelClient reactiveRestHighLevelClient;

    @Value("${elastic.maptile.granularityStep}")
    int granularityStep;

    @Autowired
    PngGenerator pngGenerator;

    public Mono<byte[]> generateTileMap(final MapTileCoordinates coordinates) {
        return generateTileMap(coordinates, null);
    }

    public Mono<byte[]> generateTileMap(final MapTileCoordinates coordinates, QueryBuilder additionalFilter) {
        return findGrid(coordinates, additionalFilter)
                .as(this::createTilePng);
    }

    Mono<MapTileGrid> findGrid(MapTileCoordinates coordinates, QueryBuilder additionalFilter) {
        return queryElasticsearch(coordinates, additionalFilter)
                .flatMapMany(searchResponse -> Flux.just(searchResponse.getAggregations().get("agg")))
                .name("elastic-aggregation")
                .metrics()
                .cast(ParsedGeoTileGrid.class)
                .single()
                .map(grid -> new MapTileGrid(coordinates, grid));
    }

    Mono<byte[]> createTilePng(Mono<MapTileGrid> mapTileGrid) {
        return mapTileGrid
                .map(grid -> pngGenerator.generatePng(grid))
                .name("tile-generation")
                .metrics();
    }

    Mono<SearchResponse> queryElasticsearch(MapTileCoordinates coordinates) {
        return queryElasticsearch(coordinates, null);
    }

    Mono<SearchResponse> queryElasticsearch(MapTileCoordinates coordinates, QueryBuilder additionalFilter) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(createQuery(coordinates, additionalFilter));
        searchSourceBuilder.size(0);

        GeoTileGridAggregationBuilder aggregrationBuilder = AggregationBuilders.geotileGrid("agg");
        aggregrationBuilder.field("location").precision(coordinates.getZ() + granularityStep);
        aggregrationBuilder.size(500000);
        searchSourceBuilder.aggregation(aggregrationBuilder);

        SearchRequest searchRequest = new SearchRequest("cell-towers");

        searchRequest.source(searchSourceBuilder);
        return reactiveRestHighLevelClient.search(searchRequest)
                .retryBackoff(2, Duration.ofMillis(250));
    }

    private QueryBuilder createQuery(MapTileCoordinates coordinates, QueryBuilder additionalFilter) {
        BoolQueryBuilder searchQuery = QueryBuilders.boolQuery()
                .filter(geoQuery(coordinates));
        if(additionalFilter != null) {
            searchQuery.filter(additionalFilter);
        }
        return searchQuery;
    }

    private GeoBoundingBoxQueryBuilder geoQuery(MapTileCoordinates coordinates) {
        BoundingBox boundingBox = new BoundingBox(coordinates);
        return QueryBuilders.geoBoundingBoxQuery("location").setCorners(boundingBox.getNorth(), boundingBox.getWest(), boundingBox.getSouth(), boundingBox.getEast());
    }

}
