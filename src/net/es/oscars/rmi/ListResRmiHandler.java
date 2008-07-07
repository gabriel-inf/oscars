package net.es.oscars.rmi;

/**
 * Interface between rmi listReservatins call and reservationManager.listReservations
 * 
 * @author Mary Thompson, David Robertson
 */

import java.io.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.aaa.*;
import net.es.oscars.aaa.UserManager.*;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.Link;
import net.es.oscars.bss.topology.TopologyUtil;
import net.es.oscars.notify.*;

import net.es.oscars.PropHandler;
import net.es.oscars.database.*;
import net.es.oscars.interdomain.*;
import net.es.oscars.oscars.*;
import net.es.oscars.wsdlTypes.*;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;

public class ListResRmiHandler {
    private OSCARSCore core;
    private Logger log;


    public ListResRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    public HashMap<String, Object> listReservations(HashMap<String, String[]> inputMap, String userName) 
        throws IOException {
        this.log.debug("list.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        String institution = null;
        String loginConstraint = null;
        String methodName = "ListReservations";
        ReservationManager rm = core.getReservationManager();

/* 
        List<String> statuses = this.getStatuses(request);
        List<String> vlans = this.getVlanTags(request);
        String description = this.getDescription(request);
        List<Link> inLinks = this.getLinks(request);
        String startTimeStr = request.getParameter("startTimeSeconds");
        String endTimeStr = request.getParameter("endTimeSeconds");
        if ((startTimeStr != null) && !startTimeStr.equals("")) {
            startTimeSeconds = Long.valueOf(startTimeStr.trim());
        }
        if ((endTimeStr != null) && !endTimeStr.equals("")) {
            endTimeSeconds = Long.valueOf(endTimeStr.trim());
        }
        
        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        UserManager userMgr = core.getUserManager();

        AuthValue authVal = userMgr.checkAccess(userName, "Reservations",
                "list");

        if (authVal == AuthValue.DENIED) {
            result.put("error", "no permission to list Reservations");
            this.log.debug("query failed: no permission to list Reservations");
            return result;
        }
        if (authVal.equals(AuthValue.MYSITE)) {
            institution = userMgr.getInstitution(userName);
        } else if (authVal.equals(AuthValue.SELFONLY)){
            loginConstraint = userName;
        }
        aaa.getTransaction().commit();

        Session bss = core.getBssSession();
        bss.beginTransaction();
        String errMessage = null;
        try {
            reservations =
                rm.list(loginConstraint, institution, statuses, description, inLinks,
                        vlans, startTimeSeconds, endTimeSeconds);
        } catch (BSSException e) {
            errMessage = e.getMessage();
        } catch (Exception e) {
            // use this so we can find NullExceptions
            errMessage = e.getMessage();
        } finally {
            if (errMessage != null) {
                result.put("error",  errMessage);
                bss.getTransaction().rollback();
                this.log.debug("list failed: " + errMessage);
                return result;  
            }

        }


        result.put();
        result.put("status", "list reservations successful ");
        result.put("method", methodName);
        result.put("success", Boolean.TRUE);

        bss.getTransaction().commit();
        this.log.info("ListReservations.end");
        this.log.debug("listReservations.end");
        */
        return result;
    }
/*
    public String getLinkIds(HashMap<String, String[]>request) {

        String linkList = request.get("linkIds");
        if (linkList == null) {
            // space needed for text area
            linkList = " ";
        }
        return linkList;
    }
    */

    /**
     * Gets list of links to search for.  If a reservation's path includes
     * one of these links, it is returned as part of the list.
     *
     * @param request servlet request
     * @
     * @return list of links to send to BSS
     */
/*
    public List<Link> getLinks(HashMap<String, String[]> request) {

       List<Link> inLinks = new ArrayList<Link>();
        String linkList = this.getLinkIds(request).trim();
        if (linkList.equals("")) {
            return inLinks;
        }
        String[] linkIds = linkList.trim().split(" ");
        if (linkIds.length > 0) {
            for (String s : linkIds) {
                if (s != null && !s.trim().equals("")) {
                    Link link = null;
                    try {
                        link = TopologyUtil.getLink(s.trim(), core.getBssDbName());
                        inLinks.add(link);
                    } catch (BSSException ex) {
                        this.log.error("Could not get link for string: [" +
                                   s.trim()+"], error: ["+ex.getMessage()+"]");
                    }
                }
            }
        }
        
        return inLinks;
    }
    */

    /**
     * Gets description search parameter and sets to blank field if empty.
     *
     * @param request servlet request
     * @
     * @return string with description
     */
    public String getDescription(HashMap<String, String[]> request) {

        String description = "";
        String descriptions [] = request.get("resvDescription"); 
        if (descriptions != null) {
            description = descriptions[0];
        }
        return description;
    }

    /**
     * Gets reservation statuses to search for.
     *
     * @param request HashMap passed from servlet 
     * @
     * @return list of statuses to send to BSS
     */
    public List<String> getStatuses(HashMap<String, String[]> request) {

        List<String> statuses = new ArrayList<String>();
        String paramStatuses[] = request.get("statuses");
        if (paramStatuses == null) {
            statuses.add("");
        } else {
            for (int i=0 ; i < paramStatuses.length; i++) {
                statuses.add(paramStatuses[i]);
            }
        }
        return statuses;
    }

    /**
     * Gets VLAN tags and/or ranges of VLAN tags to search for from
     * servlet request.
     *
     * @param request servlet request
     * @
     * @return list of vlans and/or ranges to send to BSS
     */
    public List<String> getVlanTags(HashMap<String, String[]> request) {

        List<String> vlanTags = new ArrayList<String>();
        String vlanParam [] = request.get("vlanSearch");
        if ((vlanParam != null) && !vlanParam[0].equals("")) {
            String[] paramTags = vlanParam[0].trim().split(" ");
            for (int i=0; i < paramTags.length; i++) {
                vlanTags.add(paramTags[i]);
            }
        }
        return vlanTags;
    }

}
