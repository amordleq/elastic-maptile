package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class MccColorScheme implements ColorScheme {

    private static Map<Integer, Color> colorMap = new HashMap<>();

    static {
        colorMap.put(2, Color.GREEN);
        colorMap.put(3, Color.BLUE);
        colorMap.put(4, Color.RED);
        colorMap.put(5, Color.YELLOW);
        colorMap.put(6, Color.ORANGE);
        colorMap.put(7, Color.CYAN);
        colorMap.put(8, Color.WHITE);
        colorMap.put(9, Color.MAGENTA);
        colorMap.put(0, Color.LIGHT_GRAY);
    }

    @Override
    public Color getColor(MultiBucketsAggregation.Bucket bucket) {
        // assume a single nested terms aggregation
        ParsedLongTerms aggregation = (ParsedLongTerms) bucket.getAggregations().asList().get(0);

        // FIXME: i think the buckets are guaranteed to be ordered, so maybe we can just grab the first instead of max?
        Number mcc = aggregation.getBuckets().stream()
                .max((bucket1, bucket2) -> (int) (bucket1.getDocCount() - bucket2.getDocCount()))
                .map(Terms.Bucket::getKeyAsNumber)
                .orElse(0)
                .intValue();

        if (mcc.intValue() == 0) {
            return Color.LIGHT_GRAY;
        }
        return colorMap.get((int)(mcc.intValue() / 100));
    }

}
