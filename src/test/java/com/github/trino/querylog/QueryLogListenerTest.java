package com.github.trino.querylog;

import io.trino.spi.eventlistener.QueryContext;
import io.trino.spi.eventlistener.QueryCreatedEvent;
import io.trino.spi.eventlistener.QueryMetadata;
import io.trino.spi.eventlistener.SplitCompletedEvent;
import io.trino.spi.eventlistener.SplitStatistics;
import io.trino.spi.resourcegroups.QueryType;
import io.trino.spi.resourcegroups.ResourceGroupId;
import io.trino.spi.session.ResourceEstimates;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;

// Those are just a few very crude tests.
// TODO: Add more cases with proper structure.
// TODO: Test actual JSON output, not just its presence.
class QueryLogListenerTest {

    @Test
    void queryCreatedEvents() throws IOException {
        try (LoggerContext loggerContext = Configurator.initialize(
                "queryCreatedEvents",
                "classpath:queryCreatedEvents.xml"
        )) {
            // Given there is a listener for query created event
            QueryLogListener listener = new QueryLogListener(
                    loggerContext,
                    true, true, true
            );

            // When two events are created
            listener.queryCreated(prepareQueryCreatedEvent());
            listener.queryCreated(prepareQueryCreatedEvent());

            // Then two events should be present in the log file
            long logEventsCount = Files.lines(Paths.get("target/queryCreatedEvents.log")).count();
            assertEquals(2, logEventsCount);
        }
    }


    @Test
    void onlyQueryCreatedEvents() throws IOException {
        try (LoggerContext loggerContext = Configurator.initialize(
                "onlyQueryCreatedEvents",
                "classpath:onlyQueryCreatedEvents.xml"
        )) {
            // Given there is a listener for query created event
            QueryLogListener listener = new QueryLogListener(
                    loggerContext,
                    true, false, false
            );

            // When one created event is created
            //  And one split completed event is created
            listener.queryCreated(prepareQueryCreatedEvent());
            listener.splitCompleted(prepareSplitCompletedEvent());

            // Then only created event should be present in the log file
            long logEventsCount = Files.lines(Paths.get("target/onlyQueryCreatedEvents.log")).count();
            assertEquals(1, logEventsCount);
        }
    }

    private QueryCreatedEvent prepareQueryCreatedEvent() {
        return new QueryCreatedEvent(
                Instant.now(),
                prepareQueryContext(),
                prepareQueryMetadata()
        );
    }

    private SplitCompletedEvent prepareSplitCompletedEvent() {
        return new SplitCompletedEvent(
                "queryId",
                "stageId",
                "taskId",
                Optional.of("catalogName"),
                Instant.now(),
                Optional.of(Instant.now()),
                Optional.of(Instant.now()),
                getSplitStatistics(),
                Optional.empty(),
                "payload"
        );
    }

    private SplitStatistics getSplitStatistics() {
        return new SplitStatistics(
                ofMillis(1000),
                ofMillis(2000),
                ofMillis(3000),
                ofMillis(4000),
                1,
                2,
                Optional.of(Duration.ofMillis(100)),
                Optional.of(Duration.ofMillis(200))
        );
    }

    private QueryMetadata prepareQueryMetadata() {
        return new QueryMetadata(
                "queryId",
                Optional.empty(),
                "query",
                Optional.of("updateType"),
                Optional.of("preparedQuery"),
                "queryState",
                List.of(),
                List.of(),
                URI.create("http://localhost"),
                Optional.empty(), Optional.empty()
        );
    }

    private QueryContext prepareQueryContext() {
        return new QueryContext(
                "user",
                Optional.of("principal"),
                Set.of(), // groups
                Optional.empty(), // traceToken
                Optional.empty(), // remoteClientAddress
                Optional.empty(), // userAgent
                Optional.empty(), // clientInfo
                new HashSet<>(), // clientTags
                new HashSet<>(), // clientCapabilities
                Optional.of("source"),
                Optional.of("catalog"),
                Optional.of("schema"),
                Optional.of(new ResourceGroupId("name")),
                new HashMap<>(), // sessionProperties
                new ResourceEstimates(Optional.empty(), Optional.empty(), Optional.of(1000L)),
                "serverAddress", "serverVersion", "environment",
                Optional.of(QueryType.SELECT)
        );
    }
}