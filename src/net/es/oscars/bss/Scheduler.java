package net.es.oscars.bss;

import java.util.*;
import java.io.*;
import java.lang.Throwable;
import javax.mail.MessagingException;

import org.apache.log4j.*;
import org.hibernate.*;

import net.es.oscars.*;
import net.es.oscars.bss.topology.*;
import net.es.oscars.pathfinder.*;
import net.es.oscars.pss.JnxLSP;
import net.es.oscars.pss.PSSException;

import net.es.oscars.database.HibernateUtil;
import net.es.oscars.database.Initializer;


/**
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 *
 * This class is only designed to be invoked from a standalone program
 */
public class Scheduler {
    private Logger log;
    private PCEManager pceMgr;
    private Notifier notifier;
    private ReservationManager rm;
    private Properties props;
    private String dbname;

    public Scheduler(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.pceMgr = new PCEManager(dbname);
        this.notifier = new Notifier();
        this.rm = new ReservationManager(dbname);
        this.dbname = dbname;
        PropHandler propHandler = new PropHandler("oscars.properties");
        this.props = propHandler.getPropertyGroup("pss", true);
    }

    /**
     * Handles setting up LSP's.
     *
     * @param timeInterval an integer with the time window to check 
     * @return response the list of reservation that are pending
     * @throws BSSException
     */
    public List<Reservation> pendingReservations(Integer timeInterval) 
            throws BSSException {

        List<Reservation> reservations = null;

        try {
            ReservationDAO dao = new ReservationDAO(this.dbname);
            reservations = dao.pendingReservations(timeInterval);
            for (Reservation resv: reservations) {
                // build hash map and call PSS to schedule LSP
                this.configurePSS(resv, "LSP_SETUP");
                resv.setStatus("ACTIVE");
                dao.update(resv);
                this.log.info("pendingReservations: " + resv.toString());
                String notification = this.pendingReservationMessage(resv);
                String subject = "Circuit set up";
                // this.notifier.sendMessage(subject, notification);
            }
        } catch (BSSException ex) {
            // log and rethrow
            this.log.error("pendingReservations.BSSException: " +
                    ex.getMessage());
            throw new BSSException(ex.getMessage());
        //} catch (javax.mail.MessagingException ex) {
            //throw new BSSException(ex.getMessage());
        }
        return reservations;
    }

    /**
     * Handles tearing down LSP's.
     *
     * @param timeInterval an integer with the time window to check 
     * @return response a list of reservations that have expired
     * @throws BSSException
     */
    public List<Reservation> expiredReservations(Integer timeInterval) 
            throws BSSException {

        List<Reservation> reservations = null;
        String prevStatus = null;
        String newStatus = null;

        try {
            ReservationDAO dao = new ReservationDAO(this.dbname);
            reservations = dao.expiredReservations(timeInterval);
            for (Reservation resv: reservations) {
                // build hash map and call PSS to schedule LSP
                this.configurePSS(resv, "LSP_TEARDOWN");
                prevStatus = resv.getStatus();
                if (!prevStatus.equals("PRECANCEL")) {
                    newStatus = "FINISHED";
                } else {
                    newStatus = "CANCELLED";
                    // set end time to cancel time
                    // useful in case reservation was persistent
                   long millis = System.currentTimeMillis();
                   resv.setEndTime(millis);
                }
                resv.setStatus(newStatus);
                dao.update(resv);
                this.log.info("expiredReservations: " + resv.toString());
                String notification = this.expiredReservationMessage(resv);
                String subject = "Circuit torn down";
                // this.notifier.sendMessage(subject, notification);
            }
        } catch (BSSException ex) {
            // log and rethrow
            this.log.error("expiredReservations.BSSException: " +
                    ex.getMessage());
            throw new BSSException(ex.getMessage());
        //} catch (javax.mail.MessagingException ex) {
            //throw new BSSException(ex.getMessage());
        }
        return reservations;
    }

// Private methods

    /**
     * Formats the args and calls pss to do the configuration change.
     *
     * @param resv a reservation instance
     * @param opstring a string indicating whether to do set up or tear down
     * @throws BSSException
     */
    private void configurePSS(Reservation resv, String opstring)
            throws BSSException {

        Map<String,String> lspInfo = null;
        // only used if an explicit path was given
        List<String> hops = null;
        Utils utils = new Utils(this.dbname);
        String lspFrom = null;
        String lspTo = null;
        Integer vlanTag = null;

        JnxLSP jnxLsp = new JnxLSP();
        String srcIP = this.rm.getIpAddress(resv.getSrcHost());
        String destIP = this.rm.getIpAddress(resv.getDestHost());

        // get path
        PathElem pathElem = resv.getPath().getPathElem();
        // find ingress and egress IP's
        while (pathElem != null) {
            if (pathElem.getDescription() != null) {
                if (pathElem.getDescription().equals("ingress")) {
                    lspFrom = pathElem.getIpaddr().getIP();
                } else if (pathElem.getDescription().equals("egress")) {
                    lspTo = pathElem.getIpaddr().getIP();
                }
            }
            pathElem = pathElem.getNextElem();
        }
        if (lspFrom == null) {
            throw new BSSException("no ingress loopback in path");
        }
        if (lspTo == null) {
            throw new BSSException("no egress loopback in path");
        }
        // Create an LSP object.
        lspInfo = new HashMap<String, String>();
        lspInfo.put("name", "oscars_" + resv.getId());
        lspInfo.put("from", lspFrom);
        lspInfo.put("to", lspTo);
        lspInfo.put("bandwidth", Long.toString(resv.getBandwidth()));
        lspInfo.put("lsp_class-of-service", resv.getLspClass());
        lspInfo.put("policer_burst-size-limit",
                Long.toString(resv.getBurstLimit()));
        lspInfo.put("source-address", srcIP);
        lspInfo.put("destination-address", destIP);

        Integer intParam = resv.getSrcIpPort();
        if (intParam != null) {
            lspInfo.put("source-port", Integer.toString(intParam));
        }
        intParam = resv.getSrcIpPort();
        if (intParam != null) {
            lspInfo.put("destination-port", Integer.toString(intParam));
        }
        String param = resv.getDscp();
        if (param != null) {
            lspInfo.put("dscp", param);
        }
        param = resv.getProtocol();
        if (param != null) {
            lspInfo.put("protocol", param);
        }
        param = resv.getDescription();
        if (param != null) {
            lspInfo.put("lsp_description", param);
        } else {
            lspInfo.put("lsp_description", "no description provided");
        }
        Vlan vlan = resv.getPath().getVlan();
        if (vlan != null) {
            lspInfo.put("vlanTag", vlan.getVlanTag());
        }

        lspInfo.put("login", this.props.getProperty("login"));
        // TODO:  error checking
        lspInfo.put("router", utils.getHostName(lspFrom));
        String keyfile = System.getenv("CATALINA_HOME") +
                             "/shared/oscars.conf/server/oscars.key";
        lspInfo.put("keyfile", keyfile);
        lspInfo.put("passphrase", this.props.getProperty("passphrase"));

        lspInfo.put("lsp_setup-priority",
             this.props.getProperty("lsp_setup-priority"));
        lspInfo.put("lsp_reservation-priority",
             this.props.getProperty("lsp_reservation-priority"));
        lspInfo.put("internal_interface_filter",
             this.props.getProperty("internal_interface_filter"));
        lspInfo.put("external_interface_filter",
             this.props.getProperty("external_interface_filter"));
        lspInfo.put("firewall_filter_marker", 
             this.props.getProperty("firewall_filter_marker"));

        // Additional information from the template will be used if
        // an explicit path was given.
        if (resv.getPath().isExplicit()) {
            hops = new ArrayList<String>();
            while (pathElem != null) {
                hops.add(pathElem.getIpaddr().getIP());
                pathElem = pathElem.getNextElem();
            }
        }

        // call PSS to schedule LSP
        try {
            if (opstring.equals("LSP_SETUP")) {
                jnxLsp.setupLSP(lspInfo, hops);
            } else {
                jnxLsp.teardownLSP(lspInfo);
            }
        } catch (PSSException ex) {
            throw new BSSException(ex.getMessage());
        }
    }

    /*
     * Notification message methods.  For lack of a better pattern at the
     * moment.  Configuration with unordered properties was not a solution.
     */

    /**
     * Returns a description of the pending reservation suitable for email.
     * @param resv a reservation instance
     * @return a String describing the pending reservation
     */
    public String pendingReservationMessage(Reservation resv) {

        String msg = "Reservation: " + resv.toString() + "\n";
        return msg;
    }

    /**
     * Returns a description of the expired reservation suitable for email.
     * @param resv a reservation instance
     * @return a String describing the expired reservation
     */
    public String expiredReservationMessage(Reservation resv) {

        String msg = "Reservation: " + resv.toString() + "\n";
        return msg;
    }
}
