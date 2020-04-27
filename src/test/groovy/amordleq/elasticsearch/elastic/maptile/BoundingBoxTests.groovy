package amordleq.elasticsearch.elastic.maptile

import spock.lang.Specification
import spock.lang.Unroll

class BoundingBoxTests extends Specification {

    @Unroll
    def "bounding boxes can be created from xyz tile coordinates"() {
        expect:
        new BoundingBox(x, y, z) == new BoundingBox(north, west, south, east)

        where:
        x | y | z || north             | west | south             | east
        1 | 1 | 3 || 79.17133464081945 | -135 | 66.51326044311186 | -90
        1 | 2 | 3 || 66.51326044311186 | -135 | 40.97989806962013 | -90
        1 | 3 | 3 || 40.97989806962013 | -135 | 0                 | -90
        3 | 3 | 3 || 40.97989806962013 | -45  | 0                 | 0
    }
}
