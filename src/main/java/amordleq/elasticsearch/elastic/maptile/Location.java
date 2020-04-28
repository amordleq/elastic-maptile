package amordleq.elasticsearch.elastic.maptile;

import lombok.Data;

@Data
public class Location {
    Double lat;
    Double lon;

    public Location(){
    }

    public Location(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }
}
