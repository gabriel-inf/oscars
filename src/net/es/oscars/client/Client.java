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
import net.es.oscars.oscars.AAAFaultMessageException;
import net.es.oscars.oscars.BSSFaultMessageException;
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
        opts.setTimeOutInMilliSeconds(120000); // set to 2 minutes
        sc.setOptions(opts);
        this.stub._setServiceClient(sc);
    }

    /**
     * Makes call to server to cancel a reservation.
     *
     * @param rt a ResTag instance containing the reservation's unique tag
     * @return a string with the reservation's status
     * @throws AAAFaultMessageException
     * @throws BSSFaultMessageException
     * @throws java.rmi.RemoteException
     */
    public String cancelReservation(ResTag rt) 
            throws AAAFaultMessageException, java.rmi.RemoteException,
                  Exception {

        CancelReservation canRes = null;
        CancelReservationResponse rs = new CancelReservationResponse();
        canRes = new CancelReservation();
        canRes.setCancelReservation(rt);
        rs = this.stub.cancelReservation(canRes);
        return rs.getCancelReservationResponse();
    }

    /**
     * Makes call to server to create a reservation.
     *
     * @param resRequest a ResCreateContent with reservation parameters
     * @return crr a CreateReply instance with the reservation's tag and status
     * @throws AAAFaultMessageException
     * @throws BSSFaultMessageException
     * @throws java.rmi.RemoteException
     */
    public CreateReply createReservation(ResCreateContent resRequest)
           throws AAAFaultMessageException, java.rmi.RemoteException,
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
     * Makes call to server to get list of reservations.
     *
     * @return response a ListReply instance with summaries of each reservation
     * @throws AAAFaultMessageException
     * @throws BSSFaultMessageException
     * @throws java.rmi.RemoteException
     */
    public ListReply listReservations()
           throws AAAFaultMessageException, BSSFaultMessageException,java.rmi.RemoteException
    {

        ListReservations listRes = new ListReservations();
        EmptyArg ea = new EmptyArg();
        ea.setMsg("");
        listRes.setListReservations(ea);
        ListReservationsResponse lrr = this.stub.listReservations(listRes);
        ListReply listReply = lrr.getListReservationsResponse();
        return listReply;
    }

    /**
     * Makes call to server to get a reservation's details, given its tag.
     *
     * @param rt a ResTag instance containing the reservation's unique tag
     * @return a ResDetails instance containing the reservations tag and status
     * @throws AAAFaultMessageException
     * @throws BSSFaultMessageException
     * @throws java.rmi.RemoteException
      */
    public ResDetails queryReservation(ResTag rt)
           throws AAAFaultMessageException, java.rmi.RemoteException,
              Exception {

        QueryReservation queryRes = new QueryReservation();
        queryRes.setQueryReservation(rt);
        QueryReservationResponse qrr = this.stub.queryReservation(queryRes);
        ResDetails qreply = qrr.getQueryReservationResponse();
        return qreply;
    }
 
    /**
     * Makes call to server to forward a create reservation request to the
     * next domain.
     *
     * @param fwd a Forward object
     * @return response a ForwardReply object
     * @throws AAAFaultMessageException
     * @throws BSSFaultMessageException
     * @throws java.rmi.RemoteException
     */
    public ForwardReply forward(Forward fwd)
           throws AAAFaultMessageException, BSSFaultMessageException,
                  java.rmi.RemoteException {

        ForwardReply freply = null;
        ForwardResponse frsp = null;
        
        frsp = this.stub.forward(fwd);
        freply = frsp.getForwardResponse();
        return freply;
    }
}
