package com.github.presto.querylog;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class QueryLogListenerFactoryTest {

    @Test
    public void getName() {
        QueryLogListenerFactory listenerFactory = new QueryLogListenerFactory();
        assertEquals("presto-querylog2", listenerFactory.getName());
    }

//    @Test
//    public void create() {
//    }
}