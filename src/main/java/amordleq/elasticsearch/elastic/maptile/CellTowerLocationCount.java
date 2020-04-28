package amordleq.elasticsearch.elastic.maptile;

import lombok.Data;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;

@Data
public class CellTowerLocationCount {
    Location location;
    Long count;

    public CellTowerLocationCount(MultiBucketsAggregation.Bucket bucket) {
        this(new BoundingBox(bucket.getKeyAsString()), bucket.getDocCount());
    }

    public CellTowerLocationCount(BoundingBox boundingBox, Long count) {
        this.location = new Location();
        this.location.lat = boundingBox.getCenterLatitude();
        this.location.lon = boundingBox.getCenterLongitude();

        this.count = count;
    }

}
