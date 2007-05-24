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

import net.es.oscars.interdomain.*;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.topology.*;
import net.es.oscars.pathfinder.CommonPath;
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
            throws  BSSException, InterdomainException {

        this.log.info("create.start");
 
        Reservation resv = this.tc.contentToReservation(params);
        this.log.info("create.params: " + resv.toString());
        CommonPath reqPath = this.tc.explicitPathToPath(params.getReqPath());
        Forwarder forwarder = new Forwarder();
        String url = this.rm.create(resv, login, params.getIngressRouterIP(),
                                    params.getEgressRouterIP(), reqPath);
        // checks whether next domain should be contacted, forwards to
        // the next domain if necessary, and handles the response
        this.log.debug("create, to forward");
        CreateReply forwardReply = forwarder.create(resv, reqPath);
        // persist to db
        this.rm.store(resv);

        this.log.debug("create, to toReply");
        CreateReply reply = this.tc.reservationToReply(resv);
        if (forwardReply != null && forwardReply.getPath() != null) {
            // Add remote hops to returned explicitPath
            this.AddHops(reply.getPath(), forwardReply.getPath());
            this.log.debug("complete path has " +
                           reply.getPath().getHops().getHop().length + "hops");
        }
        this.log.info("create.finish: " + resv.toString());
        return reply;
    }

    /**
     * @param params ResTag instance with with request params.
     * @param login String with user's login name
     * @return ResStatus reply CancelReservationResponse
     * @throws BSSException 
     */
    public String cancel(ResTag params, String login) 
            throws BSSException, InterdomainException  {

        Reservation resv = null;
        Forwarder forwarder = new Forwarder();
        String remoteStatus;
        
        String tag = params.getTag();
        this.log.info("cancel.start: " + tag);
        resv = this.rm.cancel(tag, login);
        this.log.info("cancel.finish " +
                      "tag: " + tag + ", status: "  + resv.getStatus());
        // checks whether next domain should be contacted, forwards to
        // the next domain if necessary, and handles the response
        this.log.debug("cancel to forward");
        remoteStatus = forwarder.cancel(resv);
        this.rm.finalizeCancel(resv, remoteStatus);
        return resv.getStatus();
    }

    /**
     * @param params ResTag instance with with request params.
     * @param authorized boolean indicating user can view all reservations
     * @return reply ResDetails instance encapsulating library reply.
     * @throws BSSException 
     */
    public ResDetails query(ResTag params, boolean authorized)
            throws BSSException, InterdomainException {

        Reservation resv = null;
        Forwarder forwarder = new Forwarder();

        String tag = params.getTag();
        this.log.info("query.start: " + tag);
        resv = this.rm.query(tag, authorized);
        ResDetails reply = this.tc.reservationToDetails(resv);
        // checks whether next domain should be contacted, forwards to
        // the next domain if necessary, and returns the response
        this.log.debug("query to forward");
        ResDetails forwardReply = forwarder.query(resv);
        this.log.debug("query, to toReply");
        if (forwardReply != null && forwardReply.getPath() != null) {
            // Add remote hops to returned explicitPath
            this.AddHops(reply.getPath(), forwardReply.getPath());
            this.log.debug("complete path has " +
                           reply.getPath().getHops().getHop().length + "hops");
        }
        this.log.info("query.finish: " + this.tc.getReservationTag(resv));
        return reply;
    }

    /**
     * @param login String with user's login name
     * @param authorized boolean indicating if user can view all reservations
     * @return reply ListReply encapsulating library reply.
     * @throws BSSException 
     */
    public ListReply list(String login, boolean authorized)
            throws BSSException {

        ListReply reply = null;
        List<Reservation> reservations = null;

        this.log.info("list.start");
        reservations = this.rm.list(login, authorized);
        reply = this.tc.reservationToListReply(reservations);
        this.log.info("list.finish: " + reply.toString());
        return reply;
    }

    /**
     * Adds the remote hops to the local hops to create the complete path.
     * @param localPath - the path from the local reservation, has the
     *                    remote hops appended to it.
     * @param remotePath - the path returned from forward.create reservation
     * 
     */
    private void AddHops(ExplicitPath localPath, ExplicitPath remotePath) {

        HopList localHops = localPath.getHops();
        Hop remoteHops[] = remotePath.getHops().getHop();       
        for (int i=0; i < remoteHops.length;  i++) {
            localHops.addHop(remoteHops[i]);
        }
        this.log.debug("added " + remoteHops.length + " remote hops to path");
    }
}
