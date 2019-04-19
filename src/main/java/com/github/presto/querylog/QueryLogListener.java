package com.github.presto.querylog;

import com.facebook.presto.spi.eventlistener.EventListener;
import com.facebook.presto.spi.eventlistener.QueryCompletedEvent;
import com.facebook.presto.spi.eventlistener.QueryCreatedEvent;
import com.facebook.presto.spi.eventlistener.SplitCompletedEvent;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.message.ObjectMessage;
import java.util.NoSuchElementException;


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

    @Override
    public void queryCreated(final QueryCreatedEvent queryCreatedEvent) {
        if (trackEventCreated) {
            //logger.warn(new ObjectMessage(queryCreatedEvent));
            logger.warn(queryCreatedEvent.getCreateTime()+this.dlm+queryCreatedEvent.getMetadata().getQueryId());

        }
    }

    @Override
    public void queryCompleted(final QueryCompletedEvent queryCompletedEvent) {
        if (trackEventCompleted) {
            //logger.info(new ObjectMessage(queryCompletedEvent));
            //logger.info(queryCompletedEvent.getCreateTime()+"|"+queryCompletedEvent.getEndTime());
            StringBuilder msg =  new StringBuilder();

            try {
                msg.append("succeeded"+this.dlm);
                if(queryCompletedEvent.getFailureInfo().isPresent()) {
                    msg.append(queryCompletedEvent.getFailureInfo().get().getErrorCode().getName() + this.dlm); //failureInfo: Optional[com.facebook.presto.spi.eventlistener.QueryFailureInfo@643e5358
                }else {
                    msg.append("getFailureInfo()"+this.dlm);
                }
                msg.append(queryCompletedEvent.getMetadata().getQueryId()+this.dlm);
                msg.append(queryCompletedEvent.getContext().getUser()+this.dlm);
                if (queryCompletedEvent.getContext().getSource().isPresent()) {
                    msg.append(queryCompletedEvent.getContext().getSource().get() + this.dlm);
                }else{
                    msg.append("getSource()"+this.dlm);
                }
                if(queryCompletedEvent.getContext().getRemoteClientAddress().isPresent()){
                    msg.append(queryCompletedEvent.getContext().getRemoteClientAddress().get() + this.dlm);
                }else{
                    msg.append("getRemoteClientAddress()"+this.dlm);
                }
                if (queryCompletedEvent.getContext().getUserAgent().isPresent()) {
                    msg.append(queryCompletedEvent.getContext().getUserAgent().get()+this.dlm);
                }else {
                    msg.append("getUserAgent()"+this.dlm);
                }

                //msg.append(queryCompletedEvent.getMetadata().getQueryState()+this.dlm);
                msg.append(queryCompletedEvent.getCreateTime()+this.dlm);
                msg.append(queryCompletedEvent.getEndTime()+this.dlm);
                //query['queryStats']['totalDrivers']
                //query['queryStats']['totalDrivers']
                msg.append(queryCompletedEvent.getStatistics().getCumulativeMemory()+this.dlm); //convert ot GB
                msg.append(queryCompletedEvent.getStatistics().getPeakUserMemoryBytes()+this.dlm); //convert to GB
                msg.append(queryCompletedEvent.getStatistics().getCpuTime().toString().substring(2)+this.dlm); //normalize time
                msg.append(queryCompletedEvent.getExecutionStartTime()+this.dlm); //convert to seconds
                msg.append(queryCompletedEvent.getStatistics().getQueuedTime().toString().substring(2)+this.dlm); //normalize_time
                // query_details['queryStats']['totalTasks']
                // query_details['queryStats']['completedTasks']
                msg.append(queryCompletedEvent.getStatistics().getPeakTotalNonRevocableMemoryBytes()+this.dlm);//normalize_bytes(query_details['queryStats']['peakTotalMemoryReservation'])
                msg.append(queryCompletedEvent.getStatistics().getPeakUserMemoryBytes()+this.dlm); // normalize_bytes
                // normalize_time(query_details['queryStats']['totalScheduledTime']),
                msg.append("absolute wall time: "+queryCompletedEvent.getStatistics().getWallTime().toString().substring(2)+this.dlm); //normalize_time(query_details['queryStats']['totalUserTime'])
                //normalize_time(query_details['queryStats']['totalBlockedTime']),
                //normalize_bytes(query_details['queryStats']['rawInputDataSize']),
                //query_details['queryStats']['rawInputPositions'],
                //normalize_bytes(query_details['queryStats']['processedInputDataSize']),
                //query_details['queryStats']['processedInputPositions'],
                msg.append(queryCompletedEvent.getStatistics().getOutputBytes()+this.dlm); //new parameter need to normalize
                //query_details['queryStats']['outputPositions'],
                msg.append(queryCompletedEvent.getStatistics().getOutputRows()+this.dlm);
                msg.append(queryCompletedEvent.getMetadata().getQuery().replace("\n"," ")+this.dlm);
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
            logger.warn(new ObjectMessage(splitCompletedEvent));
        }
    }
}
/// kill server java -cp /dev/shm/var/presto-server-0.198/lib/* -server -Xmx45G -XX:+UseG1GC -XX