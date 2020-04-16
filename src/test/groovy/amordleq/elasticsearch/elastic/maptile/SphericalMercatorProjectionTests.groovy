package amordleq.elasticsearch.elastic.maptile

import spock.lang.Specification

class SphericalMercatorProjectionTests extends Specification {

    def "transforms latitudes to y values in meters"() {
        expect:
        SphericalMercatorProjection.latitudeToY(44) == 5465442.183322753
    }

    def "transforms longitude to x values in meters"() {
        expect:
        SphericalMercatorProjection.longitudeToX(22) == 2449028.7974520186
    }

    def "transforms bounding box in WGS84 to spherical mercator coordinates"() {
        expect:
        SphericalMercatorProjection.wgs84ToSphericalMercator(new BoundingBox(44, -22, -44, 22)) == new BoundingBox(
                5465442.183322753,
                -2449028.7974520186,
                -5465442.183322752,
                2449028.7974520186
        )
    }

}
