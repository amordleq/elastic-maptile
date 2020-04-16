package amordleq.elasticsearch.elastic.maptile

import reactor.blockhound.BlockHound
import reactor.blockhound.integration.BlockHoundIntegration

class CustomizeBlockHoundForElasticSearch implements BlockHoundIntegration {

    @Override
    void applyTo(BlockHound.Builder builder) {
        builder.allowBlockingCallsInside("org.elasticsearch.common.xcontent.XContentHelper", "toXContent")
    }
}
