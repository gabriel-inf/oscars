package net.es.oscars.rmi;

import java.io.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
import net.es.oscars.servlets.*;
import net.es.oscars.wsdlTypes.*;


import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane._20070626.CtrlPlanePathContent;

public class ServletCreateRmiHandler {
    private OSCARSCore core;
    private Logger log;


    public ServletCreateRmiHandler() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
    }

    public HashMap<String, Object> createReservation(HashMap<String, String[]> inputMap, String userName) throws IOException {
        this.log.debug("create.start");
        HashMap<String, Object> result = new HashMap<String, Object>();
        String methodName = "CreateReservation";


        TypeConverter tc = core.getTypeConverter();
        Forwarder forwarder = core.getForwarder();
        ReservationManager rm = core.getReservationManager();


// FIXME
//        net.es.oscars.servlets.Utils utils = new net.es.oscars.servlets.Utils();
        EventProducer eventProducer = new EventProducer();



        Reservation resv = this.toReservation(userName, inputMap);
        PathInfo pathInfo = null;
        try {
            pathInfo = this.handlePath(inputMap);
        } catch (BSSException e) {

// FIXME
//            utils.handleFailure(out, e.getMessage(), methodName, null, null);
            return null;
        }



        Session aaa = HibernateUtil.getSessionFactory("aaa").getCurrentSession();
        aaa.beginTransaction();
        UserManager userMgr = core.getUserManager();

        // Check to see if user can create this  reservation
        // bandwidth limits are stored in megaBits
        int reqBandwidth = (int) (resv.getBandwidth() / 1000000);

        // convert from seconds to minutes
        int reqDuration = (int) (resv.getEndTime() - resv.getStartTime()) / 60;

        boolean specifyPath = false;
        String[] arrayParam = inputMap.get("explicitPath");
        if (arrayParam != null) {
            String strParam = arrayParam[0];
            if ((strParam != null) && (!strParam.trim().equals(""))) {
                specifyPath = true;
            }
        }


        AuthValue authVal = userMgr.checkModResAccess(userName, "Reservations",
                "create", reqBandwidth, reqDuration, specifyPath, false);

        if (authVal == AuthValue.DENIED) {

// FIXME
//            utils.handleFailure(out, "createReservation permission denied", methodName, aaa, null);
            return null;
        }
        aaa.getTransaction().commit();

        Session bss = HibernateUtil.getSessionFactory("bss").getCurrentSession();
        bss.beginTransaction();
        String errMessage = null;
        try {
            // url returned, if not null, indicates location of next domain
            // manager
            rm.create(resv, userName, pathInfo);
            // checks whether next domain should be contacted, forwards to
            // the next domain if necessary, and handles the response
            CreateReply forwardReply = forwarder.create(resv, pathInfo);
            rm.finalizeResv(forwardReply, resv, pathInfo);
            rm.store(resv);
            eventProducer.addEvent(OSCARSEvent.RESV_CREATE_COMPLETED, userName, "WBUI", resv);
        } catch (BSSException e) {
            errMessage = e.getMessage();
        } catch (Exception e) {
            // use this so we can find NullExceptions
            errMessage = e.getMessage();
        } finally {
            forwarder.cleanUp();
            if (errMessage != null) {
                eventProducer.addEvent(OSCARSEvent.RESV_CREATE_FAILED, userName, "WBUI", resv, "", errMessage);

// FIXME
//                utils.handleFailure(out, errMessage, methodName, null, bss);
                return null;
            }
        }


        result.put("gri", resv.getGlobalReservationId());
        result.put("status", "Created reservation with GRI " + resv.getGlobalReservationId());
        result.put("method", methodName);
        result.put("success", Boolean.TRUE);

        bss.getTransaction().commit();
        this.log.info("CreateReservation.end");
        this.log.debug("create.end");
        return result;
    }








    private Reservation toReservation(String userName, HashMap<String, String[]> inputMap) {
        String[] arrayParam = null;
        String strParam = null;
        Long bandwidth = null;
        Long seconds = 0L;

        Reservation resv = new Reservation();
        resv.setLogin(userName);

        // necessary type conversions performed here; validation done in
        // ReservationManager
        arrayParam = inputMap.get("startSeconds");
        if (arrayParam != null) {
            strParam = arrayParam[0];
            if (strParam != null && !strParam.equals("")) {
                seconds = Long.parseLong(strParam);
            }
        }
        resv.setStartTime(seconds);


        arrayParam = inputMap.get("endSeconds");
        if (arrayParam != null) {
            strParam = arrayParam[0];
            if (strParam != null && !strParam.equals("")) {
                seconds = Long.parseLong(strParam);
            }
        }
        resv.setEndTime(seconds);


        arrayParam = inputMap.get("bandwidth");
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
        arrayParam = inputMap.get("description");
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
     * Takes form parameters and builds PathInfo structures.
     *
     * @param request contains form request parameters
     * @return pathInfo a PathInfo instance with layer 2 or 3 information
     */
    public PathInfo handlePath(HashMap<String, String[]> inputMap)
            throws BSSException {

        String[] arrayParam = null;
        String strParam = null;

        CtrlPlanePathContent path = null;
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("wbui", true);
        String defaultLayer = props.getProperty("defaultLayer");

        PathInfo pathInfo = new PathInfo();

        String explicitPath = "";
        arrayParam = inputMap.get("explicitPath");
        if (arrayParam != null) {
            strParam = arrayParam[0];
            if (strParam != null && !strParam.equals("")) {
                explicitPath = strParam;
            }
        }


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

        String vlanTag = "";
        arrayParam = inputMap.get("vlanTag");
        if (arrayParam != null) {
            strParam = arrayParam[0];
            if (strParam != null && !strParam.equals("")) {
                vlanTag = strParam;
            }
        }

        String tagSrcPort = "";
        arrayParam = inputMap.get("tagSrcPort");
        if (arrayParam != null) {
            strParam = arrayParam[0];
            if (strParam != null && !strParam.equals("")) {
                tagSrcPort = strParam;
            }
        }

        String tagDestPort = "";
        arrayParam = inputMap.get("tagDestPort");
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

            arrayParam = inputMap.get("source");
            if (arrayParam != null) {
                strParam = arrayParam[0];
            } else {
                strParam = "";
            }

            strParam = strParam.trim();
            layer2Info.setSrcEndpoint(strParam);


            arrayParam = inputMap.get("destination");
            if (arrayParam != null) {
                strParam = arrayParam[0];
            } else {
                strParam = "";
            }
            strParam = strParam.trim();
            layer2Info.setDestEndpoint(strParam);
            layer2Info.setSrcVtag(srcVtagObject);
            layer2Info.setDestVtag(destVtagObject);
            pathInfo.setLayer2Info(layer2Info);

            return pathInfo;
        }

        Layer3Info layer3Info = new Layer3Info();


        arrayParam = inputMap.get("source");
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
        layer3Info.setSrcHost(strParam);




        arrayParam = inputMap.get("destination");
        if (arrayParam != null) {
            strParam = arrayParam[0];
        } else {
            strParam = "";
        }
        strParam = strParam.trim();
        layer3Info.setDestHost(strParam);


        arrayParam = inputMap.get("srcPort");
        if (arrayParam != null) {
            strParam = arrayParam[0];
        } else {
            strParam = "";
        }

        if ((strParam != null) && !strParam.trim().equals("")) {
            layer3Info.setSrcIpPort(Integer.valueOf(strParam));
        } else {
            layer3Info.setSrcIpPort(0);
        }



        arrayParam = inputMap.get("destPort");
        if (arrayParam != null) {
            strParam = arrayParam[0];
        } else {
            strParam = "";
        }

        if ((strParam != null) && !strParam.trim().equals("")) {
            layer3Info.setDestIpPort(Integer.valueOf(strParam));
        } else {
            layer3Info.setDestIpPort(0);
        }



        arrayParam = inputMap.get("protocol");
        if (arrayParam != null) {
            strParam = arrayParam[0];
        } else {
            strParam = "";
        }

        if ((strParam != null) && !strParam.trim().equals("")) {
            layer3Info.setProtocol(strParam);
        }



        arrayParam = inputMap.get("dscp");
        if (arrayParam != null) {
            strParam = arrayParam[0];
        } else {
            strParam = "";
        }
        if ((strParam != null) && !strParam.trim().equals("")) {
            layer3Info.setDscp(strParam);
        }
        pathInfo.setLayer3Info(layer3Info);

        MplsInfo mplsInfo = new MplsInfo();
        mplsInfo.setBurstLimit(10000000);
        pathInfo.setMplsInfo(mplsInfo);

        return pathInfo;
    }
}
