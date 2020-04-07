package amordleq.elasticsearch.elastic.maptile;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class MapTileHandler {

    MapTileGenerator generator;

    public MapTileHandler(MapTileGenerator generator) {
        this.generator = generator;
    }

    Mono<ServerResponse> getTile(ServerRequest serverRequest) {
        int z = Integer.parseInt(serverRequest.pathVariable("z"));
        int x = Integer.parseInt(serverRequest.pathVariable("x"));
        int y = Integer.parseInt(serverRequest.pathVariable("y"));

        return ServerResponse.ok().contentType(MediaType.IMAGE_PNG)
                .body(generator.generateTileMap(new MapTileCoordinates(x, y, z)), byte[].class);
    }
}
