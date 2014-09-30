package net.es.oscars.pss.onos.common;

import net.es.oscars.api.soap.gen.v06.PathInfo;
import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
import net.es.oscars.pss.util.URNParser;
import net.es.oscars.pss.util.URNParserResult;
import net.es.oscars.utils.soap.OSCARSServiceException;
import net.es.oscars.utils.topology.PathTools;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.log4j.Logger;

import net.es.oscars.logging.ErrSev;
import net.es.oscars.logging.ModuleName;
import net.es.oscars.logging.OSCARSNetLogger;
import net.es.oscars.pss.beans.PSSAction;
import net.es.oscars.pss.beans.PSSException;
import net.es.oscars.pss.beans.PSSRequest;
import net.es.oscars.pss.enums.ActionStatus;
import net.es.oscars.pss.notify.CoordNotifier;
import net.es.oscars.pss.soap.gen.ModifyReqContent;
import net.es.oscars.pss.soap.gen.PSSPortType;
import net.es.oscars.pss.soap.gen.SetupReqContent;
import net.es.oscars.pss.soap.gen.StatusReqContent;
import net.es.oscars.pss.soap.gen.TeardownReqContent;

import net.es.oscars.utils.svc.ServiceNames;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * main entry point for PSS
 *
 * @author haniotak,mrt
 *
 */
@javax.jws.WebService(
        serviceName = ServiceNames.SVC_PSS,
        targetNamespace = "http://oscars.es.net/OSCARS/pss",
        portName = "PSSPort",
        endpointInterface = "net.es.oscars.pss.soap.gen.PSSPortType")
@javax.xml.ws.BindingType(value = "http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class OnosPSSSoapHandler implements PSSPortType {

    private static final Logger log = Logger.getLogger(OnosPSSSoapHandler.class.getName());
    private static final String moduleName = ModuleName.PSS;

    public void setup(SetupReqContent setupReq) {
        String event = "setup";
        OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
        netLogger.init(moduleName,setupReq.getTransactionId());
        String gri = setupReq.getReservation().getGlobalReservationId();
        netLogger.setGRI(gri);
        log.info(netLogger.start(event));


        ReservedConstraintType rc = setupReq.getReservation().getReservedConstraint();

        int mbps = rc.getBandwidth();
        PathInfo pi = rc.getPathInfo();
        List<CtrlPlaneHopContent> localHops;
        try {
            localHops = PathTools.getLocalHops(pi.getPath(), PathTools.getLocalDomainId());
        } catch (OSCARSServiceException ex) {
            log.error("problem finding local hops!", ex);
            return;
        }
        CtrlPlaneLinkContent ingressLink = localHops.get(0).getLink();
        CtrlPlaneLinkContent egressLink = localHops.get(localHops.size()-1).getLink();


        String srcLinkId = ingressLink.getId();
        URNParserResult srcParsed = URNParser.parseTopoIdent(srcLinkId);
        String srcNodeId = srcParsed.getNodeId();
        String srcPortId = srcParsed.getPortId();


        String dstLinkId = egressLink.getId();
        URNParserResult dstParsed = URNParser.parseTopoIdent(dstLinkId);
        String dstNodeId = dstParsed.getNodeId();
        String dstPortId = dstParsed.getPortId();




        post("http://192.168.56.102:8080" + "/wm/calendaring/" + mbps + "/" + srcNodeId + "/" + srcPortId + "/" + dstNodeId + "/" + dstPortId);




        PSSAction act = new PSSAction();
        CoordNotifier coordNotify = new CoordNotifier();
        try {
            PSSRequest req = new PSSRequest();
            req.setSetupReq(setupReq);
            req.setRequestType(PSSRequest.PSSRequestTypes.SETUP);

            act.setRequest(req);
            act.setActionType(net.es.oscars.pss.enums.ActionType.SETUP);
            act.setStatus(ActionStatus.SUCCESS);

            log.debug(netLogger.getMsg(event,"calling coordNotify.process"));
            coordNotify.process(act);
        } catch (PSSException e) {
            log.error(netLogger.error(event,ErrSev.MAJOR,"caught PSSException " + e.getMessage()));
        }
        log.info(netLogger.end(event));
    }

    public void teardown(TeardownReqContent teardownReq) {
   }

    public  void modify(ModifyReqContent modifyReq)  {

    }

    public void status(StatusReqContent statusReq) {

    }


    public static void post(String strUrl) {

        try {
            URL url = new URL(strUrl);
            System.out.println(strUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new IOException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            conn.disconnect();
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL for POST request");
        } catch (IOException e) {
            System.out.println("Couldn't connect to the remote REST server" + e.getMessage());
            e.printStackTrace();
        }
    }



}
