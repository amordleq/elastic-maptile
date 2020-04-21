package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;

import java.awt.*;
import java.util.List;

public class BluesColorScheme implements ColorScheme {

    long maxDocumentCount = 0;

    public BluesColorScheme(int zoomLevel, int granularityStep) {
        this.maxDocumentCount = getMaxDocCountForZoomLevel(zoomLevel, granularityStep);
    }

    private static RgbBasisSplineInterpolator interpolator = new RgbBasisSplineInterpolator(List.of(
            "#08306b",
            "#08519c",
            "#2171b5",
            "#4292c6",
            "#6baed6",
            "#9ecae1",
            "#c6dbef",
            "#deebf7",
            "#f7fbff"
    ));

    @Override
    public Color getColor(MultiBucketsAggregation.Bucket bucket) {
        double intensityScale = Math.log(bucket.getDocCount()) / Math.log(maxDocumentCount);
        return interpolator.interpolate(intensityScale);
    }

    private long getMaxDocCountForZoomLevel(int zoomLevel, int granularityStep) {
        int bucketZoomLevel = zoomLevel + granularityStep;

        switch (bucketZoomLevel) {
            case 7:
                return 590000;

            case 8:
                return 480000;

            case 9:
                return 350000;

            case 10:
                return 220000;

            case 11:
                return 158000;

            case 12:
                return 66000;

            case 13:
                return 25000;

            case 14:
                return 9700;

            case 15:
                return 4900;

            case 16:
                return 2000;

            case 17:
                return 800;

            default:
                return 720;
        }
    }

}
