package amordleq.elasticsearch.elastic.maptile;

import lombok.Value;

@Value
public class BoundingBox {
    double north;
    double south;
    double east;
    double west;

    double centerLatitude;
    double centerLongitude;

    BoundingBox(double north, double west, double south, double east) {
        this.north = north;
        this.west = west;
        this.south = south;
        this.east = east;

        this.centerLatitude = (this.north + this.south) / 2;
        this.centerLongitude = (this.west + this.east) / 2;
    }

    BoundingBox(MapTileCoordinates mapTileCoordinates) {
        this(mapTileCoordinates.getX(), mapTileCoordinates.getY(), mapTileCoordinates.getZ());
    }

    BoundingBox(String zxy) {
        this(new MapTileCoordinates(zxy));
    }

    BoundingBox(final int x, final int y, final int z) {
        this(tile2lat(y, z), tile2lon(x, z), tile2lat(y+1,z), tile2lon(x+1,z));
    }

    /*
     * Original implementation from https://wiki.openstreetmap.org/wiki/Slipp_map_tilenames
     */
    private static final double tile2lon(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    private static final double tile2lat(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }
}
