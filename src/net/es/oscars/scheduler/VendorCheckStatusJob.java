package net.es.oscars.scheduler;


import net.es.oscars.bss.*;
import net.es.oscars.pss.*;
import net.es.oscars.notify.*;
import net.es.oscars.oscars.OSCARSCore;

import java.util.*;

import org.apache.log4j.Logger;
import org.quartz.*;

public class VendorCheckStatusJob implements Job {
    private Logger log;
    private OSCARSCore core;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());

        this.core = OSCARSCore.getInstance();

        EventProducer eventProducer = new EventProducer();
        String jobName = context.getJobDetail().getFullName();
        this.log.debug("starting "+jobName);

        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String nodeId = (String) jobDataMap.get("nodeId");
        String vendor = (String) jobDataMap.get("vendor");
        ArrayList<String> vlanList = (ArrayList<String>) jobDataMap.get("vlanList");
        HashMap<String, HashMap<String, String>> checklist = (HashMap<String, HashMap<String, String>>) jobDataMap.get("checklist");

        HashMap<String, Boolean> results = new HashMap<String, Boolean>();
        if (vendor.equals("cisco")) {
            try {
                net.es.oscars.pss.cisco.LSP ciscoLSP = new net.es.oscars.pss.cisco.LSP(core.getBssDbName());
                results.putAll(ciscoLSP.statusLSP(nodeId, vlanList));
            } catch (PSSException ex) {
                this.log.error(ex);
            }
        } else {
            // TODO: do the jnx case
            return;
        }


        // *************************************
        // should eventually be replaced by
        // results = LSP.checkStatus(nodeId, vlanList);
        // *************************************



        HashMap<String, String> resvsToUpdate = new HashMap<String, String>();
        Iterator<String> griIt = checklist.keySet().iterator();
        while (griIt.hasNext()) {
            String gri = griIt.next();
            HashMap<String, String> params = checklist.get(gri);
            String ingressVlan = params.get("ingressVlan");
            String egressVlan = params.get("egressVlan");
            String ingressNodeId = params.get("ingressNodeId");
            String egressNodeId = params.get("egressNodeId");
            String desiredStatus = params.get("desiredStatus");
            String ingressVendor = params.get("ingressVendor");
            String egressVendor = params.get("egressVendor");

            String which = null;
            if (nodeId.equals(ingressNodeId)) {
                which = "ingress";
            } else if (nodeId.equals(egressNodeId)) {
                which = "egress";
            }
            if (which != null) {
                boolean isPathUp = false;
                if (which.equals("ingress")) {
                    isPathUp = results.get(ingressVlan);
                    this.log.debug(jobName + ": matched "+gri+" at "+nodeId+":"+ingressVlan);
                } else if (which.equals("egress")) {
                    isPathUp = results.get(egressVlan);
                    this.log.debug(jobName + ": matched "+gri+" at "+nodeId+":"+egressVlan);
                }
                if (isPathUp) {
                    if (!desiredStatus.equals(StateEngine.ACTIVE)) {
                        // path is still up even though we wanted to tear it down
                        resvsToUpdate.put(gri, StateEngine.FAILED);
                    } else {
                        // path is up as desired
                        resvsToUpdate.put(gri, StateEngine.ACTIVE);
                    }
                } else {
                    if (desiredStatus.equals(StateEngine.ACTIVE)) {
                        // path is down even though we wanted to set it up
                        resvsToUpdate.put(gri, StateEngine.FAILED);
                    } else {
                        // path is down as desired
                        resvsToUpdate.put(gri, desiredStatus);
                    }
                }
            }
        }
        StateEngine se = new StateEngine();
        ReservationDAO resvDAO = new ReservationDAO(core.getBssDbName());


        griIt = resvsToUpdate.keySet().iterator();
        while (griIt.hasNext()) {
            String gri = griIt.next();
            HashMap<String, String> params = checklist.get(gri);
            String operation = params.get("operation");


            String newStatus = resvsToUpdate.get(gri);
            try {
                Reservation resv = resvDAO.query(gri);
                se.updateStatus(resv, newStatus);

                if (operation.equals("PATH_SETUP")) {
                    if (newStatus.equals(StateEngine.FAILED)) {
                        eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, "", "JOB", resv);
                    } else {
                        eventProducer.addEvent(OSCARSEvent.PATH_SETUP_COMPLETED, "", "JOB", resv);
                    }
                } else if (operation.equals("PATH_TEARDOWN")) {
                    if (newStatus.equals(StateEngine.FAILED)) {
                        eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FAILED, "", "JOB", resv);
                    } else {
                        eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_COMPLETED, "", "JOB", resv);
                    }
                }


            } catch (BSSException ex) {
                this.log.error(ex);
            }
        }


        this.log.debug("finishing "+jobName);
    }

}