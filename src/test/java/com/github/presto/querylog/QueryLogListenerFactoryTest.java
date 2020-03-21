package com.github.presto.querylog;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class QueryLogListenerFactoryTest {

    @Test
    public void getName() {
        QueryLogListenerFactory listenerFactory = new QueryLogListenerFactory();
        assertEquals("presto-querylog", listenerFactory.getName());
    }

    @Test
    public void createWithoutConfigShouldThrowException() {
        // Given
        Map<String, String> configs = new HashMap<>();
        configs.put(QueryLogListenerFactory.QUERYLOG_CONFIG_LOCATION, null);
        // When
        QueryLogListenerFactory listenerFactory = new QueryLogListenerFactory();
        // Then
        assertThrows(
                NullPointerException.class,
                () -> listenerFactory.create(configs),
                QueryLogListenerFactory.QUERYLOG_CONFIG_LOCATION + " is null"
        );
    }
}