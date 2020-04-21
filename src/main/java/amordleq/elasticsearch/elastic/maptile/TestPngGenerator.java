package amordleq.elasticsearch.elastic.maptile;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Profile("test")
@Component
public class TestPngGenerator implements PngGenerator{
    @Value("/test.png")
    Resource testTile;


    @Override
    public byte[] generatePng(MapTileGrid mapTileGrid, ColorScheme colorScheme) {
        try {
            return Files.readAllBytes(Paths.get(testTile.getURI()));
        } catch (IOException e) {
            throw Exceptions.propagate(e);
        }
    }
}
