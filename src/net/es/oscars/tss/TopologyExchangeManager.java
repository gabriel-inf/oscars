package net.es.oscars.tss;

import net.es.oscars.*;
import net.es.oscars.wsdlTypes.*;

import org.apache.log4j.*;

import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneTopologyContent;

import java.util.*;


/**
 * Handles topology retrievals and insertions into a topology database (TEDB)
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class TopologyExchangeManager {
    private Logger log;
    private TEDB tedb;
    private Properties props;

    /**
     * Constructor that initalizes logs and the TEDB method specified by the
     * tedb.tedbMethod property in the oscars.properties file.
     */
    public TopologyExchangeManager() {
        TEDBFactory tedbFactory = new TEDBFactory();
        PropHandler propHandler = new PropHandler("oscars.properties");
        String tedbMethod;

        this.log = Logger.getLogger(this.getClass());
        this.props = propHandler.getPropertyGroup("tedb", true);
        tedbMethod = this.props.getProperty("tedbMethod").trim(); // throw away whitespace
        this.log.info("TEDB method: "+tedbMethod);
        this.tedb = tedbFactory.createTEDB(tedbMethod);

    }

    /* Retrieves topology from the TEDB using the method specified by the
     * tedb.tedbMethod property in the oscars.properties file.
     *
     * @param content of a getNetworkTopologyRequest
     * @return Axis2 object with requested topology from the TEDB
     * @throws TSSException
     */
    public GetTopologyResponseContent getNetworkTopology(
        GetTopologyContent getTopoRequest) throws TSSException {
        this.log.info("getNetworkTopology.start");

        GetTopologyResponseContent getTopoResponse = new GetTopologyResponseContent();
        CtrlPlaneTopologyContent topology = null;
        String requestType = getTopoRequest.getTopologyType();

        topology = this.tedb.selectNetworkTopology(requestType);
        getTopoResponse.setTopology(topology);

        this.log.info("getNetworkTopology.end");

        return getTopoResponse;
    }

    /**
     * Inserts topology into a TEDB using the method specified by the
     * tedb.tedbMethod property in the oscars.properties file.
     *
     * @param topoResponse the reponse from getNetworkTopology to be inserted
     * @throws TSSException
     */
    public void insertNetworkTopology(GetTopologyResponseContent topoResponse)
        throws TSSException {
        CtrlPlaneTopologyContent topology = topoResponse.getTopology();

        this.log.info("insertNetworkTopology.start " + topology);
        tedb.insertNetworkTopology(topology);
        this.log.info("insertNetworkTopology.end");
    }
}
