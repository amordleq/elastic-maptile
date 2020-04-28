package systolic.elasticsearch.elastic.maptile;

import org.springframework.core.io.Resource;
import reactor.core.Exceptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestPngGenerator implements PngGenerator{

    final Resource testTile;

    public TestPngGenerator(Resource testTile) {
        this.testTile = testTile;
    }

    public byte[] generatePng(MapTileGrid mapTileGrid) {
        try {
            return Files.readAllBytes(Paths.get(testTile.getURI()));
        } catch (IOException e) {
            throw Exceptions.propagate(e);
        }
    }
}
