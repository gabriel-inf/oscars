package net.es.oscars.scheduler;


import net.es.oscars.bss.*;
import net.es.oscars.pss.*;
import net.es.oscars.notify.*;
import net.es.oscars.oscars.OSCARSCore;

import java.util.*;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.*;

public class VendorCheckStatusJob implements Job {
    private Logger log;
    private OSCARSCore core;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());

        // only check status 5 sec after setup is complete!
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            this.log.error(ex);
        }

        this.core = OSCARSCore.getInstance();

        String bssDbName = core.getBssDbName();
        // Need to get our own Hibernate session since this is a new thread
        Session bss = core.getBssSession();
        bss.beginTransaction();

        String jobName = context.getJobDetail().getFullName();
        this.log.debug("checkStatusJob.start "+jobName);

        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        String nodeId = (String) jobDataMap.get("nodeId");
        String vendor = (String) jobDataMap.get("vendor");
        this.log.debug(jobName + " for node: "+nodeId+ " vendor: "+vendor);

        ArrayList<String> vlanList = (ArrayList<String>) jobDataMap.get("vlanList");
        String theList = "vlans to be checked: ";
        for (String vlan : vlanList) {
            theList = theList + vlan + " ";
        }
        this.log.debug(theList);
        HashMap<String, HashMap<String, String>> checklist = (HashMap<String, HashMap<String, String>>) jobDataMap.get("checklist");

        HashMap<String, Boolean> results = new HashMap<String, Boolean>();
        boolean allowLSP = true;
        if (vendor.equals("cisco")) {
            try {
                net.es.oscars.pss.cisco.LSP ciscoLSP = new net.es.oscars.pss.cisco.LSP(core.getBssDbName());
                allowLSP = ciscoLSP.isAllowLSP();
                if (allowLSP) {
                    results.putAll(ciscoLSP.statusLSP(nodeId, vlanList));
                }
            } catch (PSSException ex) {
                this.log.error(ex);
            }
        } else {
            net.es.oscars.pss.jnx.JnxLSP jnxLSP = new net.es.oscars.pss.jnx.JnxLSP(core.getBssDbName());
            allowLSP = jnxLSP.isAllowLSP();
            // TODO: do the jnx case
        }


        // *************************************
        // should eventually be replaced by
        // results = LSP.checkStatus(nodeId, vlanList);
        // *************************************



        HashMap<String, String> resvsToUpdate = new HashMap<String, String>();
        HashMap<String, String> resvDirection = new HashMap<String, String>();
        Iterator<String> griIt = checklist.keySet().iterator();
        while (griIt.hasNext()) {
            String gri = griIt.next();
            HashMap<String, String> params = checklist.get(gri);
            String ingressVlan 		= params.get("ingressVlan");
            String egressVlan 		= params.get("egressVlan");
            String ingressNodeId 	= params.get("ingressNodeId");
            String egressNodeId 	= params.get("egressNodeId");
            String desiredStatus 	= params.get("desiredStatus");
            String direction;

            String which = null;
            if (nodeId.equals(ingressNodeId)) {
                which = "ingress";
                this.log.debug(jobName + ": ingress matched "+gri+" at "+nodeId);
            } else if (nodeId.equals(egressNodeId)) {
                which = "egress";
                this.log.debug(jobName + ": egress matched "+gri+" at "+nodeId);
            }

            if (which != null) {
                boolean isPathUp = false;
                if (which.equals("ingress")) {
                    if (allowLSP) {
                        isPathUp = results.get(ingressVlan);
                    }
                    this.log.debug(jobName + ": ingress matched "+gri+" at "+nodeId+":"+ingressVlan+" isPathUp:"+isPathUp);
                    direction = "DOWN";
                    resvDirection.put(gri, direction);
                } else if (which.equals("egress")) {
                    if (allowLSP) {
                        isPathUp = results.get(egressVlan);
                    }
                    this.log.debug(jobName + ": egress matched "+gri+" at "+nodeId+":"+egressVlan+" isPathUp:"+isPathUp);
                    direction = "UP";
                    resvDirection.put(gri, direction);
                }
                if (!allowLSP) {
                    resvsToUpdate.put(gri, desiredStatus);
                } else if (isPathUp) {
                    if (!desiredStatus.equals(StateEngine.ACTIVE)) {
                        // path is still up even though we wanted to tear it down
                        this.log.debug("failing gri "+gri+" because path is up but desired status is "+desiredStatus);
                        resvsToUpdate.put(gri, StateEngine.FAILED);
                    } else {
                        // path is up as desired
                        this.log.debug("making gri "+gri+" "+desiredStatus+" because path is up");
                        resvsToUpdate.put(gri, StateEngine.ACTIVE);
                    }
                } else {
                    if (desiredStatus.equals(StateEngine.ACTIVE)) {
                        // path is down even though we wanted to set it up
                        this.log.debug("failing gri "+gri+" because path is down but desired status is "+desiredStatus);
                        resvsToUpdate.put(gri, StateEngine.FAILED);
                    } else {
                        // path is down as desired
                        this.log.debug("making gri "+gri+" "+desiredStatus+" because path is down");
                        resvsToUpdate.put(gri, desiredStatus);
                    }
                }
            }
        }
        ReservationDAO resvDAO = new ReservationDAO(core.getBssDbName());


        griIt = resvsToUpdate.keySet().iterator();
        while (griIt.hasNext()) {
            String gri = griIt.next();
            HashMap<String, String> params = checklist.get(gri);
            String operation = params.get("operation");


            String newStatus = resvsToUpdate.get(gri);
            String direction = resvDirection.get(gri);
            try {
                Reservation resv = resvDAO.query(gri);
                this.sendNotification(resv, newStatus, operation, direction);

            } catch (BSSException ex) {
                this.log.error(ex);
            }
        }

        bss.getTransaction().commit();

        this.log.debug("checkStatusJob.end "+jobName);
    }

    private synchronized void sendNotification(Reservation resv, String newStatus, String operation, String direction) throws BSSException {
        EventProducer eventProducer = new EventProducer();
        StateEngine se = core.getStateEngine();
        PathSetupManager pe = core.getPathSetupManager();
        String status = StateEngine.getStatus(resv);
        se.updateStatus(resv, newStatus);
        String gri = resv.getGlobalReservationId();

        if (operation.equals("PATH_SETUP")) {
            if (newStatus.equals(StateEngine.FAILED)) {
                eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, "", "JOB", resv);
            } else if (status.equals(newStatus)) {
                String syncedStatus = VendorStatusSemaphore.syncStatusCheck(gri, "PATH_SETUP", direction);
                if (syncedStatus.equals("PATH_SETUP_BOTH")) {
                    pe.updateCreateStatus(1, resv);
                }
            }
        } else if (operation.equals("PATH_TEARDOWN")) {
            if (newStatus.equals(StateEngine.FAILED)) {
                eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FAILED, "", "JOB", resv);
            } else {
                String syncedStatus = VendorStatusSemaphore.syncStatusCheck(gri, "PATH_TEARDOWN", direction);
                if (syncedStatus.equals("PATH_TEARDOWN_BOTH")) {
                    pe.updateTeardownStatus(1, resv);
                }
            }
        }
   }

}