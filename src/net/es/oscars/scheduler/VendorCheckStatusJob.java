package net.es.oscars.scheduler;

import net.es.oscars.bss.*;
import net.es.oscars.pss.*;
import net.es.oscars.pss.vendor.*;
import net.es.oscars.notify.*;
import net.es.oscars.oscars.OSCARSCore;

import java.util.*;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.quartz.*;

public class VendorCheckStatusJob implements Job {
    private Logger log;
    private OSCARSCore core;

    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        this.log = Logger.getLogger(this.getClass());
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
        HashMap<String,VendorStatusInput> statusInputs =
            (HashMap<String,VendorStatusInput>) jobDataMap.get("statusInputs");
        String theList = "vlans to be checked: ";
        for (String vlan : statusInputs.keySet()) {
            theList = theList + vlan + " ";
        }
        this.log.debug(theList);
        // default timeout is for Juniper
        int timeout = 30000;
        if (vendor.equals("cisco")) {
            timeout = 5000;
        }
        try {
            this.log.debug("Sleeping for " + timeout + " secs...");
            Thread.sleep(timeout);
        } catch (InterruptedException ex) {
            this.log.error(ex);
        }
        boolean allowLSP = true;
        boolean doCheck = true;
        boolean doSleep = false;
        // allow to fail once
        for (int checkCtr=0; checkCtr < 2; checkCtr++) {
            HashMap<String, VendorStatusResult> results =
                new HashMap<String, VendorStatusResult>();

            // Ask the routers if these VLANs are up
            if (vendor.equals("cisco")) {
                try {
                    net.es.oscars.pss.vendor.cisco.LSP ciscoLSP =
                        new net.es.oscars.pss.vendor.cisco.LSP(core.getBssDbName());
                    allowLSP = ciscoLSP.isAllowLSP();
                    if (allowLSP) {
                        results.putAll(ciscoLSP.statusLSP(nodeId, statusInputs));
                    }
                } catch (PSSException ex) {
                    this.log.error(ex);
                    // TODO:  mark all as error
                }
            } else {
                try {
                    net.es.oscars.pss.vendor.jnx.JnxLSP jnxLSP = new net.es.oscars.pss.vendor.jnx.JnxLSP(core.getBssDbName());
                    allowLSP = jnxLSP.isAllowLSP();
                    if (allowLSP) {
                        results.putAll(jnxLSP.statusLSP(nodeId, statusInputs));
                    }
                } catch (PSSException ex) {
                    this.log.error(ex);
                    // TODO:  mark all as error
                }
            }
            ArrayList<String> checkedVlans = new ArrayList<String>();

            // find out what vlan goes to which reservation
            for (String vlanId: statusInputs.keySet()) {
                VendorStatusInput statusInput = statusInputs.get(vlanId);
                VendorStatusResult statusResult = results.get(vlanId);
                String gri = statusInput.getGri();
                String direction = statusInput.getDirection();
                String desiredStatus = statusInput.getDesiredStatus();
                if (direction.equals("FORWARD")) {
                    this.log.debug(jobName + ": ingress matched "+gri+" at "+nodeId);
                } else if (direction.equals("REVERSE")) {
                    this.log.debug(jobName + ": egress matched "+gri+" at "+nodeId);
                } else {
                    continue;  // TODO:  error message
                }
                boolean isPathUp = false;
                if (allowLSP) {
                    isPathUp = statusResult.isCircuitUp();
                } else {
                    if (desiredStatus.equals(StateEngine.ACTIVE)) {
                        isPathUp = true;
                    } else {
                        isPathUp = false;
                    }
                }
                this.log.debug(jobName + ": in " + direction + " direction" +
                        gri+ " at " + nodeId+":" + vlanId + " isPathUp:"+
                        isPathUp);
                String errMsg = statusResult.getErrorMessage();
                if (!errMsg.equals("")) {
                    this.log.debug("error in circuit setup: " + errMsg);
                }
                if (!allowLSP) {
                    // always pretend everything went well
                    statusResult = null;
                    this.updateReservation(statusInput, statusResult,
                                           desiredStatus);
                // if there was an error, trumps circuit status
                } else if (!errMsg.equals("")) {
                    if (checkCtr == 0) {
                        // the first check
                        this.log.debug(gri+" has error message: " + errMsg +
                                ", will recheck");
                        doSleep = true;
                    } else {
                        // after 2nd check, path is still up even though we wanted to tear it down
                        this.log.error("Failing gri "+ gri +
                            "because of error message: " + errMsg);
                        this.updateReservation(statusInput, statusResult,
                                               StateEngine.FAILED);
                    }
                } else if (isPathUp) {
                    // if we're setting up the circuit:
                    if (!desiredStatus.equals(StateEngine.ACTIVE)) {
                        if (checkCtr == 0) {
                            // the first check
                            this.log.debug(gri + " path is up but desired " +
                                "status is " + desiredStatus +
                                ", will recheck");
                            doSleep = true;
                        } else {
                            // after 2nd check, path is still up even though we
                            // wanted to tear it down
                            this.log.error("Failing gri " + gri +
                                " because path is up but desired status is " +
                                desiredStatus);
                            this.updateReservation(statusInput, statusResult,
                                                   StateEngine.FAILED);
                        }
                    } else {
                        // path is up as desired
                        this.log.debug("Making gri " + gri +
                            " " + desiredStatus + " because path is up");
                        this.updateReservation(statusInput, statusResult,
                                               StateEngine.ACTIVE);
                        checkedVlans.add(vlanId);
                    }
                } else {
                    // if we're tearing down the circuit:
                    if (desiredStatus.equals(StateEngine.ACTIVE)) {
                        if (checkCtr == 0) {
                            // the first check
                            this.log.debug(gri +
                                " path is down but desired status is " +
                                desiredStatus + ", will recheck");
                            doSleep = true;
                        } else {
                            // after 2nd check, path is down even though we
                            // wanted to set it up
                            this.log.debug("Failing gri " + gri +
                                " because path is down but desired status is "+
                                desiredStatus);
                            this.updateReservation(statusInput,
                                              statusResult, StateEngine.FAILED);
                        }
                    } else {
                        // path is down as desired
                        this.log.debug("Making gri " + gri + " " +
                            desiredStatus + " because path is down");
                        this.updateReservation(statusInput, statusResult,
                                               desiredStatus);
                        checkedVlans.add(vlanId);
                    }
                }
            }
            // don't recheck successfully checked reservations
            for (String vlan : checkedVlans) {
                this.log.debug("removing statusInput for vlan: " + vlan);
                statusInputs.remove(vlan);
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
                // everything went well OR this is the 2nd time checking
                break;
            }
        }
        bss.getTransaction().commit();
        this.log.debug("checkStatusJob.end "+jobName);
    }

    private void updateReservation(VendorStatusInput input,
                                   VendorStatusResult result,
                                   String newStatus) {
        ReservationDAO resvDAO = new ReservationDAO(core.getBssDbName());
        String gri = input.getGri();
        String operation = input.getOperation();
        String direction = input.getDirection();
        try {
            Reservation resv = resvDAO.query(gri);
            if (result != null) {
                this.sendNotification(resv, newStatus, operation, direction,
                                      result.getErrorMessage());
            } else {
                this.sendNotification(resv, newStatus, operation, direction, "");
            }
        } catch (BSSException ex) {
            this.log.error(ex);
        }
    }

    private synchronized void sendNotification(Reservation resv,
        String newStatus, String operation, String direction, String errMsg)
            throws BSSException {
        EventProducer eventProducer = new EventProducer();
        PathSetupManager pe = core.getPathSetupManager();
        String gri = resv.getGlobalReservationId();
        StateEngine stateEngine = this.core.getStateEngine();

        if (operation.equals("PATH_SETUP")) {
            if (newStatus.equals(StateEngine.FAILED)) {
                String status =
                    stateEngine.updateStatus(resv, StateEngine.FAILED);
                eventProducer.addEvent(OSCARSEvent.PATH_SETUP_FAILED, "", "JOB", resv, "", errMsg);
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
                String status =
                    stateEngine.updateStatus(resv, StateEngine.FAILED);
                eventProducer.addEvent(OSCARSEvent.PATH_TEARDOWN_FAILED, "", "JOB", resv, "", errMsg);
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
