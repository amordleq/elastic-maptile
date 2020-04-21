package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;

import java.awt.Color;

public interface ColorScheme {
    Color getColor(MultiBucketsAggregation.Bucket bucket);
}
