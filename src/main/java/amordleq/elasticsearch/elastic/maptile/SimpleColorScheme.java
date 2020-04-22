package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;

import java.awt.*;

public class SimpleColorScheme implements ColorScheme {

    @Override
    public Color getColor(MultiBucketsAggregation.Bucket bucket) {
        return Color.BLUE;
    }
}
