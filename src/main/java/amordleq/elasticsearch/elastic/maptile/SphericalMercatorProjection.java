package amordleq.elasticsearch.elastic.maptile;

public class SphericalMercatorProjection {

    private static final double RADIUS = 6378137;

    public static double latitudeToY(double lat) {
        return Math.log(Math.tan(Math.PI / 4 + Math.toRadians(lat) / 2)) * RADIUS;
    }

    public static double longitudeToX(double lon) {
        return Math.toRadians(lon) * RADIUS;
    }

    public static BoundingBox wgs84ToSphericalMercator(BoundingBox boundingBox) {
        return new BoundingBox(
                latitudeToY(boundingBox.getNorth()),
                longitudeToX(boundingBox.getWest()),
                latitudeToY(boundingBox.getSouth()),
                longitudeToX(boundingBox.getEast()));
    }

}
