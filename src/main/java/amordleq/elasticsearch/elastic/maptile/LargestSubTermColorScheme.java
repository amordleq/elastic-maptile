package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.awt.*;
import java.util.List;

public class LargestSubTermColorScheme implements ColorScheme {

    private static List<Color> availableColors = List.of(
            new Color(31, 119, 180),
            new Color(255, 127, 14),
            new Color(44, 160, 44),
            new Color(214, 39, 40),
            new Color(148, 103, 189),
            new Color(140, 86, 75),
            new Color(227, 119, 194),
            new Color(127, 127, 127),
            new Color(188, 189, 34),
            new Color(23, 190, 207)
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
