package amordleq.elasticsearch.elastic.maptile;

import lombok.Data;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;

@Data
public class CellTowerLocationCount {
    Location location;
    Long count;

    public CellTowerLocationCount(MultiBucketsAggregation.Bucket bucket) {
        this.count = bucket.getDocCount();
        String key = bucket.getKeyAsString();
        String[] zxy = key.split("/");
        int bucketZ = Integer.parseInt(zxy[0]);
        int bucketX = Integer.parseInt(zxy[1]);
        int bucketY = Integer.parseInt(zxy[2]);

        location = new Location();
        location.lat = BoundingBox.tile2lat(bucketY, bucketZ);
        location.lon = BoundingBox.tile2lon(bucketX, bucketZ);
    }

}
