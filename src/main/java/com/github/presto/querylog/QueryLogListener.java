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

    public QueryLogListener(final LoggerContext loggerContext,
                            final boolean trackEventCreated,
                            final boolean trackEventCompleted,
                            final boolean trackEventCompletedSplit) {
        this.trackEventCreated = trackEventCreated;
        this.trackEventCompleted = trackEventCompleted;
        this.trackEventCompletedSplit = trackEventCompletedSplit;
        this.logger = loggerContext.getLogger(QueryLogListener.class.getName());
        this.dlm = "|";//"\035";

    }

    public double normalizeBytes(double numberOfBytes){
        logger.warn("Number of Bytes: "+ numberOfBytes);
        return numberOfBytes/1073741824.0;
    }

    @Override
    public void queryCreated(final QueryCreatedEvent queryCreatedEvent) {
        if (trackEventCreated) {
            logger.debug(queryCreatedEvent.getMetadata().getQuery()+dlm+queryCreatedEvent.getCreateTime());

        }
    }

    @Override
    public void queryCompleted(final QueryCompletedEvent queryCompletedEvent) {
        if (trackEventCompleted) {
            StringBuilder msg =  new StringBuilder();
            String queryStatus = "succeeded";

            if (queryCompletedEvent.getMetadata().getQueryState() != "FINISHED"){
                queryStatus = "failed";
            }


            try {
                msg.append(queryStatus + dlm);
                if(queryCompletedEvent.getFailureInfo().isPresent()) {
                    msg.append(queryCompletedEvent.getFailureInfo().get().getErrorCode().getName() + dlm); //failureInfo: Optional[com.facebook.presto.spi.eventlistener.QueryFailureInfo@643e5358
                }else {
                    msg.append("Failure info not found"+dlm);
                }

                msg.append(queryCompletedEvent.getMetadata().getQueryId()+dlm);
                msg.append(queryCompletedEvent.getContext().getUser()+dlm);

                if (queryCompletedEvent.getContext().getSource().isPresent()) {
                    msg.append(queryCompletedEvent.getContext().getSource().get() + dlm);
                }else{
                    msg.append("Source not Found"+dlm);
                }
                if(queryCompletedEvent.getContext().getRemoteClientAddress().isPresent()){
                    msg.append(queryCompletedEvent.getContext().getRemoteClientAddress().get() + dlm);
                }else{
                    msg.append("client address not found"+dlm);
                }
                if (queryCompletedEvent.getContext().getUserAgent().isPresent()) {
                    msg.append(queryCompletedEvent.getContext().getUserAgent().get()+dlm);
                }else {
                    msg.append("user agent NF"+dlm);
                }
                msg.append(queryCompletedEvent.getCreateTime()+dlm);
                msg.append(queryCompletedEvent.getStatistics().getQueuedTime().getSeconds()+dlm);
                msg.append(queryCompletedEvent.getExecutionStartTime()+dlm);
                msg.append(queryCompletedEvent.getStatistics().getCpuTime().getSeconds()+dlm);
                msg.append("absolute wall time in Seconds: "+queryCompletedEvent.getStatistics().getWallTime().getSeconds()+dlm);
                if(queryCompletedEvent.getStatistics().getDistributedPlanningTime().isPresent()){
                    msg.append("distribution planning time: "+queryCompletedEvent.getStatistics().getDistributedPlanningTime().get().getSeconds()+dlm);
                }else {
                    msg.append("distribution planning time NF"+dlm);
                }
                msg.append(normalizeBytes(queryCompletedEvent.getStatistics().getCumulativeMemory())+dlm);
                msg.append(normalizeBytes(queryCompletedEvent.getStatistics().getPeakUserMemoryBytes())+dlm);
                msg.append(normalizeBytes(queryCompletedEvent.getStatistics().getPeakTotalNonRevocableMemoryBytes())+dlm);
                msg.append(normalizeBytes(queryCompletedEvent.getStatistics().getPeakUserMemoryBytes())+dlm);
                msg.append(queryCompletedEvent.getEndTime()+dlm);
                msg.append(normalizeBytes(queryCompletedEvent.getStatistics().getOutputBytes())+dlm);
                msg.append("getTotalBytes: "+normalizeBytes(queryCompletedEvent.getStatistics().getTotalBytes())+dlm);
                msg.append(queryCompletedEvent.getStatistics().getOutputRows()+dlm);
                msg.append(queryCompletedEvent.getMetadata().getQuery().replace("\n"," ")+dlm);
                //msg.append("getUri: "+queryCompletedEvent.getMetadata().getUri()+dlm); return server ip address based url
                if(queryCompletedEvent.getStatistics().getAnalysisTime().isPresent()) {
                    msg.append("getAnalysisTime: " + queryCompletedEvent.getStatistics().getAnalysisTime().get().getSeconds()+dlm);
                }else {
                    msg.append("getAnalysisTimeNF "+dlm);
                }
                msg.append(queryCompletedEvent.getMetadata().getQueryState()); // FAILED, FINISHED


                //msg.append("getOperatorSummaries: "+queryCompletedEvent.getStatistics().getOperatorSummaries().); //summary of drivers and other data

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
/// kill server java -cp /dev/shm/var/presto-server-0.198/lib/* -server -Xmx45G -XX:+UseG1GC -XX