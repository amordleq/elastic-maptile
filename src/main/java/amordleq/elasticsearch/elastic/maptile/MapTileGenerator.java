package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
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

    Mono<byte[]> generateTileMap(int z, int x, int y) {

        return queryElasticsearch(x, y, z)
                .flatMapMany(searchResponse -> Flux.just(searchResponse.getAggregations().get("agg")))
                .cast(ParsedGeoTileGrid.class)
                .single()
                .map(grid -> {
                    return pngGenerator.generatePng(x, y, z, grid);
                });
    }

    Mono<SearchResponse> queryElasticsearch(int x, int y, int zoom) {
        BoundingBox boundingBox = new BoundingBox(x, y, zoom);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(
                QueryBuilders.geoBoundingBoxQuery("location")
                        .setCorners(boundingBox.getNorth(), boundingBox.getWest(), boundingBox.getSouth(), boundingBox.getEast()));
        searchSourceBuilder.size(0);

        GeoTileGridAggregationBuilder aggregrationBuilder = AggregationBuilders.geotileGrid("agg");
        aggregrationBuilder.field("location").precision(zoom + granularityStep);
        aggregrationBuilder.size(500000);
        searchSourceBuilder.aggregation(aggregrationBuilder);

        SearchRequest searchRequest = new SearchRequest("cell-towers");

        searchRequest.source(searchSourceBuilder);
        return reactiveRestHighLevelClient.search(searchRequest);
    }

}
