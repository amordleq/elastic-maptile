package amordleq.elasticsearch.elastic.maptile;

import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.Exceptions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Profile("simple")
@Component
public class SimpleCountPngGenerator implements PngGenerator {

    @Override
    public byte[] generatePng(MapTileGrid response) {
        try {
            Long totalCount = response.getGrid().getBuckets().stream()
                    .map(MultiBucketsAggregation.Bucket::getDocCount)
                    .reduce(0L, Long::sum);
            return generatePng(totalCount);
        } catch (IOException e) {
            throw Exceptions.propagate(e);
        }
    }

    public byte[] generatePng(Long count) throws IOException {
        BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, 256, 256);

        g2d.setComposite(AlphaComposite.Src);
        g2d.setColor(Color.BLACK);
        g2d.drawLine(0, 0, 0, 256);
        g2d.drawLine(0, 0, 256, 0);
        g2d.drawLine(256, 0, 256, 256);
        g2d.drawLine(0, 256, 256, 256);
        g2d.drawString(count.toString(), 256/2, 256/2);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
