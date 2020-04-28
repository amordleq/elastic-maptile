package systolic.elasticsearch.elastic.maptile;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.bucket.geogrid.ParsedGeoTileGrid;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CellTowerRepository {

    default Flux<CellTower> findNear(double latitude, double longitude, String distance) {
        return findNear(latitude, longitude, distance, null);
    }
    default Flux<CellTower> findNear(double latitude, double longitude, String distance, QueryBuilder additionalFilter) {
        return findNear(latitude, longitude, distance, additionalFilter, -1);
    }
    Flux<CellTower>  findNear(double latitude, double longitude, String distance, QueryBuilder additionalFilter, Integer maximumNumberOfResults);

    default Mono<Long> countInRegion(BoundingBox boundingBox) { return countInRegion(boundingBox, null); }
    public Mono<Long> countInRegion(BoundingBox boundingBox, QueryBuilder additionalFilter);

    default public Mono<Long> countAll() { return countAll(null); }
    public Mono<Long> countAll(QueryBuilder additionalFilter);

    Mono<ParsedGeoTileGrid> aggregateForGeographicalCoordinates(MapTileCoordinates coordinates, String termFieldSubAggregation, QueryBuilder additionalFilter);
}
