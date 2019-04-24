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
    public Utilities utilities;

    public QueryLogListener(final LoggerContext loggerContext,
                            final boolean trackEventCreated,
                            final boolean trackEventCompleted,
                            final boolean trackEventCompletedSplit) {
        this.trackEventCreated = trackEventCreated;
        this.trackEventCompleted = trackEventCompleted;
        this.trackEventCompletedSplit = trackEventCompletedSplit;
        this.logger = loggerContext.getLogger(QueryLogListener.class.getName());

        this.utilities = new Utilities(loggerContext);
        this.dlm = "|";//"\035";

    }

    public double normalizeBytes(double numberOfBytes){
        logger.warn("Number of Bytes: "+ numberOfBytes);
        return numberOfBytes/1073741824.0;
    }

    @Override
    public void queryCreated(final QueryCreatedEvent queryCreatedEvent) {
        if (trackEventCreated) {
           // logger.debug(new ObjectMessage(queryCreatedEvent));
            logger.warn(queryCreatedEvent.getMetadata().getQuery()+dlm+queryCreatedEvent.getCreateTime());

        }
    }

    @Override
    public void queryCompleted(final QueryCompletedEvent queryCompletedEvent) {
        if (trackEventCompleted) {
            //logger.info(new ObjectMessage(queryCompletedEvent));
            //logger.info(queryCompletedEvent.getCreateTime()+"|"+queryCompletedEvent.getEndTime());
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
                    msg.append("getFailureInfo()"+dlm);
                }

                msg.append(queryCompletedEvent.getMetadata().getQueryId()+dlm);
                msg.append(queryCompletedEvent.getContext().getUser()+dlm);

                if (queryCompletedEvent.getContext().getSource().isPresent()) {
                    msg.append(queryCompletedEvent.getContext().getSource().get() + dlm);
                }else{
                    msg.append("getSource()"+dlm);
                }
                if(queryCompletedEvent.getContext().getRemoteClientAddress().isPresent()){
                    msg.append(queryCompletedEvent.getContext().getRemoteClientAddress().get() + dlm);
                }else{
                    msg.append("getRemoteClientAddress()"+dlm);
                }
                if (queryCompletedEvent.getContext().getUserAgent().isPresent()) {
                    msg.append(queryCompletedEvent.getContext().getUserAgent().get()+dlm);
                }else {
                    msg.append("getUserAgent()"+dlm);
                }
                msg.append(queryCompletedEvent.getCreateTime()+dlm);
                msg.append(queryCompletedEvent.getEndTime()+dlm);

                //query['queryStats']['totalDrivers']
                //query['queryStats']['totalDrivers']
                msg.append(utilities.normalizeBytes(queryCompletedEvent.getStatistics().getCumulativeMemory())+dlm); //convert ot GB
                msg.append(utilities.normalizeBytes(queryCompletedEvent.getStatistics().getPeakUserMemoryBytes())+dlm); //convert to GB
                msg.append(queryCompletedEvent.getStatistics().getCpuTime().getSeconds()+dlm); //normalize time
                msg.append(queryCompletedEvent.getExecutionStartTime()+dlm); //execution start time
                msg.append(queryCompletedEvent.getStatistics().getQueuedTime().getSeconds()+dlm); //normalize_time
                // query_details['queryStats']['totalTasks']
                // query_details['queryStats']['completedTasks']
                msg.append(utilities.normalizeBytes(queryCompletedEvent.getStatistics().getPeakTotalNonRevocableMemoryBytes())+dlm);//normalize_bytes(query_details['queryStats']['peakTotalMemoryReservation'])
                msg.append(utilities.normalizeBytes(queryCompletedEvent.getStatistics().getPeakUserMemoryBytes())+dlm); // normalize_bytes
                // normalize_time(query_details['queryStats']['totalScheduledTime']),
                msg.append("absolute wall time in Seconds: "+queryCompletedEvent.getStatistics().getWallTime().getSeconds()+dlm); //normalize_time(query_details['queryStats']['totalUserTime'])
                //normalize_time(query_details['queryStats']['totalBlockedTime']),
                //normalize_bytes(query_details['queryStats']['rawInputDataSize']),
                //query_details['queryStats']['rawInputPositions'],
                //normalize_bytes(query_details['queryStats']['processedInputDataSize']),
                //query_details['queryStats']['processedInputPositions'],
                msg.append(utilities.normalizeBytes(queryCompletedEvent.getStatistics().getOutputBytes())+dlm); //new parameter need to normalize
                //query_details['queryStats']['outputPositions'],
                msg.append(queryCompletedEvent.getStatistics().getOutputRows()+dlm);
                msg.append(queryCompletedEvent.getMetadata().getQuery().replace("\n"," ")+dlm);
                msg.append(queryCompletedEvent.getMetadata().getQueryState()); //FAILED, FINISHED
                logger.info(msg.toString());

            } catch (Exception ex) {

                logger.error(ex.getMessage());
            }
        }
    }

    @Override
    public void splitCompleted(final SplitCompletedEvent splitCompletedEvent) {
        if (trackEventCompletedSplit) {
            logger.warn(splitCompletedEvent.toString());
        }
    }
}
/// kill server java -cp /dev/shm/var/presto-server-0.198/lib/* -server -Xmx45G -XX:+UseG1GC -XX