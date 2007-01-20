package net.es.oscars.client;

import java.rmi.RemoteException;
import javax.net.ssl.*;
import java.security.*;
import java.util.*;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.AxisFault;

import net.es.oscars.LogWrapper;
import net.es.oscars.oscars.OSCARSStub;
import net.es.oscars.oscars.AAAFaultMessageException;
import net.es.oscars.oscars.BSSFaultMessageException;
import net.es.oscars.wsdlTypes.*;
import net.es.oscars.client.security.KeyManagement;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;

/**
 * Client handles functionality common to all clients (forwarders, tests,
 * etc.)
 */
public class Client {
    private LogWrapper log;
    private ConfigurationContext configContext;
    private OSCARSStub stub;

    public Client(boolean useKeyStore) {
        if (useKeyStore) { KeyManagement.setKeyStore(); }
        this.log = new LogWrapper(this.getClass());
    }

    public void setUp(String url, String repo) throws AxisFault {
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
     * @throws OSCARSStub.AAAFaultMessageException
     * @throws java.rmi.RemoteException
     * @throws Exception
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
     * @throws OSCARSStub.AAAFaultMessageException
     * @throws java.rmi.RemoteException
     * @throws Exception
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
     * @param login a string, possibly null, with a user's login name
     * @return response a ListReply instance with summaries of each reservation
     * @throws OSCARSStub.AAAFaultMessageException
     * @throws java.rmi.RemoteException
     * @throws Exception
     */
    public ListReply listReservations(String login)
           throws AAAFaultMessageException, java.rmi.RemoteException,
              Exception {

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
     * @throws OSCARSStub.AAAFaultMessageException
     * @throws java.rmi.RemoteException
     * @throws Exception
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
     * @param params a hash map
     * @return response a list of hash maps
     * @throws OSCARSStub.AAAFaultMessageException
     * @throws java.rmi.RemoteException
     * @throws Exception
     */
    public List<Map<String,String>>
        forward(String op, Map<String,String> params)
           throws AAAFaultMessageException, BSSFaultMessageException,
                  java.rmi.RemoteException, Exception {

        ForwardPayload pl = new ForwardPayload();
        ForwardReply freply = null;
        Forward fwd = null;
        ForwardResponse frsp = null;
        List<Map<String,String>> response = null;
        
        pl.setContentType(op);
        fwd = new Forward();
        fwd.setPayload(pl);
        fwd.setPayloadSender(params.get("payloadSender"));
 
         if (op.equals("cancelReservation")) {
            ResTag rt = new ResTag();
            rt.setTag(params.get("tag"));
            pl.setCancelReservation(rt); 
            frsp = this.stub.forward(fwd);
            freply = frsp.getForwardResponse();
 
        } else if (op.equals("queryReservation")) {
            ResTag rt = new ResTag();
            ResDetails qreply = null;
            rt.setTag(params.get("tag"));
            pl.setQueryReservation(rt);
            frsp = this.stub.forward(fwd);
            freply = frsp.getForwardResponse();
            qreply = freply.getQueryReservation();
             
        } else if (op.equals("listReservations")) {
            EmptyArg ea = new EmptyArg();
            ea.setMsg("");
            pl.setListReservations(ea);
            frsp = this.stub.forward(fwd);
            freply = frsp.getForwardResponse();
            ListReply lr = freply.getListReservations();
        } else if (op.equals("createReservation")) {
            ResCreateContent resRequest = new ResCreateContent();
            resRequest.setSrcHost(params.get("srcHost"));
            pl.setCreateReservation(resRequest);
            frsp = this.stub.forward(fwd);
            freply = frsp.getForwardResponse();
            CreateReply cr = freply.getCreateReservation();            
        }       
        return response;
    }
}
