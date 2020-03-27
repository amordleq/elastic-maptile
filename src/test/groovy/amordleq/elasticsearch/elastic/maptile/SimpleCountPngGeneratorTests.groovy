package amordleq.elasticsearch.elastic.maptile

import spock.lang.Specification

import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel

class SimpleCountPngGeneratorTests extends Specification {

    SimpleCountPngGenerator generator = new SimpleCountPngGenerator()

    def "can generate a png from a number"() {
        expect:
        JFrame frame = showImage(generator.generatePng(43))
        while(frame?.visible) {
            sleep(1000);
        }
    }

    JFrame showImage(byte[] imageData) {
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
