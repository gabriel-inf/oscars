package net.es.oscars.nsibridge.task;


import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.SimpleRequest;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.DataplaneStatusRecord;
import net.es.oscars.nsibridge.beans.db.NotificationRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.config.nsa.JsonNsaConfigProvider;
import net.es.oscars.nsibridge.config.nsa.NSAStubConfig;
import net.es.oscars.nsibridge.config.nsa.NsaConfig;
import net.es.oscars.nsibridge.ifces.CallbackMessages;
import net.es.oscars.nsibridge.prov.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.requester.ConnectionRequesterPort;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.connection.types.*;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.headers.CommonHeaderType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.types.ServiceExceptionType;
import net.es.oscars.nsibridge.soap.gen.nsi_2_0_2013_07.framework.types.TypeValuePairListType;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

public class SendNSIMessageTask extends Task  {
    private static final Logger log = Logger.getLogger(SendNSIMessageTask.class);

    private String corrId = "";
    private String connId = "";
    private CallbackMessages message;
    private Long notificationId;


    public SendNSIMessageTask() {
        this.scope = "nsi";

    }


    public void onRun() throws TaskException {
        log.debug(this.id+" starting");
        SpringContext sc = SpringContext.getInstance();
        ApplicationContext ax = sc.getContext();
        NSAStubConfig stubConfig = ax.getBean("nsaStubConfig", NSAStubConfig.class);
        try {
            super.onRun();

            CommonHeaderType reqHeader;
            CommonHeaderType outHeader;

            String replyTo;
            ResvRequest rreq = null;
            SimpleRequest sr;
            ConnectionRecord cr;

            if (message.equals(CallbackMessages.RESV_TIMEOUT) ||
                message.equals(CallbackMessages.ERROR_EVENT) ||
                message.equals(CallbackMessages.MSG_TIMEOUT) ||
                message.equals(CallbackMessages.DATAPLANE_CHANGE) ) {
                if (connId == null) {
                    throw new TaskException("connId needed for notification messages");
                }
                cr = DB_Util.getConnectionRecord(connId);
                replyTo = cr.getNotifyUrl();
                if (replyTo == null) {
                    throw new TaskException("notifyUrl needed for notification messages");
                }

                outHeader = NSI_Util.makeNsiOutgoingHeader(cr.getRequesterNSA());


            } else {
                RequestHolder rh = RequestHolder.getInstance();

                rreq = rh.findResvRequest(corrId);
                sr = rh.findSimpleRequest(corrId);

                connId = rh.findConnectionId(corrId);
                cr = DB_Util.getConnectionRecord(connId);
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

                replyTo = reqHeader.getReplyTo();

                outHeader = NSI_Util.makeNsiOutgoingHeader(reqHeader);



                log.debug("corrId: "+corrId+" replyTo: "+replyTo+" type:"+message);
            }


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



            ConnectionRequesterPort port = RequesterPortHolder.getInstance().getPort(url);


            Holder outHolder = new Holder<CommonHeaderType>();
            ServiceExceptionType st;


            ConnectionStatesType cst = new ConnectionStatesType();
            if (cr != null) {
                cst.setReservationState(cr.getReserveState());
                cst.setProvisionState(cr.getProvisionState());
                cst.setLifecycleState(cr.getLifecycleState());
            }

            XMLGregorianCalendar cal;
            GregorianCalendar gc = new GregorianCalendar();

            NotificationRecord nr;



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
                case RESV_CM_CF:
                    port.reserveCommitConfirmed(connId, outHeader, outHolder);
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

                // failures
                case RESV_FL:
                    try {
                        st = NSI_Util.makeServiceException(connId, corrId);
                        port.reserveFailed(connId, cst, st, outHeader, outHolder);
                    } catch (ServiceException ex) {
                        log.error(ex);
                    }
                    break;
                case RESV_CM_FL:
                    try {
                        st = NSI_Util.makeServiceException(connId, corrId);
                        port.reserveCommitFailed(connId, cst, st, outHeader, outHolder);
                    } catch (ServiceException ex) {
                        log.error(ex);
                    }
                    break;

                case ERROR:
                    st = NSI_Util.makeServiceException(connId, corrId);
                    port.error(st, outHeader, outHolder);

                // notifications
                case RESV_TIMEOUT:
                    nr = DB_Util.getNotificationRecord(connId, notificationId);
                    gc.setTime(nr.getTimestamp());
                    cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);

                    Double d = ax.getBean("timingConfig", TimingConfig.class).getResvTimeout();
                    NsaConfig cfg = ax.getBean("nsaConfigProvider", JsonNsaConfigProvider.class).getConfig("local");

                    port.reserveTimeout(connId, notificationId.intValue(), cal, d.intValue(), connId, cfg.getNsaId(), outHeader, outHolder);
                    break;

                case DATAPLANE_CHANGE:
                    if (cr.getDataplaneStatusRecords().size() == 0) {
                        throw new TaskException("no dataplane status records for connId:"+connId);
                    }
                    boolean dpActive = false;
                    int dpVersion = 0;
                    for (DataplaneStatusRecord dr : cr.getDataplaneStatusRecords()) {
                        if (dpVersion < dr.getVersion()) {
                            dpActive = dr.isActive();
                            dpVersion = dr.getVersion();
                        }
                    }
                    ResvRecord rr = ConnectionRecord.getCommittedResvRecord(cr);
                    if (rr == null) {
                        throw new TaskException("could not locate committed resv record for connId: "+connId);
                    }
                    boolean dpConsistent = false;
                    if (rr.getVersion() == dpVersion) {
                        dpConsistent = true;
                    }



                    nr = DB_Util.getNotificationRecord(connId, notificationId);
                    gc.setTime(nr.getTimestamp());
                    cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);

                    DataPlaneStatusType dst = new DataPlaneStatusType();
                    dst.setActive(dpActive);
                    dst.setVersion(dpVersion);
                    dst.setVersionConsistent(dpConsistent);

                    port.dataPlaneStateChange(connId, notificationId.intValue(), cal, dst, outHeader, outHolder);
                    break;

                case ERROR_EVENT:
                    try {
                        nr = DB_Util.getNotificationRecord(connId, notificationId);
                        gc.setTime(nr.getTimestamp());
                        cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);

                        st = NSI_Util.makeServiceException(connId, corrId);

                        TypeValuePairListType tvl = new TypeValuePairListType();
                        port.errorEvent(connId, notificationId.intValue(), cal, nr.getEventType(), tvl, st, outHeader, outHolder);
                    } catch (ServiceException ex) {
                        log.error(ex);

                    }
                    break;

                // query callbacks
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

                // unimplemented
                case MSG_TIMEOUT:
                    // as a uPA we will never send this
                    throw new TaskException("cannot send MSG_TIMEOUT as a uPA");
            }

        } catch (Exception ex) {
            log.error(ex);
            this.onFail();
        }

        log.debug(this.id + " finishing");

        this.onSuccess();
    }

    public String getCorrId() {
        return corrId;
    }

    public void setCorrId(String corrId) {
        this.corrId = corrId;
    }

    public String getConnId() {
        return connId;
    }

    public void setConnId(String connId) {
        this.connId = connId;
    }

    public CallbackMessages getMessage() {
        return message;
    }

    public void setMessage(CallbackMessages message) {
        this.message = message;
    }

    public Long getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(Long notificationId) {
        this.notificationId = notificationId;
    }
}
