package amordleq.elasticsearch.elastic.maptile;

import java.awt.*;
import java.util.List;

public class BluesColorScheme {

    private RgbBasisSplineInterpolator interpolator = new RgbBasisSplineInterpolator(List.of(
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

    public Color getColor(double intensityScale) {
        return interpolator.interpolate(intensityScale);
    }

}
