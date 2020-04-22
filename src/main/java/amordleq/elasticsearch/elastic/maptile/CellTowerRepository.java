package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.bucket.geogrid.ParsedGeoTileGrid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CellTowerRepository {
    Flux<CellTower> findNear(double latitude, double longitude, String distance);
    Flux<CellTower> findNear(double latitude, double longitude, String distance, QueryBuilder additionalFilter);
    Flux<CellTower>  findNear(double latitude, double longitude, String distance, QueryBuilder additionalFilter, Integer maximumNumberOfResults);
    Mono<Long> countInRegion(BoundingBox boundingBox);
    public Mono<Long> countInRegion(BoundingBox boundingBox, QueryBuilder additionalFilter);
    public Mono<Long> countAll();
    public Mono<Long> countAll(QueryBuilder additionalFilter);
    Mono<ParsedGeoTileGrid> aggregateForGeographicalCoordinates(MapTileCoordinates coordinates, String termFieldSubAggregation, QueryBuilder additionalFilter);
}
