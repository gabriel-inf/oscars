package net.es.oscars.client;

import java.rmi.RemoteException;
import javax.net.ssl.*;
import java.security.*;
import java.util.*;

import org.apache.log4j.*;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

import net.es.oscars.oscars.OSCARSStub;
import net.es.oscars.oscars.AAAFaultMessage;
import net.es.oscars.oscars.BSSFaultMessage;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.client.security.KeyManagement;


/**
 * Client handles functionality common to all clients (forwarders, tests,
 * etc.)
 */
public class Client {
    protected Logger log;
    protected ConfigurationContext configContext;
    protected OSCARSStub stub;

    public void setUp(boolean useKeyStore, String url, String repo)
            throws AxisFault {
        this.setUp(useKeyStore, url, repo, null);
    }

    public void setUp(boolean useKeyStore, String url, String repo,
                      String axisConfig) throws AxisFault {

        if (useKeyStore) { KeyManagement.setKeyStore(repo); }
        this.log = Logger.getLogger(this.getClass());
        this.configContext =
                ConfigurationContextFactory
                .createConfigurationContextFromFileSystem(repo, null);

        this.stub = new OSCARSStub(this.configContext, url);
        ServiceClient sc = this.stub._getServiceClient();
        Options opts = sc.getOptions();
        opts.setTimeOutInMilliSeconds(300000); // set to 5 minutes
        sc.setOptions(opts);
        this.stub._setServiceClient(sc);
    }

    /**
     * Makes call to server to cancel a reservation.
     *
     * @param gri a GlobalReservationId instance with reservation's unique id
     * @return a string with the reservation's status
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
     */
    public String cancelReservation(GlobalReservationId gri)
            throws AAAFaultMessage, java.rmi.RemoteException,
                  Exception {

        CancelReservation canRes = null;
        CancelReservationResponse rs = new CancelReservationResponse();
        canRes = new CancelReservation();
        canRes.setCancelReservation(gri);
        rs = this.stub.cancelReservation(canRes);
        return rs.getCancelReservationResponse();
    }

    /**
     * Makes call to server to create a reservation.
     *
     * @param resRequest a ResCreateContent with reservation parameters
     * @return crr a CreateReply instance with the reservation's GRI and status
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
     */
    public CreateReply createReservation(ResCreateContent resRequest)
           throws AAAFaultMessage, java.rmi.RemoteException,
                  Exception {

        CreateReply crr =  null;
        CreateReservationResponse resResponse =
            new CreateReservationResponse();
        CreateReservation createRes = new CreateReservation();
        createRes.setCreateReservation(resRequest);
        resResponse = this.stub.createReservation(createRes);
        crr = resResponse.getCreateReservationResponse();
        return crr;
    }

    /**
     * Makes call to server to modify a reservation.
     *
     * @param resRequest a ModifyResContent with reservation parameters
     * @return crr a ModifyResReply instance with the reservation's GRI and status
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
     */
    public ModifyResReply modifyReservation(ModifyResContent resRequest)
           throws AAAFaultMessage, java.rmi.RemoteException,
                  Exception {

        ModifyResReply crr =  null;
        ModifyReservationResponse resResponse = new ModifyReservationResponse();
        ModifyReservation modifyRes = new ModifyReservation();
        modifyRes.setModifyReservation(resRequest);
        resResponse = this.stub.modifyReservation(modifyRes);
        crr = resResponse.getModifyReservationResponse();
        return crr;
    }

    /**
     * Makes call to server to get list of reservations.
     *
     * @param listReq the reservation list request object
     * @return response a ListReply instance with summaries of each reservation
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
     */
    public ListReply listReservations(ListRequest listReq)
           throws AAAFaultMessage, BSSFaultMessage,java.rmi.RemoteException
    {

        ListReservations listRes = new ListReservations();
        listRes.setListReservations(listReq);
        ListReservationsResponse lrr = this.stub.listReservations(listRes);
        ListReply listReply = lrr.getListReservationsResponse();
        return listReply;
    }

    /**
     * Makes call to server to get a reservation's details, given its GRI.
     *
     * @param gri a GlobalReservationId instance with reservation's unique id
     * @return a ResDetails instance containing the reservations tag and status
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
      */
    public ResDetails queryReservation(GlobalReservationId gri)
           throws AAAFaultMessage, java.rmi.RemoteException,
              Exception {

        QueryReservation queryRes = new QueryReservation();
        queryRes.setQueryReservation(gri);
        QueryReservationResponse qrr = this.stub.queryReservation(queryRes);
        ResDetails qreply = qrr.getQueryReservationResponse();
        return qreply;
    }

    /**
     * Makes call to server to get a global view of domain's topology
     *
     * @param request a getNetworkTopology request containing th request type
     * @return a GetNetworkTopologyResponseContent instance containing the topology data
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
     */
    public GetTopologyResponseContent getNetworkTopology(
        GetTopologyContent request) throws AAAFaultMessage,
        java.rmi.RemoteException, Exception {

        GetNetworkTopology getTopo = new GetNetworkTopology();
        getTopo.setGetNetworkTopology(request);
        GetNetworkTopologyResponse topo = this.stub.getNetworkTopology(getTopo);
        GetTopologyResponseContent response = topo.getGetNetworkTopologyResponse();
        return response;
    }

    /**
     * Tells the server to update its topology
     *
     * @param request a initiateTopologyPull request containing the request type
     * @return a InitiateTopologyPullResponseContent instance containing the result
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
     */
    public InitiateTopologyPullResponseContent initiateTopologyPull(
           InitiateTopologyPullContent request) throws AAAFaultMessage,
           java.rmi.RemoteException, Exception {

        InitiateTopologyPull initTopoPull = new InitiateTopologyPull();
        initTopoPull.setInitiateTopologyPull(request);
        InitiateTopologyPullResponse initResult =
            this.stub.initiateTopologyPull(initTopoPull);
        InitiateTopologyPullResponseContent response =
            initResult.getInitiateTopologyPullResponse();
        return response;
    }

    /**
     * Signals that a previously made reservation should be created
     *
     * @param request a createPath request containing the reservation to be built
     * @return the response received from the request
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
     */
    public CreatePathResponseContent createPath(
           CreatePathContent request) throws AAAFaultMessage,
           java.rmi.RemoteException, Exception {

        CreatePath cpath = new CreatePath();
        cpath.setCreatePath(request);
        CreatePathResponse createResult =
            this.stub.createPath(cpath);
        CreatePathResponseContent response =
            createResult.getCreatePathResponse();

        return response;
    }

    /**
     * Verifies that a previously setup path is still active
     *
     * @param request a refreshPath request
     * @return the response received from the request
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
     */
    public RefreshPathResponseContent refreshPath(
           RefreshPathContent request) throws AAAFaultMessage,
           java.rmi.RemoteException, Exception {

        RefreshPath rpath = new RefreshPath();
        rpath.setRefreshPath(request);
        RefreshPathResponse refreshResult =
            this.stub.refreshPath(rpath);
        RefreshPathResponseContent response =
            refreshResult.getRefreshPathResponse();

        return response;
    }

    /**
     * Tearsdown a previously setup path
     *
     * @param request a teardownPath request
     * @return the response received from the request
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
     */
    public TeardownPathResponseContent teardownPath(
           TeardownPathContent request) throws AAAFaultMessage,
           java.rmi.RemoteException, Exception {

        TeardownPath tpath = new TeardownPath();
        tpath.setTeardownPath(request);
        TeardownPathResponse teardownResult =
            this.stub.teardownPath(tpath);
        TeardownPathResponseContent response =
            teardownResult.getTeardownPathResponse();

        return response;
    }

    /**
     * Makes call to server to forward a create reservation request to the
     * next domain.
     *
     * @param fwd a Forward object
     * @return response a ForwardReply object
     * @throws AAAFaultMessage
     * @throws BSSFaultMessage
     * @throws java.rmi.RemoteException
     */
    public ForwardReply forward(Forward fwd)
           throws AAAFaultMessage, BSSFaultMessage,
                  java.rmi.RemoteException {

        ForwardReply freply = null;
        ForwardResponse frsp = null;

        frsp = this.stub.forward(fwd);
        freply = frsp.getForwardResponse();
        return freply;
    }
}
