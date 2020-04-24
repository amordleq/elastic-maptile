package amordleq.elasticsearch.elastic.maptile;

import reactor.core.Exceptions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class TileCoordinatesPngGenerator implements PngGenerator {

    @Override
    public byte[] generatePng(MapTileGrid response) {
        BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, 256, 256);

        g2d.setComposite(AlphaComposite.Src);
        g2d.setColor(Color.WHITE);
        g2d.drawLine(0, 0, 0, 256);
        g2d.drawLine(0, 0, 256, 0);
        g2d.drawLine(256, 0, 256, 256);
        g2d.drawLine(0, 256, 256, 256);
        g2d.drawString(response.getCoordinates().toString(), 256/3, 256/2);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, "PNG", byteArrayOutputStream);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw Exceptions.propagate(e);
        }
    }
}
