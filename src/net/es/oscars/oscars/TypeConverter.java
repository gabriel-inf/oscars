/**
 * Type converter between Axis2 classes and Hibernate beans.
 *
 * @author David Robertson, Mary Thompson, Jason Lee
 */
package net.es.oscars.oscars;

import java.util.*;
import java.io.IOException;

import org.apache.log4j.*;

import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.topology.*;
import net.es.oscars.pathfinder.CommonPath;
import net.es.oscars.pathfinder.CommonPathElem;
import net.es.oscars.pathfinder.EdgeInfo;
import net.es.oscars.wsdlTypes.*;

/**
 * Has methods to convert between Axis2 WSDL type classes and Hibernate beans.
 * Used by both the API and the WBUI.
 */
public class TypeConverter {

    private Logger log;

    public TypeConverter() {
        this.log = Logger.getLogger(this.getClass());
    }

    /**
     * Builds Hibernate Reservation bean, given Axis2 ResCreateContent class.
     *
     * @param params ResCreateContent instance
     * @return A Hibernate Reservation instance
     * @throws BSSException
     */
    public Reservation contentToReservation(ResCreateContent params) 
            throws BSSException {

        Reservation resv = new Reservation();
        // Hibernate will pick up error if any properties are null that
        // the schema says cannot be null
        resv.setSrcHost(params.getSrcHost());
        resv.setDestHost(params.getDestHost());

        resv.setStartTime(params.getStartTime());
        resv.setEndTime(params.getEndTime());
        Long bandwidth = new Long(
                Integer.valueOf(params.getBandwidth() * 1000000).longValue());
        resv.setBandwidth(bandwidth);
        Long burstLimit = new Long(
                Integer.valueOf(params.getBurstLimit() * 1000000).longValue());
        resv.setBurstLimit(burstLimit);

        resv.setDescription(params.getDescription());
        resv.setProtocol(params.getProtocol());
        if (params.getSrcIpPort() != 0) {
            resv.setSrcIpPort(params.getSrcIpPort());
        }
        if (params.getDestIpPort() != 0) {
            resv.setDestIpPort(params.getDestIpPort());
        }
        CommonPath path = this.explicitPathToPath(params.getReqPath());
        if (path == null) {
            path = new CommonPath();
            path.setExplicit(false);
        } else {
            path.setExplicit(true);
        }
        path.setVlanTag(params.getVtag());
        PortID srcPortId = params.getSrcPortId();
        if (srcPortId != null) {
            EdgeInfo srcEdgeInfo = new EdgeInfo();
            srcEdgeInfo.setPortType(srcPortId.getType());
            srcEdgeInfo.setPortValue(srcPortId.getValue());
            path.setSrcEdgeInfo(srcEdgeInfo);
        }
        PortID destPortId = params.getDestPortId();
        if (destPortId != null) {
            EdgeInfo destEdgeInfo = new EdgeInfo();
            destEdgeInfo.setPortType(destPortId.getType());
            destEdgeInfo.setPortValue(destPortId.getValue());
            path.setDestEdgeInfo(destEdgeInfo);
        }
        resv.setCommonPath(path);
        return resv;
    }

    /**
     * Builds Axis2 CreateReply class, given Hibernate Reservation bean.
     *
     * @param resv A Hibernate Reservation instance
     * @return CreateReply instance
     */
    public CreateReply reservationToReply(Reservation resv) {
        CreateReply reply = new CreateReply();
        reply.setTag(this.getReservationTag(resv));
        reply.setStatus(resv.getStatus());
        reply.setPath(this.commonPathToExplicitPath(resv.getCommonPath()));
        return reply;
    }

    /**
     * Builds Axis2 ResDetails class, given Hibernate bean.
     * Note that this is used by only by query, and is using
     * information from a stored reservation.
     * 
     * @param resv A Hibernate reservation instance
     * @return ResDetails instance
     */
    public ResDetails reservationToDetails(Reservation resv) {

        this.log.debug("toDetails start");
        ResDetails reply = new ResDetails();
        ResSummary summary = new ResSummary();
        reply.setPath(this.pathToExplicitPath(resv.getPath()));
        Vlan vlan = resv.getPath().getVlan();
        if (vlan != null) {
            reply.setVtag(vlan.getVlanTag());
        }
        summary.setTag(this.getReservationTag(resv));
        summary.setStatus(resv.getStatus());
        summary.setSrcHost(resv.getSrcHost());
        summary.setDestHost(resv.getDestHost());
        summary.setStartTime(resv.getStartTime());
        summary.setEndTime(resv.getEndTime());
        reply.setInfo(summary);
        // make sure that protocol is in upper case to match WSDL
        if (resv.getProtocol() != null) {
            reply.setProtocol(resv.getProtocol().toUpperCase());
        }

        reply.setCreateTime(resv.getCreatedTime());

        int bandwidth = resv.getBandwidth().intValue();
        reply.setBandwidth(bandwidth);
        int burstLimit = resv.getBurstLimit().intValue();
        reply.setBurstLimit(burstLimit);
        reply.setResClass(resv.getLspClass());
        reply.setDescription(resv.getDescription());
        if (resv.getSrcIpPort() != null) {
            reply.setSrcIpPort(resv.getSrcIpPort());
        }
        if (resv.getDestIpPort() != null) {
            reply.setDestIpPort(resv.getDestIpPort());
        }
        this.log.debug("toDetails finish");
        return reply;
    }

    /**
     * Builds list of Axis2 ListReply instances, given list of Hibernate
     * Reservation beans.
     *
     * @param reservations A list of Hibernate Reservation beans
     * @return ListReply A list of Axis2 ListReply instances
     */
    public ListReply reservationToListReply(List<Reservation> reservations) {
        ListReply reply = new ListReply();
        int ctr = 0;

        if (reservations == null) { 
            this.log.info("toListReply, reservations is null");
            return reply;
        }
        int listLength = reservations.size();
        this.log.info("toListReply.reservationsSize: " +
                                                 Integer.toString(listLength));
        ResSummary[] summaries = new ResSummary[listLength];
        for (Reservation resv: reservations) {
            ResSummary summary = new ResSummary();
            summary.setTag(this.getReservationTag(resv));
            summary.setStatus(resv.getStatus());
            summary.setSrcHost(resv.getSrcHost());
            summary.setDestHost(resv.getDestHost());

            summary.setStartTime(resv.getStartTime());
            summary.setEndTime(resv.getEndTime());

            summaries[ctr] = summary;
            ctr++;
        }
        reply.setResInfo(summaries);
        return reply;
    }
    
    /**
     * Builds Axis2 ExplicitPath, given Hibernate Path bean with
     * information retrieved from database.
     *
     * @param path a Path instance
     * @return An ExplicitPath instance
     */
    public ExplicitPath pathToExplicitPath(Path path) {

        PathElem pathElem = path.getPathElem();
        ExplicitPath explicitPath =
            new net.es.oscars.wsdlTypes.ExplicitPath();
        HopList hList = new HopList();
    
        while (pathElem != null) {
            Hop hop = new Hop();
            hop.setLoose(pathElem.isLoose());
            hop.setValue(pathElem.getIpaddr().getIP());
            hList.addHop(hop);
            pathElem = pathElem.getNextElem();
        }
        explicitPath.setHops(hList);
        this.log.debug("pathToExplicitPath finish");
        return explicitPath;
    }
    
    /**
     * Builds Axis2 ExplicitPath, given non-Hibernate CommonPath bean.
     *
     * @param path a Path instance
     * @return An ExplicitPath instance
     */
    public ExplicitPath commonPathToExplicitPath(CommonPath path) {

        this.log.debug("commonPathToExplicitPath.start");
        List<CommonPathElem> elems = path.getElems();
        ExplicitPath explicitPath =
            new net.es.oscars.wsdlTypes.ExplicitPath();
        HopList hList = new HopList();
    
        for (int i=0; i < elems.size(); i++) {
            CommonPathElem pathElem = elems.get(i);
            // Don't forward interior addresses.  Ingress
            // and egress elements have different description.
            String description = pathElem.getDescription();
            if ((description != null) && description.equals("local")) {
                continue;
            }
            Hop hop = new Hop();
            hop.setLoose(pathElem.isLoose());
            hop.setValue(pathElem.getIP());
            hList.addHop(hop);
            this.log.debug("hop: " + pathElem.getIP());
        }
        explicitPath.setHops(hList);
        this.log.debug("commonPathToExplicitPath finish");
        return explicitPath;
    }
    
    /**
     * Builds non-Hibernate-dependent CommonPath, given Axis2 ExplicitPath 
     * instance.  Sets to null if missing any information.
     * @param explicitPath An ExplicitPath instance
     * @return CommonPath instance, null if param was missing any information
     */
    public CommonPath explicitPathToPath(ExplicitPath explicitPath){

        CommonPath path = new CommonPath();
        List<CommonPathElem> elems = new ArrayList<CommonPathElem>();

        if (explicitPath == null) { return null; }
        this.log.info("create.firstTest, explicitPath is not null");
        HopList hops = explicitPath.getHops();
        Hop hop[] = hops.getHop();
        if (hops != null)  {
            if (hop.length == 0) {
                explicitPath = null;
                this.log.info("create, explicitPath is null");
                return null;
            } else {
                this.log.info("create, explicitPath is not null");
            }
            CommonPathElem pathElem = null;
            for (int i=0; i < hop.length; i++) {
                pathElem = new CommonPathElem();
                pathElem.setLoose(hop[i].getLoose());
                pathElem.setIP(hop[i].getValue());
                elems.add(pathElem);
            }
        }
        path.setElems(elems);
        return path;
    }

    /**
     * Given a reservation instance, constructs a tag which is unique across
     *     domains, and which can be used to retrieve a reservation.
     *
     * @param resv a reservation instance
     * @return tag string with unique tag
     */
    public String getReservationTag(Reservation resv) {
        return this.computeTag(resv.getStartTime(), resv.getId(),
                               resv.getLogin());
    }

    /**
     * Given set of fields from a reservation, constructs a tag which is
     * unique across domains, and which can be used to retrieve a reservation.
     *
     * @param startSeconds a long with the reservation's start time
     * @param id an integer with the reservation's id
     * @param login a string with the user's login name
     * @return tag string with unique tag
     */
    private String computeTag(Long startSeconds, Integer id, String login) {
        long millis = 0;

        String abbrev = null;

        if (startSeconds == null) { return ""; }
        // punt for now
        DomainDAO domainDAO = new DomainDAO("bss");
        Domain domain = domainDAO.getLocalDomain();
        Calendar startTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        millis = startSeconds;
        startTime.setTimeInMillis(millis);
        // months start at 0 in Calendar
        int month = startTime.get(Calendar.MONTH) + 1;
        if ((domain != null) && (domain.getAbbrev() != null)) {
            abbrev = domain.getAbbrev();
        } else {
            abbrev = "";
        }
        String tag = abbrev + "-" + id + "-" +
            login + "-" +
            startTime.get(Calendar.YEAR) + "-" +
            this.fixedLengthTime(month) + "-" +
            this.fixedLengthTime(startTime.get(Calendar.DAY_OF_MONTH));
        return tag;
    }

    /**
     * If given an int whose string length is less than 2, prepends a "0".
     *
     * @param dint int, for example representing month or day
     * @return fixedLength fixed length string of length 2
     */
    private String fixedLengthTime(int dint) {
        String fixedLength = null;

        if (dint < 10) { fixedLength = "0" + dint; }
        else { fixedLength = "" + dint; }
        return fixedLength;
    }
}
