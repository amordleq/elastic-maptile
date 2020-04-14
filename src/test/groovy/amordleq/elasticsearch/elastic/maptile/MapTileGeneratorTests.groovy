package amordleq.elasticsearch.elastic.maptile

import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.aggregations.bucket.geogrid.ParsedGeoTileGrid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class MapTileGeneratorTests extends Specification {

    @Autowired
    MapTileGenerator mapTileGenerator

    def "elastic search query can be issued based on map tile coordinates"() {
        given:
        MapTileCoordinates coordinates = new MapTileCoordinates(1, 2, 3);

        when:
        SearchResponse response = mapTileGenerator.queryElasticsearch(coordinates).block();

        then:
        response.status() == RestStatus.OK
        response.aggregations
        response.aggregations.get("agg") instanceof ParsedGeoTileGrid
        ((ParsedGeoTileGrid)response.aggregations.get("agg")).getBuckets().size() == 4797
    }

    def "elasticsearch query can be limited with an additional filter"() {
        given:
        MapTileCoordinates coordinates = new MapTileCoordinates(1, 2, 3);

        when:
        SearchResponse response = mapTileGenerator.queryElasticsearch(
                coordinates,
                QueryBuilders.termQuery("radio", "GSM")
        ).block();

        then:
        response.status() == RestStatus.OK
        response.aggregations
        response.aggregations.get("agg") instanceof ParsedGeoTileGrid
        ((ParsedGeoTileGrid)response.aggregations.get("agg")).getBuckets().size() == 2920
    }
}
