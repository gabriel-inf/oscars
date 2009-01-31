package net.es.oscars.ws;

import net.es.oscars.bss.OSCARSCore;
import net.es.oscars.bss.topology.Domain;
import net.es.oscars.bss.topology.DomainDAO;
import net.es.oscars.client.Client;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.interdomain.*;
import net.es.oscars.tss.*;
import net.es.oscars.wsdlTypes.*;

import org.apache.log4j.*;

import org.hibernate.*;

import java.rmi.RemoteException;

import java.util.HashMap;
import java.util.List;


/**
 * Intermediary between Axis2 and OSCARS libraries for topology exchange
 * requests
 *
 * All exceptions are passed back to OSCARSSkeleton, which logs them and maps
 * to the ADB classes that support SOAP faults.
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class TopologyExchangeAdapter {
    private Logger log;
    private TopologyExchangeManager tm;
    private OSCARSCore core;
    private String dbname;

    public TopologyExchangeAdapter() {
        this.log = Logger.getLogger(this.getClass());
        this.core = OSCARSCore.getInstance();
        this.dbname = this.core.getBssDbName();
        this.tm = new TopologyExchangeManager();
    }

    /**
     * Passes the content of an Axis2 getNetworkTopology request to a
     * TopologyManager for topology retrieval and returns a reponse for Axis2
     *
     * @param getTopoRequest the content of the getNetworkTopology request
     * @return the reponse content to be returned to the requester
     * @throws TSSException
     */
    public GetTopologyResponseContent getNetworkTopology(
        GetTopologyContent getTopoRequest) throws TSSException {
        GetTopologyResponseContent getTopoResponse = null;

        this.log.info("getNetworkTopology.start");
        getTopoResponse = this.tm.getNetworkTopology(getTopoRequest);
        this.log.info("getNetworkTopology.end");

        return getTopoResponse;
    }

    /**
     * Sends a given getNetworkTopology request using the given client.
     * It catches any exceptions and logs them as warnings.
     *
     * @param request the request to send
     * @param pullClient the TopologyPuller used to send the request
     * @return the response from the request, null if error occurred
     */
    private GetTopologyResponseContent pullTopology(
        GetTopologyContent request, TopologyPuller pullClient) {
        GetTopologyResponseContent response = null;

        try {
            response = pullClient.getNetworkTopology(request);
        } catch (AAAFaultMessage e) {
            response = null;
            this.log.warn("AAA Error: " + e.getMessage());
        } catch (BSSFaultMessage e) {
            response = null;
            this.log.warn("BSS Error: " + e.getMessage());
        } catch (RemoteException e) {
            response = null;
            this.log.warn("Remote Error: " + e.getMessage());
        } catch (Exception e) {
            response = null;
            this.log.warn("Exception: " + e.getMessage());
        }

        return response;
    }
}
