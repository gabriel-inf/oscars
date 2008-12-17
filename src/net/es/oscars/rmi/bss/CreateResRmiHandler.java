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
import net.es.oscars.bss.topology.*;
import net.es.oscars.notify.*;
import net.es.oscars.PropHandler;
import net.es.oscars.database.*;
import net.es.oscars.interdomain.*;
import net.es.oscars.oscars.*;
import net.es.oscars.rmi.aaa.*;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.servlets.ServletUtils;

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
    public HashMap<String, Object>
        createReservation(HashMap<String, Object> params, String userName)
            throws IOException {

        this.log.debug("create.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        String methodName = "CreateReservation";

        ReservationManager rm = core.getReservationManager();
        EventProducer eventProducer = new EventProducer();
        Reservation resv = null;
        Path requestedPath = null;

        String caller = (String) params.get("caller");
        if (caller.equals("WBUI")) {
            resv = this.toReservation(userName, params);
            try {
                requestedPath = this.handlePath(params);
            } catch (BSSException e) {
                result.put("error", methodName + ": " + e.getMessage());
                this.log.debug("create reservaton failed: " + e.getMessage());
                return result;
            }
        } else if (caller.equals("AAR")) {
            resv = (Reservation) params.get("reservation");
        } else {
            this.log.error("Invalid caller");
            throw new IOException("Invalid caller!");
        }


        // bandwidth limits are stored in megaBits
        int reqBandwidth = (int) (resv.getBandwidth() / 1000000);
        // convert from seconds to minutes
        int reqDuration = (int) (resv.getEndTime() - resv.getStartTime()) / 60;

        boolean specifyPath = false;
        String[] arrayParam = (String[]) params.get("explicitPath");
        if (arrayParam != null) {
            String strParam = arrayParam[0];
            if ((strParam != null) && (!strParam.trim().equals(""))) {
                specifyPath = true;
            }
        }

        AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        AuthValue authVal = rmiClient.checkModResAccess(userName, "Reservations", "create", reqBandwidth, reqDuration, specifyPath, false);


        if (authVal == AuthValue.DENIED) {
            result.put("error", "createReservation permission denied");
            this.log.debug("createReservation failed permission denied");
            return result;
        }

        // submit reservation request
        Session bss = core.getBssSession();
        bss.beginTransaction();
        String errMessage = null;
        try {
            // url returned, if not null, indicates location of next domain
            // manager
            eventProducer.addEvent(OSCARSEvent.RESV_CREATE_RECEIVED, userName, caller, resv);
            rm.submitCreate(resv, userName, requestedPath);
            eventProducer.addEvent(OSCARSEvent.RESV_CREATE_ACCEPTED, userName, caller, resv);
        } catch (BSSException e) {
            errMessage = e.getMessage();
        } catch (Exception e) {
            // use this so we can find NullExceptions
            errMessage = e.getMessage();
        } finally {
            if (errMessage != null) {
                eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FAILED, userName, caller, resv, "", errMessage);
                result.put("error", errMessage);
                this.log.debug("createReservation failed: " + errMessage);
                return result;
            }
        }
        if (caller.equals("WBUI")) {
            result.put("gri", resv.getGlobalReservationId());
            result.put("status", "Submitted reservation with GRI " + resv.getGlobalReservationId());
            result.put("method", methodName);
            result.put("success", Boolean.TRUE);
        } else if (caller.equals("AAR")) {
            Hibernate.initialize(resv);
            Iterator<Path> pathIt = resv.getPaths().iterator();
            while (pathIt.hasNext()) {
                Path path = pathIt.next();
                Hibernate.initialize(path);
                Hibernate.initialize(path.getLayer2Data());
                Hibernate.initialize(path.getLayer3Data());
                Hibernate.initialize(path.getNextDomain());
                List<PathElem> pelems = path.getPathElems();
                for (PathElem pe : pelems) {
                    Hibernate.initialize(pe);
                    Hibernate.initialize(pe.getLink());
                }
            }
            result.put("reservation", resv);
        }
        bss.getTransaction().commit();
        this.log.debug("create.end - success");
        return result;
    }

    private Reservation toReservation(String userName, HashMap<String, Object> inputMap) {
        String[] arrayParam = null;
        String strParam = null;
        Long bandwidth = null;
        Long seconds = 0L;

        Reservation resv = new Reservation();
        resv.setLogin(userName);

        // necessary type conversions performed here; validation done in
        // ReservationManager
        arrayParam = (String[]) inputMap.get("startSeconds");
        if (arrayParam != null) {
            strParam = arrayParam[0];
            if (strParam != null && !strParam.equals("")) {
                seconds = Long.parseLong(strParam);
            }
        }
        resv.setStartTime(seconds);

        arrayParam = (String[]) inputMap.get("endSeconds");
        if (arrayParam != null) {
            strParam = arrayParam[0];
            if (strParam != null && !strParam.equals("")) {
                seconds = Long.parseLong(strParam);
            }
        }
        resv.setEndTime(seconds);

        arrayParam = (String[]) inputMap.get("bandwidth");
        if (arrayParam != null) {
            strParam = arrayParam[0];
            if (strParam != null && !strParam.equals("")) {
                bandwidth = Long.valueOf(strParam.trim()) * 1000000L;
            } else {
                bandwidth = 0L;
            }
        } else {
            bandwidth = 0L;
        }
        resv.setBandwidth(bandwidth);

        String description = "";
        arrayParam = (String[]) inputMap.get("description");
        if (arrayParam != null) {
            strParam = arrayParam[0];
            if (strParam != null && !strParam.equals("")) {
                description = strParam;
            }
        }

        arrayParam = (String[]) inputMap.get("productionType");
        // if not blank, check box indicating production circuit was checked
        if ((arrayParam != null) && (arrayParam.length > 0) &&
             (arrayParam[0] != null) &&
             !arrayParam[0].trim().equals("")) {
            this.log.info("production circuit");
            description = "[PRODUCTION CIRCUIT] " + description;
        } else {
            this.log.info("non-production circuit");
        }
        resv.setDescription(description);
        return resv;
    }

    /**
     * Takes form parameters and builds Path structures.
     *
     * @param inputMap contains form request parameters
     * @return requestedPath a Path instance with layer 2 or 3 information
     */
    public Path handlePath(HashMap<String, Object> inputMap)
            throws BSSException {

        String[] arrayParam = null;
        String strParam = null;

        List<PathElem> pathElems = new ArrayList<PathElem>();
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("wbui", true);
        String defaultLayer = props.getProperty("defaultLayer");

        Path requestedPath = new Path();
        requestedPath.setPathType(PathType.REQUESTED);
        String explicitPath = "";
        arrayParam = (String[]) inputMap.get("explicitPath");
        if (arrayParam != null) {
            strParam = arrayParam[0];
            if (strParam != null && !strParam.equals("")) {
                explicitPath = strParam;
            }
        }
        if ((explicitPath != null) && !explicitPath.trim().equals("")) {
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
            // Add a path just composed of source and destination
            requestedPath.setPathHopType("loose");
            // TODO:  necessary?
            // path.setId("userPath"); //id doesn't matter in this context
            String[] hops = new String[2];
            hops[0] = ((String[]) inputMap.get("source"))[0];
            hops[1] = ((String[]) inputMap.get("destination"))[0];
            for (int i = 0; i < hops.length; i++) {
                hops[i] = hops[i].trim();
                PathElem pathElem = new PathElem();
                // these can currently be either topology identifiers
                // or IP addresses
                // FIXME:  do somewhere else on the way back if not already
                // hop.setId(i + "");
                pathElem.setUrn(hops[i]);
                this.log.info("implicit path hop: " + hops[i]);
                pathElems.add(pathElem);
            }
            requestedPath.setPathElems(pathElems);
        }
        String vlanTag = "";
        arrayParam = (String[]) inputMap.get("vlanTag");
        if (arrayParam != null) {
            strParam = arrayParam[0];
            if (strParam != null && !strParam.equals("")) {
                vlanTag = strParam;
            }
        }
        String tagSrcPort = "";
        arrayParam = (String[]) inputMap.get("tagSrcPort");
        if (arrayParam != null) {
            strParam = arrayParam[0];
            if (strParam != null && !strParam.equals("")) {
                tagSrcPort = strParam;
            }
        }
        String tagDestPort = "";
        arrayParam = (String[]) inputMap.get("tagDestPort");
        if (arrayParam != null) {
            strParam = arrayParam[0];
            if (strParam != null && !strParam.equals("")) {
                tagDestPort = strParam;
            }
        }
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

            arrayParam = (String[]) inputMap.get("source");
            if (arrayParam != null) {
                strParam = arrayParam[0];
            } else {
                strParam = "";
            }
            strParam = strParam.trim();
            layer2Data.setSrcEndpoint(strParam);

            arrayParam = (String[]) inputMap.get("destination");
            if (arrayParam != null) {
                strParam = arrayParam[0];
            } else {
                strParam = "";
            }
            strParam = strParam.trim();
            layer2Data.setDestEndpoint(strParam);
            requestedPath.setLayer2Data(layer2Data);
            return requestedPath;
        }

        Layer3Data layer3Data = new Layer3Data();
        arrayParam = (String[]) inputMap.get("source");
        if (arrayParam != null) {
            strParam = arrayParam[0];
        } else {
            strParam = "";
        }
        strParam = strParam.trim();

        // VLAN id wasn't supplied with layer 2 id
        if (strParam.startsWith("urn:ogf:network")) {
            throw new BSSException("VLAN tag not supplied for layer 2 reservation");
        }
        layer3Data.setSrcHost(strParam);
        arrayParam = (String[]) inputMap.get("destination");
        if (arrayParam != null) {
            strParam = arrayParam[0];
        } else {
            strParam = "";
        }
        strParam = strParam.trim();
        layer3Data.setDestHost(strParam);

        arrayParam = (String[]) inputMap.get("srcPort");
        if (arrayParam != null) {
            strParam = arrayParam[0];
        } else {
            strParam = "";
        }
        if ((strParam != null) && !strParam.trim().equals("")) {
            layer3Data.setSrcIpPort(Integer.valueOf(strParam));
        } else {
            layer3Data.setSrcIpPort(0);
        }

        arrayParam = (String[]) inputMap.get("destPort");
        if (arrayParam != null) {
            strParam = arrayParam[0];
        } else {
            strParam = "";
        }
        if ((strParam != null) && !strParam.trim().equals("")) {
            layer3Data.setDestIpPort(Integer.valueOf(strParam));
        } else {
            layer3Data.setDestIpPort(0);
        }

        arrayParam = (String[]) inputMap.get("protocol");
        if (arrayParam != null) {
            strParam = arrayParam[0];
        } else {
            strParam = "";
        }
        if ((strParam != null) && !strParam.trim().equals("")) {
            layer3Data.setProtocol(strParam);
        }

        arrayParam = (String[]) inputMap.get("dscp");
        if (arrayParam != null) {
            strParam = arrayParam[0];
        } else {
            strParam = "";
        }
        if ((strParam != null) && !strParam.trim().equals("")) {
            layer3Data.setDscp(strParam);
        }
        requestedPath.setLayer3Data(layer3Data);

        MPLSData mplsData = new MPLSData();
        mplsData.setBurstLimit(10000000L);
        requestedPath.setMplsData(mplsData);

        return requestedPath;
    }
}
