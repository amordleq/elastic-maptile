package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class MapTileController {

    @Autowired
    MapTileGenerator generator;

    @Autowired
    CellTowerRepositoryImpl cellTowerRepository;

    @RequestMapping(path = "/{z}/{x}/{y}.png", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public Mono<byte[]> getTile(@PathVariable int z, @PathVariable int x, @PathVariable int y, @RequestParam(required = false) String filter) {
        MapTileCoordinates coordinates = new MapTileCoordinates(x, y, z);
        if (filter != null && !filter.isEmpty()) {
            return generator.generateTileMap(coordinates, QueryBuilders.wrapperQuery(filter));
        } else {
            return generator.generateTileMap(coordinates);
        }
    }

    @RequestMapping(path = "/count-all", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<Long> countAll(@RequestParam(required = false) String filter) {
        if (filter != null && !filter.isEmpty()) {
            return cellTowerRepository.countAll(QueryBuilders.wrapperQuery(filter));
        } else {
            return cellTowerRepository.countAll();
        }
    }

    @RequestMapping(path = "/count-region", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Mono<Long> countInRegion(@RequestParam double north, @RequestParam double west,
                                    @RequestParam double south, @RequestParam double east,
                                    @RequestParam(required = false) String filter) {
        BoundingBox boundingBox = new BoundingBox(north, west, south, east);
        if (filter != null && !filter.isEmpty()) {
            return cellTowerRepository.countInRegion(boundingBox, QueryBuilders.wrapperQuery(filter));
        } else {
            return cellTowerRepository.countInRegion(boundingBox);
        }
    }

    @RequestMapping(path = "/cell-towers", produces = MediaType.APPLICATION_STREAM_JSON_VALUE)
    @ResponseBody
    public Flux<CellTower> getCellTowersNear(@RequestParam double latitude, @RequestParam double longitude, @RequestParam String distance,
                                             @RequestParam(required = false) String filter, @RequestParam(required = false) Integer maxResults) {
        if (filter != null && !filter.isEmpty()) {
            return cellTowerRepository.findNear(latitude, longitude, distance, QueryBuilders.wrapperQuery(filter), maxResults);
        } else {
            return cellTowerRepository.findNear(latitude, longitude, distance, null, maxResults);
        }
    }
}
