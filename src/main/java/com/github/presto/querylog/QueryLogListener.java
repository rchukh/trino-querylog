package com.github.presto.querylog;

import com.facebook.presto.spi.eventlistener.EventListener;
import com.facebook.presto.spi.eventlistener.QueryCompletedEvent;
import com.facebook.presto.spi.eventlistener.QueryCreatedEvent;
import com.facebook.presto.spi.eventlistener.SplitCompletedEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;


public class QueryLogListener implements EventListener {
    private final Logger logger;
    private final boolean trackEventCreated;
    private final boolean trackEventCompleted;
    private final boolean trackEventCompletedSplit;
    private final String dlm;
    private final Utilities utilities;
    private String appName;

    public QueryLogListener(final LoggerContext loggerContext,
                            final boolean trackEventCreated,
                            final boolean trackEventCompleted,
                            final boolean trackEventCompletedSplit) {
        this.trackEventCreated = trackEventCreated;
        this.trackEventCompleted = trackEventCompleted;
        this.trackEventCompletedSplit = trackEventCompletedSplit;
        this.logger = loggerContext.getLogger(QueryLogListener.class.getName());
        this.dlm = "|";//"\035";
        this.utilities = new Utilities();
        this.appName = utilities.getApplicationName();

    }

    @Override
    public void queryCreated(final QueryCreatedEvent queryCreatedEvent) {
        if (trackEventCreated) {
            logger.debug(queryCreatedEvent.getMetadata().getQuery()+dlm+queryCreatedEvent.getCreateTime());

        }
    }

    @Override
    public void queryCompleted(final QueryCompletedEvent queryCompletedEvent) {
        if(appName==null) {
            logger.info("appName:"+appName);
            appName = utilities.getApplicationName();
        }
        logger.info("appName not empty:"+appName+":endofappName");
        appName = utilities.getApplicationName();
        logger.info("appName retied:"+appName+":endofappName");

        if (trackEventCompleted) {
            StringBuilder msg =  new StringBuilder();
            String queryStatus = "succeeded";

            if (queryCompletedEvent.getMetadata().getQueryState() != "FINISHED"){
                queryStatus = "failed";
            }


            try {
                msg.append(appName+dlm);
                msg.append(queryStatus + dlm);
                if(queryCompletedEvent.getFailureInfo().isPresent()) {
                    msg.append(queryCompletedEvent.getFailureInfo().get().getErrorCode().getName() + dlm);
                }else {
                    msg.append(dlm);
                }

                msg.append(queryCompletedEvent.getMetadata().getQueryId()+dlm);
                msg.append(queryCompletedEvent.getContext().getUser()+dlm);

                if (queryCompletedEvent.getContext().getSource().isPresent()) {
                    msg.append(queryCompletedEvent.getContext().getSource().get() + dlm);
                }else{
                    msg.append(dlm);
                }
                if(queryCompletedEvent.getContext().getRemoteClientAddress().isPresent()){
                    msg.append(queryCompletedEvent.getContext().getRemoteClientAddress().get() + dlm);
                }else{
                    msg.append(dlm);
                }
                if (queryCompletedEvent.getContext().getUserAgent().isPresent()) {
                    msg.append(queryCompletedEvent.getContext().getUserAgent().get()+dlm);
                }else {
                    msg.append(dlm);
                }
                msg.append(queryCompletedEvent.getCreateTime()+dlm);
                msg.append(queryCompletedEvent.getStatistics().getQueuedTime().getSeconds()+dlm);
                msg.append(queryCompletedEvent.getExecutionStartTime()+dlm);
                msg.append(queryCompletedEvent.getStatistics().getCpuTime().getSeconds()+dlm);
                msg.append(queryCompletedEvent.getStatistics().getWallTime().getSeconds()+dlm);
                if(queryCompletedEvent.getStatistics().getDistributedPlanningTime().isPresent()){
                    msg.append(queryCompletedEvent.getStatistics().getDistributedPlanningTime().get().getSeconds()+dlm);
                }else {
                    msg.append(dlm);
                }
                msg.append(utilities.normalizeBytes(queryCompletedEvent.getStatistics().getCumulativeMemory())+dlm);
                msg.append(utilities.normalizeBytes(queryCompletedEvent.getStatistics().getPeakUserMemoryBytes())+dlm);
                msg.append(utilities.normalizeBytes(queryCompletedEvent.getStatistics().getPeakTotalNonRevocableMemoryBytes())+dlm);
                msg.append(utilities.normalizeBytes(queryCompletedEvent.getStatistics().getPeakUserMemoryBytes())+dlm);
                msg.append(queryCompletedEvent.getEndTime()+dlm);
                msg.append(utilities.normalizeBytes(queryCompletedEvent.getStatistics().getOutputBytes())+dlm);
                msg.append(utilities.normalizeBytes(queryCompletedEvent.getStatistics().getTotalBytes())+dlm);
                msg.append(queryCompletedEvent.getStatistics().getOutputRows()+dlm);
                msg.append(queryCompletedEvent.getMetadata().getQuery().replace("\n"," ")+dlm);
                if(queryCompletedEvent.getStatistics().getAnalysisTime().isPresent()) {
                    msg.append(queryCompletedEvent.getStatistics().getAnalysisTime().get().getSeconds()+dlm);
                }else {
                    msg.append(dlm);
                }
                msg.append(queryCompletedEvent.getMetadata().getQueryState()); // FAILED, FINISHED



                logger.info(msg.toString());

            } catch (Exception ex) {

                logger.error(ex.getMessage());
            }
        }
    }

    @Override
    public void splitCompleted(final SplitCompletedEvent splitCompletedEvent) {
        if (trackEventCompletedSplit) {
            logger.debug(splitCompletedEvent.getPayload());
        }
    }
}

