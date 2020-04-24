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
    private CellTowerRepository cellTowerRepository;

    @Value("${elastic.maptile.granularityStep}")
    private int granularityStep;

    @Value("classpath:/test.png")
    private Resource testTileResource;

    @Override
    public Mono<byte[]> generateHeatmapTile(final MapTileCoordinates coordinates, final QueryBuilder additionalFilter) {
        ColorScheme colorScheme = new BluesColorScheme(coordinates.getZ(), granularityStep);
        return generateTile(coordinates, null, additionalFilter, new HeatmapPngGenerator(colorScheme));
    }

    @Override
    public Mono<byte[]> generateCountTile(final MapTileCoordinates coordinates, final QueryBuilder additionalFilter) {
        return generateTile(coordinates, null, additionalFilter, new SimpleCountPngGenerator());
    }

    @Override
    public Mono<byte[]> generateCoordinatesTile(MapTileCoordinates coordinates, QueryBuilder additionalFilter) {
        return generateTile(coordinates, null, additionalFilter, new TileCoordinatesPngGenerator());
    }

    @Override
    public Mono<byte[]> generateTestTile(final MapTileCoordinates coordinates, final QueryBuilder additionalFilter) {
        return generateTile(coordinates, null, additionalFilter, new TestPngGenerator(testTileResource));
    }

    @Override
    public Mono<byte[]> generateSubTermsTile(final MapTileCoordinates coordinates, final String term, final QueryBuilder additionalFilter) {
        ColorScheme colorScheme = new LargestSubTermColorScheme();
        return generateTile(coordinates, term, additionalFilter, new HeatmapPngGenerator(colorScheme));
    }

    private Mono<byte[]> generateTile(final MapTileCoordinates coordinates, final String term, final QueryBuilder additionalFilter, PngGenerator pngGenerator) {
        return findGrid(coordinates, term, additionalFilter)
                .as(mapTileGridMono -> createTilePng(mapTileGridMono, pngGenerator));
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
