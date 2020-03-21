package com.github.presto.querylog;

import io.prestosql.spi.eventlistener.QueryContext;
import io.prestosql.spi.eventlistener.QueryCreatedEvent;
import io.prestosql.spi.eventlistener.QueryMetadata;
import io.prestosql.spi.eventlistener.SplitCompletedEvent;
import io.prestosql.spi.eventlistener.SplitStatistics;
import io.prestosql.spi.session.ResourceEstimates;
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
import java.util.Optional;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;

// Those are just a few very crude tests.
// TODO: Add more cases with proper structure.
// TODO: Test actual JSON output, not just its presence.
public class QueryLogListenerTest {

    @Test
    public void queryCreatedEvents() throws IOException {
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
    public void onlyQueryCreatedEvents() throws IOException {
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
                "queryId", Optional.empty(),
                "query",
                Optional.of("preparedQuery"),
                "queryState",
                URI.create("http://localhost"),
                Optional.empty(), Optional.empty()
        );
    }

    private QueryContext prepareQueryContext() {
        return new QueryContext(
                "user",
                Optional.of("principal"),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), new HashSet<>(), new HashSet<>(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), new HashMap<>(),
                new ResourceEstimates(Optional.empty(), Optional.empty(), Optional.of(1000L)),
                "serverAddress", "serverVersion", "environment"
        );
    }
}