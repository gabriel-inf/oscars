package net.es.oscars.rmi.bss;

/**
 * rmi handler for createReservation. Interfaces to ReservationManager.createReservation
 *
 * @author Evangelos Chaniotakis, David Robertson
 */

import java.io.*;
import java.util.*;
import org.apache.log4j.*;
import org.hibernate.*;
import net.es.oscars.aaa.*;
import net.es.oscars.aaa.UserManager.*;
import net.es.oscars.bss.*;
import net.es.oscars.notify.*;
import net.es.oscars.PropHandler;
import net.es.oscars.database.*;
import net.es.oscars.interdomain.*;
import net.es.oscars.oscars.*;
import net.es.oscars.wsdlTypes.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;

public class CreateResRmiHandler {
    private OSCARSCore core;
    private Logger log;


    public CreateResRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    /**
     * CreateReservation rmi handler; interfaces between servlet and ReservationManager.
     *
     * @param userName String - name of user  making request
     * @param params HashMap - contains start and end times, bandwidth, description,
     *          productionType, pathinfo
     * @return HashMap - contains gri and sucess or error status
     */
    public HashMap<String, Object> createReservation(HashMap<String, Object> params, String userName)
        throws IOException {
        this.log.debug("create.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        String methodName = "CreateReservation";

        ReservationManager rm = core.getReservationManager();
        EventProducer eventProducer = new EventProducer();

        Reservation resv = this.toReservation(userName, params);
        PathInfo pathInfo = null;
        try {
            pathInfo = this.handlePath(params);
        } catch (BSSException e) {
            result.put("error", methodName + ": " + e.getMessage());
            this.log.debug("create reservaton failed: " + e.getMessage());
            return result;
        }

        Session aaa = core.getAaaSession();
        aaa.beginTransaction();
        UserManager userMgr = core.getUserManager();

        // Check to see if user can create this  reservation
        // bandwidth limits are stored in megaBits
        int reqBandwidth = (int) (resv.getBandwidth() / 1000000);

        // convert from seconds to minutes
        int reqDuration = (int) (resv.getEndTime() - resv.getStartTime()) / 60;

        Boolean specifyPath = (Boolean) params.get("explicitPath");
        
        
        AuthValue authVal = userMgr.checkModResAccess(userName, "Reservations",
                "create", reqBandwidth, reqDuration, specifyPath, false);

        if (authVal == AuthValue.DENIED) {
            result.put("error", "createReservation permission denied");
            this.log.debug("createReservation failed permission denied");
            return result;
        }
        aaa.getTransaction().commit();

        Session bss = core.getBssSession();
        bss.beginTransaction();
        String errMessage = null;
        try {
            // url returned, if not null, indicates location of next domain
            // manager
            eventProducer.addEvent(OSCARSEvent.RESV_CREATE_RECEIVED, userName, "RMI", resv);
            rm.submitCreate(resv, userName, pathInfo);
            eventProducer.addEvent(OSCARSEvent.RESV_CREATE_ACCEPTED, userName, "RMI", resv);
        } catch (BSSException e) {
            errMessage = e.getMessage();
        } catch (Exception e) {
            // use this so we can find NullExceptions
            errMessage = e.getMessage();
        } finally {
            if (errMessage != null) {
                eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FAILED, userName, "RMI", resv, "", errMessage);
                result.put("error", errMessage);
                this.log.debug("createReservation failed: " + errMessage);
                return result;
            }
        }
        result.put("gri", resv.getGlobalReservationId());
        result.put("status", "Submitted reservation with GRI " + resv.getGlobalReservationId());
        result.put("method", methodName);
        result.put("success", Boolean.TRUE);

        bss.getTransaction().commit();
        this.log.debug("create.end - success");
        return result;
    }

    private Reservation toReservation(String userName, HashMap<String, Object> params) {
        String[] arrayParam = null;
        String strParam = null;
        Long bandwidth = null;
        Long seconds = 0L;

        Reservation resv = new Reservation();
        resv.setLogin(userName);

        // necessary type conversions performed here; validation done in
        // ReservationManager
        seconds = (Long) params.get("startSeconds");
        resv.setStartTime(seconds);
        
        seconds = (Long) params.get("endSeconds");
        resv.setStartTime(seconds);
        
        bandwidth = (Long) params.get("bandwidth");
        resv.setBandwidth(bandwidth);

        String description = (String) params.get("description");
        
        Boolean isProduction =  (Boolean) params.get("productionType");
        if (isProduction) {
            this.log.info("production circuit");
            description = "[PRODUCTION CIRCUIT] " + description;
        } else {
            this.log.info("non-production circuit");
        }
        resv.setDescription(description);
        return resv;
    }

    /**
     * Takes form parameters and builds PathInfo structures.
     *
     * @param params contains form request parameters
     * @return pathInfo a PathInfo instance with layer 2 or 3 information
     */
    public PathInfo handlePath(HashMap<String, Object> params)
            throws BSSException {

        CtrlPlanePathContent path = null;
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("wbui", true);
        String defaultLayer = props.getProperty("defaultLayer");
        
        String source = (String) params.get("source");
        String destination = (String) params.get("destination");

        
        PathInfo pathInfo = new PathInfo();
        String explicitPath = (String) params.get("explicitPath");
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
        } else {
            // Add a path just composed of source and destination
//            pathInfo.setPathType("loose");
            path = new CtrlPlanePathContent();
            path.setId("userPath"); //id doesn't matter in this context
            String[] hops = new String[2];
            hops[0] = source;
            hops[1] = destination;
            for (int i = 0; i < hops.length; i++) {
                hops[i] = hops[i].trim();
                CtrlPlaneHopContent hop = new CtrlPlaneHopContent();
                // these can currently be either topology identifiers
                // or IP addresses
                hop.setId(i + "");
                hop.setLinkIdRef(hops[i]);
                this.log.info("implicit path hop: " + hops[i]);
                path.addHop(hop);
            }
            pathInfo.setPath(path);
        }

        String vlanTag = (String) params.get("vlanTag");

        String tagSrcPort = (String) params.get("tagSrcPort");

        String tagDestPort = (String) params.get("tagDestPort");
        

        //Set default to tagged if tagSrcPort and tagDestPort unspecified
        if ((tagSrcPort == null) || tagSrcPort.trim().equals("")) {
            tagSrcPort = "Tagged";
        }
        if ((tagDestPort == null) || tagDestPort.trim().equals("")) {
            tagDestPort = "Tagged";
        }

        // TODO:  layer 2 parameters trump layer 3 parameters for now, until
        // handle in Javascript
        if (((vlanTag != null) && !vlanTag.trim().equals("")) ||
              (defaultLayer !=  null && defaultLayer.equals("2"))) {
            Layer2Info layer2Info = new Layer2Info();
            VlanTag srcVtagObject = new VlanTag();
            VlanTag destVtagObject = new VlanTag();
            vlanTag = (vlanTag == null||vlanTag.equals("") ? "any" : vlanTag);
            srcVtagObject.setString(vlanTag);
            destVtagObject.setString(vlanTag);
            boolean tagged = tagSrcPort.equals("Tagged");
            srcVtagObject.setTagged(tagged);
            tagged = tagDestPort.equals("Tagged");
            destVtagObject.setTagged(tagged);
            layer2Info.setSrcEndpoint(source);
            layer2Info.setDestEndpoint(destination);
            layer2Info.setSrcVtag(srcVtagObject);
            layer2Info.setDestVtag(destVtagObject);
            pathInfo.setLayer2Info(layer2Info);
            return pathInfo;
        }

        Layer3Info layer3Info = new Layer3Info();
        // VLAN id wasn't supplied with layer 2 id
        if (source.startsWith("urn:ogf:network")) {
            throw new BSSException("VLAN tag not supplied for layer 2 reservation");
        }
        layer3Info.setSrcHost(source);
        layer3Info.setDestHost(destination);

        String srcPort = (String) params.get("srcPort");
        layer3Info.setSrcIpPort(Integer.valueOf(srcPort));

        String destPort = (String) params.get("destPort");
        layer3Info.setDestIpPort(Integer.valueOf(destPort));
        
        String protocol = (String) params.get("protocol");
        layer3Info.setProtocol(protocol);
        String dscp =  (String) params.get("dscp");
        layer3Info.setDscp(dscp);
        pathInfo.setLayer3Info(layer3Info);

        MplsInfo mplsInfo = new MplsInfo();
        mplsInfo.setBurstLimit(10000000);
        pathInfo.setMplsInfo(mplsInfo);

        return pathInfo;
    }
}
