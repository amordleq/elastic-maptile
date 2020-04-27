package amordleq.elasticsearch.elastic.maptile;

import lombok.Value;

@Value
public class BoundingBox {
    double north;
    double south;
    double east;
    double west;

    BoundingBox(double north, double west, double south, double east) {
        this.north = north;
        this.west = west;
        this.south = south;
        this.east = east;
    }

    BoundingBox(MapTileCoordinates mapTileCoordinates) {
        this(mapTileCoordinates.getX(), mapTileCoordinates.getY(), mapTileCoordinates.getZ());
    }

    /*
     * Original implementation from https://wiki.openstreetmap.org/wiki/Slipp_map_tilenames
     */
    BoundingBox(final int x, final int y, final int zoom) {
        north = tile2lat(y, zoom);
        south = tile2lat(y + 1, zoom);
        west = tile2lon(x, zoom);
        east = tile2lon(x + 1, zoom);
    }

    static final double tile2lon(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    static final double tile2lat(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }
}
