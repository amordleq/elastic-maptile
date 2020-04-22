package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MapTileGenerator {

    @Autowired
    private CellTowerRepositoryImpl cellTowerRepository;

    @Value("${elastic.maptile.granularityStep}")
    int granularityStep;

    @Value("/test.png")
    Resource testTile;

    public Mono<byte[]> generateHeatmapTile(final MapTileCoordinates coordinates, QueryBuilder additionalFilter) {
        ColorScheme colorScheme = new BluesColorScheme(coordinates.getZ(), granularityStep);
        return findGrid(coordinates, null, additionalFilter)
                .as(mapTileGridMono -> createTilePng(mapTileGridMono, new HeatmapPngGenerator(colorScheme)));
    }

    public Mono<byte[]> generateCountTile(final MapTileCoordinates coordinates, QueryBuilder additionalFilter) {
        return findGrid(coordinates, null, additionalFilter)
                .as(mapTileGridMono -> createTilePng(mapTileGridMono, new SimpleCountPngGenerator()));
    }

    public Mono<byte[]> generateTestTile(final MapTileCoordinates coordinates, QueryBuilder additionalFilter) {
        return findGrid(coordinates, null, additionalFilter)
                .as(mapTileGridMono -> createTilePng(mapTileGridMono, new TestPngGenerator(testTile)));
    }

    // FIXME: maybe not the best generic design, but playing with possibilities here
    public Mono<byte[]> generateMccTileMap(final MapTileCoordinates coordinates, final QueryBuilder additionalFilter) {
        ColorScheme colorScheme = new MccColorScheme();
        return findGrid(coordinates, "mcc", additionalFilter)
                .as(mapTileGridMono -> createTilePng(mapTileGridMono, new HeatmapPngGenerator(colorScheme)));
    }

    private Mono<MapTileGrid> findGrid(MapTileCoordinates coordinates, String termFieldSubAggregation, QueryBuilder additionalFilter) {
        return cellTowerRepository.aggregateForGeographicalCoordinates(coordinates, termFieldSubAggregation, additionalFilter)
                .map(grid -> new MapTileGrid(coordinates, grid));
    }

    private Mono<byte[]> createTilePng(Mono<MapTileGrid> mapTileGrid, PngGenerator pngGenerator) {
        return mapTileGrid
                .map(pngGenerator::generatePng)
                .name("tile-generation")
                .metrics();
    }

}
