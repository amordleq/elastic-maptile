package systolic.elasticsearch.elastic.maptile;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.bucket.geogrid.ParsedGeoGrid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class MapTileController {

    @Autowired
    MapTileService mapTileService;

    @Autowired
    CellTowerRepositoryImpl cellTowerRepository;

    @RequestMapping(path = "/heatmap/{z}/{x}/{y}.png", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public Mono<byte[]> getHeatmapTile(@PathVariable int z, @PathVariable int x, @PathVariable int y, @RequestParam(required = false) String filter) {
        MapTileCoordinates coordinates = new MapTileCoordinates(x, y, z);
        return mapTileService.generateHeatmapTile(coordinates, nullSafeQueryBuilder(filter));
    }

    @RequestMapping(path = "/subterm/{term}/{z}/{x}/{y}.png", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public Mono<byte[]> getSubtermTile(@PathVariable String term, @PathVariable int z, @PathVariable int x, @PathVariable int y, @RequestParam(required = false) String filter) {
        MapTileCoordinates coordinates = new MapTileCoordinates(x, y, z);
        return mapTileService.generateSubTermsTile(coordinates, term, nullSafeQueryBuilder(filter));
    }

    @RequestMapping(path = "/debug/{z}/{x}/{y}.png", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public Mono<byte[]> getCoordinatesTiles(@PathVariable int z, @PathVariable int x, @PathVariable int y, @RequestParam(required = false) String filter) {
        MapTileCoordinates coordinates = new MapTileCoordinates(x, y, z);
        return mapTileService.generateDebugTile(coordinates, nullSafeQueryBuilder(filter));
    }

    @RequestMapping(path = "/test/{z}/{x}/{y}.png", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public Mono<byte[]> getTestTile(@PathVariable int z, @PathVariable int x, @PathVariable int y, @RequestParam(required = false) String filter) {
        MapTileCoordinates coordinates = new MapTileCoordinates(x, y, z);
        return mapTileService.generateTestTile(coordinates, nullSafeQueryBuilder(filter));
    }


    @RequestMapping(path = "/count-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<Long> countAll(@RequestParam(required = false) String filter) {
        return cellTowerRepository.countAll(nullSafeQueryBuilder(filter));
    }

    @RequestMapping(path = "/count-region", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<Long> countInRegion(@RequestParam double north, @RequestParam double west,
                                    @RequestParam double south, @RequestParam double east,
                                    @RequestParam(required = false) String filter) {
        BoundingBox boundingBox = new BoundingBox(north, west, south, east);
        return cellTowerRepository.countInRegion(boundingBox, nullSafeQueryBuilder(filter));
    }

    @RequestMapping(path = "/aggregate-counts-for-tile", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<List<CellTowerLocationCount>> aggregateCountsForTile(@RequestParam int z, @RequestParam int x, @RequestParam int y, @RequestParam(required = false) String filter) {
        MapTileCoordinates mapTileCoordinates = new MapTileCoordinates(x, y, z);
        return cellTowerRepository.aggregateForGeographicalCoordinates(mapTileCoordinates, null, nullSafeQueryBuilder(filter))
                .map(ParsedGeoGrid::getBuckets)
                .flatMapMany(Flux::fromIterable)
                .map(CellTowerLocationCount::new)
                .collectList();
    }

    @RequestMapping(path = "/cell-towers", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<List<CellTower>> getCellTowersNear(@RequestParam double latitude, @RequestParam double longitude, @RequestParam String distance,
                                                   @RequestParam(required = false) String filter, @RequestParam(required = false) Integer maxResults) {
        return cellTowerRepository.findNear(latitude, longitude, distance, nullSafeQueryBuilder(filter), maxResults)
                .collectList();
    }

    private QueryBuilder nullSafeQueryBuilder(String filter) {
        if (filter == null || filter.isEmpty()) {
            return null;
        } else {
            return QueryBuilders.wrapperQuery(filter);
        }
    }
}
