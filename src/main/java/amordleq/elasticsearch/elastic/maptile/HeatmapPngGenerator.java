package amordleq.elasticsearch.elastic.maptile;

import lombok.Value;
import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGrid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

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

    @Override
    public Mono<byte[]> generatePng(MapTileGrid mapTileGrid) {
        BoundingBox tileBoundingBox = new BoundingBox(mapTileGrid.getCoordinates());
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

        return Mono.just(imageToByteArray(img));
    }

    private void writeToGraphics(Graphics2D g2d, GeoGrid.Bucket bucket, BoundingBox tileBoundingBox, Scale scale, int z) {
        BoundingBox bucketBoundingBox = createBoundingBoxFromBucket(bucket);
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
        double intensityScale = docCount / (5000f / (zoomLevel + 2));

        int redMin = 8;
        int greenMin = 48;
        int blueMin = 107;

        int redMax = 247;
        int greenMax = 251;
        int blueMax = 255;

        int red = (int) (((redMax - redMin) * intensityScale) + redMin);
        int green = (int) (((greenMax - greenMin) * intensityScale) + greenMin);
        int blue = (int) (((blueMax - blueMin) * intensityScale) + blueMin);

        return new Color(Math.min(red, 255), Math.min(green, 255), Math.min(blue, 255));
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
            return Math.abs((int) (offsetBox.getOffsetX() / xScale));
        }

        public int scaleY(OffsetBox offsetBox) {
            return Math.abs((int)Math.floor(offsetBox.getOffsetY() / yScale));
        }

        public int scaleWidth(OffsetBox offsetBox) {
            return Math.abs((int) (offsetBox.getWidth() / xScale));
        }

        public int scaleHeight(OffsetBox offsetBox) {
            return Math.abs((int)Math.ceil(offsetBox.getHeight() / yScale));
        }
    }
}
