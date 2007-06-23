package net.es.oscars.interdomain;

import java.rmi.RemoteException;
import java.util.*;

import org.apache.log4j.*;
import org.apache.axis2.AxisFault;

import net.es.oscars.oscars.OSCARSStub;
import net.es.oscars.oscars.TypeConverter;
import net.es.oscars.oscars.AAAFaultMessageException;
import net.es.oscars.oscars.BSSFaultMessageException;
import net.es.oscars.pathfinder.CommonPath;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.client.*;

/**
 * Forwarding client.
 */
public class Forwarder extends Client {
    private Logger log;
    private TypeConverter tc;

    public Forwarder() {
        this.log = Logger.getLogger(this.getClass());
        this.tc = new TypeConverter();
    }

    private void setup(Reservation resv, String url) 
            throws InterdomainException {

        this.log.debug("setup.start: " + url);
        String catalinaHome = System.getProperty("catalina.home");
        // check for trailing slash
        if (!catalinaHome.endsWith("/")) {
            catalinaHome += "/";
        }
        String repo = catalinaHome + "shared/oscars.conf/axis2.repo/";
        System.setProperty("axis2.xml", repo + "axis2.xml");
        try {
            super.setUp(true, url, repo, repo + "axis2.xml");
        } catch (AxisFault af) {
            this.log.error("setup.axisFault: " + af.getMessage());
            throw new InterdomainException("failed to reach remote domain:" +
                                           url +  af.getMessage());
        }
        this.log.debug("setup.finish: " + url);
    }

    public CreateReply create(Reservation resv) 
            throws  InterdomainException {

        CreateReply createReply = null;

        CommonPath reqPath = resv.getCommonPath();
        if (reqPath == null) {
           throw new InterdomainException(
                  "no path provided to forwarder create");
        }
        String url = reqPath.getUrl();
        if (url == null) { return null; }
        this.log.info("create.start forward to  " + url);
        ForwardReply reply = this.forward("createReservation", resv, url,
                                          reqPath);
        createReply = reply.getCreateReservation();
        this.log.info("create.finish remote tag is: " + createReply.getTag());
        return createReply;
    }

    public ResDetails query(Reservation resv) throws InterdomainException {
        
        String url = null;

        if (resv.getPath() != null && resv.getPath().getNextDomain() != null) {
            url = resv.getPath().getNextDomain().getUrl();
        }
        if (url == null) { return null; }
        this.log.info("query forward to " + url);
        ForwardReply reply = this.forward("queryReservation", resv, url, null);
        return reply.getQueryReservation();
    }

    public String cancel(Reservation resv)
            throws InterdomainException {
        
        String url = null;

        if (resv.getPath() != null && resv.getPath().getNextDomain() != null) {
            url = resv.getPath().getNextDomain().getUrl();
        }
        if (url == null) { return null; }
        this.log.info("cancel start forward to: " + url);
        ForwardReply reply = this.forward("cancelReservation", resv, url,
                                          null);
        return reply.getCancelReservation();
    }

    public ForwardReply forward(String operation, Reservation resv, String url,
                                CommonPath reqPath)
            throws InterdomainException {

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
                        toCreateRequest(resv, reqPath));

            } else if (operation.equals("cancelReservation")) {
                ResTag rt = new ResTag();
                rt.setTag(this.tc.getReservationTag(resv));
                forPayload.setCancelReservation(rt);

            } else if (operation.equals("queryReservation")) {
                ResTag rt = new ResTag();
                rt.setTag(this.tc.getReservationTag(resv));
                forPayload.setQueryReservation(rt);
 
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
        } catch (AAAFaultMessageException e) {
            this.log.error("forward.AAAFaultMessageException: " +
                                             e.getMessage());
            throw new InterdomainException("AAAFaultMessage from :" +
                                            url + e.getMessage());
        } catch (BSSFaultMessageException e) {
            this.log.error ("forward.BSSFaultMessageException: " +
                                            e.getMessage());
            throw new InterdomainException("BSSFaultMessage from :" +
                                            url +  e.getMessage());
        }
    }
    
    public ResCreateContent toCreateRequest(Reservation resv,
                                            CommonPath reqPath) {

        long millis = -1;
        ResCreateContent resCont = new ResCreateContent();

        resCont.setSrcHost(resv.getSrcHost());
        resCont.setDestHost(resv.getDestHost());
        resCont.setStartTime(resv.getStartTime());
        resCont.setEndTime(resv.getEndTime());
        /* output bandwidth is in bytes, input is in Mbytes */
        Long bandwidth = resv.getBandwidth()/1000000;
        resCont.setBandwidth( bandwidth.intValue());
        resCont.setProtocol(resv.getProtocol());
        resCont.setDescription(resv.getDescription());
        // TODO - maybe we should set ingress router or srcHost for next hop
        // equal to egress router.
        ExplicitPath explicitPath =
            this.tc.commonPathToExplicitPath(reqPath);
        resCont.setReqPath(explicitPath);
        return resCont;
    }
}
