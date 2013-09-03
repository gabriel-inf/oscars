package net.es.oscars.nsibridge.oscars;

import net.es.oscars.api.soap.gen.v06.*;
import net.es.oscars.authN.soap.gen.DNType;
import net.es.oscars.authN.soap.gen.VerifyDNReqType;
import net.es.oscars.authN.soap.gen.VerifyReply;
import net.es.oscars.common.soap.gen.MessagePropertiesType;
import net.es.oscars.common.soap.gen.SubjectAttributes;
import net.es.oscars.nsibridge.config.OscarsConfig;
import net.es.oscars.nsibridge.config.OscarsStubConfig;
import net.es.oscars.nsibridge.task.OscarsStubStatusTask;
import net.es.oscars.utils.clients.AuthNClient;
import net.es.oscars.utils.clients.CoordClient;
import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.sharedConstants.AuthZConstants;
import net.es.oscars.utils.sharedConstants.ErrorCodes;
import net.es.oscars.utils.soap.ErrorReport;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.utils.task.TaskException;
import net.es.oscars.utils.task.sched.Workflow;
import net.es.oscars.utils.topology.PathTools;
import oasis.names.tc.saml._2_0.assertion.AttributeType;
import org.apache.log4j.Logger;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OscarsProxy {
    private static Logger log = Logger.getLogger(OscarsProxy.class);
    private CoordClient coordClient;
    private AuthNClient authNClient;
    private OscarsConfig oscarsConfig;
    private OscarsStubConfig oscarsStubConfig;
    private HashMap<String, OscarsStates> stubStates = new HashMap<String, OscarsStates>();

    private static OscarsProxy instance;

    public HashMap<String, OscarsStates> getStubStates() {
        return stubStates;
    }

    public void setStubState(String gri, OscarsStates state) {
        this.stubStates.put(gri, state);
    }

    public static OscarsProxy getInstance()  throws OSCARSServiceException {
        if (instance == null) instance = new OscarsProxy();
        return instance;

    }
    private OscarsProxy() {

    }

    public void initialize() throws OSCARSServiceException {
        this.initAuthNClient();
        this.initCoordClient();
    }

    public ModifyResReply sendModify(ModifyResContent modifyReservation) throws OSCARSServiceException {
        MessagePropertiesType msgProps = modifyReservation.getMessageProperties();
        if (msgProps == null) {
            msgProps = this.makeMessageProps();
        }
        SubjectAttributes subjectAttributes = this.sendAuthNRequest(msgProps);
        msgProps = updateMessageProperties(msgProps, subjectAttributes);
        modifyReservation.setMessageProperties(msgProps);


        Object[] req = new Object[]{subjectAttributes, modifyReservation};

        if (oscarsStubConfig.isStub()) {
            // log.info("stub mode for sendModify");
            ModifyResReply cr = new ModifyResReply();
            stubStates.put(modifyReservation.getGlobalReservationId(), OscarsStates.INPATHCALCULATION);
            try {
                long delay = oscarsStubConfig.getResponseDelayMillis();
                log.debug("sleeping for " + delay + "ms");
                Thread.sleep(delay);
                this.scheduleStatusUpdate(modifyReservation.getGlobalReservationId(), OscarsStates.RESERVED, oscarsStubConfig.getResvDelayMillis());



            } catch (InterruptedException ex) {
                log.debug(ex);
            }
            return cr;
        } else {
            Object[] res = coordClient.invoke("modifyReservation", req);
            ModifyResReply cr = (ModifyResReply) res[0];
            return cr;
        }
    }

    public CancelResReply sendCancel(CancelResContent cancelReservation) throws OSCARSServiceException {
        MessagePropertiesType msgProps = cancelReservation.getMessageProperties();
        if (msgProps == null) {
            msgProps = this.makeMessageProps();
        }
        SubjectAttributes subjectAttributes = this.sendAuthNRequest(msgProps);
        msgProps = updateMessageProperties(msgProps, subjectAttributes);
        cancelReservation.setMessageProperties(msgProps);


        Object[] req = new Object[]{subjectAttributes, cancelReservation};

        if (oscarsStubConfig.isStub()) {
            // log.info("stub mode for sendCancel");
            CancelResReply cr = new CancelResReply();
            stubStates.put(cancelReservation.getGlobalReservationId(), OscarsStates.RESERVED);
            try {
                long delay = oscarsStubConfig.getResponseDelayMillis();
                log.debug("sleeping for " + delay + "ms");
                Thread.sleep(delay);
                this.scheduleStatusUpdate(cancelReservation.getGlobalReservationId(), OscarsStates.CANCELLED, oscarsStubConfig.getCancelDelayMillis());

            } catch (InterruptedException ex) {
                log.debug(ex);
            }
            return cr;
        } else {
            Object[] res = coordClient.invoke("cancelReservation", req);
            CancelResReply cr = (CancelResReply) res[0];
            return cr;
        }
    }

    public CreateReply sendCreate(ResCreateContent createReservation) throws OSCARSServiceException {
        MessagePropertiesType msgProps = createReservation.getMessageProperties();
        if (msgProps == null) {
            msgProps = this.makeMessageProps();
        }

        SubjectAttributes subjectAttributes = this.sendAuthNRequest(msgProps);
        msgProps = updateMessageProperties(msgProps, subjectAttributes);
        createReservation.setMessageProperties(msgProps);

        // Build the query
        Object[] req = new Object[]{subjectAttributes, createReservation};
        if (oscarsStubConfig.isStub()) {
            // log.info("stub mode for sendCreate");
            CreateReply cr = new CreateReply();
            cr.setGlobalReservationId(UUID.randomUUID().toString());
            OscarsStates state = OscarsStates.CREATED;
            if (createReservation.getDescription() != null ) {
                if (createReservation.getDescription().contains(OscarsFail.OSCARS_FAIL_RESERVE.toString())) {
                    state = OscarsStates.FAILED;
                }
            }
            cr.setStatus(state.toString());
            stubStates.put(cr.getGlobalReservationId(), state);
            try {
                long delay = oscarsStubConfig.getResponseDelayMillis();
                log.debug("sleeping for " + delay + "ms");
                Thread.sleep(delay);
                this.scheduleStatusUpdate(cr.getGlobalReservationId(), OscarsStates.RESERVED, oscarsStubConfig.getResvDelayMillis());

            } catch (InterruptedException ex) {
                log.debug(ex);
            }

            return cr;
        } else {
            Object[] res = coordClient.invoke("createReservation",req);
            CreateReply cr = (CreateReply) res[0];
            return cr;
        }
    }


    public TeardownPathResponseContent sendTeardown(TeardownPathContent tp) throws OSCARSServiceException {
        MessagePropertiesType msgProps = tp.getMessageProperties();
        if (msgProps == null) {
            msgProps = this.makeMessageProps();
        }

        SubjectAttributes subjectAttributes = this.sendAuthNRequest(msgProps);
        msgProps = updateMessageProperties(msgProps, subjectAttributes);
        tp.setMessageProperties(msgProps);

        // Build the query
        Object[] req = new Object[]{subjectAttributes, tp};
        if (oscarsStubConfig.isStub()) {
            // log.info("stub mode for sendTeardown");
            TeardownPathResponseContent tr = new TeardownPathResponseContent();
            tr.setStatus("INTEARDOWN");
            stubStates.put(tp.getGlobalReservationId(), OscarsStates.INTEARDOWN);
            try {
                long delay = oscarsStubConfig.getResponseDelayMillis();
                log.debug("sleeping for " + delay + "ms");

                Thread.sleep(delay);
                this.scheduleStatusUpdate(tp.getGlobalReservationId(), OscarsStates.RESERVED, oscarsStubConfig.getTeardownDelayMillis());
            } catch (InterruptedException ex) {
                log.debug(ex);
            }

            return tr;
        } else {
            Object[] res = coordClient.invoke("sendTeardown",req);
            TeardownPathResponseContent tr = (TeardownPathResponseContent) res[0];
            return tr;
        }
    }



    public CreatePathResponseContent sendSetup(CreatePathContent tp) throws OSCARSServiceException {
        MessagePropertiesType msgProps = tp.getMessageProperties();
        if (msgProps == null) {
            msgProps = this.makeMessageProps();
        }

        SubjectAttributes subjectAttributes = this.sendAuthNRequest(msgProps);
        msgProps = updateMessageProperties(msgProps, subjectAttributes);
        tp.setMessageProperties(msgProps);

        // Build the query
        Object[] req = new Object[]{subjectAttributes, tp};
        if (oscarsStubConfig.isStub()) {
            // log.info("stub mode for sendSetup");
            CreatePathResponseContent tr = new CreatePathResponseContent();
            stubStates.put(tp.getGlobalReservationId(), OscarsStates.INSETUP);
            try {
                long delay = oscarsStubConfig.getResponseDelayMillis();
                log.debug("sleeping for " + delay + "ms");
                Thread.sleep(delay);
                this.scheduleStatusUpdate(tp.getGlobalReservationId(), OscarsStates.ACTIVE, oscarsStubConfig.getSetupDelayMillis());

            } catch (InterruptedException ex) {
                log.debug(ex);
            }

            return tr;
        } else {
            Object[] res = coordClient.invoke("sendSetup",req);
            CreatePathResponseContent tr = (CreatePathResponseContent) res[0];
            return tr;
        }
    }


    public QueryResReply sendQuery(QueryResContent qc) throws OSCARSServiceException {
        MessagePropertiesType msgProps = qc.getMessageProperties();
        if (msgProps == null) {
            msgProps = this.makeMessageProps();
        }

        SubjectAttributes subjectAttributes = this.sendAuthNRequest(msgProps);
        msgProps = updateMessageProperties(msgProps, subjectAttributes);
        qc.setMessageProperties(msgProps);

        // Build the query
        Object[] req = new Object[]{subjectAttributes, qc};
        if (oscarsStubConfig.isStub()) {
            String gri = qc.getGlobalReservationId();
            OscarsStates stubState = this.getStubStates().get(gri);

            if (stubState == null) stubState = OscarsStates.CREATED;

            // log.info("stub query mode for gri: " + gri + ", stub state: " + stubState);
            QueryResReply tr = new QueryResReply();
            ResDetails rd = new ResDetails();
            rd.setGlobalReservationId(gri);
            rd.setStatus(stubState.toString());
            tr.setReservationDetails(rd);
            try {
                long delay = oscarsStubConfig.getQueryDelayMillis();
                // log.debug("sleeping for " + delay + "ms");
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
                log.debug(ex);
            }

            return tr;
        } else {
            Object[] res = coordClient.invoke("queryReservation",req);
            QueryResReply tr = (QueryResReply) res[0];
            return tr;
        }
    }



    private void initCoordClient() throws OSCARSServiceException {
        if (coordClient != null) {
            return;
        }
        if (oscarsStubConfig.isStub()) return;

        ContextConfig cc = ContextConfig.getInstance();
        cc.setServiceName(ServiceNames.SVC_COORD);

        // not used yet, will be when reservations are managed
        String configFilename = null;
        try {
            configFilename = cc.getFilePath(ServiceNames.SVC_COORD ,cc.getContext(), ConfigDefaults.CONFIG);
        } catch (ConfigException e) {
            e.printStackTrace();
        }



        HashMap<String,Object> coordMap = (HashMap<String,Object>) ConfigHelper.getConfiguration(configFilename);
        if (coordMap == null) {
            throw new OSCARSServiceException("could not load coordinator config file "+configFilename);

        }
        Map soap = (HashMap<String,Object>) coordMap.get("soap");
        if (soap == null ) {
            throw new OSCARSServiceException("soap stanza not found in "+configFilename);
        }

        try {
            URL coordHost = new URL ((String)soap.get("publishTo"));
            URL coordWsdl = cc.getWSDLPath(ServiceNames.SVC_COORD,null);
            coordClient = CoordClient.getClient(coordHost, coordWsdl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void initAuthNClient() throws OSCARSServiceException {

        if (authNClient != null) {
            return;
        }

        if (oscarsStubConfig.isStub()) return;

        ContextConfig cc = ContextConfig.getInstance();
        cc.setServiceName(ServiceNames.SVC_COORD);

        String configFilename = null;
        try {
            configFilename = cc.getFilePath(ServiceNames.SVC_AUTHN, cc.getContext(), ConfigDefaults.CONFIG);
        } catch (ConfigException e) {
            e.printStackTrace();
        }

        HashMap<String,Object> authNMap = (HashMap<String,Object>) ConfigHelper.getConfiguration(configFilename);
        if (authNMap == null) {
            throw new OSCARSServiceException("could not load authN config file "+configFilename);

        }
        Map soap = (HashMap<String,Object>) authNMap.get("soap");
        if (soap == null ) {
            throw new OSCARSServiceException("soap stanza not found in "+configFilename);
        }

        try {
            URL authNHost = new URL ((String)soap.get("publishTo"));
            URL authNWsdl = cc.getWSDLPath(ServiceNames.SVC_AUTHN,null);
            authNClient = AuthNClient.getClient(authNHost, authNWsdl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private MessagePropertiesType makeMessageProps() {
        MessagePropertiesType msgProps = new MessagePropertiesType();
        msgProps.setGlobalTransactionId(UUID.randomUUID().toString());
        return msgProps;
    }


    private SubjectAttributes sendAuthNRequest (MessagePropertiesType msgProps)
            throws OSCARSServiceException {

        String userDN   = this.oscarsConfig.getUserDN();
        String issuerDN = this.oscarsConfig.getIssuerDN();


        VerifyDNReqType verifyDNReq = new VerifyDNReqType();
        DNType DN = new DNType();
        DN.setSubjectDN(userDN);
        DN.setIssuerDN(issuerDN);
        verifyDNReq.setDN(DN);
        verifyDNReq.setTransactionId(msgProps.getGlobalTransactionId());
        Object[] req = new Object[]{verifyDNReq};
        SubjectAttributes subjectAttrs;
        if (oscarsStubConfig.isStub()) {
            // log.info("stub mode for sendAuthNRequest");
            try {
                long delay = oscarsStubConfig.getAuthDelayMillis();
                // log.info("sleeping for " + delay + "ms");
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
                log.debug(ex);
            }

            subjectAttrs = new SubjectAttributes();
        } else {
            Object[] res = authNClient.invoke("verifyDN",req);
            VerifyReply reply = (VerifyReply)res[0];
            subjectAttrs = reply.getSubjectAttributes();
            if (subjectAttrs == null || subjectAttrs.getSubjectAttribute().isEmpty()){
                ErrorReport errRep = new ErrorReport (ErrorCodes.ACCESS_DENIED,
                        "no atributes for user " + userDN,
                        ErrorReport.USER);
                throw new OSCARSServiceException(errRep);
            }
        }
        return subjectAttrs;

    }



    private MessagePropertiesType updateMessageProperties (MessagePropertiesType msgProps, SubjectAttributes subjectAttributes) {
        SubjectAttributes originator;
        if (msgProps == null) {
            msgProps = new MessagePropertiesType();
        }
        String transId = msgProps.getGlobalTransactionId();
        if (transId == null || transId.equals("")) {
            transId = PathTools.getLocalDomainId() + "-NSI-" + UUID.randomUUID().toString();
            msgProps.setGlobalTransactionId(transId);
        }
        originator = msgProps.getOriginator();
        if ((originator == null) && (subjectAttributes != null)) {
            for (AttributeType att: subjectAttributes.getSubjectAttribute()) {
                if (att.getName().equals(AuthZConstants.LOGIN_ID)) {
                    originator = new SubjectAttributes();
                    originator.getSubjectAttribute().add(att);
                }
            }
            msgProps.setOriginator(originator);
        }
        return msgProps;
    }

    private void scheduleStatusUpdate(String gri, OscarsStates state, Long delay) {
        Workflow wf = Workflow.getInstance();

        long now = new Date().getTime();
        Long when = now + delay;

        OscarsStubStatusTask ost = new OscarsStubStatusTask(gri, state);
        try {
            UUID taskId = wf.schedule(ost, when);
            log.info("scheduled stub status update in "+delay+"ms, task id: "+taskId+" gri: "+gri+" state: "+state);
        } catch (TaskException ex) {

            log.error(ex);
        }

    }

    public OscarsConfig getOscarsConfig() {
        return oscarsConfig;
    }

    public void setOscarsConfig(OscarsConfig oscarsConfig) {
        this.oscarsConfig = oscarsConfig;
    }

    public OscarsStubConfig getOscarsStubConfig() {
        return oscarsStubConfig;
    }

    public void setOscarsStubConfig(OscarsStubConfig oscarsStubConfig) {
        this.oscarsStubConfig = oscarsStubConfig;
    }
}