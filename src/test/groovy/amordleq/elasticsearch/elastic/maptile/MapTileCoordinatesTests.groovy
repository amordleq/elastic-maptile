package amordleq.elasticsearch.elastic.maptile

import spock.lang.Specification
import spock.lang.Unroll

class MapTileCoordinatesTests extends Specification {

    @Unroll
    def "coordinates can be constructed from tile name"() {
        expect:
        new MapTileCoordinates(tileName) == new MapTileCoordinates(x, y, z)

        where:
        tileName || x | y | z
        '3/1/1'  || 1 | 1 | 3
        '3/1/2'  || 1 | 2 | 3
        '3/1/3'  || 1 | 3 | 3
        '3/2/3'  || 2 | 3 | 3
        '3/3/3'  || 3 | 3 | 3
    }
}
