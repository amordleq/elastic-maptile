package amordleq.elasticsearch.elastic.maptile

import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.blockhound.BlockHound
import reactor.core.publisher.Hooks
import reactor.core.publisher.Mono
import spock.lang.Specification

@SpringBootTest
class CountProviderTests extends Specification {

    @Autowired
    CountProvider countProvider

    def setupSpec() {
        Hooks.onOperatorDebug()
        BlockHound.install(new CustomizeBlockHoundForEnvironment())
    }

    def "verify sunny day case contains no blocking calls"() {
        given:
        Mono underTest = countProvider.countAll()

        when:
        underTest.block()

        then:
        notThrown(Exception.class)
    }

    def "can count all documents"() {
        expect:
        countProvider.countAll().block() == 41857886
    }

    def "can count subset of documents with bounding box and filter"() {
        given:
        BoundingBox region = new BoundingBox(10, -10, -10, 10)
        QueryBuilder additionalFilter = QueryBuilders.termQuery("radio", "GSM")

        when:
        Mono<Long> count = countProvider.countInRegion(region, additionalFilter)

        then:
        count.block() == 146600
    }
}
