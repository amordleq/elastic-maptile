package amordleq.elasticsearch.elastic.maptile

import reactor.blockhound.BlockHound
import reactor.core.publisher.Mono
import spock.lang.Specification

import java.time.Duration

class BlockHoundInstallationTests extends Specification {

    def setupSpec() {
        BlockHound.install(new CustomizeBlockHoundForEnvironment())
    }

    def "verify blockhound is installed correctly"() {
        when:
        Mono.delay(Duration.ofMillis(1)).doOnNext(next -> {
            Thread.sleep(10);
        }).block()

        then:
        thrown(Exception);
    }
}
