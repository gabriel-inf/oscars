package net.es.oscars.scheduler;
import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.Path;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.pss.cisco.LSP;
import net.es.oscars.pss.jnx.JnxLSP;
import net.es.oscars.pss.*;
import net.es.oscars.oscars.*;
import net.es.oscars.notify.*;

import org.apache.log4j.Logger;
import org.hibernate.*;
import org.quartz.*;
import java.util.*;

public class VendorCreatePathJob extends ChainingJob  implements Job {
    private Logger log;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
        String jobName = context.getJobDetail().getFullName();
        this.log.debug("VendorCreatePathJob.start name: "+jobName);

        OSCARSCore core = OSCARSCore.getInstance();

        String bssDbName = core.getBssDbName();
        // Need to get our own Hibernate session since this is a new thread
        Session bss = core.getBssSession();
        bss.beginTransaction();

        ReservationDAO resvDAO = new ReservationDAO(bssDbName);


        EventProducer eventProducer = new EventProducer();
        String status;
        StateEngine stateEngine = new StateEngine();


        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String direction = (String) dataMap.get("direction");
        String routerType = (String) dataMap.get("routerType");
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
                eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, "", "JOB", resv, "", bssex.getMessage()+"\n"+ex.getMessage());
            }
            eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, "", "JOB", resv, "", ex.getMessage());
            this.runNextJob(context);
            return;
        }



        try {
            StateEngine.canUpdateStatus(resv, StateEngine.ACTIVE);
        } catch (BSSException ex) {
            this.log.error(ex);
            this.runNextJob(context);
            return;
        }


        String errString = "";
        boolean pathWasSetup = true;
        LSP ciscoLSP = null;
        JnxLSP jnxLSP = null;
        if (routerType.equals("jnx")) {
            jnxLSP = new JnxLSP(bssDbName);
            try {
                jnxLSP.createPath(resv, lspData, direction);
            } catch (PSSException ex) {
                this.log.error("Could not set up path!", ex);
                errString = ex.getMessage();
                pathWasSetup = false;
            }
        } else if (routerType.equals("cisco")) {
            ciscoLSP = new LSP(bssDbName);
            try {
                ciscoLSP.createPath(resv, lspData, direction);
            } catch (PSSException ex) {
                this.log.error("Could not set up path!", ex);
                errString = ex.getMessage();
                pathWasSetup = false;
            }
        }

        try {
            status = StateEngine.getStatus(resv);
            if (!pathWasSetup) {
                status = stateEngine.updateStatus(resv, StateEngine.FAILED);
                eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, "", "JOB", resv, "", errString);
            } else {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("desiredStatus", StateEngine.ACTIVE);
                params.put("operation", "PATH_SETUP");
                if (direction.equals("forward")) {
                    this.log.debug("setting forward status check params");
                    params.put("ingressNodeId", lspData.getIngressLink().getPort().getNode().getTopologyIdent());
                    params.put("ingressVlan", lspData.getVlanTag());
                    params.put("ingressVendor", routerType);
                } else if (direction.equals("reverse")) {
                    this.log.debug("setting reverse status check params");
                    params.put("egressNodeId", lspData.getEgressLink().getPort().getNode().getTopologyIdent());
                    params.put("egressVlan", lspData.getVlanTag());
                    params.put("egressVendor", routerType);
                }
                VendorMaintainStatusJob.addToCheckList(gri, params);
                /*
                Iterator<String> paramIt = VendorMaintainStatusJob.checklist.get(gri).keySet().iterator();
                while (paramIt.hasNext()) {
                    String key = paramIt.next();
                    this.log.debug("key: "+key+ " val: "+VendorMaintainStatusJob.checklist.get(gri).get(key));
               }
               */
            }
        } catch (BSSException ex) {
            this.log.error("State engine error", ex);
            eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, "", "JOB", resv, "", ex.getMessage());
        }

        bss.getTransaction().commit();

        this.runNextJob(context);

        this.log.debug("VendorCreatePathJob.end name: "+jobName);

    }

}
