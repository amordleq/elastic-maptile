package amordleq.elasticsearch.elastic.maptile

import spock.lang.Specification

class CellTowerLocationCountTests extends Specification {

    def "location is based on the center of the provided area"() {
        when:
        CellTowerLocationCount locationCount = new CellTowerLocationCount(
                new BoundingBox(50.0d, -100.0d, 10.0d, -50.0d),
                100
        )

        then:
        locationCount.location == new Location(30.0d, -75.0d)
    }
}
