package net.es.oscars.nsibridge.task;


import java.util.List;

import net.es.oscars.nsibridge.beans.QueryRequest;
import net.es.oscars.nsibridge.client.ClientUtil;

import net.es.oscars.nsibridge.prov.*;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.requester.ConnectionRequesterPort;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.types.QueryRecursiveResultType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.connection.types.QuerySummaryConfirmedType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;
import net.es.oscars.nsi.soap.gen.nsi_2_0_2013_07.framework.types.ServiceExceptionType;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.xml.ws.Holder;

public class QueryRecursiveJob implements Job  {
    private static final Logger log = Logger.getLogger(QueryRecursiveJob.class);
    
    static final public String PARAM_REQUEST = "request";
    
    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        
        QueryRequest request =  (QueryRequest)ctx.getJobDetail().getJobDataMap().get(PARAM_REQUEST);
        if(request == null){
            throw new RuntimeException("Request is null");
            
        }
        //check for replyToUrl
        if(request.getInHeader() == null || request.getInHeader().getReplyTo() == null){
            throw new RuntimeException("No replyTo provided for query");
        }
        
        //build the client. if this fails we can't send failure
        ConnectionRequesterPort client = ClientUtil.createRequesterClient(request.getInHeader().getReplyTo());
        try {
            //perform query
            QuerySummaryConfirmedType summResult = RequestProcessor.getInstance().syncQuerySum(request);   
            List<QueryRecursiveResultType> result = NSI_OSCARS_Translation.querySummToRecursive(summResult);
            client.queryRecursiveConfirmed(result , request.getInHeader(), new  Holder<CommonHeaderType>());
            log.info("Recursive query confirmation sent to " + request.getInHeader().getReplyTo() + 
                    ", corr id: "+ request.getInHeader().getCorrelationId());
        } catch (Exception ex) {
            log.error("Recursive query failed: " + ex.getMessage());
            ServiceExceptionType serviceEx = new ServiceExceptionType();
            serviceEx.setNsaId(NSI_OSCARS_Translation.findNsaId());
            serviceEx.setText(ex.getMessage());
            serviceEx.setErrorId("500");
            try {
                client.queryRecursiveFailed(serviceEx, request.getInHeader(), new  Holder<CommonHeaderType>());
                log.info("Recursive query failed sent to " + request.getInHeader().getReplyTo() + 
                        ", corr id: "+ request.getInHeader().getCorrelationId());
            } catch (ServiceException e) {
                log.error("Could not send recursive query failure message: " + ex.getMessage());
            }
        }
        
    }

}
