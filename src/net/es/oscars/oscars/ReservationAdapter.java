/**
 * Intermediary between Axis2 and OSCARS libraries.
 *
 * All exceptions are passed back to OSCARSService, which logs them and maps
 * to the ADB classes that support SOAP faults.
 *  
 * @author David Robertson, Mary Thompson, Jason Lee
 */
package net.es.oscars.oscars;

import java.util.*;
import java.io.IOException;

import net.es.oscars.LogWrapper;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.BSSException;
import net.es.oscars.wsdlTypes.*;

/**
 * Acts as intermediary from Axis2 service to OSCARS library and Hibernate.
 */
public class ReservationAdapter {
    private LogWrapper log;
    private ReservationManager rm;

    public ReservationAdapter() {
        this.log = new LogWrapper(ReservationAdapter.class);
        this.rm = new ReservationManager();
    }

    /**
     * @param params ResCreateContent instance with with request params.
     * @param login String with user's login name
     * @return reply CreateReply encapsulating library reply.
     * @throws BSSException
     */
    public CreateReply create(ResCreateContent params, String login) 
            throws  BSSException {

        this.log.info("create.start", params.toString());
        this.rm.setSession();
        Reservation reservation = this.toReservation(params);
        String queryDomain = this.rm.create(reservation, login,
                                            params.getIngressRouterIP(),
                                             params.getEgressRouterIP());
        CreateReply reply = this.toReply(reservation);
        this.log.info("create.finish", reply.toString());
        return reply;
    }

    /**
     * @param params ResTag instance with with request params.
     * @param login String with user's login name
     * @return ResStatus reply CancelReservationResponse encapsulating library reply.
     * @throws BSSException 
     */
    public String cancel(ResTag params, String login) throws BSSException {

        String reply = null;
        String tag = params.getTag();
        this.log.info("cancel.start", tag);
        this.rm.setSession();
        reply = this.rm.cancel(tag, login);
        this.log.info("cancel.finish",
                      "tag: " + tag + ", status: " + reply);
        /** TODO Don't know if this should be fromString or fromValue --mrt
         */
        //return ResStatus.fromString(reply);
        return reply;
    }

    /**
     * @param params ResTag instance with with request params.
     * @param authorized boolean indicating user can view all reservations
     * @return reply ResDetails instance encapsulating library reply.
     * @throws BSSException 
     */
    public ResDetails query(ResTag params, boolean authorized)
            throws BSSException {

        Reservation reservation = null;

        String tag = params.getTag();
        this.log.info("query.start", tag);
        this.rm.setSession();
        reservation = this.rm.query(tag, authorized);
        ResDetails reply = this.toDetails(reservation);
        this.log.info("query.finish", reply.toString());
        return reply;
    }

    /**
     * @param login String with user's login name
     * @param authorized boolean indicating where user can view all reservations
     * @return reply ListReply encapsulating library reply.
     * @throws BSSException 
     */
    public ListReply list(String login, boolean authorized)
            throws BSSException {

        ListReply reply = null;
        List<Reservation> reservations = null;

        this.log.info("list.start", "");
        this.rm.setSession();
        if (!authorized) { login = null; }

        reservations = this.rm.list(login);
        reply = this.toListReply(reservations);
        this.log.info("list.finish", reply.toString());
        return reply;
    }

    // private methods for bean conversion

    /**
     * Converts Axis bean to Hibernate bean.
     * @param params ResCreateContent instance
     * @return A Hibernate reservation instance
     */
    private Reservation toReservation(ResCreateContent params) {

        Reservation reservation = new Reservation();
        reservation.setSrcHost(params.getSrcHost());
        reservation.setDestHost(params.getDestHost());
        reservation.setStartTime(
                    params.getStartTime().getTimeInMillis());
        reservation.setEndTime(
                    params.getEndTime().getTimeInMillis());
        Long bandwidth = new Long(
                Integer.valueOf(params.getBandwidth() * 1000000).longValue());
        reservation.setBandwidth(bandwidth);
        Long burstLimit = new Long(
                Integer.valueOf(params.getBurstLimit() * 1000000).longValue());
        reservation.setBurstLimit(burstLimit);
        reservation.setDescription(params.getDescription());
        reservation.setProtocol(params.getProtocol());
        reservation.setSrcPort(params.getSrcPort());
        reservation.setDestPort(params.getDestPort());
        return reservation;
    }

    /**
     * Converts Hibernate bean to Axis bean.
     * @param reservation A Hibernate reservation instance
     * @return CreateReply instance
     */
    private CreateReply toReply(Reservation reservation) {
        CreateReply reply = new CreateReply();
        reply.setTag(this.rm.toTag(reservation));
        /** TODO check on fromString vs fromValue */
        //reply.setStatus(ResStatus.fromString(reservation.getStatus()));
     reply.setStatus(reservation.getStatus());
        return reply;
    }

    /**
     * Converts Hibernate bean to Axis bean.
     * @param reservation A Hibernate reservation instance
     * @return ResDetails instance
     */
    private ResDetails toDetails(Reservation reservation) {

        String path = this.rm.pathToString(reservation, "ip");
        long millis = 0;

        ResDetails reply = new ResDetails();
        reply.setPath(path);
        reply.setTag(this.rm.toTag(reservation));
        //reply.setStatus(ResStatus.fromString(reservation.getStatus()));
        reply.setStatus(reservation.getStatus());
        reply.setSrcHost(reservation.getSrcHost());
        reply.setDestHost(reservation.getDestHost());
        // make sure that protocol is in upper case to match WSDL
        //reply.setProtocol(ResProtocolType.fromString(reservation.getProtocol().toUpperCase()));
        reply.setProtocol(reservation.getProtocol().toUpperCase());

        Calendar startTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        millis = reservation.getStartTime();
        startTime.setTimeInMillis(millis);
        reply.setStartTime(startTime);
        Calendar endTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        millis = reservation.getEndTime();
        endTime.setTimeInMillis(millis);
        reply.setEndTime(endTime);
        Calendar createTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        millis = reservation.getCreatedTime();
        createTime.setTimeInMillis(millis);
        reply.setCreateTime(createTime);

        int bandwidth = reservation.getBandwidth().intValue();
        reply.setBandwidth(bandwidth);
        int burstLimit = reservation.getBurstLimit().intValue();
        reply.setBurstLimit(burstLimit);
        reply.setResClass(reservation.getLspClass());
        reply.setDescription(reservation.getDescription());
        if (reservation.getSrcPort() != null){
            reply.setSrcPort(reservation.getSrcPort());
        } else {
            reply.setSrcPort(0);
        }
        if (reservation.getDestPort() != null){
            reply.setDestPort(reservation.getDestPort());
        } else {
            reply.setDestPort(0);
        } 
        return reply;
    }

    /**
     * Converts list of Hibernate beans to list of Axis beans.
     * @param reservations A list of Hibernate reservations
     * @return ListReply A list of Axis beans
     */
    private ListReply toListReply(List<Reservation> reservations) {
        ListReply reply = new ListReply();
        int ctr = 0;
        long millis = 0;

        if (reservations == null) { 
            this.log.info("Reservation Adapter","reservations is null");
            return reply;
        }
        int listLength = reservations.size();
        this.log.info("ResA size is", Integer.toString(listLength));
        ResInfoContent[] contents = new ResInfoContent[listLength];
        for (Reservation resv: reservations) {
            ResInfoContent content = new ResInfoContent();
            content.setTag(this.rm.toTag(resv));
           // content.setStatus(ResStatus.fromString(resv.getStatus()));
            content.setStatus(resv.getStatus());
            content.setSrcHost(resv.getSrcHost());
            content.setDestHost(resv.getDestHost());

            Calendar startTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            millis = resv.getStartTime();
            startTime.setTimeInMillis(millis);
            content.setStartTime(startTime);
            Calendar endTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            millis = resv.getEndTime();
            endTime.setTimeInMillis(millis);
            content.setEndTime(endTime);

            contents[ctr] = content;
            ctr++;
        }
        reply.setResInfo(contents);
        return reply;
    }
}
