package net.es.oscars.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.log4j.*;
import org.hibernate.*;

import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;

import net.es.oscars.aaa.UserManager;
import net.es.oscars.aaa.UserManager.AuthValue;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.ReservationManager;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.interdomain.*;
import net.es.oscars.oscars.TypeConverter;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.notify.*;


public class CreateReservation extends HttpServlet {

    private NotifyInitializer notifier;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        Logger log = Logger.getLogger(this.getClass());
        log.info("CreateReservation.start");

        this.notifier = new NotifyInitializer();
        try {
            this.notifier.init();
        } catch (NotifyException ex) {
            log.error("*** COULD NOT INITIALIZE NOTIFIER ***");
            // TODO:  ReservationAdapter, ReservationManager, etc. will
            // have init methods that throw exceptions that will not be
            // ignored it NotifyInitializer cannot be created.  Don't
            // want exceptions in constructor
        }
        TypeConverter tc = new TypeConverter();
        Forwarder forwarder = new Forwarder();
        ReservationManager rm = new ReservationManager("bss");
        UserSession userSession = new UserSession();
        Utils utils = new Utils();
        PrintWriter out = response.getWriter();
        ReservationDetails detailsOutput = new ReservationDetails();

        response.setContentType("text/xml");

        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }

        Reservation resv = this.toReservation(userName, request);
        PathInfo pathInfo = null;
        try {
            pathInfo = this.handlePath(request);
        } catch (BSSException e) {
            utils.handleFailure(out, e.getMessage(), null, null);
            return;
        }

        Session aaa = HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();

        UserManager userMgr = new UserManager("aaa");

        // Check to see if user can create this  reservation
        // bandwidth limits are stored in megaBits
        int reqBandwidth = (int) (resv.getBandwidth() / 1000000);

        // convert from milli-seconds to minutes
        int reqDuration = (int) (resv.getEndTime() - resv.getStartTime()) / 6000;
        boolean specifyPath = false;

        if (request.getParameter("explicitPath") != null) {
            specifyPath = true;
        }

        AuthValue authVal = userMgr.checkModResAccess(userName, "Reservations",
                "create", reqBandwidth, reqDuration, specifyPath, false);

        if (authVal == AuthValue.DENIED) {
            utils.handleFailure(out, "createReservation permission denied",
                aaa, null);

            return;
        }

        aaa.getTransaction().commit();

        Session bss = HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();

        try {
            // url returned, if not null, indicates location of next domain
            // manager
            rm.create(resv, userName, pathInfo);
            // checks whether next domain should be contacted, forwards to
            // the next domain if necessary, and handles the response
            CreateReply forwardReply = forwarder.create(resv, pathInfo);
            rm.finalizeResv(forwardReply, resv, pathInfo);
            log.info("to store");
            rm.store(resv);
            log.info("past store");
            Map<String,String> messageInfo = new HashMap<String,String>();
            messageInfo.put("subject",
                "Reservation " + resv.getGlobalReservationId() +
                " scheduling through browser succeeded");
            messageInfo.put("body",
                "Reservation scheduling through browser succeeded.\n" +
                 resv.toString("bss") + "\n");
            messageInfo.put("alertLine", resv.getDescription());
            NotifierSource observable = this.notifier.getSource();
            Object obj = (Object) messageInfo;
            observable.eventOccured(obj);
        } catch (BSSException e) {
            this.sendFailureNotification(resv, e.getMessage());
            utils.handleFailure(out, e.getMessage(), null, bss);
            return;
        } catch (Exception e) {
            // use this so we can find NullExceptions
            this.sendFailureNotification(resv, e.getMessage());
            utils.handleFailure(out, e.toString(), null, bss);
            return;
        }

        log.info("to page creation");
        out.println("<xml>");
        out.println("<status>Created reservation with GRI " +
            resv.getGlobalReservationId() + "</status>");
        utils.tabSection(out, request, response, "ListReservations");
        detailsOutput.contentSection(out, resv, userName);
        out.println("</xml>");
        bss.getTransaction().commit();
        log.info("CreateReservation.end");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        this.doGet(request, response);
    }

    public Reservation toReservation(String userName, HttpServletRequest request) {
        String strParam = null;
        Long millis = null;
        Long bandwidth = null;

        Reservation resv = new Reservation();
        resv.setLogin(userName);

        // necessary type conversions performed here; validation done in
        // ReservationManager
        strParam = request.getParameter("startTime");
        millis = (strParam != null) ? Long.parseLong(strParam) : null;
        resv.setStartTime(millis);
        strParam = request.getParameter("endTime");
        millis = (strParam != null) ? Long.parseLong(strParam) : null;
        resv.setEndTime(millis);

        strParam = request.getParameter("bandwidth");
        bandwidth = (strParam != null)
            ? (Long.valueOf(strParam.trim()) * 1000000L) : null;
        resv.setBandwidth(bandwidth);
        String description = request.getParameter("description");
        // only one checkbox
        String productionStatus = request.getParameter("production");
        // if checkbox was checked, indicate that is production circuit
        Logger log = Logger.getLogger(this.getClass());
        if ((productionStatus != null) &&
                !productionStatus.trim().equals("")) {
            log.info("**** productionStatus " + productionStatus);
            description = "[PRODUCTION CIRCUIT] " + description;
        } else {
            log.info("**** productionStatus is blank");
        }
        resv.setDescription(description);

        return resv;
    }

    /**
     * Takes form parameters and builds PathInfo structures.
     *
     * @param request contains form request parameters
     * @return pathInfo a PathInfo instance with layer 3 information
     */
    public PathInfo handlePath(HttpServletRequest request) 
            throws BSSException {

        CtrlPlanePathContent path = null;
        Logger log = Logger.getLogger(this.getClass());

        PathInfo pathInfo = new PathInfo();
        String explicitPath = request.getParameter("explicitPath");

        if ((explicitPath != null) && !explicitPath.trim().equals("")) {
            log.info("explicit path: " + explicitPath);
            path = new CtrlPlanePathContent();
            path.setId("userPath"); //id doesn't matter in this context

            String[] hops = explicitPath.split("\\s+");

            for (int i = 0; i < hops.length; i++) {
                hops[i] = hops[i].trim();

                if (hops[i].equals(" ") || hops[i].equals("")) {
                    continue;
                }

                CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
                // these can currently be either topology identifiers
                // or IP addresses
                hop.setId(i + "");
                hop.setLinkIdRef(hops[i]);
                log.info("explicit path hop: " + hops[i]);
                path.addHop(hop);
            }

            pathInfo.setPath(path);
        }

        String vlanTag = request.getParameter("vlanTag");
        String tagSrcPort = request.getParameter("tagSrcPort");
        String tagDestPort = request.getParameter("tagDestPort");
        
        //Set default to tagged if tagSrcPort and tagDestPort unspecified
        if(tagSrcPort == null){
            tagSrcPort = "1";
        }
        if(tagDestPort == null){
            tagDestPort = "1";
        }
        
        // TODO:  layer 2 parameters trump layer 3 parameters for now, until
        // handle in Javascript
        if (vlanTag != null) {
            Layer2Info layer2Info = new Layer2Info();
            VlanTag srcVtagObject = new VlanTag();
            VlanTag destVtagObject = new VlanTag();
            srcVtagObject.setString(vlanTag);
            destVtagObject.setString(vlanTag);
            srcVtagObject.setTagged(tagSrcPort.equals("1"));
            destVtagObject.setTagged(tagDestPort.equals("1"));
            layer2Info.setSrcEndpoint(request.getParameter("source"));
            layer2Info.setDestEndpoint(request.getParameter("destination"));
            layer2Info.setSrcVtag(srcVtagObject);
            layer2Info.setDestVtag(destVtagObject);
            pathInfo.setLayer2Info(layer2Info);

            return pathInfo;
        }

        Layer3Info layer3Info = new Layer3Info();
        String strParam = request.getParameter("source");
        // VLAN id wasn't supplied with layer 2 id
        if (strParam.startsWith("urn:ogf:network")) {
            throw new BSSException(
                    "VLAN tag not supplied for layer 2 reservation");
        }
        layer3Info.setSrcHost(strParam);
        layer3Info.setDestHost(request.getParameter("destination"));

        strParam = request.getParameter("srcPort");

        if (strParam != null) {
            layer3Info.setSrcIpPort(Integer.valueOf(strParam));
        } else {
            layer3Info.setSrcIpPort(0);
        }

        strParam = request.getParameter("destPort");

        if (strParam != null) {
            layer3Info.setDestIpPort(Integer.valueOf(strParam));
        } else {
            layer3Info.setDestIpPort(0);
        }

        layer3Info.setProtocol(request.getParameter("protocol"));
        layer3Info.setDscp(request.getParameter("dscp"));
        pathInfo.setLayer3Info(layer3Info);

        MplsInfo mplsInfo = new MplsInfo();
        mplsInfo.setBurstLimit(10000000);
        pathInfo.setMplsInfo(mplsInfo);

        return pathInfo;
    }

    private void sendFailureNotification(Reservation resv, String errMsg) {

        // ugly, but notifies in all cases.  Have to be careful if creation
        // did not get too far.
        Map<String,String> messageInfo = new HashMap<String,String>();
        if (resv == null) {
            messageInfo.put("subject",
                "Reservation scheduling entirely failed");
            messageInfo.put("body",
                "Reservation scheduling through browser entirely failed");
        } else if (resv.getGlobalReservationId() != null) {
            messageInfo.put("subject", 
                "Reservation " + resv.getGlobalReservationId() + " failed");
            messageInfo.put("body",
                    "Scheduling reservation through browser failed with" +
                            errMsg + "\n" + resv.toString("bss"));
            messageInfo.put("alertLine", resv.getDescription());
        }
        NotifierSource observable = this.notifier.getSource();
        Object obj = (Object) messageInfo;
        observable.eventOccured(obj);
    }
}
