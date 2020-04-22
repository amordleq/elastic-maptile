package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MapTileGenerator {

    @Autowired
    private CellTowerRepositoryImpl cellTowerRepository;

    @Value("${elastic.maptile.granularityStep}")
    int granularityStep;

    @Autowired
    PngGenerator pngGenerator;

    public Mono<byte[]> generateTileMap(final MapTileCoordinates coordinates) {
        return generateTileMap(coordinates, null);
    }

    public Mono<byte[]> generateTileMap(final MapTileCoordinates coordinates, QueryBuilder additionalFilter) {
        ColorScheme colorScheme = new BluesColorScheme(coordinates.getZ(), granularityStep);
        return findGrid(coordinates, null, additionalFilter)
                .as(mapTileGridMono -> createTilePng(mapTileGridMono, colorScheme));
    }

    // FIXME: maybe not the best generic design, but playing with possibilities here
    public Mono<byte[]> generateMccTileMap(final MapTileCoordinates coordinates, final QueryBuilder additionalFilter) {
        ColorScheme colorScheme = new MccColorScheme();
        return findGrid(coordinates, "mcc", additionalFilter)
                .as(mapTileGridMono -> createTilePng(mapTileGridMono, colorScheme));
    }

    Mono<MapTileGrid> findGrid(MapTileCoordinates coordinates, String termFieldSubAggregation, QueryBuilder additionalFilter) {
        return cellTowerRepository.aggregateForGeographicalCoordinates(coordinates, termFieldSubAggregation, additionalFilter)
                .map(grid -> new MapTileGrid(coordinates, grid));
    }

    Mono<byte[]> createTilePng(Mono<MapTileGrid> mapTileGrid, ColorScheme colorScheme) {
        return mapTileGrid
                .map(grid -> pngGenerator.generatePng(grid, colorScheme))
                .name("tile-generation")
                .metrics();
    }

}
