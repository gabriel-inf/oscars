package net.es.oscars.api.client;

import java.net.InetAddress;
import java.net.URL;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static java.util.Arrays.asList;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;


import net.es.oscars.utils.config.ConfigDefaults;
import net.es.oscars.utils.config.ConfigException;
import net.es.oscars.utils.config.ConfigHelper;
import net.es.oscars.utils.config.ContextConfig;
import net.es.oscars.utils.soap.*;
import net.es.oscars.utils.svc.ServiceNames;
import net.es.oscars.common.soap.gen.*;
import net.es.oscars.api.soap.gen.v06.CancelResContent;
import net.es.oscars.api.soap.gen.v06.CancelResReply;
import net.es.oscars.api.soap.gen.v06.CreatePathContent;
import net.es.oscars.api.soap.gen.v06.CreatePathResponseContent;
import net.es.oscars.api.soap.gen.v06.EventContent;
import net.es.oscars.api.soap.gen.v06.MplsInfo;
import net.es.oscars.api.soap.gen.v06.QueryResContent;
import net.es.oscars.api.soap.gen.v06.QueryResReply;
import net.es.oscars.api.soap.gen.v06.ResCreateContent;
import net.es.oscars.api.soap.gen.v06.CreateReply;
import net.es.oscars.api.soap.gen.v06.ModifyResContent;
import net.es.oscars.api.soap.gen.v06.ModifyResReply;
import net.es.oscars.api.soap.gen.v06.ResDetails;
import net.es.oscars.api.soap.gen.v06.GlobalReservationId;
import net.es.oscars.api.soap.gen.v06.Layer2Info;
import net.es.oscars.api.soap.gen.v06.Layer3Info;
import net.es.oscars.api.soap.gen.v06.TeardownPathContent;
import net.es.oscars.api.soap.gen.v06.TeardownPathResponseContent;
import net.es.oscars.api.soap.gen.v06.VlanTag;
import net.es.oscars.api.soap.gen.v06.ListReply;
import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
import net.es.oscars.api.soap.gen.v06.ListRequest;

import net.es.oscars.api.soap.gen.v06.ObjectFactory;

import net.es.oscars.utils.clients.IDCClient05;
import net.es.oscars.utils.clients.IDCClient06;

import org.apache.log4j.Logger;

import org.ho.yaml.Yaml;

public final class SimpleOSCARSClient {

    /**
     * IDC Test client.
     * Arguments are: <request> <protocol version> <auth type>
     * auth types can be: x509, UT
     * 
     * @param args
     */
    private static Logger LOG = null;
    public static ContextConfig cc = ContextConfig.getInstance();
    public static String outputFormat = "humanReadable";

    public static void main(String args[]) {
        try {
            String parameter_file = "-";

            OptionParser parser = new OptionParser();
            OptionSpec<String> FILE = parser.accepts("f", "file:yaml file to read").withRequiredArg().ofType(String.class);
            OptionSpec<String> OVERRIDES = parser.accepts("o", "override:key=value. Override the YAML variable 'key' with value 'value'").withRequiredArg().ofType(String.class);

            OptionSet options = parser.parse( args );

            if (options.has (FILE)) {
                parameter_file = options.valueOf(FILE);
            }
            List<String> overrides = options.valuesOf( OVERRIDES );

            String yaml;

            Map run_parameters;
            if ("-".equals(parameter_file)) {
                run_parameters = (Map) Yaml.load(System.in);
            }
            else {
                run_parameters = (Map) Yaml.load(parameter_file);
            }

            for (String override : overrides) {
                String [] fields = override.split("=", 2);
                if (fields.length != 2) {
                    System.err.println("Invalid override("+override+"). Must be of the form key=value");
                    System.exit(-1);
                }

                try {
                    Integer value = new Integer(Integer.parseInt(fields[1]));
                    run_parameters.put(fields[0], value);
                }
                catch (NumberFormatException e) {
                    try {
                        Float value = new Float(Float.parseFloat(fields[1]));
                        run_parameters.put(fields[0], value);
                    }
                    catch (NumberFormatException ee) {
                        run_parameters.put(fields[0], fields[1]);
                    }
                }
            }

            String context = (String) run_parameters.get("context");
            if (context == null) {
                context = "DEVELOPMENT";
            }
            if (!context.equals("UNITTEST") &&
                !context.equals("SDK") &&
                !context.equals("DEVELOPMENT") &&
                !context.equals("PRODUCTION") ) 
            {
                System.err.println("Unrecognized context value: " + context);
                System.exit(-1);
            }

            String config_path = (String) run_parameters.get("config_path");
            if (config_path == null) {
                config_path = "config/";
            }

            if (run_parameters.get("output-format") != null) {
                outputFormat = (String) run_parameters.get("output-format");
            }

            if (!outputFormat.equals("humanReadable") &&
                !outputFormat.equals("rawResponse"))
            {
                System.err.println("Unrecognized output format: "+outputFormat);
                System.exit(-1);
            }

            cc.setContext(context);
            cc.setServiceName(ServiceNames.SVC_API);
            try {
                cc.loadManifest(ServiceNames.SVC_API, ConfigDefaults.MANIFEST); // manifest.yaml
                cc.setLog4j();
            } catch (ConfigException ex) {
                System.out.println("Caught ConfigurationException " + ex.getMessage());
                System.exit(-1);
            }

            LOG = Logger.getLogger(SimpleOSCARSClient.class);

            Double protocol_version   = (Double) run_parameters.get("version");

            if (protocol_version == 0.6) {
                handle_client_0_6(run_parameters);
            }
            else if (protocol_version == 0.5) {
                //handle_client_0_5(run_parameters);
            }
            else {
                LOG.error("invalid version: "+(protocol_version == null?protocol_version:"null"));
            }
        } catch (Exception ee) {
            //LOG.debug (ee.toString());
            System.err.println( "IDCTest Caught exception " + ee.toString());
            ee.printStackTrace();
        }

        System.exit(0);
    }

    public static void handle_client_0_6(Map run_parameters) {
        try {
            String request  = (String) run_parameters.get("command");
            String authType = (String) run_parameters.get("auth-type");

            JAXBContext jaxbContext = JAXBContext.newInstance("net.es.oscars.api.soap.gen.v06");
            Marshaller marshaller = jaxbContext.createMarshaller();

            URL wsdl = cc.getWSDLPath("0.6");
            URL url = new URL((String) run_parameters.get("url"));

            IDCClient06 client = IDCClient06.getClient (url, wsdl, authType);
            if (request.equals("setupPath")) {
                try {
                    String gri = (String) run_parameters.get("gri");

                    // Send a createPath query 
                    CreatePathContent query = new CreatePathContent();
                    if (gri != null)  {
                        query.setGlobalReservationId(gri);
                    }
                    query.setToken("TestToken");
                    Object[] req = new Object[]{query};
                    Object[] res = client.invoke("createPath",req);
                    CreatePathResponseContent response = (CreatePathResponseContent) res[0];
                    if (outputFormat.equals("humanReadable")) {
                        System.out.println("Response: " + response.getGlobalReservationId() + " , " + response.getStatus());
                    }
                    else if (outputFormat.equals("rawResponse")) {
                        marshaller.marshal( (new ObjectFactory()).createCreatePathResponse(response), System.out );
                    }
                } catch (OSCARSServiceException ex) {
                    ErrorReport errorReport = ex.getErrorReport();
                    if (errorReport != null ){
                        LOG.debug("OSCARSServiceException ErrorCode is " + errorReport.getErrorCode() +
                                  " type is " + errorReport.getErrorType() +
                                  " message is " + errorReport.getErrorMsg());
                    /* OSCARSFault faultInfo = (OSCARSFault) ex.getFaultInfo();
                    if (faultInfo != null) {
                        LOG.debug("OSCARSServiceException type is  " + faultInfo.getMsg() + " message is " +
                                  ex.getMessage() + " " + faultInfo.getDetails());  */
                    } else {
                        LOG.debug("OSCARSServiceException " + ex.getMessage());
                    }
                }
            } else if (request.equals("teardownPath")) {
                try {
                    String gri = (String) run_parameters.get("gri");

                    // Send a teardownPath query
                    TeardownPathContent query = new TeardownPathContent();
                    if (gri != null)  {
                        query.setGlobalReservationId(gri);
                    }
                    query.setToken("TestToken");
                    Object[] req = new Object[]{query};
                    Object[] res = client.invoke("teardownPath",req);
                    TeardownPathResponseContent response = (TeardownPathResponseContent) res[0];
                    if (outputFormat.equals("humanReadable")) {
                        System.out.println("Response: " + response.getGlobalReservationId() + " , " + response.getStatus());
                    }
                    else if (outputFormat.equals("rawResponse")) {
                        marshaller.marshal( (new ObjectFactory()).createTeardownPathResponse(response), System.out );
                    }
                } catch (OSCARSServiceException ex) {
                    ErrorReport errorReport = ex.getErrorReport();
                    if (errorReport != null ){
                        LOG.debug("OSCARSServiceException ErrorCode is " + errorReport.getErrorCode() +
                                  " type is " + errorReport.getErrorType() +
                                  " message is " + errorReport.getErrorMsg());
                    } else {
                        LOG.debug("OSCARSServiceException " + ex.getMessage());
                    }
                }
            } else if (request.equals("queryReservation")) {
                try {
                    String gri = (String) run_parameters.get("gri");

                    QueryResContent query = new QueryResContent();
                    query.setGlobalReservationId(gri);
                    Object[] req = new Object[]{query};
                    LOG.debug("calling client.invoke");
                    Object[] res = client.invoke("queryReservation",req);
                    QueryResReply response = (QueryResReply)res[0];
                    if (outputFormat.equals("humanReadable")) {
                        ResDetails details = response.getReservationDetails();
                        printResDetails(details);
                    }
                    else if (outputFormat.equals("rawResponse")) {
                        marshaller.marshal( (new ObjectFactory()).createQueryReservationResponse(response), System.out );
                    }
                } catch (OSCARSServiceException ex) {
                    ErrorReport errorReport = ex.getErrorReport();
                    if (errorReport != null ){
                        LOG.debug("OSCARSServiceException ErrorCode is " + errorReport.getErrorCode() +
                                  " type is " + errorReport.getErrorType() +
                                  " message is " + errorReport.getErrorMsg());
                            } else {
                        LOG.debug("OSCARSServiceException " + ex.getMessage());
                    }
                }
            } else if (request.equals("createReservation")) {
                try {
                    // Send a createReservation query
                    ResCreateContent query = new ResCreateContent();
                    query = configure(run_parameters);

                    Object[] req = new Object[]{query};
                    Object[] res = client.invoke("createReservation",req);
                    CreateReply response = (CreateReply) res[0];
                    if (outputFormat.equals("humanReadable")) {
                        System.out.println ("Response: " + response.getGlobalReservationId() + " , " + response.getStatus());
                        System.out.println ("\n[createReservation]  gri= " + response.getGlobalReservationId() +
                                        "\n                     transactionId=" + response.getMessageProperties().getGlobalTransactionId() +
                                        "\n                     status=" + response.getStatus());

                    }
                    else if (outputFormat.equals("rawResponse")) {
                        marshaller.marshal( (new ObjectFactory()).createCreateReservationResponse(response), System.out );
                    }
                } catch (OSCARSServiceException ex) {
                    ErrorReport errorReport = ex.getErrorReport();
                    if (errorReport != null ){
                        LOG.debug("OSCARSServiceException ErrorCode is " + errorReport.getErrorCode() +
                                  " type is " + errorReport.getErrorType() +
                                  " message is " + errorReport.getErrorMsg());
                    } else {
                        LOG.debug("OSCARSServiceException " + ex.getMessage());
                    }
                }
            } else if (request.equals("modifyReservation")) {
                try {
                    String gri = (String) run_parameters.get("gri");

                    // Send a createReservation query
                    ResCreateContent createCon = new ResCreateContent();
                    createCon = configure(run_parameters);

                    // hack
                    ModifyResContent query = new ModifyResContent();
                    query.setDescription(createCon.getDescription());
                    query.setUserRequestConstraint(createCon.getUserRequestConstraint());
                    query.setGlobalReservationId(gri);

                    Object[] req = new Object[]{query};
                    Object[] res = client.invoke("modifyReservation",req);
                    ModifyResReply response = (ModifyResReply) res[0];
                    if (outputFormat.equals("humanReadable")) {
                        System.out.println ("Response: " + response.getReservation().getGlobalReservationId() + " , " +
                                response.getReservation().getStatus());
                        System.out.println ("[modifyReservation] completed gri= " +
                                    response.getReservation().getGlobalReservationId() + " status=" +
                                    response.getReservation().getStatus());
                    }
                    else if (outputFormat.equals("rawResponse")) {
                        marshaller.marshal( (new ObjectFactory()).createModifyReservationResponse(response), System.out );
                    }
                } catch (OSCARSServiceException ex) {
                    ErrorReport errorReport = ex.getErrorReport();
                    if (errorReport != null ){
                        LOG.debug("OSCARSServiceException ErrorCode is " + errorReport.getErrorCode() +
                                  " type is " + errorReport.getErrorType() +
                                  " message is " + errorReport.getErrorMsg());
                    } else {
                        LOG.debug("OSCARSServiceException " + ex.getMessage());
                    }
                }
            } else if (request.equals("cancelReservation")){
                try {
                    String gri = (String) run_parameters.get("gri");

                    CancelResContent query = new CancelResContent();
                    query.setGlobalReservationId(gri);
                    Object[] req = new Object[]{query};
                    LOG.debug("calling client.invoke");
                    Object[] res = client.invoke("cancelReservation",req);
                    CancelResReply response = (CancelResReply) res[0];
                    if (outputFormat.equals("humanReadable")) {
                        String status = response.getStatus();
                        System.out.println("return from cancel is " + status);
                    }
                    else if (outputFormat.equals("rawResponse")) {
                        marshaller.marshal( (new ObjectFactory()).createCancelReservationResponse(response), System.out );
                    }

                } catch (OSCARSServiceException ex) {
                    ErrorReport errorReport = ex.getErrorReport();
                    if (errorReport != null ){
                        LOG.debug("OSCARSServiceException ErrorCode is " + errorReport.getErrorCode() +
                                  " type is " + errorReport.getErrorType() +
                                  " message is " + errorReport.getErrorMsg());
                    } else {
                        LOG.debug("OSCARSServiceException " + ex.getMessage());
                    }
                }
            } else if (request.equals("list") || request.equals("listReservations")){
                try {
                    Integer numReq = (Integer) run_parameters.get("number");
                    Integer offset = (Integer) run_parameters.get("reservation-offset");
                    String status  = (String)  run_parameters.get("status");
                    Long startTime = null;
                    Long endTime = null;

                    try {
                        String curr_startTime = (String)  run_parameters.get("start-time");
                        String curr_endTime = (String)  run_parameters.get("end-time");
                        if (curr_endTime != null && curr_startTime != null) {
                            HashMap<String, Long> times = parseTimes(curr_startTime, curr_endTime);
                            startTime = times.get("start");
                            endTime = times.get("end");
                        }
                        else if (curr_startTime != null && curr_endTime != null) {
                            die("Either both startTime and endTime need to exist or neither can");
                        }
                    }
                    catch (Exception ce1) {
                        try {
                            startTime = new Long((Integer) run_parameters.get("start-time"));
                            endTime = new Long((Integer) run_parameters.get("end-time"));
                        }
                        catch (Exception ce2) {
                            die("If start-time and end-time are specified, they need to either both be strings or both be unix timestamps: "+ce2);
                        }
                    }

                    ListRequest listReq = new ListRequest();
                    if (startTime != null && endTime != null) {
                        listReq.setStartTime(startTime);
                        listReq.setEndTime(endTime);
                    }
                    if (numReq != null) {
                        listReq.setResRequested(numReq);
                    }
                    if (offset != null) {
                        listReq.setResOffset(offset);
                    }
                    if (status != null) {
                        listReq.getResStatus().add(status);
                    }
                    Object [] req = new Object[]{listReq};
                    Object [] res = client.invoke("listReservations",req);
                    ListReply response = (ListReply) res[0];
                    if (outputFormat.equals("humanReadable")) {
                        List <ResDetails> resDetailsList = response.getResDetails();
                        for (ResDetails resDetails: resDetailsList) {
                            printResDetails(resDetails);
                        }
                    }
                    else if (outputFormat.equals("rawResponse")) {
                        marshaller.marshal( (new ObjectFactory()).createListReservationsResponse(response), System.out );
                    }

                } catch (OSCARSServiceException ex) {
                    ErrorReport errorReport = ex.getErrorReport();
                    if (errorReport != null ){
                        LOG.debug("OSCARSServiceException ErrorCode is " + errorReport.getErrorCode() +
                                  " type is " + errorReport.getErrorType() +
                                  " message is " + errorReport.getErrorMsg());
                    } else {
                        LOG.debug("OSCARSServiceException " + ex.getMessage());
                    }
                }
            } else {
                LOG.error("unrecognized command " + request);
            }
        }
        catch (Exception ee) {
            LOG.error("Problem handling version 6 client: "+ee.toString());
            ee.printStackTrace();
        }
    }

/*
    public static void handle_client_0_5() {
        try {
            // Send a createPath query
            IDCClient05 client = IDCClient05.getClient (host, wsdl, authType);
            net.es.oscars.api.soap.gen.v05.CreatePathContent query = new net.es.oscars.api.soap.gen.v05.CreatePathContent();
            query.setGlobalReservationId(args[3]);
            query.setToken("TestToken");

            Object[] req = new Object[]{query};
            Object[] res = client.invoke("createPath",req);

            net.es.oscars.api.soap.gen.v05.CreatePathResponseContent response = (net.es.oscars.api.soap.gen.v05.CreatePathResponseContent) res[0];
            LOG.debug("Response: " + response.getGlobalReservationId() + " , " + response.getStatus());
        } catch (OSCARSServiceException ex) {
            OSCARSFault faultInfo = (OSCARSFault) ex.getFaultInfo();
            if (faultInfo != null) {
                LOG.debug("OSCARSServiceExeption type is  " + faultInfo.getMsg() + " message is " +
                          ex.getMessage() + " " + faultInfo.getDetails());
            } else {
                LOG.debug("OSCARSServiceExeption " + ex.getMessage());
            }
        }

        return;
    }
*/

    @SuppressWarnings("unchecked")
    public static ResCreateContent configure(Map run_parameters) {
        ResCreateContent resContent = new ResCreateContent();;
        String gri = (String) run_parameters.get("gri");
        String login = (String) run_parameters.get("login");
        Integer layer = (Integer) run_parameters.get("layer");
        Integer bandwidth = (Integer) run_parameters.get("bandwidth");
        Integer burstLimit = (Integer) run_parameters.get("burstLimit");
        String lspClass = (String) run_parameters.get("lspClass");
        String src = (String) run_parameters.get("src");
        String dst = (String) run_parameters.get("dst");
        String description = (String) run_parameters.get("description");
        String srcVlan = (String) run_parameters.get("srcvlan");
        String dstVlan = (String) run_parameters.get("dstvlan");
        ArrayList<String> pathArray = (ArrayList<String>) run_parameters.get("path");
        String pathSetupMode = (String) run_parameters.get("path-setup-mode");
        String pathType = (String) run_parameters.get("path-type");
        Long start_time = null;
        Long end_time = null;

        try {
            String curr_start_time = (String)  run_parameters.get("start-time");
            String curr_end_time = (String)  run_parameters.get("end-time");
            if (curr_end_time != null && curr_start_time != null) {
                HashMap<String, Long> times = parseTimes(curr_start_time, curr_end_time);
                start_time = times.get("start");
                end_time = times.get("end");
            }
            else if (curr_start_time != null && curr_end_time != null) {
                die("Either both start-time and end-time need to exist or neither can");
            }
        }
        catch (Exception ce1) {
            try {
                start_time = new Long((Integer) run_parameters.get("start-time"));
                end_time = new Long((Integer) run_parameters.get("end-time"));
            }
            catch (Exception ce2) {
                die("If start-time and end-time are specified, they need to either both be strings or both be unix timestamps: "+ce2);
            }
        }

        if (layer != 2 && layer != 3) {
            die("Layer must be 2 or 3");
        }
        if (src == null || src.equals("")) {
            die("Source must be specified");
        }
        if (dst == null || dst.equals("")) {
            die("Destination must be specified");
        }
        if (bandwidth == null) {
            die("bandwidth must be specified");
        }
        if (description == null || description.equals("")) {
            die("description must be specified");
        }

        if (gri != null ) { 
            resContent.setGlobalReservationId(gri);
        }
        resContent.setDescription(description);
        UserRequestConstraintType uc = new UserRequestConstraintType();
        uc.setBandwidth(bandwidth);
        uc.setStartTime(start_time);
        uc.setEndTime(end_time);
        PathInfo pathInfo =  new PathInfo();
        pathInfo.setPathSetupMode(pathSetupMode);
        pathInfo.setPathType(pathType);
        if (layer.equals("2")) {
            Layer2Info layer2Info = new Layer2Info();
            layer2Info.setSrcEndpoint(src);
            layer2Info.setDestEndpoint(dst);
            if (srcVlan != null) {
                VlanTag vlan = new VlanTag();
                vlan.setValue(srcVlan);
                vlan.setTagged(true);
                layer2Info.setSrcVtag(vlan);
            }
            if (dstVlan != null) {
                VlanTag vlan = new VlanTag();
                vlan.setValue(dstVlan);
                vlan.setTagged(true);
                layer2Info.setDestVtag(vlan);
            }
            pathInfo.setLayer2Info(layer2Info);
        } else if (layer.equals("3")) {
            // TODO
            if (burstLimit !=  null && burstLimit != 0){
                MplsInfo mplsInfo = new MplsInfo();
                mplsInfo.setBurstLimit(burstLimit);
                mplsInfo.setLspClass(lspClass);
                pathInfo.setMplsInfo(mplsInfo);
            }
        }
        if ( pathArray != null && !pathArray.isEmpty() ) {
            CtrlPlanePathContent path = new CtrlPlanePathContent ();
            List<CtrlPlaneHopContent> hops = path.getHop();
            for (String hop : pathArray) {
               CtrlPlaneHopContent cpHop = new CtrlPlaneHopContent();
               cpHop.setLinkIdRef(hop);
               hops.add(cpHop);
            }
            pathInfo.setPath(path);
        }
        uc.setPathInfo(pathInfo);
        resContent.setUserRequestConstraint(uc);
        return resContent;
    }
     
    private static void die(String msg) {
        LOG.error(msg);
        System.exit(1);
    }

    private static HashMap<String, Long> parseTimes(String start_time, String end_time) {
        HashMap<String, Long> result = new HashMap<String, Long>();
        Long startTime = 0L;
        Long endTime = 0L;
        Long createTime= System.currentTimeMillis()/1000;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (start_time == null || start_time.equals("now") || start_time.equals("")) {
            startTime = System.currentTimeMillis()/1000;
        } else {
            try {
                startTime = df.parse(start_time.trim()).getTime()/1000;
            } catch (java.text.ParseException ex) {
                die("Error parsing start date: "+ex.getMessage());
            }
        }
        if (end_time == null || end_time.equals("")) {
            die("No end time specified.");
        } else if (end_time.startsWith("+")) {
            String[] hm = end_time.substring(1).split("\\:");
            if (hm.length != 3) {
                die("Error parsing end date format");
            } 
            try {
                Integer seconds = Integer.valueOf(hm[0])*3600*24; //days
                seconds += Integer.valueOf(hm[1])*3600; // hours
                seconds += Integer.valueOf(hm[2])*60; // minutes
                if (seconds < 60) {
                    die("Duration must be > 60 sec");
                }
                endTime = startTime + seconds;
            } catch (NumberFormatException ex) {
                die("Error parsing end date format: "+ex.getMessage());
            }
        } else {
            try {
                endTime = df.parse(end_time.trim()).getTime()/1000;
            } catch (java.text.ParseException ex) {
                die("Error parsing emd date: "+ex.getMessage());
            }
        }
        
        
        result.put("start", startTime);
        result.put("end", endTime);
        result.put("create", createTime);
        return result;
    }

    /**
     * print out all information about this reservation
     * @param resDetails Must contain a userConstraint, may contain a reservedConstaint
     *       if reservedConstraint exists, use info from it, otherwise use userConstraint.
     *       The pathInfo element may contain one of layer2Info or layer3Info. It may also contain
     *       a path structure. If the path structure exists the info from it will be used rather than
     *       data from layer2Info. Layer3 is currently not implemented
     */
    public static void printResDetails(ResDetails resDetails) {
        System.out.println("\nGRI: " + resDetails.getGlobalReservationId());
        System.out.println("Login: " + resDetails.getLogin());
        System.out.println("Description: " + resDetails.getDescription());
        System.out.println("Status: "
                + resDetails.getStatus().toString());
        UserRequestConstraintType uConstraint = resDetails.getUserRequestConstraint();
        System.out.println("startTime: " + new Date(uConstraint.getStartTime()*1000).toString());
        System.out.println("endTime: " + new Date(uConstraint.getEndTime()*1000).toString());
        System.out.println("bandwidth: " + Integer.toString(uConstraint.getBandwidth()));
        PathInfo pathInfo = null;
        String pathType = null;
        ReservedConstraintType rConstraint = resDetails.getReservedConstraint();
        if (rConstraint !=  null) {
            pathInfo=rConstraint.getPathInfo();
            pathType = "reserved";
        } else {
            uConstraint = resDetails.getUserRequestConstraint();
            if (uConstraint ==null) {
                System.out.println("invalid reservation, no reserved or requested path");
                return;
            }
            pathInfo=uConstraint.getPathInfo();
            pathType="requested";
            System.out.println("no path reserved, using requested path ");
        }
        Layer3Info layer3Info = pathInfo.getLayer3Info();
        if (layer3Info != null) {
            System.out.println("Source host: " +
                    layer3Info.getSrcHost());
            System.out.println("Destination host: " +
                    layer3Info.getDestHost());
        }
        CtrlPlanePathContent path = pathInfo.getPath();
        if (path != null) {
            List<CtrlPlaneHopContent> hops = path.getHop();
            if (hops.size() > 0) {
                System.out.println("Hops in " + pathType + " path are:");
                for ( CtrlPlaneHopContent ctrlHop : hops ) {
                    CtrlPlaneLinkContent link = ctrlHop.getLink();
                    String vlanRangeAvail = "any";
                    if (link != null ) {
                        CtrlPlaneSwcapContent swcap= link.getSwitchingCapabilityDescriptors();
                        if (swcap != null) {
                            CtrlPlaneSwitchingCapabilitySpecificInfo specInfo = swcap.getSwitchingCapabilitySpecificInfo();
                            if (specInfo != null) {
                                vlanRangeAvail = specInfo.getVlanRangeAvailability(); 
                            }
                        }
                        System.out.println(link.getId() + " vlanRange: " + vlanRangeAvail);
                    } else {
                        String id = ctrlHop.getLinkIdRef();
                        System.out.println(id);
                    }
                }
            }
            else {
                Layer2Info layer2Info = pathInfo.getLayer2Info();
                if (layer2Info != null) {
                    String vlanRange = "any";
                    if (layer2Info.getSrcVtag() != null) {
                        vlanRange = layer2Info.getSrcVtag().getValue();
                    }
                    System.out.println("Source urn: " +
                            layer2Info.getSrcEndpoint() + " vlanTag:" + vlanRange);
                    vlanRange = "any";
                    if (layer2Info.getDestVtag() != null) {
                        vlanRange = layer2Info.getDestVtag().getValue();
                    }
                    System.out.println("Destination urn: " +
                            layer2Info.getDestEndpoint() + " vlanTag:" + vlanRange);
                }
            }
        } else {
            System.out.println("no path information in " + pathType + " constraint");
        }
    }
}
