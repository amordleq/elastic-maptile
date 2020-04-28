package systolic.elasticsearch.elastic.maptile;

import lombok.Value;
import org.elasticsearch.search.aggregations.bucket.geogrid.ParsedGeoTileGrid;

@Value
public class MapTileGrid {
    MapTileCoordinates coordinates;
    ParsedGeoTileGrid grid;
}
