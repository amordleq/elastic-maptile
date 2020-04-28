package systolic.elasticsearch.elastic.maptile;

public interface PngGenerator {
    byte[] generatePng(MapTileGrid response);
}
