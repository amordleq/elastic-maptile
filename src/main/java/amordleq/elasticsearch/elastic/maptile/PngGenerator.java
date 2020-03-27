package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGrid;

public interface PngGenerator {
    byte[] generatePng(GeoGrid geoGrid);
}
