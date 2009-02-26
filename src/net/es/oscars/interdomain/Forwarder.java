package net.es.oscars.interdomain;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.*;
import org.apache.axis2.AxisFault;
import org.quartz.JobDataMap;

import net.es.oscars.ConfigFinder;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.OSCARSCore;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.events.EventProducer;
import net.es.oscars.bss.events.OSCARSEvent;
import net.es.oscars.bss.topology.*;
import net.es.oscars.scheduler.SubscribeJob;
import net.es.oscars.ws.AAAFaultMessage;
import net.es.oscars.ws.BSSFaultMessage;
import net.es.oscars.ws.WSDLTypeConverter;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.client.*;

/**
 * Forwarding client.
 */
public class Forwarder extends Client {
    private Logger log;

    public Forwarder() {
        this.log = Logger.getLogger(this.getClass());
    }

    private void setup(Reservation resv, String url)
            throws InterdomainException {

        this.log.debug("setup.start: " + url);
        String axis2Xml = null;
        try{
            axis2Xml = ConfigFinder.getInstance().find(ConfigFinder.AXIS_TOMCAT_DIR, "axis2.xml");
        }catch(RemoteException e){
            throw new InterdomainException(e.getMessage());
        }
        String repo = (new File(axis2Xml)).getParent();
        System.setProperty("axis2.xml", axis2Xml);
        try {
            super.setUp(true, url, repo, axis2Xml);
        } catch (AxisFault af) {
            this.log.error("setup.axisFault: " + af.getMessage());
            throw new InterdomainException("failed to reach remote domain:" + url +  af.getMessage());
        }
        this.log.debug("setup.finish: " + url);
    }

    public boolean create(Reservation resv) throws InterdomainException {
        String login = resv.getLogin();
        Path path = null;
        try {
            path = resv.getPath(PathType.INTERDOMAIN);
        } catch (BSSException ex) {
            throw new InterdomainException(ex.getMessage());
        }
        if (path == null) {
           throw new InterdomainException(
                  "no path provided to forwarder create");
        }
        
        //check subscriptions to previous and next domain
        this.checkSubscriptions(path);
        
        Domain nextDomain = path.getNextDomain();
        if (nextDomain == null) {
            return false;
        }
        
        String url = nextDomain.getUrl();
        this.log.info("create.start forward to  " + url);
        EventProducer eventProducer = new EventProducer();
        eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FWD_STARTED, login, "JOB", resv);
        ForwardReply reply = this.forward("createReservation", resv, url);
        eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FWD_ACCEPTED, login, "JOB", resv);
        this.log.info("create.finish GRI is: " +
                      resv.getGlobalReservationId());
        return reply != null ? true : false;
    }

    public boolean modify(Reservation resv, Reservation persistentResv)
            throws InterdomainException {

        String url = null;
        String login = resv.getLogin();

        Path path = null;
        try {
            path = persistentResv.getPath(PathType.INTERDOMAIN);
        } catch (BSSException ex) {
            throw new InterdomainException(ex.getMessage());
        }
        
        if(path == null){
            return false;
        }
        
        //check subscriptions to previous and next domain
        this.checkSubscriptions(path);
        
        // currently get the next domain from the stored path
        if (path.getNextDomain() != null) {
            url = path.getNextDomain().getUrl();
        }

        if (url != null) {
            this.log.info("modify.start forward to  " + url);
            EventProducer eventProducer = new EventProducer();
            eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_FWD_STARTED, login, "JOB", persistentResv);
            ForwardReply reply = this.forward("modifyReservation", resv, url);
            eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_FWD_ACCEPTED, login, "JOB", persistentResv);
            this.log.info("modify.finish GRI is: " + persistentResv.getGlobalReservationId());
            return reply != null ? true : false;
        } else {
            return false;
        }
    }


    public boolean query(Reservation resv) throws InterdomainException {

        String url = null;
        Path path = null;
        try {
            path = resv.getPath(PathType.INTERDOMAIN);
        } catch (BSSException ex) {
            throw new InterdomainException(ex.getMessage());
        }


        if (path != null && path.getNextDomain() != null) {
            url = path.getNextDomain().getUrl();
        }
        if (url == null) { return false; }
        this.log.info("query forward to " + url);
        ForwardReply reply = this.forward("queryReservation", resv, url);
        return reply != null ? true : false;
    }

    public boolean cancel(Reservation resv) throws InterdomainException {

        String url = null;
        String login = resv.getLogin();

        Path path = null;
        try {
            path = resv.getPath(PathType.INTERDOMAIN);
        } catch (BSSException ex) {
            throw new InterdomainException(ex.getMessage());
        }
        
        if(path == null){
            return false;
        }
        
        //check subscriptions to previous and next domain
        this.checkSubscriptions(path);
        
        if (path.getNextDomain() != null) {
            url = path.getNextDomain().getUrl();
        }

        if (url == null) { return false; }
        this.log.info("cancel start forward to: " + url);
        EventProducer eventProducer = new EventProducer();
        eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_FWD_STARTED, login, "JOB", resv);
        ForwardReply reply = this.forward("cancelReservation", resv, url);
        eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_FWD_ACCEPTED, login, "JOB", resv);
        return reply != null ? true : false;
    }

    public boolean createPath(Reservation resv)
            throws InterdomainException {

        String url = null;
        Path path = null;
        try {
            path = resv.getPath(PathType.INTERDOMAIN);
        } catch (BSSException ex) {
            throw new InterdomainException(ex.getMessage());
        }
        
        if(path == null){
            return false;
        }
        
        //check subscriptions to previous and next domain
        this.checkSubscriptions(path);
        
        if (path.getNextDomain() != null) {
            url = path.getNextDomain().getUrl();
        }
        if (url == null) { return false; }
        this.log.info("createPath forward to: " + url);
        ForwardReply reply = this.forward("createPath", resv, url);
        return reply != null ? true : false;
    }

    public boolean refreshPath(Reservation resv)
            throws InterdomainException {

        String url = null;
        Path path = null;
        try {
            path = resv.getPath(PathType.INTERDOMAIN);
        } catch (BSSException ex) {
            throw new InterdomainException(ex.getMessage());
        }
        if(path == null){
            return false;
        }
        
        //check subscriptions to previous and next domain
        this.checkSubscriptions(path);
        
        if (path.getNextDomain() != null) {
            url = path.getNextDomain().getUrl();
        }
        if (url == null) { return false; }
        this.log.info("refreshPath forward to: " + url);
        ForwardReply reply = this.forward("refreshPath", resv, url);
        return reply != null ? true : false;
    }

    public boolean teardownPath(Reservation resv)
            throws InterdomainException {

        String url = null;
        Path path = null;
        try {
            path = resv.getPath(PathType.INTERDOMAIN);
        } catch (BSSException ex) {
            throw new InterdomainException(ex.getMessage());
        }
        
        if(path == null){
            return false;
        }
        
        //check subscriptions to previous and next domain
        this.checkSubscriptions(path);
        
        if (path.getNextDomain() != null) {
            url = path.getNextDomain().getUrl();
        }
        if (url == null) { return false; }
        this.log.info("teardownPath forward to: " + url);
        ForwardReply reply = this.forward("teardownPath", resv, url);
        return reply != null ? true : false;
    }

    public ForwardReply forward(String operation, Reservation resv,
                                String url) throws InterdomainException {

        this.log.debug("forward.start:  to " + url);
        setup(resv, url);
        String login = resv.getLogin();
        this.log.debug("forward.login: " + login);
        ForwardReply reply = null;
        Forward fwd =  new Forward();
        ForwardPayload forPayload = new ForwardPayload();
        fwd.setPayloadSender(login);
        forPayload.setContentType(operation);
        try {
            if (operation.equals("createReservation")) {
                forPayload.setCreateReservation(
                        toCreateRequest(resv));

            } else if (operation.equals("cancelReservation")) {
                GlobalReservationId rt = new GlobalReservationId();
                rt.setGri(resv.getGlobalReservationId());
                forPayload.setCancelReservation(rt);

            } else if (operation.equals("queryReservation")) {
                GlobalReservationId rt = new GlobalReservationId();
                rt.setGri(resv.getGlobalReservationId());
                forPayload.setQueryReservation(rt);
            } else if (operation.equals("modifyReservation")) {
                ModifyResContent modResContent = this.toModifyRequest(resv);
                forPayload.setModifyReservation(modResContent);

            } else if (operation.equals("createPath")) {
                CreatePathContent cp = new CreatePathContent();
                cp.setGlobalReservationId(resv.getGlobalReservationId());
                forPayload.setCreatePath(cp);
            } else if (operation.equals("refreshPath")) {
                RefreshPathContent rp = new RefreshPathContent();
                rp.setGlobalReservationId(resv.getGlobalReservationId());
                forPayload.setRefreshPath(rp);
            } else if (operation.equals("teardownPath")) {
                TeardownPathContent tp = new TeardownPathContent();
                tp.setGlobalReservationId(resv.getGlobalReservationId());
                forPayload.setTeardownPath(tp);
            }
            fwd.setPayload(forPayload);
            this.log.debug("forward.payloadSender: " + fwd.getPayloadSender());
            reply = super.forward(fwd);
            return reply;
        } catch (java.rmi.RemoteException e) {
            this.log.error("forward.failed, " + url + ": " + e.getMessage() );
            e.printStackTrace();
            throw new InterdomainException("failed to reach remote domain:" +
                                            url + e.getMessage());
        } catch (AAAFaultMessage e) {
            this.log.error("forward.AAAFaultMessage: " +
                                             e.getMessage());
            throw new InterdomainException("AAAFaultMessage from :" +
                                            url + e.getMessage());
        } catch (BSSFaultMessage e) {
            this.log.error ("forward.BSSFaultMessage: " +
                                            e.getMessage());
            throw new InterdomainException("BSSFaultMessage from :" +
                                            url +  e.getMessage());
        }
    }

    public ModifyResContent toModifyRequest(Reservation resv)
           throws InterdomainException {
        ModifyResContent modResContent = new ModifyResContent();
        PathInfo pathInfo = null;
        try {
            pathInfo =
                WSDLTypeConverter.getPathInfo(resv, PathType.INTERDOMAIN, false,
                                              null);
        } catch (BSSException e) {
            throw new InterdomainException(e.getMessage());
        }
        
        modResContent.setStartTime(resv.getStartTime());
        modResContent.setEndTime(resv.getEndTime());
        /* output bandwidth is in bytes, input is in Mbytes */
        Long bandwidth = resv.getBandwidth()/1000000;
        modResContent.setBandwidth( bandwidth.intValue());
        modResContent.setDescription(resv.getDescription());
        modResContent.setGlobalReservationId(resv.getGlobalReservationId());

        modResContent.setPathInfo(pathInfo);
        return modResContent;
    }

    public ResCreateContent toCreateRequest(Reservation resv)
            throws InterdomainException {

        ResCreateContent resCont = new ResCreateContent();
        PathInfo pathInfo = null;
        try {
            pathInfo =
                WSDLTypeConverter.getPathInfo(resv, PathType.INTERDOMAIN, false, null);
        } catch (BSSException ex) {
            throw new InterdomainException(ex.getMessage());
        }

        resCont.setStartTime(resv.getStartTime());
        resCont.setEndTime(resv.getEndTime());
        /* output bandwidth is in bytes, input is in Mbytes */
        Long bandwidth = resv.getBandwidth()/1000000;
        resCont.setBandwidth( bandwidth.intValue());
        resCont.setDescription(resv.getDescription());
        resCont.setGlobalReservationId(resv.getGlobalReservationId());
        resCont.setPathInfo(pathInfo);
        return resCont;
    }
    
    private void checkSubscriptions(Path path) throws InterdomainException{
        OSCARSCore core = OSCARSCore.getInstance();
        ServiceManager serviceMgr = core.getServiceManager();
        List<PathElem> pathElems = path.getPathElems();
        List<String> neighbors = new ArrayList<String>();
        DomainDAO domainDAO = new DomainDAO(core.getBssDbName());
        String localDomainId = domainDAO.getLocalDomain().getTopologyIdent();
        String prevDomainId = null;
        
        //Get previous domain link
        for(PathElem elem : pathElems){
            String elemDomainId = URNParser.parseTopoIdent(elem.getUrn()).get("domainId");
            if(localDomainId.equals(elemDomainId)){
                break;
            }
            prevDomainId = elemDomainId;
        }
        
        //Check previous domain subscription
        if(prevDomainId != null){
            neighbors.add(prevDomainId);
            this.log.debug("prevDomainId=" + prevDomainId);
        }
        //Check next domain subscription
        if( path.getNextDomain() != null){
            neighbors.add(path.getNextDomain().getTopologyIdent());
            this.log.debug("nextDomainId=" + path.getNextDomain().getTopologyIdent());
        }
        
        //Check subscriptions for previous and next domain
        for(String neighbor : neighbors){
            Object subscrId = serviceMgr.getServiceMapData("NB", neighbor);
            if(subscrId != null){
                this.log.debug("Subscription exists for " + neighbor);
                continue;
            }
            JobDataMap dataMap = new JobDataMap();
            dataMap.put("subscribe", true);
            dataMap.put("neighbor", neighbor);
            serviceMgr.scheduleServiceJob(SubscribeJob.class, dataMap, new Date());
            //wait up to 10 seconds for subscription
            for(int i=0; i < 30;i++){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new InterdomainException(e.getMessage());
                }
                if(serviceMgr.getServiceMapData("NB", neighbor) != null){
                    break;
                }
            }
            //If still null then throw an Exception
            if(serviceMgr.getServiceMapData("NB", neighbor) == null){
                throw new InterdomainException("Unable to complete " +
                        "interdomain request because unable to subscribe to " +
                        "notifications from " + neighbor);
            }
        }
    }
}
