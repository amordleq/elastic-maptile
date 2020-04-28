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

    @Unroll
    def "bounding boxes can be created from a zxy string"() {
        expect:
        new BoundingBox(tileName) == new BoundingBox(x, y, z)

        where:
        tileName || x | y | z
        '3/1/1'  || 1 | 1 | 3
        '3/1/2'  || 1 | 2 | 3
        '3/1/3'  || 1 | 3 | 3
        '3/2/3'  || 2 | 3 | 3
        '3/3/3'  || 3 | 3 | 3
    }

    @Unroll
    def "bounding boxes know their center points"() {
        expect:
        new BoundingBox(north, west, south, east).collect {
            [it.centerLatitude, it.centerLongitude]
        }.first() == [(double)centerLatitude, (double)centerLongitude]

        where:
        north | west | south | east || centerLatitude | centerLongitude
        80    | -135 | 60    | -90  || 70             | -112.5
        50    | -135 | 40    | -90  || 45             | -112.5
        40    | -135 | 0     | -90  || 20             | -112.5
        40    | -45  | 0     | 0    || 20             | -22.5
    }
}
