package amordleq.elasticsearch.elastic.maptile

import spock.lang.Specification

import java.awt.Color

class RgbBasisSplineInterpolatorTests extends Specification {

    def "can interpolate between two colors"() {
        given:
        RgbBasisSplineInterpolator rgbBasisSplineInterpolator = new RgbBasisSplineInterpolator(List.of('#000000', '#ffffff'))

        expect:
        rgbBasisSplineInterpolator.interpolate(0) == new Color(0, 0, 0)
        rgbBasisSplineInterpolator.interpolate(0.5) == new Color(128, 128, 128)
        rgbBasisSplineInterpolator.interpolate(1) == new Color(255, 255, 255)
    }

}
