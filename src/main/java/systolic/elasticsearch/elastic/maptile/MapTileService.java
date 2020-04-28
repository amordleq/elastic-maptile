package systolic.elasticsearch.elastic.maptile;

import org.elasticsearch.index.query.QueryBuilder;
import reactor.core.publisher.Mono;

public interface MapTileService {

    Mono<byte[]> generateHeatmapTile(MapTileCoordinates coordinates, QueryBuilder additionalFilter);

    Mono<byte[]> generateDebugTile(MapTileCoordinates coordinates, QueryBuilder additionalFilter);

    Mono<byte[]> generateTestTile(MapTileCoordinates coordinates, QueryBuilder additionalFilter);

    Mono<byte[]> generateSubTermsTile(MapTileCoordinates coordinates, String term, QueryBuilder additionalFilter);
}
