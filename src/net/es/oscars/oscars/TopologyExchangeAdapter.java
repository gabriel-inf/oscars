package net.es.oscars.oscars;

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
     * Passes the content of an initiateTopologyPull request to a
     * TopologyManager and returns the result to Axis2.
     *
     * @param initTopoPullRequest the content of an initiateTopologyPull
     * @return the reponse content to be returned to the requester
     * @throws TSSException
     */
    public InitiateTopologyPullResponseContent initiateTopologyPull(
        InitiateTopologyPullContent initTopoPullRequest)
            throws TSSException {

        InitiateTopologyPullResponseContent initTopoResponse = new InitiateTopologyPullResponseContent();
        HashMap<Domain, Boolean> visitedDomains = new HashMap<Domain, Boolean>();
        GetNetworkTopology getTopoRequest = new GetNetworkTopology();
        GetTopologyContent request = new GetTopologyContent();
        TopologyPuller pullClient = null;
        StringBuffer resultMsg = new StringBuffer();

        /* Connect to DB */
        Session bss = this.core.getBssSession();
        bss.beginTransaction();

        DomainDAO domainDAO = new DomainDAO("bss");
        List<Domain> domains = domainDAO.list();
        Domain localDomain = domainDAO.getLocalDomain();

        /* Remove local domain from list */
        if (localDomain != null) {
            domains.remove(localDomain);
        } else {
            this.log.error("no local domain in database");
            throw new TSSException("No local domain has been specified");
        }

        /* Initialize client*/
        try {
            //set to local domain URL for initialization, will be set later
            pullClient = new TopologyPuller(localDomain.getUrl());
        } catch (InterdomainException e) {
            this.log.error(e.getMessage());
            throw new TSSException(e.getMessage());
        }

        /* Contact each domain */
        request.setTopologyType("all");
        this.log.info("initiateTopologyPull.start");

        while (!domains.isEmpty()) {
            Domain d = domains.remove(0);

            if (visitedDomains.get(d) == null) {
                /* Pull topology from domain */
                String url = d.getUrl();
                pullClient.setUrl(url);

                this.log.info("will pull topology from " + url);
                GetTopologyResponseContent response = this.pullTopology(request,
                        pullClient);

                /* Insert into TEDB */
                if (response != null) {
                    this.log.info("successfully got topology from " + url);
                    try {
                        this.tm.insertNetworkTopology(response);
                    } catch (TSSException e) {
                        //only log, dont't fail just because one fails
                        this.log.warn("could not insert topology, error message follows");
                        this.log.warn(e.getMessage());
                        resultMsg.append("could not insert topology: " + e.getMessage());
                    }
                } else {
                    this.log.warn("topology response from ["+url+"] was null");
                }

                visitedDomains.put(d, new Boolean(true));
            }
        }

        this.log.info("initiateTopologyPull.end");

        if (resultMsg.length() > 0) {
            initTopoResponse.setResultMsg(resultMsg.toString());
        } else { initTopoResponse.setResultMsg("SUCCESS"); }

        return initTopoResponse;
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
