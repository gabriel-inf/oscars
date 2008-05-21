package net.es.oscars.servlets;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.mail.MessagingException;

import org.apache.log4j.*;
import org.hibernate.*;
import net.sf.json.*;

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
import net.es.oscars.PropHandler;


public class ModifyReservation extends HttpServlet {
    private Logger log;
    private NotifyInitializer notifier;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        this.log = Logger.getLogger(this.getClass());
        this.log.info("ModifyReservation.start");

        this.notifier = new NotifyInitializer();
        try {
            this.notifier.init();
        } catch (NotifyException ex) {
            this.log.error("*** COULD NOT INITIALIZE NOTIFIER ***");
            // TODO:  ReservationAdapter, ReservationManager, etc. will
            // have init methods that throw exceptions that will not be
            // ignored it NotifyInitializer cannot be created.  Don't
            // want exceptions in constructor
        }
        TypeConverter tc = new TypeConverter();
        Forwarder forwarder = new Forwarder();
        ModifyResReply forwardReply = null;
        ReservationManager rm = new ReservationManager("bss");
        UserSession userSession = new UserSession();
        Utils utils = new Utils();
        PrintWriter out = response.getWriter();

        response.setContentType("text/json-comment-filtered");

        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }

        Reservation resv = this.toReservation(request);
        boolean allUsers = false;
        Session aaa =
            HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        UserManager userMgr = new UserManager("aaa");
        AuthValue authVal = userMgr.checkAccess(userName, "Reservations",
                "modify");
        switch (authVal) {
            case DENIED:
                this.log.info("denied");
                utils.handleFailure(out,
                        "modifyReservation: permission denied", aaa, null);
                return;
            case SELFONLY: allUsers = false; break;
            case ALLUSERS: allUsers = true; break;
        }
        aaa.getTransaction().commit();

        Session bss = HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        PathInfo pathInfo = null;
        try {
            // For now, get pathInfo from current reservation since it
            // cannot be modified yet but is necessary for the interface.
            // Redundant operation in that it will be converted back unchanged.
            // allUsers is for the modify permission, but no point in this
            // succeeding if will be unable to modify. 
            Reservation tempResv =
                rm.query(resv.getGlobalReservationId(), userName, allUsers); 
            pathInfo = this.handlePath(tempResv);
        } catch (BSSException e) {
            utils.handleFailure(out, e.getMessage(), null, null);
            return;
        }
        String errMessage = null;
        try {
            Reservation persistentResv = rm.modify(resv, userName, pathInfo);
            tc.ensureLocalIds(pathInfo);
            // checks whether next domain should be contacted, forwards to
            // the next domain if necessary, and handles the response
            this.log.debug("modify, to forward");
            InterdomainException interException = null;
            forwardReply = forwarder.modify(resv, pathInfo);
            persistentResv = rm.finalizeModifyResv(forwardReply, resv, pathInfo);
            Map<String,String> messageInfo = new HashMap<String,String>();
            messageInfo.put("subject",
                "Reservation " + persistentResv.getGlobalReservationId() +
                " modification through browser succeeded");
            messageInfo.put("body",
                "Reservation modification through browser succeeded.\n" +
                 persistentResv.toString("bss") + "\n");
            messageInfo.put("alertLine", persistentResv.getDescription());
            NotifierSource observable = this.notifier.getSource();
            Object obj = (Object) messageInfo;
            observable.eventOccured(obj);
        } catch (BSSException e) {
            errMessage = e.getMessage();
        } catch (InterdomainException e) {
            errMessage = e.getMessage();
        } catch (Exception e) {
            // use this so we can find NullExceptions
            errMessage = e.getMessage();
        } finally {
            forwarder.cleanUp();
            if (errMessage != null) {
                this.sendFailureNotification(resv, errMessage);
                utils.handleFailure(out, errMessage, null, bss);
                return;
            }
        }

        Map outputMap = new HashMap();
        outputMap.put("status", "Modified reservation with GRI " +
            resv.getGlobalReservationId());
        outputMap.put("method", "ModifyReservation");
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        bss.getTransaction().commit();
        this.log.info("ModifyReservation.end");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        this.doGet(request, response);
    }


    public Reservation toReservation(HttpServletRequest request) {
        String strParam = null;
        Long bandwidth = null;
        Long seconds = 0L;

        Reservation resv = new Reservation();

        strParam = request.getParameter("gri");
        resv.setGlobalReservationId(strParam);

        // necessary type conversions performed here; validation done in
        // ReservationManager
        strParam = request.getParameter("modifyStartSeconds");
        if ((strParam != null) && (!strParam.equals(""))) {
            seconds = Long.parseLong(strParam);
        }
        resv.setStartTime(seconds);
        strParam = request.getParameter("modifyEndSeconds");
        if ((strParam != null) && (!strParam.equals(""))) {
            seconds = Long.parseLong(strParam);
        }
        resv.setEndTime(seconds);

        // currently hidden form fields; not modifiable 
        strParam = request.getParameter("modifyBandwidth");
        bandwidth = ((strParam != null) && !strParam.trim().equals(""))
            ? (Long.valueOf(strParam.trim()) * 1000000L) : 0L;
        resv.setBandwidth(bandwidth);
        String description = request.getParameter("modifyDescription");
        resv.setDescription(description);
        return resv;
    }

    /**
     * Takes current reservation and builds PathInfo structures.
     * This is only necessary to satisfy the reservation manager's
     * modifyReservation interface.  Eventually it will be modifiable
     * via the Web interface.
     *
     * @param resv a current reservation
     * @return pathInfo a PathInfo instance with layer 2 or 3 information
     */
    public PathInfo handlePath(Reservation resv)
            throws BSSException {

        TypeConverter tc = new TypeConverter();
        PathInfo pathInfo = tc.getPathInfo(resv);
        Layer2Info layer2Info = pathInfo.getLayer2Info();
        // TODO:  hopefully temporary fix
        VlanTag srcVtag = layer2Info.getSrcVtag();
        srcVtag.setTagged(true);
        VlanTag destVtag = layer2Info.getDestVtag();
        destVtag.setTagged(true);
        return pathInfo;
    }

    private void sendFailureNotification(Reservation resv, String errMsg) {

        Map<String,String> messageInfo = new HashMap<String,String>();
        messageInfo.put("subject", 
                "Modifying reservation " + resv.getGlobalReservationId() +
                " through browser failed");
        messageInfo.put("body",
               "Modifying reservation through browser failed with" +
                        errMsg + "\n" + resv.toString("bss"));
        messageInfo.put("alertLine", resv.getDescription());
        NotifierSource observable = this.notifier.getSource();
        Object obj = (Object) messageInfo;
        observable.eventOccured(obj);
    }
}
