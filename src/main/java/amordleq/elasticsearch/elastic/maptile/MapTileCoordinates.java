package amordleq.elasticsearch.elastic.maptile;

import lombok.Value;

@Value
public class MapTileCoordinates {
    int x;
    int y;
    int z;

    public MapTileCoordinates(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public MapTileCoordinates(String zxyName) {
        String[] zxyPieces = zxyName.split("/");
        int z = Integer.parseInt(zxyPieces[0]);
        int x = Integer.parseInt(zxyPieces[1]);
        int y = Integer.parseInt(zxyPieces[2]);

        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return z+"/"+x+"/"+y;
    }
}
