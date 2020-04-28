package systolic.elasticsearch.elastic.maptile;

import lombok.Value;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGrid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HeatmapPngGenerator implements PngGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(HeatmapPngGenerator.class);

    private final ColorScheme colorScheme;

    public HeatmapPngGenerator(ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

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
            writeToGraphics(g2d, bucket, tileBoundingBox, scale, colorScheme);
        }

        return imageToByteArray(img);
    }

    private void writeToGraphics(Graphics2D g2d, GeoGrid.Bucket bucket, BoundingBox tileBoundingBox, Scale scale, ColorScheme colorScheme) {
        BoundingBox bucketBoundingBox = SphericalMercatorProjection.wgs84ToSphericalMercator(new BoundingBox(bucket.getKeyAsString()));
        LOG.trace("Bucket bounds are:{},{},{},{}", bucketBoundingBox.getNorth(), bucketBoundingBox.getWest(), bucketBoundingBox.getSouth(), bucketBoundingBox.getEast());

        OffsetBox offsetBox = new OffsetBox(bucketBoundingBox, tileBoundingBox);
        LOG.trace("Offset:{}/{}", offsetBox.getOffsetX(), offsetBox.getOffsetY());
        LOG.trace("Extent:{}/{}", offsetBox.getWidth(), offsetBox.getHeight());

        g2d.setColor(colorScheme.getColor(bucket));
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
