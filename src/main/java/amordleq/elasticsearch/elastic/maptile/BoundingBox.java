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
        String[] zxyPieces = zxy.split("/");
        int z = Integer.parseInt(zxyPieces[0]);
        int x = Integer.parseInt(zxyPieces[1]);
        int y = Integer.parseInt(zxyPieces[2]);

        this.north = tile2lat(y, z);
        this.west = tile2lon(x, z);
        this.south = tile2lat(y + 1, z);
        this.east = tile2lon(x + 1, z);

        this.centerLatitude = (this.north + this.south) / 2;
        this.centerLongitude = (this.west + this.east) / 2;
    }

    BoundingBox(final int x, final int y, final int z) {
        this.north = tile2lat(y, z);
        this.west = tile2lon(x, z);
        this.south = tile2lat(y + 1, z);
        this.east = tile2lon(x + 1, z);

        this.centerLatitude = (this.north + this.south) / 2;
        this.centerLongitude = (this.west + this.east) / 2;
    }

    /*
     * Original implementation from https://wiki.openstreetmap.org/wiki/Slipp_map_tilenames
     */
    static final double tile2lon(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    static final double tile2lat(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }
}
