package net.es.oscars.nsibridge.task;


import net.es.nsi.lib.client.config.ClientConfig;
import net.es.nsi.lib.client.util.ClientUtil;
import net.es.oscars.nsibridge.beans.ResvRequest;
import net.es.oscars.nsibridge.beans.SimpleRequest;
import net.es.oscars.nsibridge.beans.db.ConnectionRecord;
import net.es.oscars.nsibridge.beans.db.DataplaneStatusRecord;
import net.es.oscars.nsibridge.beans.db.NotificationRecord;
import net.es.oscars.nsibridge.beans.db.ResvRecord;
import net.es.oscars.nsibridge.common.WorkflowAction;
import net.es.oscars.nsibridge.common.WorkflowRecord;
import net.es.oscars.nsibridge.config.RequestersConfig;
import net.es.oscars.nsibridge.config.SpringContext;
import net.es.oscars.nsibridge.config.TimingConfig;
import net.es.oscars.nsibridge.config.nsa.JsonNsaConfigProvider;
import net.es.oscars.nsibridge.config.nsa.NSAStubConfig;
import net.es.oscars.nsibridge.config.nsa.NsaConfig;
import net.es.oscars.nsibridge.config.nsa.NsaConfigProvider;
import net.es.oscars.nsibridge.ifces.CallbackMessages;
import net.es.oscars.nsibridge.prov.*;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.connection.ifce.ServiceException;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.connection.requester.ConnectionRequesterPort;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.connection.types.*;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.framework.headers.CommonHeaderType;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.framework.types.ServiceExceptionType;
import net.es.nsi.lib.soap.gen.nsi_2_0_r117.framework.types.TypeValuePairListType;
import net.es.oscars.utils.task.Task;
import net.es.oscars.utils.task.TaskException;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.Holder;
import java.util.GregorianCalendar;
import java.util.UUID;

public class SendNSIMessageTask extends Task  {
    private static final Logger log = Logger.getLogger(SendNSIMessageTask.class);

    private String corrId = "";
    private String connId = "";
    private CallbackMessages message;
    private Long notificationId;


    public SendNSIMessageTask() {
        this.scope = UUID.randomUUID().toString();

    }


    public void onRun() throws TaskException {
        log.debug(this.id+" starting");
        if (message != null) {
            log.debug("message: "+message.toString());
        } else {
            log.error("no message!");
            return;
        }
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
                log.debug("connId: "+connId+" corrId: "+corrId+" notify: "+replyTo+" type:"+message);


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

            if (replyTo == null) {
                log.error("null replyTo");
                this.onSuccess();
                return;
            }

            RequestersConfig rc = SpringContext.getInstance().getContext().getBean("requestersConfig", RequestersConfig.class);
            if (rc == null) {
                log.error("could not get requester config");
            }

            ClientConfig cc = rc.getClientConfig(replyTo);
            if (cc == null) {
                log.error("could not get client config for URL "+replyTo);
            }



            ConnectionRequesterPort port = ClientUtil.getInstance().getRequesterPort(replyTo, cc);
            Client client = ClientProxy.getClient(port);
            HTTPConduit conduit = (HTTPConduit) client.getConduit();
            TLSClientParameters params = conduit.getTlsClientParameters();


            Holder outHolder = new Holder<CommonHeaderType>();
            ServiceExceptionType st;


            ConnectionStatesType cst = new ConnectionStatesType();
            if (cr != null) {
                cst.setReservationState(cr.getReserveState());
                cst.setProvisionState(cr.getProvisionState());
                cst.setLifecycleState(cr.getLifecycleState());
                DataPlaneStatusType dst = new DataPlaneStatusType();

                int version = 0;
                boolean active = false;

                for (DataplaneStatusRecord tmp : cr.getDataplaneStatusRecords()) {
                    if (tmp.getVersion() >= version) {
                        version = tmp.getVersion();
                        active = tmp.isActive();
                    }
                }
                dst.setActive(active);
                dst.setVersion(version);
                dst.setVersionConsistent(true);
                cst.setDataPlaneStatus(dst);
            } else {
                cst.setReservationState(ReservationStateEnumType.RESERVE_FAILED);
                cst.setProvisionState(ProvisionStateEnumType.RELEASED);
                cst.setLifecycleState(LifecycleStateEnumType.FAILED);
                DataPlaneStatusType dst = new DataPlaneStatusType();
                dst.setActive(false);
                dst.setVersion(0);
                dst.setVersionConsistent(true);
                cst.setDataPlaneStatus(dst);
            }



            XMLGregorianCalendar cal;
            GregorianCalendar gc = new GregorianCalendar();

            NotificationRecord nr;


            if (!stubConfig.isPerformCallback()) {
                log.info("not performing callback - this NSA is a stub");
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
                    if (rrct.getVersion() == null) {
                        rcct.setVersion(0);
                    } else {
                        rcct.setVersion(rrct.getVersion());
                    }
                    rcct.getAny().addAll(rrct.getAny());
                    rcct.getOtherAttributes().putAll(rrct.getOtherAttributes());

                    if (!stubConfig.isPerformCallback()) {
                        this.onSuccess();
                        return;
                    } else {
                        port.reserveConfirmed(connId, gri, description, rcct, outHeader, outHolder);
                    }
                    break;
                case RESV_CM_CF:
                    if (!stubConfig.isPerformCallback()) {
                        this.onSuccess();
                        return;
                    } else {
                        port.reserveCommitConfirmed(connId, outHeader, outHolder);
                    }
                    break;
                case RESV_AB_CF:
                    if (!stubConfig.isPerformCallback()) {
                        this.onSuccess();
                        return;
                    } else {
                        port.reserveAbortConfirmed(connId, outHeader, outHolder);
                    }
                    break;

                case PROV_CF:
                    if (!stubConfig.isPerformCallback()) {
                        this.onSuccess();
                        return;
                    } else {
                        port.provisionConfirmed(connId, outHeader, outHolder);
                    }
                    break;
                case REL_CF:
                    if (!stubConfig.isPerformCallback()) {
                        this.onSuccess();
                        return;
                    } else {
                        port.releaseConfirmed(connId, outHeader, outHolder);
                    }
                    break;

                case TERM_CF:
                    if (!stubConfig.isPerformCallback()) {
                        this.onSuccess();
                        return;
                    } else {
                        port.terminateConfirmed(connId, outHeader, outHolder);
                    }
                    break;

                // failures
                case RESV_FL:
                    try {
                        st = NSI_Util.makeServiceException(connId, corrId);
                        if (!stubConfig.isPerformCallback()) {
                            this.onSuccess();
                            return;
                        } else {
                            port.reserveFailed(connId, cst, st, outHeader, outHolder);
                        }
                    } catch (ServiceException ex) {
                        log.error(ex.getMessage(), ex);
                    }
                    break;
                case RESV_CM_FL:
                    try {
                        st = NSI_Util.makeServiceException(connId, corrId);
                        if (!stubConfig.isPerformCallback()) {
                            this.onSuccess();
                            return;
                        } else {
                            port.reserveCommitFailed(connId, cst, st, outHeader, outHolder);
                        }
                    } catch (ServiceException ex) {
                        log.error(ex.getMessage(), ex);
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
                    if (!stubConfig.isPerformCallback()) {
                        this.onSuccess();
                        return;
                    } else {
                        port.reserveTimeout(connId, notificationId.intValue(), cal, d.intValue(), connId, cfg.getNsaId(), outHeader, outHolder);
                    }
                    break;

                case DATAPLANE_CHANGE:
                    if (cr.getDataplaneStatusRecords().size() == 0) {
                        throw new TaskException("no dataplane status records for connId:"+connId);
                    }
                    boolean dpActive = false;
                    int dpVersion = 0;
                    for (DataplaneStatusRecord dr : cr.getDataplaneStatusRecords()) {
                        if (dpVersion <= dr.getVersion()) {
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
                    log.info("sent dataplane state change with connId: "+connId+" version: "+dpVersion+" active: "+dpActive);
                    WorkflowRecord wfRecord = WorkflowRecord.getInstance();
                    wfRecord.setRecord(connId, WorkflowAction.DATAPLANE_UPDATE_VERSION, dpVersion);
                    wfRecord.setRecord(connId, WorkflowAction.DATAPLANE_UPDATE_ACTIVE, dpActive);

                    if (!stubConfig.isPerformCallback()) {
                        this.onSuccess();
                        return;
                    } else {
                        port.dataPlaneStateChange(connId, notificationId.intValue(), cal, dst, outHeader, outHolder);
                    }

                    break;


                case ERROR_EVENT:
                    try {
                        nr = DB_Util.getNotificationRecord(connId, notificationId);
                        gc.setTime(nr.getTimestamp());
                        cal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);

                        st = NSI_Util.makeServiceException(connId, corrId);

                        TypeValuePairListType tvl = new TypeValuePairListType();

                        NsaConfigProvider ncp = (NsaConfigProvider) ax.getBean("nsaConfigProvider");
                        String nsaId = ncp.getConfig("local").getNsaId();

                        if (!stubConfig.isPerformCallback()) {
                            this.onSuccess();
                            return;
                        } else {
                            port.errorEvent(connId, notificationId.intValue(), cal, nr.getEventType(), connId, nsaId, tvl, st, outHeader, outHolder);
                        }
                    } catch (ServiceException ex) {
                        log.error(ex.getMessage(), ex);

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
            log.error(ex.getMessage(), ex);
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
