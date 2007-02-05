package net.es.oscars.bss;

import java.util.*;
import javax.mail.MessagingException;
import org.hibernate.*;

import net.es.oscars.LogWrapper;
import net.es.oscars.Notifier;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.bss.topology.*;
import net.es.oscars.pss.JnxLSP;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 */
public class Scheduler {
    private LogWrapper log;
    private PathManager pathMgr;
    private Notifier notifier;
    private ReservationManager rm;

    public Scheduler() {
        this.log = new LogWrapper(this.getClass());
        this.pathMgr = new PathManager();
        this.notifier = new Notifier();
        this.rm = new ReservationManager();
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

        Session session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();

        ReservationDAO dao = new ReservationDAO();

        dao.setSession(session);

        reservations = dao.pendingReservations(timeInterval);
        for (Reservation r: reservations) {
            // build hash map and call PSS to schedule LSP
            this.configurePSS(r, "LSP_SETUP");
            Integer id = r.getId().intValue();
            dao.updateStatus(id, "ACTIVE");
            String notification = this.pendingReservationMessage(r);
            String subject = "Circuit set up";
            /*
            try {
                this.notifier.sendMessage(subject, notification);
            } catch (javax.mail.MessagingException e) {
                throw new BSSException(e.getMessage());
            }
            */
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

        ReservationDAO dao = new ReservationDAO();
        Session session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        dao.setSession(session);
        reservations = dao.expiredReservations(timeInterval);

        for (Reservation r: reservations) {
            // build hash map and call PSS to schedule LSP
            this.configurePSS(r, "LSP_TEARDOWN");
            Integer id = r.getId().intValue();
            dao.updateStatus(id, "FINISHED");
            String notification = this.expiredReservationMessage(r);
            String subject = "Circuit torn down";
            /*
            try {
                this.notifier.sendMessage(subject, notification);
            } catch (javax.mail.MessagingException e) {
                throw new BSSException(e.getMessage());
            }
            */
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

        IpaddrDAO ipaddrDAO = null;
        Map<String,String> lspInfo = null;
        List<Ipaddr> hops = null;
        Path path = null;

        Session session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        JnxLSP jnxLsp = new JnxLSP();
        ipaddrDAO = new IpaddrDAO();
        ipaddrDAO.setSession(session);
        path = resv.getPath();
        String ingressLoopback = this.getLoopback(path, "ingress");
        String egressLoopback = this.getLoopback(path, "egress");
        // Egress loopback allowed to be non-MPLS
        if (egressLoopback == null) {
            hops = this.pathMgr.getIpaddrs(path);
            egressLoopback = hops.get(hops.size()-1).getIp();
        }

        // Create an LSP object.
        lspInfo = new HashMap<String, String>();
        lspInfo.put("user_var_name_user_var", "oscars_" + resv.getId());
        lspInfo.put("user_var_lsp_from_user_var", ingressLoopback);
        lspInfo.put("user_var_lsp_to_user_var", egressLoopback);
        lspInfo.put("user_var_bandwidth_user_var", Long.toString(resv.getBandwidth()));
        lspInfo.put("user_var_lsp_class-of-service_user_var", resv.getLspClass());
        lspInfo.put("user_var_policer_burst-size-limit_user_var", Long.toString(resv.getBurstLimit()));


        InetAddress srcAddr = null;
        InetAddress dstAddr = null;
        try { 
            srcAddr = InetAddress.getByName( resv.getSrcHost() );
            dstAddr = InetAddress.getByName( resv.getDestHost() );
        } catch  ( UnknownHostException e) {
            System.out.println("Unknown host " + resv.getSrcHost() + " or " + 
                    resv.getDestHost());
        }
/*
        lspInfo.put("user_var_source-address_user_var", resv.getSrcHost());
        lspInfo.put("user_var_destination-address_user_var", resv.getDestHost());
*/
        lspInfo.put("user_var_source-address_user_var", srcAddr.getHostAddress());
        lspInfo.put("user_var_destination-address_user_var", dstAddr.getHostAddress());

        Integer intParam = resv.getSrcPort();
        if (intParam != null) {
            lspInfo.put("user_var_source-port_user_var", Integer.toString(intParam));
        }
        intParam = resv.getSrcPort();
        if (intParam != null) {
            lspInfo.put("user_var_destination-port_user_var", Integer.toString(intParam));
        }
        String param = resv.getDscp();
        if (param != null) {
            lspInfo.put("user_var_dscp_user_var", param);
        } else {
            lspInfo.put("user_var_dscp_user_var", "4");
        }
        param = resv.getProtocol();
        if (param != null) {
            lspInfo.put("user_var_protocol_user_var", param);
        }
        param = resv.getDescription();
        if (param != null) {
            lspInfo.put("user_var_lsp_description_user_var", param);
        } else {
            lspInfo.put("user_var_lsp_description_user_var", "no description provided");
        }

        // TODO:  check values
        lspInfo.put("user", "jason");
        lspInfo.put("host", "dev-m20-rt1.es.net");
        String keyfile = System.getenv("OSCARS_HOME") + "/conf/private/server/pss_key";
        lspInfo.put("keyfile", keyfile);
        lspInfo.put("passphrase", "passphrase");

        lspInfo.put("user_var_lsp_setup-priority_user_var", "4");
        lspInfo.put("user_var_lsp_reservation-priority_user_var", "4");
        lspInfo.put("user_var_external_interface_filter_user_var",
                    "external-interface-inbound-inet.0-filter");
        lspInfo.put("user_var_firewall_filter_marker_user_var", 
                    "oscars-filters-start");

        // call PSS to schedule LSP
        this.log.info("configurePSS." + opstring + ".start", resv.toString());
        if (opstring.equals("LSP_SETUP")) {
            jnxLsp.setupLSP(lspInfo);
        } else {
            jnxLsp.teardownLSP(lspInfo);
        }
        this.log.info("configurePSS." + opstring + ".finish", lspInfo.toString());
    }

    /**
     * Gets loopback IP, given beginning path instance, and loopback type
     * @param path beginning path instance
     * @param loopbackType string, either "ingress" or "egress"
     * @return ingressLoopback string with the ingress loopback IP, if any 
     */
    public String getLoopback(Path path, String loopbackType) {

        Ipaddr ipaddr = null;

        Session session = 
            HibernateUtil.getSessionFactory("bss").getCurrentSession();
        IpaddrDAO ipaddrDAO = new IpaddrDAO();
        ipaddrDAO.setSession(session);
        while (path != null) {
            String addressType = path.getAddressType();
            if (loopbackType.equals(addressType)) {
                ipaddr = path.getIpaddr();
                return ipaddr.getIp();
            }
            path = path.getNextPath();
        }
        return null;
    }

    /*
     * Notification message methods.  For lack of a better pattern at the
     * moment.  Configuration with unordered properties was not a solution.
     */

    /**
     * Returns a description of the pending reservation suitable for email.
     * @param reservation a reservation instance
     * @return a String describing the pending reservation
     */
    public String pendingReservationMessage(Reservation resv) {
        String msg = "";

        this.rm.setSession();
        msg += "Reservation tag: " + this.rm.toTag(resv) + "\n";
        //msg += "Path set up time: " + resv.getLspConfigTime() + "\n";
        msg += "Reservation status: " + resv.getStatus() + "\n";
        return msg;
    }

    /**
     * Returns a description of the expired reservation suitable for email.
     * @param reservation a reservation instance
     * @return a String describing the expired reservation
     */
    public String expiredReservationMessage(Reservation resv) {
        String msg = "";

        this.rm.setSession();
        msg += "Reservation tag: " + this.rm.toTag(resv) + "\n";
        //msg += "Path tear down time: " + resv.getLspConfigTime() + "\n";
        msg += "Reservation status: " + resv.getStatus() + "\n";
        return msg;
    }
}
