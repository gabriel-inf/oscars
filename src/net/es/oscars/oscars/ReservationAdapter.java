/**
 * Intermediary between Axis2 and OSCARS libraries.
 *
 * All exceptions are passed back to OSCARSSkeleton, which logs them and maps
 * to the ADB classes that support SOAP faults.
 *
 * @author David Robertson, Mary Thompson, Jason Lee
 */
package net.es.oscars.oscars;

import java.util.*;
import java.io.IOException;

import org.apache.log4j.*;
import org.ogf.schema.network.topology.ctrlplane._20070626.*;

import net.es.oscars.notify.*;
import net.es.oscars.interdomain.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.*;


/**
 * Acts as intermediary from Axis2 service to OSCARS library and Hibernate.
 */
public class ReservationAdapter {
    private Logger log;
    private ReservationManager rm;
    private TypeConverter tc;
    private NotifyInitializer notifier;
    private String dbname;

    public ReservationAdapter() {
        this.log = Logger.getLogger(this.getClass());
        this.dbname = "bss";
        this.rm = new ReservationManager("bss");
        this.tc = new TypeConverter();
        this.notifier = new NotifyInitializer();
        try {
            this.notifier.init();
        } catch (NotifyException ex) {
            this.log.error("*** COULD NOT INITIALIZE NOTIFIER ***");
            // TODO:  ReservationAdapter, ReservationManager, etc. will
            // have init methods that throw exceptions that will not be
            // ignored it NotifyInitializer cannot be created.  Don't
            // want exceptions in constructor
        }
    }

    /**
     * Called by OSCARSSkeleton to create a reservation.
     * First finds or validates a path through the local domain and holds the 
     * resources. Then forwards the request to the next domain if there is one.
     * Finally stores the information received back from the forwarded request
     * to make a complete interdomain reservation.
     * 
     * @param params ResCreateContent instance with with request params.
     * @param login String with user's login name
     * @return reply CreateReply encapsulating library reply.
     * @throws BSSException
     */
    public CreateReply create(ResCreateContent params, String login)
            throws BSSException, InterdomainException {

        this.log.info("create.start");
        this.logCreateParams(params);
        Reservation resv = this.tc.contentToReservation(params);
        Forwarder forwarder = new Forwarder();
        PathInfo pathInfo = params.getPathInfo();
        CreateReply forwardReply = null;
        CreateReply reply = null;
        try {


            this.rm.create(resv, login, pathInfo);
            this.tc.ensureLocalIds(pathInfo);
            // checks whether next domain should be contacted, forwards to
            // the next domain if necessary, and handles the response
            this.log.debug("create, to forward");
            forwardReply = forwarder.create(resv, pathInfo);
            this.rm.finalizeResv(forwardReply, resv, pathInfo);
            // persist to db
            this.rm.store(resv);

            this.log.debug("create, to toReply");
            reply = this.tc.reservationToReply(resv);
            if (pathInfo.getLayer3Info() != null && forwardReply != null && forwardReply.getPathInfo() != null) {
                // Add remote hops to returned explicitPath
                this.addHops(reply.getPathInfo(), forwardReply.getPathInfo());
            }
            // set to input argument, which possibly has been modified during
            // reservation creation
            pathInfo.getPath().setId("unimplemented");
            this.tc.clientConvert(pathInfo);
            reply.setPathInfo(pathInfo);
            Map<String,String> messageInfo = new HashMap<String,String>();
            messageInfo.put("subject",
                            "Reservation " + resv.getGlobalReservationId() +
                             " scheduling through API succeeded");
            messageInfo.put("body", "Reservation scheduling succeeded.\n" +
                                  resv.toString("bss"));
            messageInfo.put("alertLine", resv.getDescription());
            NotifierSource observable = this.notifier.getSource();
            Object obj = (Object) messageInfo;
            observable.eventOccured(obj);
        } catch (BSSException e) {
            // send notification in all cases
            this.sendFailureNotification(resv, e.getMessage());
            throw new BSSException(e.getMessage());
        } catch (InterdomainException e) {
            // send notification in all cases
            this.sendFailureNotification(resv, e.getMessage());
            throw new InterdomainException(e.getMessage());
        }
        this.log.info("create.finish: " + resv.toString("bss"));
        return reply;
    }

    /**
     * Modifies some pieces of a SCHEDULED reservation. For now only
     * start and end times are actually modified.
     * Attempts to modify local reservation and if that succeeds forwards
     * the request to the next domain.
     * 
     * @param params ModifyResContent instance with with request params.
     * @param login String with user's login name
     * @return reply CreateReply encapsulating library reply.
     * @throws BSSException
     */
    public ModifyResReply modify(ModifyResContent params, String login, boolean allUsers)
            throws BSSException, InterdomainException {

        this.log.info("modify.start");

        Reservation resv = this.tc.contentToReservation(params);
        this.log.debug("Reservation was: "+resv.getGlobalReservationId());

        Forwarder forwarder = new Forwarder();
        PathInfo pathInfo = params.getPathInfo();
        ModifyResReply forwardReply = null;
        ModifyResReply reply = null;
        try {
            Reservation persistentResv = this.rm.modify(resv, login, pathInfo);
            this.tc.ensureLocalIds(pathInfo);
            // checks whether next domain should be contacted, forwards to
            // the next domain if necessary, and handles the response
            this.log.debug("modify, to forward");
            forwardReply = forwarder.modify(resv, pathInfo);
            persistentResv = this.rm.finalizeModifyResv(forwardReply, resv, pathInfo);


            this.log.debug("modify, to toModifyReply");
            reply = this.tc.reservationToModifyReply(persistentResv);

            // set to input argument, which possibly has been modified during
            // reservation creation
            if (pathInfo != null && pathInfo.getPath() != null) {
                pathInfo.getPath().setId("unimplemented");
                this.tc.clientConvert(pathInfo);
            }
            reply.getReservation().setPathInfo(pathInfo);


        } catch (BSSException e) {
            // send notification in all cases
            this.sendFailureNotification(resv, "modify failed with error: " + e.getMessage());
            throw new BSSException(e.getMessage());
        } catch (InterdomainException e) {
            // send notification in all cases
            this.sendFailureNotification(resv, "modify failed with error: " + e.getMessage());
            throw new InterdomainException(e.getMessage());
        }


        this.log.info("modify.finish");
        return reply;
    }

    /**
     * Cancels a scheduled or active reservation. Cancels the local reservation
     * and forwards the reply to the next domain, if any.
     * 
     * @param params GlobalReservationId instance with with request params.
     * @param login String with user's login name
     * @param allUsers boolean true if user can cancel other user's reservations
     * @return ResStatus reply CancelReservationResponse
     * @throws BSSException
     */
    public String cancel(GlobalReservationId params, String login, boolean allUsers)
            throws BSSException, InterdomainException {

        Reservation resv = null;
        Forwarder forwarder = new Forwarder();
        String remoteStatus;

        String gri = params.getGri();
        this.log.info("cancel.start: " + gri);
        resv = this.rm.cancel(gri, login, allUsers);
        this.log.info("cancel.finish " +
                      "GRI: " + gri + ", status: "  + resv.getStatus());
        // checks whether next domain should be contacted, forwards to
        // the next domain if necessary, and handles the response
        this.log.debug("cancel to forward");
        remoteStatus = forwarder.cancel(resv);
        this.rm.finalizeCancel(resv, remoteStatus);
        return resv.getStatus();
    }

    /**
     * Returns detailed information about a reservation.
     * Gets the local path from the intradomain path. Then forwards to 
     * request to get the inter domain hops and combines them to give a 
     * complete interdomain path. 
     * TODO: change TypeConverter.reservationToDetails to get the hops 
     * from the interdomain path rather than the intradomain path and then
     * drop the forwarder call. At the moment we don't require the interdomain
     * path to be complete since the topology exchanges may not all be complete yet.
     * 
     * @param params GlobalReservationId instance with with request params.
     * @param allUsers boolean indicating user can view all reservations
     * @return reply ResDetails instance encapsulating library reply.
     * @throws BSSException
     */
    public ResDetails
        query(GlobalReservationId params, String login, boolean allUsers)
            throws BSSException, InterdomainException {

        Reservation resv = null;
        Forwarder forwarder = new Forwarder();

        String gri = params.getGri();
        this.log.info("query.start: " + gri);
        resv = this.rm.query(gri, login, allUsers);
        ResDetails reply = this.tc.reservationToDetails(resv);
        // checks whether next domain should be contacted, forwards to
        // the next domain if necessary, and returns the response
        this.log.debug("query to forward");
        ResDetails forwardReply = forwarder.query(resv);
        this.log.debug("query, to toReply");
        if (forwardReply != null && forwardReply.getPathInfo() != null) {
            // Add remote hops to returned explicitPath
            this.addHops(reply.getPathInfo(), forwardReply.getPathInfo());
        }
        this.log.info("query.finish: " + reply.getGlobalReservationId());
        return reply;
    }

    /**
     * List all the reservations on this IDC that meet the input constraints.
     * 
     * @param login String with user's login name
     *
     * @param loginIds a list of user logins. If not null or empty, results will
     * only include reservations submitted by these specific users. If null / empty
     * results will include reservations by all users.
     *
     * @param request the listRequest received by OSCARSSkeleton. Includes an array
     *  of reservation statuses. a list of topology identifiers, start and end times,
     *  number of reservations requested, and offset of first reservation to return.
     *
     * If statuses is not empty, results will only include reservations with one of these statuses.
     * If null / empty, results will include reservations with any status.
     *
     * If topology identifiers is not null / empty, results will only
     * include reservations whose path includes at least one of the links.
     * If null / empty, results will include reservations with any path.
     *
     * startTime is the start of the time window to look in; null for everything before the endTime
     *
     * endTime is the end of the time window to look in; null for everything after the startTime,
     * leave both start and endTime null to disregard time
     *
     * @return reply ListReply encapsulating library reply.
     * @throws BSSException
     */
    public ListReply list(String login, List<String> loginIds,
        ListRequest request) throws BSSException {
        ListReply reply = null;
        List<Reservation> reservations = null;
        ArrayList<net.es.oscars.bss.topology.Link> inLinks = new ArrayList<net.es.oscars.bss.topology.Link>();
        ArrayList<String> statuses = new ArrayList<String>();
        PSLookupClient lookupClient = new PSLookupClient();

        this.log.info("list.start");
        String[] linkIds = request.getLinkId();
        String[] resStatuses = request.getResStatus();


        if (linkIds != null && linkIds.length > 0 ) {
            for (String s : linkIds) {
                s = s.trim();
                if (s != null && !s.equals("")) {
                    net.es.oscars.bss.topology.Link link = null;
                    try {
                        if (s.startsWith("urn:ogf:network")) {
                            link = TopologyUtil.getLink(s, this.dbname);
                        } else {
                            String urn = lookupClient.lookup(s);
                            link = TopologyUtil.getLink(urn, this.dbname);
                        }
                        inLinks.add(link);

                    } catch (BSSException ex) {
                        this.log.error("Could not get link for string: ["+s.trim()+"], error: ["+ex.getMessage()+"]");
                    }
                }
            }
        }

        if (resStatuses != null && resStatuses.length > 0 ) {
            for (String s : request.getResStatus()) {
                if (s != null && !s.trim().equals("")) {
                    statuses.add(s.trim());
                }
            }
        }

        Long startTime = null;
        Long endTime = null;
        ListRequestSequence_type0 tmp;
        tmp = request.getListRequestSequence_type0();
        if (tmp != null) {
            startTime = tmp.getStartTime();
            endTime = tmp.getEndTime();
        }
        String description = request.getDescription();
        reservations = this.rm.list(login, loginIds, statuses, description, inLinks, startTime, endTime);

        reply = this.tc.reservationToListReply(reservations,
                request.getResRequested(), request.getResOffset());
        this.log.info("list.finish: " + reply.toString());
        return reply;
    }

    /**
     * Adds the remote hops to the local hops to create the complete path.
     * 
     * @param localPathInfo - the path from the local reservation, has the
     *                        remote hops appended to it.
     * @param remotePathInfo - path returned from forward.create reservation
     *
     */
    private void addHops(PathInfo localPathInfo, PathInfo remotePathInfo) {

        CtrlPlanePathContent localPath = localPathInfo.getPath();
        if (localPath == null) { return; }
        CtrlPlaneHopContent[] localHops = localPath.getHop();
        CtrlPlanePathContent remotePath = remotePathInfo.getPath();
        if (remotePath == null) { return; }
        CtrlPlaneHopContent[] remoteHops = remotePath.getHop();
        for (int i=0; i < remoteHops.length;  i++) {
            localPath.addHop(remoteHops[i]);
        }
        this.log.debug("added " + remoteHops.length +
                       " remote hops to path");
        this.log.debug("complete path has " + localHops.length + " hops");
    }

    /**
     * Logs all incoming reservation creation parameters that are not
     * null before conversion.  XSD schema enforces not-null parameters.
     *
     * @param params ResCreateContent instance with with request params.
     */
    public void logCreateParams(ResCreateContent params)  {

        this.log.info("logCreateParams.start");
        if (params.getGlobalReservationId() != null) {
            this.log.info("GRI: " + params.getGlobalReservationId());
        }
        this.log.info("startTime: " + params.getStartTime());
        this.log.info("end time: " + params.getEndTime());
        this.log.info("bandwidth: " + params.getBandwidth());
        this.log.info("description: " + params.getDescription());
        PathInfo pathInfo = params.getPathInfo();
        this.log.info("path setup mode: " + pathInfo.getPathSetupMode());
        CtrlPlanePathContent path = pathInfo.getPath();
        if (path != null) {
            this.log.info("using ERO");
            CtrlPlaneHopContent[] hops = path.getHop();
            for (int i=0; i < hops.length; i++) {
                this.log.info("hop: " + hops[i].getLinkIdRef());
            }
        }
        Layer2Info layer2Info = pathInfo.getLayer2Info();
        if (layer2Info != null) {
            this.log.info("setting up a layer 2 reservation");
            if (layer2Info.getSrcVtag() != null) {
                this.log.info("src VLAN tag: " + layer2Info.getSrcVtag());
            }
            if (layer2Info.getDestVtag() != null) {
                this.log.info("dest VLAN tag: " + layer2Info.getDestVtag());
            }
            this.log.info("source endpoint: " + layer2Info.getSrcEndpoint());
            this.log.info("dest endpoint: " + layer2Info.getDestEndpoint());
        }
        Layer3Info layer3Info = pathInfo.getLayer3Info();
        if (layer3Info != null) {
            this.log.info("setting up a layer 3 reservation");
            this.log.info("source host: " + layer3Info.getSrcHost());
            this.log.info("dest host: " + layer3Info.getDestHost());
            if (layer3Info.getProtocol() != null) {
                this.log.info("protocol: " +  layer3Info.getProtocol());
            }
            if (layer3Info.getSrcIpPort() != 0) {
                this.log.info("src IP port: " +  layer3Info.getSrcIpPort());
            }
            if (layer3Info.getDestIpPort() != 0) {
                this.log.info("dest IP port: " +  layer3Info.getDestIpPort());
            }
            if (layer3Info.getDscp() != null) {
                this.log.info("dscp: " +  layer3Info.getDscp());
            }
        }
        MplsInfo mplsInfo = pathInfo.getMplsInfo();
        if (mplsInfo != null) {
            this.log.info("using MPLS information");
            this.log.info("burst limit: " + mplsInfo.getBurstLimit());
            if (mplsInfo.getLspClass() != null) {
                this.log.info("LSP class: " + mplsInfo.getLspClass());
            }
        }
        this.log.info("logCreateParams.finish");
    }

    private void sendFailureNotification(Reservation resv, String errMsg) {

        // ugly, but notifies in all cases.  Have to be careful if creation
        // did not get too far.
        Map<String,String> messageInfo = new HashMap<String,String>();
        if (resv == null) {
            messageInfo.put("subject",
                "Reservation scheduling through API entirely failed");
            messageInfo.put("body",
                "Reservation scheduling entirely failed with " + errMsg);
        } else if (resv.getGlobalReservationId() != null) {
            messageInfo.put("subject",
                "Reservation " + resv.getGlobalReservationId() + " failed");
            messageInfo.put("body",
                "Reservation scheduling through API failed with " +
                 errMsg + "\n" + resv.toString("bss"));
            messageInfo.put("alertLine", resv.getDescription());
        }
        NotifierSource observable = this.notifier.getSource();
        Object obj = (Object) messageInfo;
        observable.eventOccured(obj);
    }
}
