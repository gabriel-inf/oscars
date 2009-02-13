package net.es.oscars.rmi.bss;

import org.apache.log4j.*;
import org.hibernate.*;

import java.rmi.RemoteException;

import net.es.oscars.aaa.AuthValue;
import net.es.oscars.bss.OSCARSCore;
import net.es.oscars.tss.*;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.rmi.RmiUtils;
import net.es.oscars.rmi.aaa.AaaRmiInterface;

/**
 * Intermediary between client and core for topology exchange requests
 *
 * @author Andrew Lake (alake@internet2.edu)
 */
public class TopologyRmiHandler {
    private Logger log;
    private TopologyExchangeManager tm;
    private OSCARSCore core;
    private String dbname;

    public TopologyRmiHandler() {
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
     * @param userName string with name of user making request
     * @return the reponse content to be returned to the requester
     * @throws RemoteException
     */
    public GetTopologyResponseContent getNetworkTopology(
        GetTopologyContent getTopoRequest, String userName)
            throws RemoteException {

        this.log.info("getNetworkTopology.start");
        String methodName = "GetNetworkTopology";
        AaaRmiInterface rmiClient = RmiUtils.getAaaRmiClient(methodName, log);
        AuthValue authVal =
            rmiClient.checkAccess(userName, "Domains", "query");
        if (authVal.equals(AuthValue.DENIED)) {
            this.log.info("denied");
            throw new RemoteException
                ("permission denied for getting network topology");
        }
        Session bss = core.getBssSession();
        bss.beginTransaction();
        GetTopologyResponseContent getTopoResponse = null;
        try {
            getTopoResponse = this.tm.getNetworkTopology(getTopoRequest);
        } catch (TSSException e) {
            bss.getTransaction().rollback();
            throw new RemoteException(e.getMessage());
        }
        bss.getTransaction().commit();
        this.log.info("getNetworkTopology.end");
        return getTopoResponse;
    }
}
