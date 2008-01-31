package net.es.oscars.dojoServlets;

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
import net.es.oscars.Notifier;


public class CreateReservation extends HttpServlet {
    private Logger log;

    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        this.log = Logger.getLogger(this.getClass());
        this.log.info("CreateReservation.start");

        Notifier notifier = new Notifier();
        TypeConverter tc = new TypeConverter();
        Forwarder forwarder = new Forwarder();
        ReservationManager rm = new ReservationManager("bss");
        UserSession userSession = new UserSession();
        Utils utils = new Utils();
        PrintWriter out = response.getWriter();

        response.setContentType("text/json-comment-filtered");

        String userName = userSession.checkSession(out, request);
        if (userName == null) { return; }

        Reservation resv = this.toReservation(userName, request);
        PathInfo pathInfo = this.handlePath(request);

        Session aaa = HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();

        UserManager userMgr = new UserManager("aaa");

        // Check to see if user can create this  reservation
        // bandwidth limits are stored in megaBits
        int reqBandwidth = (int) (resv.getBandwidth() / 1000000);

        // convert from milli-seconds to minutes
        int reqDuration = (int) (resv.getEndTime() - resv.getStartTime()) / 6000;
        boolean specifyPath = false;

        String strParam = request.getParameter("explicitPath");
        if ((strParam != null) && (!strParam.trim().equals(""))) {
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
            rm.store(resv);
            String subject = "Reservation " + resv.getGlobalReservationId() +
                             " scheduling through browser succeeded";
            String notification = "Reservation scheduling through browser succeeded.\n" +
                                  resv.toString("bss") + "\n";
            try {
                notifier.sendMessage(subject, notification);
            } catch (javax.mail.MessagingException ex) {
                ;  // swallow for now
            } catch (UnsupportedOperationException ex) {
                ;
            }
        } catch (BSSException e) {
            this.sendFailureNotification(notifier, resv, e.getMessage());
            utils.handleFailure(out, e.getMessage(), null, bss);
            return;
        } catch (Exception e) {
            // use this so we can find NullExceptions
            this.sendFailureNotification(notifier, resv, e.getMessage());
            utils.handleFailure(out, e.toString(), null, bss);
            return;
        }

        Map outputMap = new HashMap();
        outputMap.put("status", "Created reservation with GRI " +
            resv.getGlobalReservationId());
        outputMap.put("method", "CreateReservation");
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("/* " + jsonObject + " */");
        bss.getTransaction().commit();
        this.log.info("CreateReservation.end");
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        this.doGet(request, response);
    }


    public Reservation toReservation(String userName, HttpServletRequest request) {
        String strParam = null;
        Long bandwidth = null;
        Long seconds = 0L;

        Reservation resv = new Reservation();
        resv.setLogin(userName);

        // necessary type conversions performed here; validation done in
        // ReservationManager
        strParam = request.getParameter("startSeconds");
        if ((strParam != null) && (!strParam.equals(""))) {
            seconds = Long.parseLong(strParam);
        }
        resv.setStartTime(seconds);
        strParam = request.getParameter("endSeconds");
        if ((strParam != null) && (!strParam.equals(""))) {
            seconds = Long.parseLong(strParam);
        }
        resv.setEndTime(seconds);

        strParam = request.getParameter("bandwidth");
        bandwidth = (strParam != null)
            ? (Long.valueOf(strParam.trim()) * 1000000L) : 0L;
        resv.setBandwidth(bandwidth);
        strParam = request.getParameter("description");
        resv.setDescription(request.getParameter("description"));
        return resv;
    }

    /**
     * Takes form parameters and builds PathInfo structures.
     *
     * @param request contains form request parameters
     * @return pathInfo a PathInfo instance with layer 3 information
     */
    public PathInfo handlePath(HttpServletRequest request) {
        CtrlPlanePathContent path = null;

        PathInfo pathInfo = new PathInfo();
        String explicitPath = request.getParameter("explicitPath");

        if ((explicitPath != null) && !explicitPath.trim().equals("")) {
            this.log.info("explicit path: " + explicitPath);
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
                this.log.info("explicit path hop: " + hops[i]);
                path.addHop(hop);
            }

            pathInfo.setPath(path);
        }

        String vlanTag = request.getParameter("vlanTag");
        String tagSrcPort = request.getParameter("tagSrcPort");
        this.log.info("tagSrcPort: " + tagSrcPort);
        String tagDestPort = request.getParameter("tagDestPort");
        this.log.info("tagDestPort: " + tagDestPort);
        
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
            boolean tagged = tagSrcPort.equals("Tagged");
            srcVtagObject.setTagged(tagged);
            tagged = tagDestPort.equals("Tagged");
            destVtagObject.setTagged(tagged);
            layer2Info.setSrcEndpoint(request.getParameter("source"));
            layer2Info.setDestEndpoint(request.getParameter("destination"));
            layer2Info.setSrcVtag(srcVtagObject);
            layer2Info.setDestVtag(destVtagObject);
            pathInfo.setLayer2Info(layer2Info);

            return pathInfo;
        }

        Layer3Info layer3Info = new Layer3Info();
        layer3Info.setSrcHost(request.getParameter("source"));
        layer3Info.setDestHost(request.getParameter("destination"));

        String strParam = request.getParameter("srcPort");

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

    private void sendFailureNotification(Notifier notifier, Reservation resv,
                                         String errMsg) {

        String subject = "";
        String notification = "";
        // ugly, but notifies in all cases.  Have to be careful if creation
        // did not get too far.
        if (resv == null) {
            subject += "Reservation scheduling entirely failed";
            notification += "Reservation scheduling through browser entirely failed";
        } else if (resv.getGlobalReservationId() != null) {
            subject += "Reservation " + resv.getGlobalReservationId() + " failed";
            notification = "Scheduling reservation through browser failed with" +
                            errMsg + "\n" + resv.toString("bss") + "\n";
        }
        try {
            notifier.sendMessage(subject, notification);
        } catch (javax.mail.MessagingException ex) {
            ;  // swallow exception for now
        } catch (UnsupportedOperationException ex) {
            ;
        }
    }
}
