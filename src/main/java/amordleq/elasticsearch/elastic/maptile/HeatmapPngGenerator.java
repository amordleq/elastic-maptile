package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGrid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Profile("demo")
@Component
public class HeatmapPngGenerator implements PngGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(HeatmapPngGenerator.class);

    @Override
    public byte[] generatePng(int x, int y, int z, GeoGrid geoGrid) {
        //FIXME:  this is all kinds of broken.  it's also not very reactive nor particularly optimized for either readability
        //or performance.  basically, this is horrible right now

        BoundingBox tileBoundingBox = new BoundingBox(x, y, z);
        double xScale = Math.abs(tileBoundingBox.getWest() - tileBoundingBox.getEast()) / 256;
        double yScale = Math.abs(tileBoundingBox.getSouth() - tileBoundingBox.getNorth()) / 256;

        BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, 256, 256);

        AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f);
        g2d.setComposite(alphaComposite);
        g2d.setColor(Color.BLUE);

        LOG.trace("Tile bounds are: {},{},{},{}", tileBoundingBox.getNorth(), tileBoundingBox.getWest(), tileBoundingBox.getSouth(), tileBoundingBox.getEast());

        for (GeoGrid.Bucket bucket : geoGrid.getBuckets()) {
            String key = bucket.getKeyAsString();
            long docCount = bucket.getDocCount();
            double intensityScale = docCount / (30000f / z);
            int intensity = Math.min((int) (255 * intensityScale), 240);
            g2d.setColor(new Color(intensity, intensity, 255));

            String[] zxy = key.split("/");
            int bucketZ = Integer.parseInt(zxy[0]);
            int bucketX = Integer.parseInt(zxy[1]);
            int bucketY = Integer.parseInt(zxy[2]);

            BoundingBox bucketBoundingBox = new BoundingBox(bucketX, bucketY, bucketZ);
            LOG.trace("Bucket bounds are:{},{},{},{}", bucketBoundingBox.getNorth(), bucketBoundingBox.getWest(), bucketBoundingBox.getSouth(), bucketBoundingBox.getEast());
            double offsetX = bucketBoundingBox.getWest() - tileBoundingBox.getWest();
            double offsetY = bucketBoundingBox.getNorth() - tileBoundingBox.getNorth();
            double width = Math.abs(bucketBoundingBox.getWest() - bucketBoundingBox.getEast());
            double height = Math.abs(bucketBoundingBox.getSouth() - bucketBoundingBox.getNorth());
            LOG.trace("Offset:{}/{}", offsetX, offsetY);
            LOG.trace("Extent:{}/{}", width, height);

            int scaledX = Math.abs((int) (offsetX / xScale));
            int scaledY = Math.abs((int) (offsetY / yScale));
            int scaledWidth = Math.abs((int) (width / xScale));
            int scaledHeight = Math.abs((int) (height / yScale));

            g2d.fillRect(scaledX, scaledY, scaledWidth, scaledHeight);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "PNG", byteArrayOutputStream);
        } catch (IOException e) {
            throw Exceptions.propagate(e);
        }
        return byteArrayOutputStream.toByteArray();
    }
}
