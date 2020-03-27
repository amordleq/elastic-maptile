package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGrid;
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

    @Override
    public byte[] generatePng(int x, int y, int z, GeoGrid geoGrid) {
        //FIXME:  this is all kinds of broken.  it's also not very reactive nor particularly optimized for either readability
        //or performance.  basically, this is horrible right now

        BoundingBox tileBoundingBox = new BoundingBox(x, y, z);
        double xScale = (tileBoundingBox.getWest() - tileBoundingBox.getEast()) * 256;
        double yScale = (tileBoundingBox.getSouth() - tileBoundingBox.getNorth()) * 256;

        BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, 256, 256);

        g2d.setComposite(AlphaComposite.Src);
        g2d.setColor(Color.BLUE);

        System.out.println("Tile bounds are:"+tileBoundingBox.getNorth()+","+tileBoundingBox.getWest()+","+tileBoundingBox.getSouth()+","+tileBoundingBox.getEast());

        for(GeoGrid.Bucket bucket:geoGrid.getBuckets()){
            String key = bucket.getKeyAsString();
            String[] zxy = key.split("/");
            int bucketZ = Integer.parseInt(zxy[0]);
            int bucketX = Integer.parseInt(zxy[1]);
            int bucketY = Integer.parseInt(zxy[2]);

            BoundingBox bucketBoundingBox = new BoundingBox(bucketX, bucketY, bucketZ);
            System.out.println("Bucket bounds are:"+bucketBoundingBox.getNorth()+","+bucketBoundingBox.getWest()+","+bucketBoundingBox.getSouth()+","+bucketBoundingBox.getEast());
            double offsetX = bucketBoundingBox.getWest() - tileBoundingBox.getWest();
            double offsetY = bucketBoundingBox.getNorth() - tileBoundingBox.getNorth();
            double width = bucketBoundingBox.getWest() - bucketBoundingBox.getEast();
            double height = bucketBoundingBox.getSouth() - bucketBoundingBox.getNorth();
            System.out.println("Offset:"+offsetX+"/"+offsetY);
            System.out.println("Extent:"+width+"/"+height);

            int scaledX = Math.abs((int)(offsetX / xScale));
            int scaledY = Math.abs((int)(offsetY / yScale));
            int scaledWidth = Math.abs((int)(width / xScale));
            int scaledHeight = Math.abs((int)(height / yScale));

            g2d.drawRect(scaledX, scaledY, scaledWidth, scaledHeight);
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
