package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class MapTileController {

    @Autowired
    MapTileGenerator generator;

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
}
