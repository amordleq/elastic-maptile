package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.index.query.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MapTileServiceImpl implements MapTileService {

    @Autowired
    private CellTowerRepositoryImpl cellTowerRepository;

    @Value("${elastic.maptile.granularityStep}")
    int granularityStep;

    @Value("/test.png")
    Resource testTile;

    @Override
    public Mono<byte[]> generateHeatmapTile(final MapTileCoordinates coordinates, final QueryBuilder additionalFilter) {
        ColorScheme colorScheme = new BluesColorScheme(coordinates.getZ(), granularityStep);
        return findGrid(coordinates, null, additionalFilter)
                .as(mapTileGridMono -> createTilePng(mapTileGridMono, new HeatmapPngGenerator(colorScheme)));
    }

    @Override
    public Mono<byte[]> generateCountTile(final MapTileCoordinates coordinates, final QueryBuilder additionalFilter) {
        return findGrid(coordinates, null, additionalFilter)
                .as(mapTileGridMono -> createTilePng(mapTileGridMono, new SimpleCountPngGenerator()));
    }

    @Override
    public Mono<byte[]> generateTestTile(final MapTileCoordinates coordinates, final QueryBuilder additionalFilter) {
        return findGrid(coordinates, null, additionalFilter)
                .as(mapTileGridMono -> createTilePng(mapTileGridMono, new TestPngGenerator(testTile)));
    }

    @Override
    public Mono<byte[]> generateSubTermsTile(final MapTileCoordinates coordinates, final String term, final QueryBuilder additionalFilter) {
        ColorScheme colorScheme = new LargestSubTermColorScheme();
        return findGrid(coordinates, term, additionalFilter)
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
