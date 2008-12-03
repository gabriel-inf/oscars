package net.es.oscars.rmi.bss;

/**
 * Interface between rmi queryReservation and ReservationManager.queryReservation
 *
 * @author Mary Thompson, David Robertson
 */

import java.io.*;
import java.rmi.RemoteException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import org.apache.log4j.*;
import org.hibernate.*;
import net.es.oscars.aaa.*;
import net.es.oscars.aaa.UserManager.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.Layer2Data;
import net.es.oscars.bss.topology.Layer3Data;
import net.es.oscars.bss.topology.MPLSData;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.bss.topology.PathType;
import net.es.oscars.bss.topology.PathElem;
import net.es.oscars.database.*;
import net.es.oscars.oscars.*;

/**
 * QueryResRmiHandler - interfaces between servlet and ReservationManager
 */
public class QueryResRmiHandler {
    private OSCARSCore core;
    private Logger log = Logger.getLogger(QueryResRmiHandler.class);


    public QueryResRmiHandler() {
        this.core = OSCARSCore.getInstance();
    }

    /**
     * Finds reservation based on information passed from servlet.
     *
     * @param params HashMap contains the gri of the reservation
     * @param userName String - name of user  making request
     * @return HashMap contains: gri, status, user, description
     *   start, end and create times, bandwidth, vlan tag, and path information.
     * @throws IOException
     */
    public HashMap<String, Object>
          queryReservation(HashMap<String, Object> params, String userName)
            throws IOException, RemoteException {
        this.log.debug("query.start");
        String methodName = "QueryReservation";
        HashMap<String, Object> result = new HashMap<String, Object>();

        ReservationManager rm = core.getReservationManager();
        UserManager userMgr = core.getUserManager();

        boolean internalIntradomainHops = false;
        Reservation reservation = null;
        String institution = null;
        String loginConstraint = null;
        result.put("method", methodName);
        
        String caller = (String) params.get("caller");

        // AAA section start
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();

        // check to see if user is allowed to query at all, and if they can
        // only look at reservations they have made
        AuthValue authVal = userMgr.checkAccess(userName, "Reservations", "query");
        if (authVal == AuthValue.DENIED) {
            result.put("error", "no permission to query Reservations");
            this.log.debug("query failed: no permission to query Reservations");
            aaa.getTransaction().rollback();
            return result;
        }
        if (authVal.equals(AuthValue.MYSITE)) {
            institution = userMgr.getInstitution(userName);
        } else if (authVal.equals(AuthValue.SELFONLY)){
            loginConstraint = userName;
        }
        
        // Put some things in result but only for WBUI calls
        if (caller.equals("WBUI")) {
	        // check to see if user is allowed to see the buttons allowing
	        // reservation modification
	        authVal = userMgr.checkAccess(userName, "Reservations", "modify");
	        if (authVal != AuthValue.DENIED) {
	            result.put("resvModifyDisplay", Boolean.TRUE);
	            result.put("resvCautionDisplay", Boolean.TRUE);
	        } else {
	            result.put("resvModifyDisplay", Boolean.FALSE);
	            result.put("resvCautionDisplay", Boolean.FALSE);
	        }
	        // check to see if user is allowed to see the clone button, which
	        // requires generic reservation create authorization
	        authVal = userMgr.checkModResAccess(userName, "Reservations", "create", 0, 0, false, false);
	        if (authVal != AuthValue.DENIED) {
	            result.put("resvCloneDisplay", Boolean.TRUE);
	        } else {
	            result.put("resvCloneDisplay", Boolean.FALSE);
	        }
        }
        
        // check to see if may look at internal intradomain path elements
        // if user can specify hops on create, he can look at them
        AuthValue authValHops = userMgr.checkModResAccess(userName, "Reservations", "create", 0, 0, true, false );
        if  (authValHops != AuthValue.DENIED ) {
            internalIntradomainHops = true;
        };
        
        aaa.getTransaction().commit();
        // AAA section end

        
        // BSS section start
        Session bss = core.getBssSession();
        bss.beginTransaction();
        String gri = (String) params.get("gri");
        
        if (caller.equals("WBUI")) {
	        try {
	            reservation = rm.query(gri, loginConstraint, institution);
	        } catch (BSSException e) {
	            result.put("error",  e.getMessage());
	            bss.getTransaction().rollback();
	            this.log.debug("query failed: " + e.getMessage());
	            return result;
	        }
	        if (reservation == null) {
	            result.put("error", "reservation does not exist");
	            bss.getTransaction().rollback();
	            this.log.debug("query failed: reservation does not exist");
	            return result;
	        }
	        try {
		        result.put("status", "Reservation details for " + reservation.getGlobalReservationId());
		        this.contentSection(result, reservation, userName, internalIntradomainHops);
		        result.put("success", Boolean.TRUE);
	        } catch (BSSException ex) {
	        	log.error(ex);
	            bss.getTransaction().rollback();
	            throw new RemoteException(ex.getMessage());
	        }
        } else if (caller.equals("API")) {
	        try {
	            reservation = rm.query(gri, loginConstraint, institution);
		        if (reservation == null) {
		        	throw new BSSException("Reservation with gri: "+gri+" does not exist");
		        }
	            
	            this.initialize(reservation);
	            result.put("reservation", reservation);
	        } catch (BSSException e) {
	            bss.getTransaction().rollback();
	            this.log.debug(e.getMessage());
	        	throw new RemoteException(e.getMessage());
	        }
        } else {
            bss.getTransaction().rollback();
            throw new RemoteException("Internal error");
        }

        bss.getTransaction().commit();
        
        // BSS section end
        this.log.debug("query.end");
        return result;
    }
    
    public void initialize(Reservation reservation) {
        Hibernate.initialize(reservation);
        Hibernate.initialize(reservation.getToken());
        Set<Path> paths = reservation.getPaths();
        Iterator<Path> pathIt = paths.iterator();
        while (pathIt.hasNext()) {
        	Path path = pathIt.next();
        	Hibernate.initialize(path);
        	Hibernate.initialize(path.getLayer2Data());
        	Hibernate.initialize(path.getLayer3Data());
        	Hibernate.initialize(path.getMplsData());
        	Hibernate.initialize(path.getNextDomain());
        	for (PathElem pe : path.getPathElems()) {
        		Hibernate.initialize(pe);
        		Hibernate.initialize(pe.getLink());
        		Hibernate.initialize(pe.getLink().getL2SwitchingCapabilityData());
        		Hibernate.initialize(pe.getLink().getPort());
        		Hibernate.initialize(pe.getLink().getPort().getNode());
        		Hibernate.initialize(pe.getLink().getPort().getNode().getNodeAddress());
        		Hibernate.initialize(pe.getLink().getPort().getNode().getDomain());
        		Hibernate.initialize(pe.getLink().getPort().getNode().getDomain().getSite());
        	}
        }
    }

    public void contentSection(Map outputMap, Reservation resv, String userName,
                   boolean internalIntradomainHops) throws BSSException {
	
	    InetAddress inetAddress = null;
	    String hostName = null;
	    Long longParam = null;
	    Integer intParam = null;
	    String strParam = null;
	
	    // this will replace LSP name if one was given instead of a GRI
	    String gri = resv.getGlobalReservationId();
    	Path path = resv.getPath(PathType.LOCAL);
	    Layer2Data layer2Data = path.getLayer2Data();
	    Layer3Data layer3Data = path.getLayer3Data();
	    MPLSData mplsData = path.getMplsData();
	    String status = resv.getStatus();
	    // always blank NEW GRI field, current GRI is in griReplace's
	    // innerHTML
	    outputMap.put("newGri", "");
	    outputMap.put("griReplace", gri);
	    outputMap.put("statusReplace", status);
	    outputMap.put("userReplace", resv.getLogin());
	    String sanitized = resv.getDescription().replace("<", "");
	    String sanitized2 = sanitized.replace(">", "");
	    outputMap.put("descriptionReplace", sanitized2);
	
	    outputMap.put("modifyStartSeconds", resv.getStartTime());
	    outputMap.put("modifyEndSeconds", resv.getEndTime());
	    outputMap.put("createdTimeConvert", resv.getCreatedTime());
	    // convert to Mbps, commas added by Dojo
	    outputMap.put("bandwidthReplace", resv.getBandwidth()/1000000);
	    if (layer2Data != null) {
	        outputMap.put("sourceReplace", layer2Data.getSrcEndpoint());
	        outputMap.put("destinationReplace", layer2Data.getDestEndpoint());
	        String vlanTag = BssUtils.getVlanTag(path);
	        if (vlanTag != null) {
	            int storedVlan = Integer.parseInt(vlanTag);
	            int vlanNum = Math.abs(storedVlan);
	            outputMap.put("vlanReplace", vlanNum + "");
	            if (storedVlan >= 0) {
	                outputMap.put("taggedReplace", "true");
	            } else {
	                outputMap.put("taggedReplace", "false");
	            }
	        } else {
	            if (status.equals("SUBMITTED") || status.equals("ACCEPTED")) {
	                outputMap.put("vlanReplace", "VLAN setup in progress");
	            } else {
	                outputMap.put("vlanReplace",
	                              "No VLAN tag was ever set up");
	            }
	        }
	    } else if (layer3Data != null) {
	        strParam = layer3Data.getSrcHost();
	        try {
	            inetAddress = InetAddress.getByName(strParam);
	            hostName = inetAddress.getHostName();
	        } catch (UnknownHostException e) {
	            hostName = strParam;
	        }
	        outputMap.put("sourceReplace", hostName);
	        strParam = layer3Data.getDestHost();
	        try {
	            inetAddress = InetAddress.getByName(strParam);
	            hostName = inetAddress.getHostName();
	        } catch (UnknownHostException e) {
	            hostName = strParam;
	        }
	        outputMap.put("destinationReplace", hostName);
	        intParam = layer3Data.getSrcIpPort();
	        if ((intParam != null) && (intParam != 0)) {
	            outputMap.put("sourcePortReplace", intParam);
	        }
	        intParam = layer3Data.getDestIpPort();
	        if ((intParam != null) && (intParam != 0)) {
	            outputMap.put("destinationPortReplace", intParam);
	        }
	        strParam = layer3Data.getProtocol();
	        if (strParam != null) {
	            outputMap.put("protocolReplace", strParam);
	        }
	        strParam = layer3Data.getDscp();
	        if (strParam !=  null) {
	            outputMap.put("dscpReplace", strParam);
	        }
	    }
	    if (mplsData != null) {
	        longParam = mplsData.getBurstLimit();
	        if (longParam != null) {
	            outputMap.put("burstLimitReplace", longParam);
	        }
	        if (mplsData.getLspClass() != null) {
	            outputMap.put("lspClassReplace", mplsData.getLspClass());
	        }
	    }
	    String pathStr = BssUtils.pathToString(path, false);
	    // in this case, path has not been set up yet, or an error has occurred
	    // and the path will never be set up
	    if ((pathStr != null) && pathStr.equals("")) {
	        return;
	    }
	    // don't allow non-authorized user to see internal hops
	    if ((pathStr != null) && !internalIntradomainHops) {
	        String[] hops = pathStr.trim().split("\n");
	        pathStr = hops[0] + "\n";
	        pathStr += hops[hops.length-1];
	    }
	    if (pathStr != null) {
	        StringBuilder sb = new StringBuilder();
	        // Utils.pathToString has new line separated hops
	        String[] hops = pathStr.trim().split("\n");
	        sb.append("<tbody>");
	        // enforce one hop per line in outer table cell
	        for (int i=0; i < hops.length; i++) {
	            sb.append("<tr><td class='innerHops'>" + hops[i] + "</td></tr>");
	        }
	        sb.append("</tbody>");
	        outputMap.put("pathReplace", sb.toString());
	    }
	    String interPathStr = BssUtils.pathToString(path, true);
	    if ((interPathStr != null) &&
	            !interPathStr.trim().equals("")) {
	        StringBuilder sb = new StringBuilder();
	        String[] hops = interPathStr.trim().split("\n");
	        // enforce one hop per line in outer table cell
	        sb.append("<tbody>");
	        for (int i=0; i < hops.length; i++) {
	            sb.append("<tr><td class='innerHops'>" + hops[i] +
	                      "</td></tr>");
	        }
	        sb.append("</tbody>");
	        outputMap.put("interPathReplace", sb.toString());
	    }
	}

}
