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
import org.ogf.schema.network.topology.ctrlplane.*;
import org.quartz.*;

import net.es.oscars.lookup.LookupException;
import net.es.oscars.lookup.LookupFactory;
import net.es.oscars.lookup.PSLookupClient;
import net.es.oscars.notify.*;
import net.es.oscars.interdomain.*;
import net.es.oscars.scheduler.*;
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
    private String dbname;
    private OSCARSCore core;
    private static HashMap<String, String> payloadSender;
    
    public ReservationAdapter() {

        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
        this.dbname = this.core.getBssDbName();
        this.rm = this.core.getReservationManager();
        this.tc = this.core.getTypeConverter();

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
            throws BSSException {

        this.log.info("create.start");
        this.logCreateParams(params);
        Reservation resv = this.tc.contentToReservation(params);
        if (!this.core.initialized) {
            this.log.error("Core has not been initialized!");
            throw new BSSException("Core has not been initialized!");
        }

        PathInfo pathInfo = params.getPathInfo();
        CreateReply reply = null;
        EventProducer eventProducer = new EventProducer();
        this.setPayloadSender(resv);
        try {
            eventProducer.addEvent(OSCARSEvent.RESV_CREATE_RECEIVED, login, "API", resv, pathInfo);
            this.rm.submitCreate(resv, login, pathInfo);
            eventProducer.addEvent(OSCARSEvent.RESV_CREATE_ACCEPTED, login, "API", resv, pathInfo);

            this.log.debug("create, to toReply");
            reply = this.tc.reservationToReply(resv);
            
            ///PathInfo is unchanged so just return the user-given pathInfo
            reply.setPathInfo(pathInfo);
        } catch (BSSException e) {
            // send notification in all cases
            String errMsg = this.generateErrorMsg(resv, e.getMessage());
            eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FAILED, login, "API", resv, pathInfo, "", errMsg);
            throw new BSSException(e.getMessage());
        }

        this.log.info("create.finish: " + resv.toString("bss"));

        return reply;
    }

	/**
     * Extracts important information from Notify messages related to
     * creating a reservation and schedules them for execution.
     *
     * @param event the event that occurred
     * @param producerId the URL of the event producer
     * @param reqStatus the required status of the reservation
     */
    public void handleEvent(EventContent event, String producerId, String reqStatus){
        String eventType = event.getType();
        ResDetails resDetails = event.getResDetails();
        if(resDetails == null){
            this.log.error("No revservation details provided for event " + 
                           eventType + " from " + producerId);
            return;
        }
        String gri = resDetails.getGlobalReservationId();
        PathInfo pathInfo = resDetails.getPathInfo();
        String confirmed = "";
        String completed = "";
        String failed = "";
        
        if(reqStatus.equals(StateEngine.INCREATE)){
            confirmed = OSCARSEvent.RESV_CREATE_CONFIRMED;
            completed = OSCARSEvent.RESV_CREATE_COMPLETED;
            failed = OSCARSEvent.RESV_CREATE_FAILED;
        }else if(reqStatus.equals(StateEngine.INMODIFY)){
            confirmed = OSCARSEvent.RESV_MODIFY_CONFIRMED;
            completed = OSCARSEvent.RESV_MODIFY_COMPLETED;
            failed = OSCARSEvent.RESV_MODIFY_FAILED;
        }else if(reqStatus.equals(StateEngine.RESERVED)){
            confirmed = OSCARSEvent.RESV_CANCEL_CONFIRMED;
            completed = OSCARSEvent.RESV_CANCEL_COMPLETED;
            failed = OSCARSEvent.RESV_CANCEL_FAILED;
        }
        
        try{
            if(eventType.equals(confirmed)){
                this.rm.submitResvJob(gri, pathInfo, producerId, reqStatus, true);
            }else if(eventType.equals(completed)){
                this.rm.submitResvJob(gri, pathInfo, producerId, reqStatus, false);
            }else if(eventType.equals(failed)){
                String src = event.getErrorSource();
                String code = event.getErrorCode();
                String msg = event.getErrorMessage();
                this.rm.submitFailed(gri, pathInfo, producerId,
                                     src, code, msg, reqStatus);
            }else{
                this.log.debug("Discarding event " + eventType);
            }
        }catch(BSSException e){
            this.log.error(e.getMessage());
        }
    }

    /**
     * Modifies some pieces of a SCHEDULED reservation. For now only
     * start and end times are actually modified.
     * Attempts to modify local reservation and if that succeeds forwards
     * the request to the next domain.
     *
     * @param params ModifyResContent instance with with request params.
     * @param login String with user's login name
     * @param institution String with name of user's institution
     * @return reply CreateReply encapsulating library reply.
     * @throws BSSException
     */
    public ModifyResReply modify(ModifyResContent params, String loginConstraint,
                                    String login, String institution)
            throws BSSException{

        this.log.info("modify.start");
        EventProducer eventProducer = new EventProducer();
        Reservation resv = this.tc.contentToReservation(params);
        this.log.info("Reservation was: "+resv.getGlobalReservationId());

        ModifyResReply reply = null;
        try {
            eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_RECEIVED, login, "API", resv);
            Reservation persistentResv = this.rm.submitModify(resv, loginConstraint, login, institution);
            eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_ACCEPTED, login, "API", resv);
            reply = this.tc.reservationToModifyReply(persistentResv);
        } catch (BSSException e) {
            String errMsg = this.generateErrorMsg(resv, e.getMessage());
            eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_FAILED, login, "API",
                resv, "", errMsg);
            throw e;
        } catch (Exception e) {
            String errMsg = this.generateErrorMsg(resv, e.getMessage());
            eventProducer.addEvent(OSCARSEvent.RESV_MODIFY_FAILED, login, "API",
                resv, "", errMsg);
            throw new BSSException(e.getMessage());
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
     * @param institution String with name of user's institution
     * @return ResStatus reply CancelReservationResponse
     * @throws BSSException
     */
    public String cancel(GlobalReservationId params, String loginConstraint, 
                         String login, String institution) throws BSSException{
    	
        EventProducer eventProducer = new EventProducer();
        String gri = params.getGri();
        Reservation resv = null;
        try{
            this.log.info("cancel.start: " + gri);
            resv = this.rm.getConstrainedResv(gri, loginConstraint, institution);
            this.rm.submitCancel(resv,loginConstraint, login, institution);
            this.log.info("cancel.finish " + "GRI: " + gri + 
                          ", status: "  + resv.getStatus());
        }catch(BSSException e){
            eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_FAILED, login, "API",
                resv, "", e.getMessage());
            throw e;
        }catch(Exception e){
            eventProducer.addEvent(OSCARSEvent.RESV_CANCEL_FAILED, login, "API",
                resv, "", e.getMessage());
            throw new BSSException(e);
        }
        
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
     * @param params GlobalReservationId instance with request params.
     * @param login String user's login name
     * @param institution String user's institution name
     * @return reply ResDetails instance encapsulating library reply.
     * @throws BSSException
     */
    public ResDetails
        query(GlobalReservationId params, String login, String institution)
            throws BSSException, InterdomainException {

        Reservation resv = null;
        Forwarder forwarder = this.core.getForwarder();

        String gri = params.getGri();
        this.log.info("query.start: " + gri);
        resv = this.rm.query(gri, login, institution);
        ResDetails reply = this.tc.reservationToDetails(resv);
        // checks whether next domain should be contacted, forwards to
        // the next domain if necessary, and returns the response
        this.log.debug("query to forward");
        ResDetails forwardReply = null;
        InterdomainException interException = null;
        try {
            forwardReply = forwarder.query(resv);
        } catch (InterdomainException e) {
            interException = e;
        } finally {
            forwarder.cleanUp();
            if (interException != null) {
                throw interException;
            }
        }
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
     * @param institution String with the user's institution name
     *
     * @param request the listRequest received by OSCARSSkeleton. Includes an
     *  array of reservation statuses. a list of topology identifiers, a list
     *  of VLAN tags or ranges, start and end times, number of reservations
     *  requested, and offset of first reservation to return.  The items
     *  in the listRequest are
     *
     * If statuses is not empty, results will only include reservations with one
     * of these statuses.  If null / empty, results will include reservations
     * with any status.
     *
     * If topology identifiers is not null / empty, results will only
     * include reservations whose path includes at least one of the links.
     * If null / empty, results will include reservations with any path.
     *
     * vlanTags a list of VLAN tags.  If not null or empty,
     * results will only include reservations where (currently) the first link
     * in the path has a VLAN tag from the list (or ranges in the list).  If
     * null / empty, results will include reservations with any associated
     * VLAN.
     *
     * startTime is the start of the time window to look in; null for
     * everything before the endTime.
     *
     * endTime is the end of the time window to look in; null for
     * everything after the startTime, Leave both start and endTime null to
     * disregard time.
     *
     * @return reply ListReply encapsulating library reply.
     * @throws BSSException
     */
    public ListReply list(String login, String institution,ListRequest request)
                 throws BSSException, LookupException{
        ListReply reply = null;
        List<Reservation> reservations = null;

        ArrayList<net.es.oscars.bss.topology.Link> inLinks =
            new ArrayList<net.es.oscars.bss.topology.Link>();
        ArrayList<String> inVlanTags = new ArrayList<String>();
        ArrayList<String> statuses = new ArrayList<String>();

        // lookup name via perfSONAR Lookup Service
        PSLookupClient lookupClient = core.getLookupClient();

        this.log.info("list.start");
        String[] linkIds = request.getLinkId();
        VlanTag[] vlanTags = request.getVlanTag();
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
                            try {
                                String urn = lookupClient.lookup(s);
                                link = TopologyUtil.getLink(urn, this.dbname);
                            } catch(LookupException e){
                                throw new BSSException(e.getMessage());
                            }

                        }
                        inLinks.add(link);
                    } catch (BSSException ex) {
                        this.log.error("Could not get link for string: [" + s.trim()+"], error: ["+ex.getMessage()+"]");
                    }
                }
            }
        }

        if (vlanTags != null && vlanTags.length > 0) {
            for (VlanTag v: vlanTags) {
                String s = v.getString().trim();
                if (s != null && !s.equals("")) {
                    inVlanTags.add(s);
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
        reservations =
            this.rm.list(request.getResRequested(),
                         request.getResOffset(),
                         login, institution, statuses, description, inLinks,
                         inVlanTags, startTime, endTime);

        reply = this.tc.reservationToListReply(reservations);

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
        this.log.debug("added " + remoteHops.length + " remote hops to path");
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
                this.log.info("hop: " + this.tc.hopToURN(hops[i]));
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

    private String generateErrorMsg(Reservation resv, String errMsg) {
        //Have to be careful if creation did not get too far.
        if (resv == null) {
            errMsg = "Reservation scheduling entirely failed with " + errMsg;
        }else if (resv.getGlobalReservationId() != null) {
           errMsg = "Reservation scheduling through API failed with " + errMsg;
        }

        return errMsg;
    }
    
    /**
     * Adds a payload sender to the global hash map
     * @param gri the GRI of the reservation being created
     * @param sender the original sender of the createReservation message
     */
    static synchronized public void addPayloadSender(String gri, String sender){
    	if(ReservationAdapter.payloadSender == null){
    		ReservationAdapter.payloadSender = new HashMap<String,String>();
    	}
    	ReservationAdapter.payloadSender.put(gri, sender);
    }
    /**
     * Sets the payload sender of a reservation
     * @param resv the reservation with the payload to set
     */
    private synchronized void setPayloadSender(Reservation resv) {
    	String gri = resv.getGlobalReservationId();
		if(gri == null || ReservationAdapter.payloadSender == null){
			return;
		}
		
		if(ReservationAdapter.payloadSender.containsKey(gri)){
			resv.setPayloadSender(ReservationAdapter.payloadSender.get(gri));
			ReservationAdapter.payloadSender.remove(gri);
		}
	}
}
