package amordleq.elasticsearch.elastic.maptile;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Profile("test")
@Component
public class TestPngGenerator implements PngGenerator{
    @Value("/test.png")
    Resource testTile;


    @Override
    public Mono<byte[]> generatePng(MapTileGrid mapTileGrid) {
        try {
            return Mono.just(Files.readAllBytes(Paths.get(testTile.getURI())));
        } catch (IOException e) {
            throw Exceptions.propagate(e);
        }
    }
}
