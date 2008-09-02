package net.es.oscars.scheduler;
import java.util.HashMap;

import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.pss.cisco.LSP;
import net.es.oscars.pss.jnx.JnxLSP;
import net.es.oscars.pss.*;
import net.es.oscars.oscars.*;
import net.es.oscars.notify.*;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.*;

public class VendorTeardownPathJob extends ChainingJob  implements Job {
    private Logger log;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        this.log.debug("VendorTeardownPathJob.start name:"+context.getJobDetail().getFullName());

        OSCARSCore core = OSCARSCore.getInstance();

        String bssDbName = core.getBssDbName();
        Session bss = core.getBssSession();
        bss.beginTransaction();

        ReservationDAO resvDAO = new ReservationDAO(bssDbName);

        EventProducer eventProducer = new EventProducer();
        String status;
        StateEngine stateEngine = core.getStateEngine();


        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String direction = (String) dataMap.get("direction");
        String routerType = (String) dataMap.get("routerType");
        String newStatus = (String) dataMap.get("newStatus");
        String gri = (String) dataMap.get("gri");
        Reservation resv = null;
        try {
            resv = resvDAO.query(gri);
        } catch (BSSException ex) {
            this.log.error("Could not locate reservation in DB for gri: "+gri);
            this.runNextJob(context);
            return;
        }



        LSPData lspData = new LSPData(bssDbName);
        Path path = resv.getPath();
        try {
            lspData.setPathVars(path.getPathElem());
        } catch (PSSException ex) {
            this.log.error(ex);
            try {
                status = stateEngine.updateStatus(resv, StateEngine.FAILED);
            } catch (BSSException bssex) {
                this.log.error("State engine error", bssex);
                eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FAILED, "", "JOB", resv, "", bssex.getMessage()+"\n"+ex.getMessage());
            }
            eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FAILED, "", "JOB", resv, "", ex.getMessage());
            this.runNextJob(context);
            return;
        }




        try {
            StateEngine.canUpdateStatus(resv, newStatus);
        } catch (BSSException ex) {
            this.log.error(ex);
            this.runNextJob(context);
            return;
        }

        String errString = "";
        boolean pathWasTornDown = true;
        LSP ciscoLSP = null;
        JnxLSP jnxLSP = null;
        if (routerType.equals("jnx")) {
            jnxLSP = new JnxLSP(bssDbName);
            try {
                jnxLSP.teardownPath(resv, lspData, direction);
            } catch (PSSException ex) {
                pathWasTornDown = false;
                this.log.error("Could not set up path", ex);
                errString = ex.getMessage();
            }
        } else if (routerType.equals("cisco")) {
            ciscoLSP = new LSP(bssDbName);
            try {
                ciscoLSP.teardownPath(resv, lspData, direction);
            } catch (PSSException ex) {
                pathWasTornDown = false;
                this.log.error("Could not set up path", ex);
                errString = ex.getMessage();
            }
        }

        try {
            status = StateEngine.getStatus(resv);
            if (!pathWasTornDown) {
                status = stateEngine.updateStatus(resv, StateEngine.FAILED);
                eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FAILED, "", "JOB", resv, "", errString);
            } else {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("desiredStatus", newStatus);
                params.put("operation", "PATH_TEARDOWN");
                if (direction.equals("forward")) {
                    params.put("ingressNodeId", lspData.getIngressLink().getPort().getNode().getTopologyIdent());
                    params.put("ingressVlan", lspData.getVlanTag());
                    params.put("ingressVendor", routerType);
                } else if (direction.equals("reverse")) {
                    params.put("egressNodeId", lspData.getEgressLink().getPort().getNode().getTopologyIdent());
                    params.put("egressVlan", lspData.getVlanTag());
                    params.put("egressVendor", routerType);
                }
                VendorMaintainStatusJob.addToCheckList(gri, params);
            }
        } catch (BSSException ex) {
            this.log.error("State engine error", ex);
            eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FAILED, "", "JOB", resv, "", ex.getMessage());
        }


        bss.getTransaction().commit();

        this.runNextJob(context);
        this.log.debug("VendorTeardownPathJobs.end");

    }

}
