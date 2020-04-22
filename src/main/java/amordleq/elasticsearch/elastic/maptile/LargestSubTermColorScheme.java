package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.awt.*;
import java.util.List;

public class LargestSubTermColorScheme implements ColorScheme {

    private static List<Color> availableColors = List.of(
            new Color(27,158,119),
            new Color(217,95,2),
            new Color(117, 112,179),
            new Color(231,41,138),
            new Color(102,166,30),
            new Color(230,171,2),
            new Color(166,118,29),
            new Color(180,180,180)
    );

    @Override
    public Color getColor(MultiBucketsAggregation.Bucket bucket) {
        ParsedTerms aggregation = (ParsedTerms) bucket.getAggregations().asList().get(0);

        // FIXME: i think the buckets are guaranteed to be ordered, so maybe we can just grab the first instead of max?
        Long key = aggregation.getBuckets().stream()
                .max((bucket1, bucket2) -> (int) (bucket1.getDocCount() - bucket2.getDocCount()))
                .map(this::naturalKeyForBucket)
                .orElse(0L);

        return availableColors.get((int)(key % availableColors.size()));
    }

    private Long naturalKeyForBucket(Terms.Bucket bucket) {
        try {
            return bucket.getKeyAsNumber().longValue();
        } catch (NumberFormatException e) {
            return (long) bucket.getKeyAsString().hashCode();
        }
    }

}
