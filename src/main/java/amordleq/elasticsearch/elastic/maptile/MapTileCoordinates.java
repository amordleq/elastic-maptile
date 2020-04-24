package amordleq.elasticsearch.elastic.maptile;

import lombok.Value;

@Value
public class MapTileCoordinates {
    int x;
    int y;
    int z;

    @Override
    public String toString() {
        return z+"/"+x+"/"+y;
    }
}
