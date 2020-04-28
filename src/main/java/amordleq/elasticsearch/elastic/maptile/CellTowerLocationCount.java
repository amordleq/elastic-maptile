package amordleq.elasticsearch.elastic.maptile;

import lombok.Data;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;

@Data
public class CellTowerLocationCount {
    Location location;
    Long count;

    public CellTowerLocationCount(MultiBucketsAggregation.Bucket bucket) {
        BoundingBox boundingBox = new BoundingBox(bucket.getKeyAsString());

        this.location = new Location();
        this.location.lat = boundingBox.getCenterLatitude();
        this.location.lon = boundingBox.getCenterLongitude();

        this.count = bucket.getDocCount();
    }

}
