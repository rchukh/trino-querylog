/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.trino.querylog;

import io.trino.spi.eventlistener.EventListener;
import io.trino.spi.eventlistener.QueryCompletedEvent;
import io.trino.spi.eventlistener.QueryCreatedEvent;
import io.trino.spi.eventlistener.SplitCompletedEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.message.ObjectMessage;

public class QueryLogListener implements EventListener {
    private final Logger logger;
    private final boolean trackEventCreated;
    private final boolean trackEventCompleted;
    private final boolean trackEventCompletedSplit;

    public QueryLogListener(final LoggerContext loggerContext,
                            final boolean trackEventCreated,
                            final boolean trackEventCompleted,
                            final boolean trackEventCompletedSplit) {
        this.trackEventCreated = trackEventCreated;
        this.trackEventCompleted = trackEventCompleted;
        this.trackEventCompletedSplit = trackEventCompletedSplit;
        this.logger = loggerContext.getLogger(QueryLogListener.class.getName());
    }

    @Override
    public void queryCreated(final QueryCreatedEvent queryCreatedEvent) {
        if (trackEventCreated) {
            logger.info(new ObjectMessage(queryCreatedEvent));
        }
    }

    @Override
    public void queryCompleted(final QueryCompletedEvent queryCompletedEvent) {
        if (trackEventCompleted) {
            logger.info(new ObjectMessage(queryCompletedEvent));
        }
    }

    @Override
    public void splitCompleted(final SplitCompletedEvent splitCompletedEvent) {
        if (trackEventCompletedSplit) {
            logger.info(new ObjectMessage(splitCompletedEvent));
        }
    }
}
