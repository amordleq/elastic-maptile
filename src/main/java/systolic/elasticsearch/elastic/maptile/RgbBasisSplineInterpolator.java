package systolic.elasticsearch.elastic.maptile;

import java.awt.*;
import java.util.List;

public class RgbBasisSplineInterpolator {

    private BasisSpline r;
    private BasisSpline g;
    private BasisSpline b;

    public RgbBasisSplineInterpolator(List<String> colors) {
        int[] rValues = new int[colors.size()];
        int[] gValues = new int[colors.size()];
        int[] bValues = new int[colors.size()];

        int index = 0;
        for (String item : colors) {
            Color color = parseColor(item);
            rValues[index] = color.getRed();
            gValues[index] = color.getGreen();
            bValues[index++] = color.getBlue();
        }

        r = new BasisSpline(rValues);
        g = new BasisSpline(gValues);
        b = new BasisSpline(bValues);
    }

    public Color interpolate(double t) {
        return new Color(
                r.interpolate(t),
                g.interpolate(t),
                b.interpolate(t)
        );
    }

    private static Color parseColor(String hexColor) {
        int r = Integer.parseInt(hexColor.substring(1, 3), 16);
        int g = Integer.parseInt(hexColor.substring(3, 5), 16);
        int b = Integer.parseInt(hexColor.substring(5, 7), 16);
        return new Color(r, g, b);
    }

    private static class BasisSpline {

        private int[] values;

        public BasisSpline(int[] values) {
            this.values = values;
        }

        public int interpolate(double t) {
            int i;
            int n = values.length - 1;

            if (t <= 0) {
                i = 0;
                t = 0;
            } else if (t >= 1) {
                t = 1;
                i = n - 1;
            } else {
                i = (int) Math.floor(t * n);
            }

            int v1 = values[i];
            int v2 = values[i + 1];
            int v0 = i > 0 ? values[i - 1] : 2 * v1 - v2;
            int v3 = i < n - 1 ? values[i + 2] : 2 * v2 - v1;
            return (int) Math.round(basis((t - i / (double) n) * n, v0, v1, v2, v3));
        }

        private static double basis(double t1, double v0, double v1, double v2, double v3) {
            double t2 = t1 * t1;
            double t3 = t2 * t1;
            return ((1 - 3 * t1 + 3 * t2 - t3) * v0
                    + (4 - 6 * t2 + 3 * t3) * v1
                    + (1 + 3 * t1 + 3 * t2 - 3 * t3) * v2
                    + t3 * v3) / 6;
        }
    }

}
