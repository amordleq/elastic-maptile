package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class MapController {

    @Autowired
    MapTileGenerator generator;

    @Autowired
    CountProvider countProvider;

    @RequestMapping(path="/{z}/{x}/{y}.png", produces = "image/png")
    @ResponseBody
    public Mono<byte[]> getTile(@PathVariable int z, @PathVariable int x, @PathVariable int y, @RequestParam(required = false) String filter) {
        MapTileCoordinates coordinates = new MapTileCoordinates(x, y, z);
        if (filter != null && !filter.isEmpty()) {
            return generator.generateTileMap(coordinates, QueryBuilders.wrapperQuery(filter));
        } else {
            return generator.generateTileMap(coordinates);
        }
    }

    @RequestMapping(path="/count-all", produces = "text/plain")
    @ResponseBody
    public Mono<Long> countAll(@RequestParam(required = false) String filter) {
        if (filter != null && !filter.isEmpty()) {
            return countProvider.countAll(QueryBuilders.wrapperQuery(filter));
        } else {
            return countProvider.countAll();
        }
    }

    @RequestMapping(path="/count-region", produces = "text/plain")
    @ResponseBody
    public Mono<Long> countInRegion(@RequestParam double north, @RequestParam double west, @RequestParam double south, @RequestParam double east, @RequestParam(required = false) String filter) {
        BoundingBox boundingBox = new BoundingBox(north, west, south, east);
        if (filter != null && !filter.isEmpty()) {
            return countProvider.countInRegion(boundingBox, QueryBuilders.wrapperQuery(filter));
        } else {
            return countProvider.countInRegion(boundingBox);
        }
    }
}
