package net.es.oscars.bss.policy;

import java.util.*;

import org.apache.log4j.*;

import net.es.oscars.bss.*;
import net.es.oscars.bss.topology.PathType;

import java.util.Properties;
import net.es.oscars.PropHandler;

/**
 * This class contains methods for handling reservation setup policy
 *
 * @author David Robertson (dwrobertson@lbl.gov), Jason Lee (jrlee@lbl.gov)
 */
public class PolicyManager {
    private Logger log;
    private String dbname;
    private PolicyClient policyClient;
    private String vlanFilter;

    public PolicyManager(String dbname) {
        this.log = Logger.getLogger(this.getClass());
        this.dbname = dbname;
        PropHandler propHandler = new PropHandler("oscars.properties");
        Properties props = propHandler.getPropertyGroup("policy", true);
        this.vlanFilter = props.getProperty("vlanFilter");
        if (this.vlanFilter == null){
            this.vlanFilter = "vlanMap";
        }
        this.log.debug("VLAN filter: "+this.vlanFilter);
        try {
            this.policyClient = new PolicyClient(props);
        } catch (BSSException e) {
            this.log.error("Unable to create policy client:" + e.getMessage());
        }
    }

    /**
     * Checks whether adding this reservation would cause oversubscription
     * on a port.
     *
     * @param activeReservations existing reservations
     * @param newReservation new reservation instance
     * @throws BSSException
     */
    public void checkOversubscribed(List<Reservation> activeReservations,
               Reservation newReservation) throws BSSException {
        this.log.info("checkOversubscribed.start");
        
        BandwidthFilter bwf = new BandwidthFilter();
        bwf.applyFilter(newReservation, activeReservations);
        
        if(newReservation.getPath(PathType.LOCAL).isLayer2()){
            PolicyFilter vlf = PolicyFilterFactory.create(this.vlanFilter);
            vlf.applyFilter(newReservation, activeReservations);
        }
        
        this.log.info("checkOversubscribed.end");
    }

    /**
     * @return the policyClient
     */
    public PolicyClient getPolicyClient() {
        return this.policyClient;
    }

    /**
     * @param policyClient the policyClient to set
     */
    public void setPolicyClient(PolicyClient policyClient) {
        this.policyClient = policyClient;
    }
}
