package net.es.oscars.servlets;

/**
 * CreateReservation servlet
 *
 * @author David Robertson, Evangelos Chaniotakis
 *
 */
import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.log4j.*;
import net.sf.json.JSONObject;

import net.es.oscars.PropHandler;
import net.es.oscars.bss.Reservation;
import net.es.oscars.bss.BSSException;
import net.es.oscars.bss.topology.*;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.bss.BssRmiInterface;

public class CreateReservation extends HttpServlet {
    private Logger log = Logger.getLogger(CreateReservation.class);

    /**
     * doGet
     *
     * @param request HttpServlerRequest - contains start and end times, bandwidth, description,
     *          productionType, pathinfo
     * @param response HttpServler Response - contain gri and success or error status
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {

        String methodName= "CreateReservation";
        this.log.info("CreateReservation.start");

        PrintWriter out = response.getWriter();
        UserSession userSession = new UserSession();
        String userName = userSession.checkSession(out, request, methodName);
        if (userName == null) {
            this.log.error("No user session: cookies invalid");
            return;
        }
        response.setContentType("application/json");
        HashMap<String, Object> outputMap = new HashMap<String, Object>();

        Reservation resv = null;
        Path requestedPath = null;
        try {
            resv = this.toReservation(userName, request);
            requestedPath = this.handlePath(request);
            resv.setPath(requestedPath);
        } catch (BSSException e) {
            ServletUtils.handleFailure(out, null, e, methodName);
            return;
        }
        String gri = null;
        try {
            BssRmiInterface rmiClient =
                RmiUtils.getBssRmiClient(methodName, log);
            gri = rmiClient.createReservation(resv, userName);
        } catch (Exception ex) {
            ServletUtils.handleFailure(out, null, ex, methodName);
            return;
        }
        outputMap.put("gri", gri);
        outputMap.put("status", "Submitted reservation with GRI " + gri);
        outputMap.put("method", methodName);
        outputMap.put("success", Boolean.TRUE);
        JSONObject jsonObject = JSONObject.fromObject(outputMap);
        out.println("{}&&" + jsonObject);
        this.log.info("CreateReservation.end");
        return;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        this.doGet(request, response);
    }

    private Reservation
        toReservation(String userName, HttpServletRequest request)
           throws BSSException {

        String strParam = null;
        Long bandwidth = null;
        Long seconds = 0L;

        Reservation resv = new Reservation();
        resv.setLogin(userName);

        // necessary type conversions performed here; validation done in
        // ReservationManager
        strParam = request.getParameter("startSeconds");
        if (strParam != null && !strParam.trim().equals("")) {
            seconds = Long.parseLong(strParam.trim());
        } else {
            throw new BSSException("error: start time is a required parameter");
        }
        resv.setStartTime(seconds);

        strParam = request.getParameter("endSeconds");
        if (strParam != null && !strParam.trim().equals("")) {
            seconds = Long.parseLong(strParam.trim());
        } else {
            throw new BSSException("error: end time is a required parameter");
        }
        resv.setEndTime(seconds);

        strParam = request.getParameter("bandwidth");
        if (strParam != null && !strParam.trim().equals("")) {
            bandwidth = Long.valueOf(strParam.trim()) * 1000000L;
        } else {
            throw new BSSException("error: bandwidth is a required parameter.");
        }
        resv.setBandwidth(bandwidth);

        String description = "";
        strParam = request.getParameter("description");
        if (strParam != null && !strParam.trim().equals("")) {
            description = strParam.trim();
        }

        strParam = request.getParameter("productionType");
        // if not blank, check box indicating production circuit was checked
        if (strParam != null && !strParam.trim().equals("")) {
            this.log.info("production circuit");
            description = "[PRODUCTION CIRCUIT] " + strParam.trim();
        } else {
            this.log.info("non-production circuit");
        }
        resv.setDescription(description);
        return resv;
    }

    /**
     * Takes form parameters and builds Path structures.
     *
     * @param request HttpServletRequest
     * @return requestedPath a Path instance with layer 2 or 3 information
     */
    private Path handlePath(HttpServletRequest request)
            throws BSSException {

        String strParam = null;

        List<PathElem> pathElems = new ArrayList<PathElem>();
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("wbui", true);
        String defaultLayer = props.getProperty("defaultLayer");

        Path requestedPath = new Path();
        requestedPath.setPathType(PathType.REQUESTED);
        String explicitPath = "";
        String source = null;
        String destination = null;
        strParam = request.getParameter("source");
        if ((strParam != null) && !strParam.trim().equals("")) {
            source = strParam;
        } else {
            throw new BSSException("error:  source is a required parameter");
        }
        strParam = request.getParameter("destination");
        if ((strParam != null) && !strParam.trim().equals("")) {
            destination = strParam;
        } else {
            throw new BSSException("error:  destination is a required parameter");
        }
        strParam = request.getParameter("explicitPath");
        if (strParam != null && !strParam.trim().equals("")) {
            explicitPath = strParam.trim();
            this.log.info("explicit path: " + explicitPath);

            String[] hops = explicitPath.split("\\s+");
            for (int i = 0; i < hops.length; i++) {
                hops[i] = hops[i].trim();
                if (hops[i].equals(" ") || hops[i].equals("")) {
                    continue;
                }
                PathElem pathElem = new PathElem();
                // these can currently be either topology identifiers
                // or IP addresses
                pathElem.setUrn(hops[i]);
                this.log.info("explicit path hop: " + hops[i]);
                pathElems.add(pathElem);
            }
            requestedPath.setPathElems(pathElems);
        } else {
            // empty; will get filled in by ReservationManager
            requestedPath.setPathElems(pathElems);
        }
        String vlanTag = "";
        strParam = request.getParameter("vlanTag");
        if (strParam != null && !strParam.trim().equals("")) {
            vlanTag = strParam.trim();
        }
        // src and dest default to tagged
        String tagSrcPort = "Tagged";
        strParam = request.getParameter("tagSrcPort");
        if (strParam != null && !strParam.trim().equals("")) {
            tagSrcPort = strParam.trim();
        }
        String tagDestPort = "Tagged";
        strParam = request.getParameter("tagDestPort");
        if (strParam != null && !strParam.trim().equals("")) {
            tagDestPort = strParam.trim();
        }


        // TODO: support VLAN tagging

        // TODO:  layer 2 parameters trump layer 3 parameters for now, until
        // handle in Javascript
        if (!vlanTag.equals("") ||
              (defaultLayer !=  null && defaultLayer.equals("2"))) {
            Layer2Data layer2Data = new Layer2Data();
            vlanTag = (vlanTag == null||vlanTag.equals("") ? "any" : vlanTag);
            boolean tagged = tagSrcPort.equals("Tagged");
            if (!tagged) {
                vlanTag = "0";
            }
            layer2Data.setSrcVtag(vlanTag);
            tagged = tagDestPort.equals("Tagged");
            if (!tagged) {
                vlanTag = "0";
            }
            layer2Data.setDestVtag(vlanTag);

            layer2Data.setSrcEndpoint(source);
            layer2Data.setDestEndpoint(destination);
            requestedPath.setLayer2Data(layer2Data);
            return requestedPath;
        }

        Layer3Data layer3Data = new Layer3Data();
        // VLAN id wasn't supplied with layer 2 id
        if (source.startsWith("urn:ogf:network")) {
            throw new BSSException("VLAN tag not supplied for layer 2 reservation");
        }
        layer3Data.setSrcHost(source);
        layer3Data.setDestHost(destination);

        strParam = request.getParameter("srcPort");
        if ((strParam != null) && !strParam.trim().equals("")) {
            layer3Data.setSrcIpPort(Integer.valueOf(strParam.trim()));
        } else {
            layer3Data.setSrcIpPort(0);
        }
        strParam = request.getParameter("destPort");
        if ((strParam != null) && !strParam.trim().equals("")) {
            layer3Data.setDestIpPort(Integer.valueOf(strParam.trim()));
        } else {
            layer3Data.setDestIpPort(0);
        }
        strParam = request.getParameter("protocol");
        if ((strParam != null) && !strParam.trim().equals("")) {
            layer3Data.setProtocol(strParam.trim());
        }
        strParam = request.getParameter("dscp");
        if ((strParam != null) && !strParam.trim().equals("")) {
            layer3Data.setDscp(strParam.trim());
        }
        requestedPath.setLayer3Data(layer3Data);

        MPLSData mplsData = new MPLSData();
        mplsData.setBurstLimit(10000000L);
        requestedPath.setMplsData(mplsData);

        return requestedPath;
    }
}
