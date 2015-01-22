package net.es.oscars.nsibridge.task;


import net.es.nsi.lib.client.config.ClientConfig;
import net.es.nsi.lib.client.util.ClientUtil;
import net.es.oscars.nsibridge.beans.QueryRequest;

import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.prov.*;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.connection.ifce.ServiceException;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.connection.requester.ConnectionRequesterPort;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.connection.types.QuerySummaryConfirmedType;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.framework.headers.CommonHeaderType;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.framework.types.ServiceExceptionType;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.xml.ws.Holder;
import java.net.MalformedURLException;
import java.net.URL;

public class QuerySummaryJob implements Job  {
    private static final Logger log = Logger.getLogger(QuerySummaryJob.class);
    
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
        ClientConfig cc = SpringContext.getInstance().getContext().getBean("clientConfig", ClientConfig.class);
        String replyTo = request.getInHeader().getReplyTo();
        URL url;
        try {
            url = new URL(replyTo);
        } catch (MalformedURLException e) {
            log.error(e.getMessage(), e);
            return;
        }

        ConnectionRequesterPort client = ClientUtil.getInstance().getRequesterPort(url, cc);
        try {
            //perform query
            QuerySummaryConfirmedType result = RequestProcessor.getInstance().syncQuerySum(request);
            client.querySummaryConfirmed(result.getReservation(), request.getInHeader(), new  Holder<CommonHeaderType>());
            log.info("Query confirmation sent to " + request.getInHeader().getReplyTo() + 
                    ", corr id: "+ request.getInHeader().getCorrelationId());
        } catch (Exception ex) {
            log.error("Query failed: " + ex.getMessage());
            ServiceExceptionType serviceEx = new ServiceExceptionType();
            serviceEx.setNsaId(NSI_OSCARS_Translation.findNsaId());
            serviceEx.setText(ex.getMessage());
            serviceEx.setErrorId("500");
            try {
                client.error(serviceEx, request.getInHeader(), new Holder<CommonHeaderType>());
                log.info("Query failed error sent to " + request.getInHeader().getReplyTo() +                         ", corr id: "+ request.getInHeader().getCorrelationId());
            } catch (ServiceException e) {
                log.error("Could not send query failure message: " + ex.getMessage());
            }
        }
        
    }

}
