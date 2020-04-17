package amordleq.elasticsearch.elastic.maptile

import reactor.blockhound.BlockHound
import reactor.blockhound.integration.BlockHoundIntegration

class CustomizeBlockHoundForEnvironment implements BlockHoundIntegration {

    Boolean logErrorsOnly

    CustomizeBlockHoundForEnvironment(Boolean logErrorsOnly = Boolean.FALSE) {
        this.logErrorsOnly = logErrorsOnly;
    }

    @Override
    void applyTo(BlockHound.Builder builder) {
        if (logErrorsOnly) {
            builder.blockingMethodCallback(it -> {
                new Exception(it.toString()).printStackTrace();
            })
        }

        builder.allowBlockingCallsInside("io.netty.util.concurrent.GlobalEventExecutor", "addTask");
        builder.allowBlockingCallsInside("io.netty.util.concurrent.GlobalEventExecutor", "takeTask");

        builder.allowBlockingCallsInside("org.elasticsearch.common.xcontent.XContentHelper", "toXContent");
    }


}
