package amordleq.elasticsearch.elastic.maptile

import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import spock.lang.Specification

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MapTileControllerTests extends Specification {

    @SpringBean
    MapTileService mapTileService = Mock()

    @Autowired
    WebTestClient webClient

    def "can fetch test tiles"() {
        given:
        byte[] expectedImage = "Test".bytes
        mapTileService.generateTestTile(_, _) >> Mono.just(expectedImage)

        expect:
        webClient.get().uri('/test/3/1/2.png')
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_PNG_VALUE)
                .expectBody(byte[])
                .isEqualTo(expectedImage)
    }

    def "can fetch count tiles"() {
        given:
        byte[] expectedImage = "Count".bytes
        mapTileService.generateCountTile(_, _) >> Mono.just(expectedImage)

        expect:
        webClient.get().uri('/count/3/1/2.png')
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_PNG_VALUE)
                .expectBody(byte[])
                .isEqualTo(expectedImage)
    }

    def "can fetch heatmap tiles"() {
        given:
        byte[] expectedImage = "Heatmap".bytes
        mapTileService.generateHeatmapTile(_, _) >> Mono.just(expectedImage)

        expect:
        webClient.get().uri('/heatmap/3/1/2.png')
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_PNG_VALUE)
                .expectBody(byte[])
                .isEqualTo(expectedImage)
    }

    def "can fetch subterm tiles"() {
        given:
        byte[] expectedImage = "Subterm".bytes
        mapTileService.generateSubTermsTile(_, "testTerm", _) >> Mono.just(expectedImage)

        expect:
        webClient.get().uri('/subterm/testTerm/3/1/2.png')
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.IMAGE_PNG_VALUE)
                .expectBody(byte[])
                .isEqualTo(expectedImage)
    }

}
