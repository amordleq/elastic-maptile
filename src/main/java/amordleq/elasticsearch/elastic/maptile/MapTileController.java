package amordleq.elasticsearch.elastic.maptile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class MapTileController {

    @Autowired
    MapTileGenerator generator;

    @RequestMapping(path="/{z}/{x}/{y}.png", produces = "image/png")
    @ResponseBody
    public Mono<byte[]> tile(@PathVariable int z, @PathVariable int x, @PathVariable int y) {
        return generator.generateTileMap(new MapTileCoordinates(x, y, z));
    }
}
