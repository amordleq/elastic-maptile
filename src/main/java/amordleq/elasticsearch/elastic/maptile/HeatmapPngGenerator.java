package amordleq.elasticsearch.elastic.maptile;

import lombok.Value;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGrid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Profile("demo")
@Component
public class HeatmapPngGenerator implements PngGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(HeatmapPngGenerator.class);

    @org.springframework.beans.factory.annotation.Value("${elastic.maptile.granularityStep}")
    private int granularityStep;

    private BluesColorScheme colorScheme = new BluesColorScheme();

    @Override
    public byte[] generatePng(MapTileGrid mapTileGrid) {
        BoundingBox tileBoundingBox = SphericalMercatorProjection.wgs84ToSphericalMercator(new BoundingBox(mapTileGrid.getCoordinates()));
        Scale scale = new Scale(tileBoundingBox, 256);

        BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, 256, 256);
        g2d.setComposite(AlphaComposite.SrcOver);

        LOG.trace("Tile bounds are: {},{},{},{}", tileBoundingBox.getNorth(), tileBoundingBox.getWest(), tileBoundingBox.getSouth(), tileBoundingBox.getEast());

        for (GeoGrid.Bucket bucket : mapTileGrid.getGrid().getBuckets()) {
            writeToGraphics(g2d, bucket, tileBoundingBox, scale, mapTileGrid.getCoordinates().getZ());
        }

        return imageToByteArray(img);
    }

    private void writeToGraphics(Graphics2D g2d, GeoGrid.Bucket bucket, BoundingBox tileBoundingBox, Scale scale, int z) {
        BoundingBox bucketBoundingBox = SphericalMercatorProjection.wgs84ToSphericalMercator(createBoundingBoxFromBucket(bucket));
        LOG.trace("Bucket bounds are:{},{},{},{}", bucketBoundingBox.getNorth(), bucketBoundingBox.getWest(), bucketBoundingBox.getSouth(), bucketBoundingBox.getEast());

        OffsetBox offsetBox = new OffsetBox(bucketBoundingBox, tileBoundingBox);
        LOG.trace("Offset:{}/{}", offsetBox.getOffsetX(), offsetBox.getOffsetY());
        LOG.trace("Extent:{}/{}", offsetBox.getWidth(), offsetBox.getHeight());

        g2d.setColor(calculateColorForBucket(bucket, z));
        g2d.fillRect(scale.scaleX(offsetBox), scale.scaleY(offsetBox), scale.scaleWidth(offsetBox), scale.scaleHeight(offsetBox));
    }

    private byte[] imageToByteArray(BufferedImage image) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream);

        try {
            ImageIO.write(image, "PNG", bufferedOutputStream);
        } catch (IOException e) {
            throw Exceptions.propagate(e);
        }

        return byteArrayOutputStream.toByteArray();
    }

    private BoundingBox createBoundingBoxFromBucket(GeoGrid.Bucket bucket) {
        String key = bucket.getKeyAsString();
        String[] zxy = key.split("/");
        int bucketZ = Integer.parseInt(zxy[0]);
        int bucketX = Integer.parseInt(zxy[1]);
        int bucketY = Integer.parseInt(zxy[2]);
        return new BoundingBox(bucketX, bucketY, bucketZ);
    }

    private Color calculateColorForBucket(GeoGrid.Bucket bucket, int zoomLevel) {
        long docCount = bucket.getDocCount();
        long maxDocCount = getMaxDocCountForZoomLevel(zoomLevel);
        double intensityScale = Math.log(docCount) / Math.log(maxDocCount);
        return colorScheme.getColor(intensityScale);
    }

    // TODO Not sure what to do with this. Obviously it's very cell tower-specific, so it doesn't belong here.
    // It would be nice if we could ask the database for these values.
    private long getMaxDocCountForZoomLevel(int zoomLevel) {
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

    @Value
    private static class OffsetBox {
        double offsetX;
        double offsetY;
        double width;
        double height;

        public OffsetBox(BoundingBox bucketBoundingBox, BoundingBox tileBoundingBox) {
            offsetX = bucketBoundingBox.getWest() - tileBoundingBox.getWest();
            offsetY = bucketBoundingBox.getNorth() - tileBoundingBox.getNorth();
            width = Math.abs(bucketBoundingBox.getWest() - bucketBoundingBox.getEast());
            height = Math.abs(bucketBoundingBox.getSouth() - bucketBoundingBox.getNorth());
        }
    }

    @Value
    private static class Scale {
        double xScale;
        double yScale;

        public Scale(BoundingBox tileBoundingBox, int imageSize) {
            xScale = Math.abs(tileBoundingBox.getWest() - tileBoundingBox.getEast()) / imageSize;
            yScale = Math.abs(tileBoundingBox.getSouth() - tileBoundingBox.getNorth()) / imageSize;
        }

        public int scaleX(OffsetBox offsetBox) {
            return Math.abs((int) Math.round(offsetBox.getOffsetX() / xScale));
        }

        public int scaleY(OffsetBox offsetBox) {
            return Math.abs((int) Math.round(offsetBox.getOffsetY() / yScale));
        }

        public int scaleWidth(OffsetBox offsetBox) {
            return Math.abs((int) Math.round(offsetBox.getWidth() / xScale));
        }

        public int scaleHeight(OffsetBox offsetBox) {
            return Math.abs((int) Math.round(offsetBox.getHeight() / yScale));
        }
    }
}
