package amordleq.elasticsearch.elastic.maptile;

import reactor.core.publisher.Mono;

public interface PngGenerator {
    Mono<byte[]> generatePng(MapTileGrid response);
}
