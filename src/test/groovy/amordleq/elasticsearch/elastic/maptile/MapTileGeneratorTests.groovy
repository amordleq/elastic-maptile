package amordleq.elasticsearch.elastic.maptile

import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.aggregations.bucket.geogrid.ParsedGeoTileGrid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.blockhound.BlockHound
import reactor.core.publisher.Hooks
import spock.lang.Ignore
import spock.lang.Specification

@SpringBootTest
class MapTileGeneratorTests extends Specification {

    @Autowired
    MapTileGenerator mapTileGenerator

    def setupSpec() {
        Hooks.onOperatorDebug()
        // FIXME: see test below
        //BlockHound.install(new CustomizeBlockHoundForElasticSearch())
    }

    // FIXME: this test isn't actually doing what you might expect.  the problem
    // is that the custom reactive es client ends up causing things to happen on a
    // different thread pool (presumably because of how it's wrapping the async
    // calls from the standard es high level client).  so anything downstream
    // of that mono creation won't be properly checked because it won't be happening
    // on a thread blockhound knows is non-blocking.
    @Ignore("because i'm having problems with blockhound when running from gradle command line right now")
    def "verify sunny day case contains no blocking calls"() {
        given:
        MapTileCoordinates coordinates = new MapTileCoordinates(1, 2, 3);

        when:
        mapTileGenerator.queryElasticsearch(coordinates).block()

        then:
        notThrown(Exception.class)
    }

    def "elastic search query can be issued based on map tile coordinates"() {
        given:
        MapTileCoordinates coordinates = new MapTileCoordinates(1, 2, 3);

        when:
        SearchResponse response = mapTileGenerator.queryElasticsearch(coordinates).block();

        then:
        response.status() == RestStatus.OK
        response.aggregations
        response.aggregations.get("agg") instanceof ParsedGeoTileGrid
        ((ParsedGeoTileGrid) response.aggregations.get("agg")).getBuckets().size() == 4797
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
        ((ParsedGeoTileGrid) response.aggregations.get("agg")).getBuckets().size() == 2920
    }

}
