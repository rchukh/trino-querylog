package com.github.trino.querylog;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class QueryLogListenerFactoryTest {

    @Test
    void getName() {
        QueryLogListenerFactory listenerFactory = new QueryLogListenerFactory();
        assertEquals("trino-querylog", listenerFactory.getName());
    }

    @Test
    void createWithoutConfigShouldThrowException() {
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