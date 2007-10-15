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
import net.es.oscars.interdomain.*;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.topology.*;
import net.es.oscars.wsdlTypes.*;

/**
 * Acts as intermediary from Axis2 service to OSCARS library and Hibernate.
 */
public class ReservationAdapter {
    private Logger log;
    private ReservationManager rm;
    private TypeConverter tc;

    public ReservationAdapter() {
        this.log = Logger.getLogger(this.getClass());
        this.rm = new ReservationManager("bss");
        this.tc = new TypeConverter();
    }

    /**
     * @param params ResCreateContent instance with with request params.
     * @param login String with user's login name
     * @return reply CreateReply encapsulating library reply.
     * @throws BSSException
     */
    public CreateReply create(ResCreateContent params, String login) 
            throws BSSException, InterdomainException {

        this.log.info("create.start");
        Reservation resv = this.tc.contentToReservation(params);
        this.log.info("create.params: " + resv.toString());
        Forwarder forwarder = new Forwarder();
        PathInfo pathInfo = params.getPathInfo();
        this.rm.create(resv, login, pathInfo);
        // checks whether next domain should be contacted, forwards to
        // the next domain if necessary, and handles the response
        this.log.debug("create, to forward");
        CreateReply forwardReply = forwarder.create(resv, pathInfo);
        this.rm.finalizeResv(forwardReply, resv, pathInfo);
        // persist to db
        this.rm.store(resv);

        this.log.debug("create, to toReply");
        CreateReply reply = this.tc.reservationToReply(resv);
        if (pathInfo.getLayer3Info() != null && forwardReply != null && forwardReply.getPathInfo() != null) {
            // Add remote hops to returned explicitPath
            this.addHops(reply.getPathInfo(), forwardReply.getPathInfo());
        }
        // set to input argument, which possibly has been modified during
        // reservation creation
        pathInfo.getPath().setId("unimplemented");
        this.tc.clientConvert(pathInfo);
        reply.setPathInfo(pathInfo);
        this.log.info("create.finish: " + resv.toString());
        return reply;
    }

    /**
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
     * @param params GlobalReservationId instance with with request params.
     * @param allUsers boolean indicating user can view all reservations
     * @return reply ResDetails instance encapsulating library reply.
     * @throws BSSException 
     */
    public ResDetails query(GlobalReservationId params, String login, boolean allUsers)
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
     * @param login String with user's login name
     * @param allUsers boolean indicating if user can view all reservations
     * @return reply ListReply encapsulating library reply.
     * @throws BSSException 
     */
    public ListReply list(String login, boolean allUsers)
            throws BSSException {

        ListReply reply = null;
        List<Reservation> reservations = null;

        this.log.info("list.start");
        reservations = this.rm.list(login, allUsers);
        reply = this.tc.reservationToListReply(reservations);
        this.log.info("list.finish: " + reply.toString());
        return reply;
    }

    /**
     * Adds the remote hops to the local hops to create the complete path.
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
}
