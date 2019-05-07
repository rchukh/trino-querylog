package com.github.presto.querylog;

import com.facebook.presto.spi.Plugin;
import com.facebook.presto.spi.eventlistener.EventListenerFactory;

import java.util.Collections;

public class QueryLogPlugin implements Plugin {

    @Override
    public Iterable<EventListenerFactory> getEventListenerFactories() {
        return Collections.singletonList(new QueryLogListenerFactory());
    }
}