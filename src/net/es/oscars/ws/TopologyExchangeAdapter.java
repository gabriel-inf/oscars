package net.es.oscars.ws;

import net.es.oscars.tss.*;
import net.es.oscars.wsdlTypes.*;

import org.apache.log4j.*;

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

    public TopologyExchangeAdapter() {
        this.log = Logger.getLogger(this.getClass());
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
}
