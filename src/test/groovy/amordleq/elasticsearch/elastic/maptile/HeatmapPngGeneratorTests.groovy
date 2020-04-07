package amordleq.elasticsearch.elastic.maptile

import org.elasticsearch.search.aggregations.bucket.geogrid.GeoGrid
import spock.lang.Specification

import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel

class HeatmapPngGeneratorTests extends Specification {

    HeatmapPngGenerator generator = new HeatmapPngGenerator()

    def "can generate a heatmap png from a geogrid"() {
        given:
        int tileX = 1
        int tileY = 2
        int tileZ = 4

        and:
        GeoGrid.Bucket bucket = Mock()
        GeoGrid geoGrid = Mock()
        geoGrid.buckets >> [bucket]
        bucket.getKeyAsString() >> "5/2/4"

        expect:
        JFrame frame = showImage(generator.generatePng(tileX, tileY, tileZ, geoGrid).block())
        while(frame?.visible) {
            sleep(1000);
        }
    }

    private static JFrame showImage(byte[] imageData) {
        JFrame frame = new JFrame();
        ImageIcon icon = new ImageIcon(imageData);
        JLabel label = new JLabel(icon);
        frame.add(label);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        return frame;
    }
}
