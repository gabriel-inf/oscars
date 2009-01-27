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

import net.es.oscars.notify.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.bss.*;
import net.es.oscars.rmi.bss.*;
import net.es.oscars.rmi.bss.xface.*;


/**
 * Acts as intermediary from Axis2 service to OSCARS library and Hibernate.
 */
public class ReservationAdapter {
    private Logger log = Logger.getLogger(ReservationAdapter.class);
    private ReservationManager rm;
    private String dbname;
    private static HashMap<String, String> payloadSender;

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
    public CreateReply create(ResCreateContent soapParams, String login,
               BssRmiInterface rmiClient) throws BSSException {

        this.log.info("create.start");
        this.logCreateParams(soapParams);
        Reservation resvRequest =
            WSDLTypeConverter.contentToReservation(soapParams);

        PathInfo pathInfo = soapParams.getPathInfo();
        net.es.oscars.bss.topology.Path path =
            WSDLTypeConverter.convertPath(pathInfo);
        resvRequest.addPath(path);
        CreateReply reply = null;
        this.setPayloadSender(resvRequest);

        Reservation resvResult = null;
        try {
            resvResult =
                rmiClient.createReservation(resvRequest, login);
            this.log.debug("create, to toReply");
            reply = WSDLTypeConverter.reservationToReply(resvResult);

            ///PathInfo is unchanged so just return the user-given pathInfo
            reply.setPathInfo(pathInfo);
        } catch (IOException e) {
            // send notification in all cases
            String errMsg =
                this.generateErrorMsg(resvRequest, e.getMessage());
            this.log.error(errMsg);
            throw new BSSException(e.getMessage());
        }
        this.log.info("create.finish: " + resvResult.toString("bss"));
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
    public ModifyResReply modify(ModifyResContent request, String username,
               BssRmiInterface rmiClient) throws BSSException {

        this.log.info("modify.start");
        ModifyResReply reply = null;
        HashMap<String, Object> result = new HashMap<String, Object>();
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("caller", "AAR");
        // FIXME: map request to params
        // FIXME: IMPORTANT
        try {
            result = rmiClient.modifyReservation(params, username);
        } catch (Exception ex) {
            throw new BSSException(ex.getMessage());
        }
        Reservation resv = (Reservation) result.get("reservation");

        reply = WSDLTypeConverter.reservationToModifyReply(resv);

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
    public String cancel(CancelReservation request, String username,
            BssRmiInterface rmiClient) throws BSSException {

        String gri = request.getCancelReservation().getGri();

        try {
            rmiClient.cancelReservation(gri, username);
        } catch (Exception ex) {
            throw new BSSException(ex.getMessage());
        }
        return "reservation canceled";
    }

    /**
     * Returns detailed information about a reservation.
     * Gets the local path from the intradomain path. Then forwards to
     * request to get the inter domain hops and combines them to give a
     * complete interdomain path.
     * TODO: change WSDLTypeConverter.reservationToDetails to get the hops
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
    public ResDetails query(QueryReservation request, String username,
            BssRmiInterface rmiClient) throws BSSException {

        String gri = request.getQueryReservation().getGri();
        this.log.info("QueryReservation.start: " + gri);
        RmiQueryResRequest rmiRequest = new RmiQueryResRequest();
        rmiRequest.setGlobalReservationId(gri);
        RmiQueryResReply result = null;
        try {
            result = rmiClient.queryReservation(rmiRequest, username);
        } catch (Exception ex) {
            throw new BSSException(ex.getMessage());
        }
        Reservation resv = result.getReservation();
        ResDetails reply = WSDLTypeConverter.reservationToDetails(resv);
        this.log.info("QueryReservation.finish: " +
                       reply.getGlobalReservationId());
        return reply;
    }

    /**
     * List all the reservations on this IDC that meet the input constraints.
     *
     * @param username String with user's login name
     * @param institution String with the user's institution name
     *
     * @param request the listRequest received by OSCARSSkeleton. Includes an
     *  array of reservation statuses. a list of topology identifiers, a list
     *  of VLAN tags or ranges, start and end times, number of reservations
     *  requested, and offset of first reservation to return.  The items
     *  in the listRequest are
     *
     * @return reply ListReply encapsulating library reply.
     * @throws BSSException
     */
    public ListReply
        list(ListRequest request, String username, BssRmiInterface rmiClient)
            throws BSSException {

        this.log.info("list.start");
        Long startTime = null;
        Long endTime = null;
        ListRequestSequence_type0 tmp;
        tmp = request.getListRequestSequence_type0();
        if (tmp != null) {
            startTime = tmp.getStartTime();
            endTime = tmp.getEndTime();
        }
        List<String> inLinks = new ArrayList<String>();
        List<String> inVlanTags = new ArrayList<String>();
        List<String> statuses = new ArrayList<String>();

        String[] linkIds = request.getLinkId();
        VlanTag[] vlanTags = request.getVlanTag();
        String[] resStatuses = request.getResStatus();
        if (linkIds != null) {
            for (String linkId: linkIds) {
                if ((linkId != null) && !linkId.trim().equals("")) {
                    inLinks.add(linkId.trim());
                }
            }
        }
        if (vlanTags != null) {
            for (VlanTag v: vlanTags) {
                if (v != null) {
                    String s = v.getString();
                    if (s != null && !s.trim().equals("")) {
                        inVlanTags.add(s.trim());
                    }
                }
            }
        }
        if (resStatuses != null) {
            for (String s: resStatuses) {
                if (s != null && !s.trim().equals("")) {
                    statuses.add(s.trim());
                }
            }
        }
        RmiListResRequest rmiRequest = new RmiListResRequest();
        rmiRequest.setNumRequested(request.getResRequested());
        rmiRequest.setResOffset(request.getResOffset());
        rmiRequest.setStatuses(statuses);
        rmiRequest.setDescription(request.getDescription());
        rmiRequest.setLinkIds(inLinks);
        rmiRequest.setVlanTags(inVlanTags);
        rmiRequest.setStartTime(startTime);
        rmiRequest.setEndTime(endTime);
        RmiListResReply rmiReply = null;
        try {
            rmiReply = rmiClient.listReservations(rmiRequest, username);
        } catch (Exception ex) {
            throw new BSSException(ex.getMessage());
        }
        ListReply reply =
           WSDLTypeConverter.reservationToListReply(rmiReply.getReservations());
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
                this.log.info("hop: " + WSDLTypeConverter.hopToURN(hops[i]));
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
