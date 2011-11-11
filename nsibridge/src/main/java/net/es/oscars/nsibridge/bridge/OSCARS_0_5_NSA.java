package net.es.oscars.nsibridge.bridge;


import java.util.HashMap;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;

import net.es.oscars.client.improved.ClientException;
import net.es.oscars.client.improved.cancel.CancelClient;
import net.es.oscars.client.improved.create.CreateClient;
import net.es.oscars.client.improved.create.CreateRequestParams;
import net.es.oscars.client.improved.query.QueryClient;
import net.es.oscars.nsibridge.soap.gen.ifce.ServiceException;
import net.es.oscars.nsibridge.soap.gen.ifce.ProvisionRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReleaseRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.ReserveRequestType;
import net.es.oscars.nsibridge.soap.gen.ifce.TerminateRequestType;
import net.es.oscars.wsdlTypes.CreateReply;
import net.es.oscars.wsdlTypes.ResCreateContent;
import net.es.oscars.wsdlTypes.ResDetails;

public class OSCARS_0_5_NSA extends SimpleNSA implements ProviderAPI, RequesterAPI, NSAAPI {
    static class MyLock extends Object {
    }    
    
    static public MyLock lockObject = new MyLock();
    
    Logger log = Logger.getLogger(OSCARS_0_5_NSA.class);
    public OSCARS_0_5_NSA(String nsaId) {
        super(nsaId);
    }
    private HashMap<String, String> idcGriToConnId = new HashMap<String, String>();
    private HashMap<String, String> connIdToIdcGri = new HashMap<String, String>();
    
    private Integer getVlan(String stp) throws ServiceException {
        if (stp.equals("urn:ogf:network:stp:Dominica:D1")) {
            return 1001;
        } else if (stp.equals("urn:ogf:network:stp:Dominica:D2")) {
            return 1001;
        } else if (stp.equals("urn:ogf:network:stp:Dominica:D3")) {
            return 1001;
        } else if (stp.equals("urn:ogf:network:stp:Dominica:D4")) {
            return 1001;
        }
        throw new ServiceException("cannot find VLAN for stp: "+stp);
    }
    private String getUrn(String stp) throws ServiceException {
        if (stp.equals("urn:ogf:network:stp:Dominica:D1")) {
            return "urn:ogf:network:domain=dev.es.net:node=aofa-sdn1:port=xe-2/1/0:link=*";
        } else if (stp.equals("urn:ogf:network:stp:Dominica:D2")) {
            return "urn:ogf:network:domain=dev.es.net:node=star-sdn1:port=xe-7/0/0:link=*";
        } else if (stp.equals("urn:ogf:network:stp:Dominica:D3")) {
            return "urn:ogf:network:domain=dev.es.net:node=star-cr1:port=xe-1/1/0:link=*";
        } else if (stp.equals("urn:ogf:network:stp:Dominica:D4")) {
            return "urn:ogf:network:domain=dev.es.net:node=bnl-mr3:port=xe-7/2/0:link=*";
        }
        
        throw new ServiceException("cannot find OSCARS URN for stp: "+stp);

    }
    
    
    /**************
     *            *
     * LOCAL SIDE *
     *            *
     *************/

    public boolean localReservation(ReserveRequestType rrt) throws ServiceException {
        String connId = rrt.getReserve().getReservation().getConnectionId();

        
        int bandwidth = rrt.getReserve().getReservation().getServiceParameters().getBandwidth().getDesired();
        String desc = rrt.getReserve().getReservation().getDescription();
        String srcStp = rrt.getReserve().getReservation().getPath().getSourceSTP().getStpId();
        String destStp = rrt.getReserve().getReservation().getPath().getDestSTP().getStpId();
        XMLGregorianCalendar xgcStart = rrt.getReserve().getReservation().getServiceParameters().getSchedule().getStartTime();
        XMLGregorianCalendar xgcEnd = rrt.getReserve().getReservation().getServiceParameters().getSchedule().getEndTime();
        long startTime = xgcStart.toGregorianCalendar().getTimeInMillis()/1000;
        long endTime = xgcEnd.toGregorianCalendar().getTimeInMillis()/1000;
        
        String srcVlan = this.getVlan(srcStp).toString();
        String destVlan = this.getVlan(destStp).toString();
        String srcUrn = this.getUrn(srcStp);
        String destUrn = this.getUrn(destStp);
        
        CreateRequestParams rparams = new CreateRequestParams();

        rparams.setBandwidth(10);
        rparams.setDescription(desc+"(NSI)");
        rparams.setStartTime(startTime);
        rparams.setEndTime(endTime);
        rparams.setSrc(srcUrn);
        rparams.setDst(destUrn);
        rparams.setSrcVlan(srcVlan);
        rparams.setDstVlan(destVlan);
        rparams.setLayer("2");
        rparams.setPathSetupMode("timer-automatic");
        // pathInfo.setPathType("requested");

        CreateClient cl = new CreateClient();
        cl.configureSoap();
        ResCreateContent createReservation = null;
        try {
            createReservation = cl.formRequest(rparams);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);

            ServiceException ni = new ServiceException(ex.getMessage());
            throw ni;
        }

        synchronized(lockObject) {
            try {
                log.debug("Calling create() on IDC...");
                CreateReply createReply = cl.performRequest(createReservation);
                String gri = createReply.getGlobalReservationId();
                this.idcGriToConnId.put(gri, connId);
                this.connIdToIdcGri.put(connId, gri);
            } catch (Exception e) {
                e.printStackTrace(System.err);
                throw new ServiceException(e.getMessage());
            }
        }
        
        /**************
         * FIXME add scheduler job for status check & confirmation
         **************/
        log.debug("IDC responded");
        return true;
    }
    
    
    private String getIDCResvStatus(String gri) throws ServiceException {
        QueryClient qcl = new QueryClient();
        qcl.configureSoap();
        ResDetails rd = null;
        try {
            rd = qcl.query(gri);
            log.info("OSCARS status for "+gri+" : "+rd.getStatus());
            return rd.getStatus();
        } catch (Exception e) {
            log.error(e);
            log.error("Error getting status: ");
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        }
    }
    
    public boolean localProvision(ProvisionRequestType prt) throws ServiceException {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }
    public boolean localRelease(ReleaseRequestType rrt) throws ServiceException {
        String connId = rrt.getRelease().getConnectionId();
        CancelClient cl = new CancelClient();
        cl.configureSoap();

        String gri = this.connIdToIdcGri.get(connId);
        if (gri == null) return false;
        log.info("starting idc cancel for gri: "+gri+" for connId "+connId);
        try {
            cl.cancel(gri);
        } catch (ClientException e) {
            e.printStackTrace();
            throw new ServiceException(e.getMessage());
        }
        
        return true;
    }
    public boolean localTerminate(TerminateRequestType trt) throws ServiceException {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }
    
}
