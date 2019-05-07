package com.github.presto.querylog;

import com.facebook.presto.spi.eventlistener.EventListener;
import com.facebook.presto.spi.eventlistener.EventListenerFactory;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.Map;

import static java.util.Objects.requireNonNull;

public class QueryLogListenerFactory implements EventListenerFactory {
    private static final String QUERYLOG_CONFIG_LOCATION = "presto.querylog.log4j2.configLocation";
    private static final String QUERYLOG_TRACK_CREATED = "presto.querylog.log.queryCreatedEvent";
    private static final String QUERYLOG_TRACK_COMPLETED = "presto.querylog.log.queryCompletedEvent";
    private static final String QUERYLOG_TRACK_COMPLETED_SPLIT = "presto.querylog.log.splitCompletedEvent";

    private static final String QUERYLOG_CONFIG_LOCATION_ERROR = QUERYLOG_CONFIG_LOCATION + " is null";

    @Override
    public String getName() {
        return "querylog";
    }

    @Override
    public EventListener create(Map<String, String> map) {
        String log4j2ConfigLocation = requireNonNull(map.get(QUERYLOG_CONFIG_LOCATION), QUERYLOG_CONFIG_LOCATION_ERROR);
        LoggerContext loggerContext = Configurator.initialize("presto-querylog", log4j2ConfigLocation);
        boolean trackEventCreated = getBooleanConfig(map, QUERYLOG_TRACK_CREATED, true);
        boolean trackEventCompleted = getBooleanConfig(map, QUERYLOG_TRACK_COMPLETED, true);
        boolean trackEventCompletedSplit = getBooleanConfig(map, QUERYLOG_TRACK_COMPLETED_SPLIT, true);
        return new QueryLogListener(loggerContext, trackEventCreated, trackEventCompleted, trackEventCompletedSplit);
    }

    /**
     * Get {@code boolean} parameter value, or return default.
     *
     * @param params       Map of parameters
     * @param paramName    Parameter name
     * @param paramDefault Parameter default value
     * @return Parameter value or default.
     */
    private boolean getBooleanConfig(Map<String, String> params, String paramName, boolean paramDefault) {
        String value = params.get(paramName);
        if (value != null && !value.trim().isEmpty()) {
            return Boolean.parseBoolean(value);
        }
        return paramDefault;
    }
}
