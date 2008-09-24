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
        this.core = OSCARSCore.getInstance();

        try {
            this.log.debug("Sleeping for 5 secs...");
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            this.log.error(ex);
        }

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


        HashMap<String, String> resvsToUpdate = new HashMap<String, String>();
        HashMap<String, String> resvDirection = new HashMap<String, String>();

        boolean allowLSP = true;


        boolean doCheck = true;
        boolean doSleep = false;

        while (doCheck) {
            HashMap<String, Boolean> results = new HashMap<String, Boolean>();


            // *************************************
            // should eventually be replaced by
            // results = LSP.checkStatus(nodeId, vlanList);
            // *************************************

            // Ask the routers if these VLANs are up
            if (vendor.equals("cisco")) {
                try {
                    net.es.oscars.pss.vendor.cisco.LSP ciscoLSP = new net.es.oscars.pss.vendor.cisco.LSP(core.getBssDbName());
                    allowLSP = ciscoLSP.isAllowLSP();
                    if (allowLSP) {
                        results.putAll(ciscoLSP.statusLSP(nodeId, vlanList));
                    }
                } catch (PSSException ex) {
                    this.log.error(ex);
                }
            } else {
                net.es.oscars.pss.vendor.jnx.JnxLSP jnxLSP = new net.es.oscars.pss.vendor.jnx.JnxLSP(core.getBssDbName());
                allowLSP = jnxLSP.isAllowLSP();
                // TODO: do the jnx case
            }

            ArrayList<String> checkedGris = new ArrayList<String>();

            // find out what vlan goes to which reservation
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
                        } else {
                            if (desiredStatus.equals(StateEngine.ACTIVE)) {
                                isPathUp = true;
                            } else {
                                isPathUp = false;
                            }
                        }
                        this.log.debug(jobName + ": ingress matched "+gri+" at "+nodeId+":"+ingressVlan+" isPathUp:"+isPathUp);
                        direction = "FORWARD";
                        resvDirection.put(gri, direction);
                    } else if (which.equals("egress")) {
                        if (allowLSP) {
                            isPathUp = results.get(egressVlan);
                        } else {
                            if (desiredStatus.equals(StateEngine.ACTIVE)) {
                                isPathUp = true;
                            } else {
                                isPathUp = false;
                            }
                        }
                        this.log.debug(jobName + ": egress matched "+gri+" at "+nodeId+":"+egressVlan+" isPathUp:"+isPathUp);
                        direction = "REVERSE";
                        resvDirection.put(gri, direction);
                    }
                    // OK, we have the vlan-gri mapping


                    if (!allowLSP) {
                        // always pretend everything went well
                        resvsToUpdate.put(gri, desiredStatus);
                    } else if (isPathUp) {
                        // if we're setting up the circuit:
                        if (!desiredStatus.equals(StateEngine.ACTIVE)) {
                            if (doCheck) {
                                // the first check
                                this.log.debug(gri+" path is up but desired status is "+desiredStatus+", will recheck");
                                doSleep = true;
                            } else {
                                // after 2nd check, path is still up even though we wanted to tear it down
                                this.log.error("Failing gri "+gri+" because path is up but desired status is "+desiredStatus);
                                resvsToUpdate.put(gri, StateEngine.FAILED);
                            }
                        } else {
                            // path is up as desired
                            this.log.debug("Making gri "+gri+" "+desiredStatus+" because path is up");
                            resvsToUpdate.put(gri, StateEngine.ACTIVE);
                            checkedGris.add(gri);
                        }
                    } else {
                        // if we're tearing down the circuit:
                        if (desiredStatus.equals(StateEngine.ACTIVE)) {
                            if (doCheck) {
                                // the first check
                                this.log.debug(gri+" path is down but desired status is "+desiredStatus+", will recheck");
                                doSleep = true;
                            } else {
                                // after 2nd check, path is down even though we wanted to set it up
                                this.log.debug("Failing gri "+gri+" because path is down but desired status is "+desiredStatus);
                                resvsToUpdate.put(gri, StateEngine.FAILED);
                            }

                        } else {
                            // path is down as desired
                            this.log.debug("Making gri "+gri+" "+desiredStatus+" because path is down");
                            resvsToUpdate.put(gri, desiredStatus);
                            checkedGris.add(gri);
                        }
                    }
                }
            }

            // don't recheck successfully checked reservations
            for (String gri : checkedGris) {
                checklist.remove(gri);
            }

            // if this is set it means at least one reservation was not found to be in the expected state
            // so sleep a bit then try again
            if (doSleep) {
                doSleep = false;
                // retry the check in 60 secs
                try {
                    this.log.debug("Sleeping for 60 secs...");
                    Thread.sleep(60000);
                } catch (InterruptedException ex) {
                    this.log.error(ex);
                }
            } else {
                // everything went well OR this is the 2nd time checking, don't do a 3rd
                doCheck = false;
            }
        }

        ReservationDAO resvDAO = new ReservationDAO(core.getBssDbName());

        // update reservation statuses, send out notifications
        Iterator<String> griIt = resvsToUpdate.keySet().iterator();
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
        PathSetupManager pe = core.getPathSetupManager();
        String gri = resv.getGlobalReservationId();

        if (operation.equals("PATH_SETUP")) {
            if (newStatus.equals(StateEngine.FAILED)) {
                eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, "", "JOB", resv);
            } else {
                String syncedStatus = VendorStatusSemaphore.syncStatusCheck(gri, "PATH_SETUP", direction);
                if (syncedStatus.equals("PATH_SETUP_BOTH")) {
                    this.log.debug("Updating status for gri:"+gri);
                    pe.updateCreateStatus(1, resv);
                } else {
                    this.log.debug("Not updating status for gri:"+gri);
                }
            }
        } else if (operation.equals("PATH_TEARDOWN")) {
            if (newStatus.equals(StateEngine.FAILED)) {
                eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FAILED, "", "JOB", resv);
            } else {
                String syncedStatus = VendorStatusSemaphore.syncStatusCheck(gri, "PATH_TEARDOWN", direction);
                if (syncedStatus.equals("PATH_TEARDOWN_BOTH")) {
                    this.log.debug("Updating status for gri:"+gri);
                    pe.updateTeardownStatus(1, resv);
                } else {
                    this.log.debug("Not updating status for gri:"+gri);
                }
            }
        }
   }

}