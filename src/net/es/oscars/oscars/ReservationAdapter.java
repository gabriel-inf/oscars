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
import net.es.oscars.interdomain.*;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.bss.Reservation;
import net.es.oscars.pathfinder.Path;
import net.es.oscars.pathfinder.Domain;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.topology.*;
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
            throws  BSSException, InterdomainException {

        this.log.info("create.start", params.toString());
 
        this.rm.setSession();
        Reservation resv = this.toReservation(params);
        Forwarder forwarder = new Forwarder();
        Domain nextDomain = this.rm.create(resv, login,
                                           params.getIngressRouterIP(),
                                           params.getEgressRouterIP());
        // checks whether next domain should be contacted, forwards to
        // the next domain if necessary, and handles the response
        forwarder.create(resv, nextDomain);
        CreateReply reply = this.toReply(resv);
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
        /** TODO Check for nextDomain
         */
        
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

        Reservation resv = null;

        String tag = params.getTag();
        this.log.info("query.start", tag);
        this.rm.setSession();
        resv = this.rm.query(tag, authorized);
        ResDetails reply = this.toDetails(resv);
        /* TODO check for nextDomain
         * */
        this.log.info("query.finish ", this.rm.toTag(resv));
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
        reservations = this.rm.list(login, authorized);
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

        Reservation resv = new Reservation();
        resv.setSrcHost(params.getSrcHost());
        resv.setDestHost(params.getDestHost());
        resv.setStartTime(params.getStartTime().getTimeInMillis());
        resv.setEndTime(params.getEndTime().getTimeInMillis());
        Long bandwidth = new Long(
                Integer.valueOf(params.getBandwidth() * 1000000).longValue());
        resv.setBandwidth(bandwidth);
        Long burstLimit = new Long(
                Integer.valueOf(params.getBurstLimit() * 1000000).longValue());
        resv.setBurstLimit(burstLimit);
        resv.setDescription(params.getDescription());
        resv.setProtocol(params.getProtocol());
        resv.setSrcPort(params.getSrcPort());
        resv.setDestPort(params.getDestPort());
        return resv;
    }

    /**
     * Converts Hibernate bean to Axis bean.
     * @param resv A Hibernate reservation instance
     * @return CreateReply instance
     */
    private CreateReply toReply(Reservation resv) {
        CreateReply reply = new CreateReply();
        reply.setTag(this.rm.toTag(resv));
        reply.setStatus(resv.getStatus());
        reply.setPath(toPath(resv));
        return reply;
    }

    /**
     * Converts Hibernate bean to Axis bean.
     * @param resv A Hibernate reservation instance
     * @return ResDetails instance
     */
    private ResDetails toDetails(Reservation resv) {

       // String path = this.rm.pathToString(resv, "ip");
        long millis = 0;

        ResDetails reply = new ResDetails();
        reply.setPath(this.toPath(resv));
        reply.setTag(this.rm.toTag(resv));
        //reply.setStatus(ResStatus.fromString(resv.getStatus()));
        reply.setStatus(resv.getStatus());
        reply.setSrcHost(resv.getSrcHost());
        reply.setDestHost(resv.getDestHost());
        // make sure that protocol is in upper case to match WSDL
        //reply.setProtocol(ResProtocolType.fromString(resv.getProtocol().toUpperCase()));
        reply.setProtocol(resv.getProtocol().toUpperCase());

        Calendar startTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        millis = resv.getStartTime();
        startTime.setTimeInMillis(millis);
        reply.setStartTime(startTime);
        Calendar endTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        millis = resv.getEndTime();
        endTime.setTimeInMillis(millis);
        reply.setEndTime(endTime);
        Calendar createTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        millis = resv.getCreatedTime();
        createTime.setTimeInMillis(millis);
        reply.setCreateTime(createTime);

        int bandwidth = resv.getBandwidth().intValue();
        reply.setBandwidth(bandwidth);
        int burstLimit = resv.getBurstLimit().intValue();
        reply.setBurstLimit(burstLimit);
        reply.setResClass(resv.getLspClass());
        reply.setDescription(resv.getDescription());
        if (resv.getSrcPort() != null){
            reply.setSrcPort(resv.getSrcPort());
        } else {
            reply.setSrcPort(0);
        }
        if (resv.getDestPort() != null){
            reply.setDestPort(resv.getDestPort());
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
    private ExplicitPath toPath(Reservation resv){
    	ExplicitPath ePath = new net.es.oscars.wsdlTypes.ExplicitPath();
    	HopList hList = new HopList();
    	Ipaddr ip = null;
    
    	Path nextPath = resv.getPath();
    	
    	while (nextPath != null ) {
    		Hop nextHop = new Hop();
           	nextHop.setLoose(false);
    	     ip=nextPath.getIpaddr();
    	     nextHop.setValue(ip.getIp());
    	     /* TODO parse the ip address to find out if it is ipv4 or 4 */
   	         nextHop.setType("ipv4");
    	     hList.addHop(nextHop);
    	     nextPath = nextPath.getNextPath();
    	}
    	ePath.setHops(hList);
    	/* TODO figure out a vlan tag */
    	ePath.setVtag(" ");
    	return ePath;
    }
}
