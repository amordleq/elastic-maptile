package amordleq.elasticsearch.elastic.maptile

import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.aggregations.bucket.geogrid.ParsedGeoTileGrid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.blockhound.BlockHound
import reactor.core.publisher.Hooks
import reactor.core.publisher.Mono
import spock.lang.Specification

// FIXME: this entire test approach is suspicious/wrong.  the method
// to query elasticsearch shouldn't even be public.  however, we're waiting
// for the next release of spring-data-elasticsearch before redoing any of this
// as the newer version makes certain testing options significantly easier
@SpringBootTest
class CellTowerRepositoryImplTests extends Specification {

    @Autowired
    CellTowerRepositoryImpl cellTowerRepository

    def setupSpec() {
        Hooks.onOperatorDebug()
        BlockHound.install(new CustomizeBlockHoundForEnvironment())
    }

    // FIXME: this test isn't actually doing what you might expect.  the problem
    // is that the custom reactive es client ends up causing things to happen on a
    // different thread pool (presumably because of how it's wrapping the async
    // calls from the standard es high level client).  so anything downstream
    // of that mono creation won't be properly checked because it won't be happening
    // on a thread blockhound knows is non-blocking.
    // that will all be fixed once we can switch to spring-data-elasticsearch 4.x
    // and remove the need for using the ReactiveRestHighLevelClient
    def "verify sunny day case contains no blocking calls"() {
        given:
        MapTileCoordinates coordinates = new MapTileCoordinates(1, 2, 3);

        when:
        cellTowerRepository.queryElasticsearch(coordinates, null, null).block()

        then:
        notThrown(Exception.class)
    }

    def "elastic search query can be issued based on map tile coordinates"() {
        given:
        MapTileCoordinates coordinates = new MapTileCoordinates(1, 2, 3);

        when:
        SearchResponse response = cellTowerRepository.queryElasticsearch(coordinates, null, null).block();

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
        SearchResponse response = cellTowerRepository.queryElasticsearch(
                coordinates,
                null,
                QueryBuilders.termQuery("radio", "GSM"),
        ).block();

        then:
        response.status() == RestStatus.OK
        response.aggregations
        response.aggregations.get("agg") instanceof ParsedGeoTileGrid
        ((ParsedGeoTileGrid) response.aggregations.get("agg")).getBuckets().size() == 2920
    }

    def "can count all documents"() {
        expect:
        cellTowerRepository.countAll().block() == 41857886
    }

    def "can count subset of documents with bounding box and filter"() {
        given:
        BoundingBox region = new BoundingBox(10, -10, -10, 10)
        QueryBuilder additionalFilter = QueryBuilders.termQuery("radio", "GSM")

        when:
        Mono<Long> count = cellTowerRepository.countInRegion(region, additionalFilter)

        then:
        count.block() == 146600
    }
}
