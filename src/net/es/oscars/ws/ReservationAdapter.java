/**
 * Intermediary between Axis2 and OSCARS libraries.
 *
 * All exceptions are passed back to OSCARSSkeleton, which logs them and maps
 * to the ADB classes that support SOAP faults.
 *
 * @author David Robertson, Mary Thompson, Jason Lee
 */
package net.es.oscars.ws;

import java.util.*;
import java.io.IOException;

import org.apache.log4j.*;
import org.ogf.schema.network.topology.ctrlplane.*;

import net.es.oscars.wsdlTypes.*;
import net.es.oscars.bss.*;
import net.es.oscars.rmi.bss.*;
import net.es.oscars.rmi.bss.xface.*;


/**
 * Acts as intermediary from Axis2 service to OSCARS core via RMI calls.
 */
public class ReservationAdapter {
    private Logger log = Logger.getLogger(ReservationAdapter.class);
    private static HashMap<String, String> payloadSender;

    /**
     * Called by OSCARSSkeleton to create a reservation.
     * First finds or validates a path through the local domain and holds the
     * resources. Then forwards the request to the next domain if there is one.
     * Finally stores the information received back from the forwarded request
     * to make a complete interdomain reservation.
     *
     * @param params ResCreateContent instance with with request params.
     * @param username String with user's login name
     * @return reply CreateReply encapsulating server reply.
     * @throws BSSException
     */
    public CreateReply create(ResCreateContent soapParams, String username,
               BssRmiInterface rmiClient) throws BSSException {

        this.log.info("create.start");
        this.logCreateParams(soapParams);
        Reservation resv =
            WSDLTypeConverter.contentToReservation(soapParams);
        this.setPayloadSender(resv);
        PathInfo pathInfo = soapParams.getPathInfo();
        net.es.oscars.bss.topology.Path path =
            WSDLTypeConverter.convertPath(pathInfo);
        resv.setPath(path);
        CreateReply reply = null;

        String gri = null;
        try {
            gri = rmiClient.createReservation(resv, username);
            this.log.debug("create, to toReply");
            //Set status=ACCEPTED since can be assumed by lack of exception
            resv.setStatus(StateEngine.ACCEPTED);
            resv.setGlobalReservationId(gri);
            reply = WSDLTypeConverter.reservationToReply(resv);

            ///PathInfo is unchanged so just return the user-given pathInfo
            reply.setPathInfo(pathInfo);
        } catch (IOException e) {
            // send notification in all cases
            String errMsg =
                this.generateErrorMsg(resv, e.getMessage());
            this.log.error(errMsg);
            throw new BSSException(e.getMessage());
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
     * @param request ModifyResContent instance with with request params.
     * @param username String with user's login name
     * @return reply ModResReply encapsulating server reply.
     * @throws BSSException
     */
    public ModifyResReply modify(ModifyResContent request, String username,
               BssRmiInterface rmiClient) throws BSSException {

        this.log.info("modify.start");
        ModifyResReply reply = null;
        Reservation resv = new Reservation();
        resv.setGlobalReservationId(request.getGlobalReservationId());
        this.setPayloadSender(resv);
        resv.setStartTime(request.getStartTime());
        resv.setEndTime(request.getEndTime());
        Long bandwidth = new Long(
                Long.valueOf((long)request.getBandwidth() * 1000000L));
        resv.setBandwidth(bandwidth);
        resv.setDescription(request.getDescription());
        // path currently can't be modified
        Reservation modifiedResv = null;
        try {
            modifiedResv = rmiClient.modifyReservation(resv, username);
        } catch (Exception ex) {
            throw new BSSException(ex.getMessage());
        }
        reply = WSDLTypeConverter.reservationToModifyReply(modifiedResv);
        this.log.info("modify.finish");
        return reply;
    }

    /**
     * Cancels a scheduled or active reservation. Cancels the local reservation
     * and forwards the reply to the next domain, if any.
     *
     * @param params GlobalReservationId instance with with request params.
     * @param username String with user's login name
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
     * @param username String user's login name
     * @param institution String user's institution name
     * @return reply ResDetails instance encapsulating server reply.
     * @throws BSSException
     */
    public ResDetails query(QueryReservation request, String username,
            BssRmiInterface rmiClient) throws BSSException {

        String gri = request.getQueryReservation().getGri();
        this.log.info("QueryReservation.start: " + gri);
        RmiQueryResReply result = null;
        try {
            result = rmiClient.queryReservation(gri, username);
        } catch (Exception ex) {
            throw new BSSException(ex.getMessage());
        }
        ResDetails reply = WSDLTypeConverter.reservationToDetails(
                               result.getReservation(),
                               result.isInternalPathAuthorized(),
                               result.getLocalDomain());
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
     * @return reply ListReply encapsulating server reply.
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
           WSDLTypeConverter.reservationToListReply(
                   rmiReply.getReservations(),
                   rmiReply.isInternalPathAuthorized(),
                   rmiReply.getLocalDomain());
        this.log.info("list.finish: " + reply.toString());
        return reply;
    }

    /**
     * Makes a request for a network topology retrieval.
     *
     * @param params GetTopologyContent instance with with request params.
     * @param username String with user's login name
     * @return reply GetTopologyResponseContent encapsulating server reply.
     * @throws BSSException
     */
    public GetTopologyResponseContent
        getNetworkTopology(GetTopologyContent soapParams, String username,
               BssRmiInterface rmiClient) throws BSSException {

        this.log.info("getNetworkTopology.start");
        GetTopologyResponseContent reply = null;
        // note that Axis2 types are used in this method only
        try {
            reply =
                rmiClient.getNetworkTopology(soapParams, username);
        } catch (IOException e) {
            this.log.error(e.getMessage());
            throw new BSSException(e.getMessage());
        }
        this.log.info("createPath.finish");
        return reply;
    }

    /**
     * Sets up a path. Forwards request
     * first, and sets-up path is reply successful. If there is an error during
     * local path setup a teardownPath message is issued.
     *
     * @param params CreatePathContent instance with with request params.
     * @param username String with user's login name
     * @return reply CreatePathResponseContent encapsulating server reply.
     * @throws BSSException
     */
    public CreatePathResponseContent
        createPath(CreatePathContent soapParams, String username,
               BssRmiInterface rmiClient) throws BSSException {

        this.log.info("createPath.start");
        CreatePathResponseContent reply = new CreatePathResponseContent();
        RmiPathRequest rmiRequest = new RmiPathRequest();
        String gri = soapParams.getGlobalReservationId();
        rmiRequest.setGlobalReservationId(gri);
        rmiRequest.setToken(soapParams.getToken());
        String status = null;
        try {
            status = rmiClient.createPath(rmiRequest, username);
        } catch (IOException e) {
            this.log.error(e.getMessage());
            throw new BSSException(e.getMessage());
        }
        reply.setGlobalReservationId(gri);
        reply.setStatus(status);
        this.log.info("createPath.finish");
        return reply;
    }

    /**
     * Verifies a path in response to a refreshPath request.
     *
     * @param params RefreshPathContent instance with with request params.
     * @param username String with user's login name
     * @return reply RefreshPathResponseContent encapsulating server reply.
     * @throws BSSException
     */
    public RefreshPathResponseContent
        refreshPath(RefreshPathContent soapParams, String username,
               BssRmiInterface rmiClient) throws BSSException {

        this.log.info("refreshPath.start");
        RefreshPathResponseContent reply = new RefreshPathResponseContent();
        RmiPathRequest rmiRequest = new RmiPathRequest();
        String gri = soapParams.getGlobalReservationId();
        rmiRequest.setGlobalReservationId(gri);
        rmiRequest.setToken(soapParams.getToken());
        String status = null;
        try {
            status = rmiClient.refreshPath(rmiRequest, username);
        } catch (IOException e) {
            this.log.error(e.getMessage());
            throw new BSSException(e.getMessage());
        }
        reply.setGlobalReservationId(gri);
        reply.setStatus(status);
        this.log.info("refreshPath.finish");
        return reply;
    }

    /**
     * Removes a path in response to a teardown request.
     *
     * @param params TeardownPathContent instance with with request params.
     * @param username String with user's login name
     * @return reply TeardownPathResponseContent encapsulating server reply.
     * @throws BSSException
     */
    public TeardownPathResponseContent
        teardownPath(TeardownPathContent soapParams, String username,
               BssRmiInterface rmiClient) throws BSSException {

        this.log.info("teardownPath.start");
        TeardownPathResponseContent reply = new TeardownPathResponseContent();
        RmiPathRequest rmiRequest = new RmiPathRequest();
        String gri = soapParams.getGlobalReservationId();
        rmiRequest.setGlobalReservationId(gri);
        rmiRequest.setToken(soapParams.getToken());
        String status = null;
        try {
            status = rmiClient.teardownPath(rmiRequest, username);
        } catch (IOException e) {
            this.log.error(e.getMessage());
            throw new BSSException(e.getMessage());
        }
        reply.setGlobalReservationId(gri);
        reply.setStatus(status);
        this.log.info("teardownPath.finish");
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
        Logger log = Logger.getLogger(ReservationAdapter.class);
        if(ReservationAdapter.payloadSender == null){
            ReservationAdapter.payloadSender = new HashMap<String,String>();
        }
        //Try to get the lock for up to 10 seconds and then just take it because probably
        //an uncaught exception messed with things
        for(int i = 0; payloadSender.containsKey(gri) && i < 10; i++){
            log.debug("Waiting for lock on payloadSenders...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
        }
        ReservationAdapter.payloadSender.put(gri, sender);
        log.debug("got lock on payload senders");
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
    
    /**
     * Releases the payloadSender if an error occurs before the payloadSender is set
     * @param gri
     */
    public static synchronized void releasePayloadSender(String gri) {
        if(gri == null || ReservationAdapter.payloadSender == null){
            return;
        }

        if(ReservationAdapter.payloadSender.containsKey(gri)){
            ReservationAdapter.payloadSender.remove(gri);
        }
    }
}
