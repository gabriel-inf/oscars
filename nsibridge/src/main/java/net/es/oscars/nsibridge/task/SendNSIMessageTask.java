package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.SimpleRequest;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.nsa.NSAStubConfig;
import net.es.oscars.nsibridge.ifces.CallbackMessages;
import net.es.oscars.nsibridge.prov.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.requester.ConnectionRequesterPort;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.requester.ConnectionServiceRequester;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ConnectionStatesType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ReservationConfirmCriteriaType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ReservationRequestCriteriaType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.ReserveConfirmedType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.types.ServiceExceptionType;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.frontend.ClientFactoryBean;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Holder;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SendNSIMessageTask extends Task  {
    private static final Logger log = Logger.getLogger(SendNSIMessageTask.class);

    private String corrId = "";
    private CallbackMessages message;


    public SendNSIMessageTask(String corrId, CallbackMessages message) {
        this.scope = "nsi";
        this.corrId = corrId;
        this.message = message;
    }

    public void onRun() throws TaskException {
        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.getContext();
        NSAStubConfig stubConfig = ax.getBean("nsaStubConfig", NSAStubConfig.class);
        try {
            super.onRun();

            RequestHolder rh = RequestHolder.getInstance();

            ResvRequest rreq = rh.findResvRequest(corrId);
            SimpleRequest sr = rh.findSimpleRequest(corrId);

            CommonHeaderType reqHeader;
            String connId = rh.findConnectionId(corrId);
            ConnectionRecord cr = NSI_Util.getConnectionRecord(connId);


            if (rreq != null) {
                // log.debug("found resv request for corrId: "+corrId);
                reqHeader = rreq.getInHeader();
            } else if (sr != null) {
                // simpleRequest
                // log.debug("found simple request for corrId: "+corrId);
                reqHeader = sr.getInHeader();
            } else {
                log.error("no request found, exiting");
                this.onFail();
                return;
            }

            String replyTo = reqHeader.getReplyTo();
            log.debug("corrId: "+corrId+" replyTo: "+replyTo+" type:"+message);
            URL url;
            try {
                url = new URL(replyTo);
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return;
            }

            if (!stubConfig.isPerformCallback()) {
                log.info("not performing callback - this NSA is a stub");
                this.onSuccess();
                return;
            }


            // set logging
            LoggingInInterceptor in = new LoggingInInterceptor();
            in.setPrettyLogging(true);

            LoggingOutInterceptor out = new LoggingOutInterceptor();
            out.setPrettyLogging(true);

            // Client clientProxy = ClientProxy.getClient(port);
            // clientProxy.getInInterceptors().add(in);
            // clientProxy.getOutInterceptors().add(out);

            // ConnectionServiceRequester client = new ConnectionServiceRequester();
            // ConnectionRequesterPort port = client.getConnectionServiceRequesterPort();
            // set callback address

//            BindingProvider bp = (BindingProvider) port;
//            bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url.toString());

            JaxWsProxyFactoryBean fb = new JaxWsProxyFactoryBean();
            fb.getInInterceptors().add(in);
            fb.getOutInterceptors().add(out);
            fb.setAddress(url.toString());

            Map props = fb.getProperties();
            if (props == null) {
                props = new HashMap<String, Object>();
            }
            props.put("jaxb.additionalContextClasses",
                    new Class[] {
                            net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.services.point2point.ObjectFactory.class
                    });
            fb.setProperties(props);



            fb.setServiceClass(ConnectionRequesterPort.class);
            ConnectionRequesterPort port = (ConnectionRequesterPort) fb.create();





            CommonHeaderType outHeader = NSI_Util.makeNsiOutgoingHeader(reqHeader);

            Holder outHolder = new Holder<CommonHeaderType>();
            ServiceExceptionType st = NSI_Util.makeServiceException("internal error");

            ConnectionStatesType cst = new ConnectionStatesType();
            if (cr != null) {
                cst.setReservationState(cr.getReserveState());
                cst.setProvisionState(cr.getProvisionState());
                cst.setLifecycleState(cr.getLifecycleState());
            }


            switch (message) {
                case RESV_CF:
                    if (rreq == null) {
                        throw new TaskException("no resv req");
                    }

                    String gri = rreq.getReserveType().getGlobalReservationId();
                    String description = rreq.getReserveType().getDescription();

                    ReservationRequestCriteriaType rrct = rreq.getReserveType().getCriteria();

                    ReservationConfirmCriteriaType rcct = new ReservationConfirmCriteriaType();
                    rcct.setSchedule(rrct.getSchedule());
                    rcct.setServiceType(rrct.getServiceType());
                    rcct.setVersion(rrct.getVersion());
                    rcct.getAny().addAll(rrct.getAny());
                    rcct.getOtherAttributes().putAll(rrct.getOtherAttributes());

                    port.reserveConfirmed(connId, gri, description, rcct, outHeader, outHolder);
                    break;
                case RESV_FL:
                    port.reserveFailed(connId, cst, st, outHeader, outHolder);
                    break;
                case RESV_CM_CF:
                    port.reserveCommitConfirmed(connId, outHeader, outHolder);
                    break;
                case RESV_CM_FL:
                    port.reserveCommitFailed(connId, cst, st, outHeader, outHolder);
                    break;
                case RESV_AB_CF:
                    port.reserveAbortConfirmed(connId, outHeader, outHolder);
                    break;

                case PROV_CF:
                    port.provisionConfirmed(connId, outHeader, outHolder);
                    break;
                case REL_CF:
                    port.releaseConfirmed(connId, outHeader, outHolder);
                    break;
                case TERM_CF:
                    port.terminateConfirmed(connId, outHeader, outHolder);
                    break;
                case RESV_TIMEOUT:
                    // port.reserveTimeout();
                    break;
                case MSG_TIMEOUT:
                    // as a uPA we should probably never send this
                    // port.messageDeliveryTimeout();
                    break;
                case DATAPLANE_CHANGE:
                    // port.dataPlaneStateChange();
                    break;


                case QUERY_NOT_FL:
                    break;
                case QUERY_NOT_CF:
                    break;
                case QUERY_REC_CF:
                    break;
                case QUERY_REC_FL:
                    break;
                case QUERY_SUM_CF:
                    break;
                case QUERY_SUM_FL:
                    break;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex);
            this.onFail();
        }

        log.debug(this.id + " finishing");

        this.onSuccess();
    }



}
